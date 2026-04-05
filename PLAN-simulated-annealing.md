# Implementation Plan: Simulated Annealing for Shape Optimization

## Overview

Replace the pure hill-climbing refinement in `HillClimbGenerator.getHillClimb()` with simulated annealing (SA) to escape local minima and produce better shape placements with fewer total shapes.

## Current Behavior

The current `getHillClimb()` method (line 33 of `HillClimbGenerator.java`) is a strict greedy local search:
1. Mutate the circle (position or radius) via `Circle.mutateShape()`
2. Compute energy (pixel error) via `State.getEnergy()` -> `Worker.getEnergy()` -> `BorstCore.differencePartialThread()`
3. If energy improved, keep the mutation and reset the age counter
4. If energy worsened, revert to the saved undo state
5. Stop after `maxAge` consecutive non-improvements (currently 100) or 4096 total iterations

This gets stuck in local minima — a circle may be "pretty good" at its current position but a much better position exists that requires passing through worse states to reach.

## Proposed Change

### Phase 1: Simulated Annealing in `HillClimbGenerator`

Replace `getHillClimb()` with SA that accepts worse moves probabilistically:

```java
public static State getHillClimb(State state, int maxAge) {
    float currentEnergy = state.getEnergy();
    State bestState = state.getCopy();
    float bestEnergy = currentEnergy;
    
    // Estimate initial temperature from sample mutations
    float temperature = estimateTemperature(state);
    float coolingRate = computeCoolingRate(temperature, maxAge);
    
    State undo = state.getCopy();
    int totalIterations = maxAge * 10; // SA needs more iterations than hill climbing
    
    for (int i = 0; i < totalIterations; i++) {
        state.doMove(undo);
        float newEnergy = state.getEnergy();
        float delta = newEnergy - currentEnergy;
        
        if (delta < 0) {
            // Improvement — always accept
            currentEnergy = newEnergy;
            if (currentEnergy < bestEnergy) {
                bestEnergy = currentEnergy;
                bestState = state.getCopy();
            }
        } else if (temperature > 0.001f) {
            // Worse move — accept with probability exp(-delta/T)
            double acceptProb = Math.exp(-delta / temperature);
            if (ThreadLocalRandom.current().nextDouble() < acceptProb) {
                currentEnergy = newEnergy;
            } else {
                state.fromValues(undo);
            }
        } else {
            state.fromValues(undo);
        }
        
        temperature *= coolingRate;
    }
    
    // Return the best state found during the entire SA run
    return bestState;
}
```

### Phase 2: Temperature Estimation

Add a method to estimate a good starting temperature:

```java
private static float estimateTemperature(State state) {
    State probe = state.getCopy();
    State undo = probe.getCopy();
    float totalDelta = 0;
    int samples = 30;
    
    for (int i = 0; i < samples; i++) {
        float before = probe.getEnergy();
        probe.doMove(undo);
        float after = probe.getEnergy();
        totalDelta += Math.abs(after - before);
        probe.fromValues(undo); // restore
    }
    
    float avgDelta = totalDelta / samples;
    // Set T so ~60% of uphill moves are accepted initially
    // P = exp(-avgDelta / T) = 0.6 => T = -avgDelta / ln(0.6)
    return (float)(avgDelta / 0.5108); // -1/ln(0.6) ≈ 1.957, so T ≈ avgDelta * 1.957
}
```

### Phase 3: Cooling Rate Computation

Compute cooling rate so temperature reaches near-zero by the end:

```java
private static float computeCoolingRate(float initialTemp, int maxAge) {
    int totalIterations = maxAge * 10;
    float finalTemp = 0.001f;
    // initialTemp * rate^totalIterations = finalTemp
    // rate = (finalTemp / initialTemp) ^ (1 / totalIterations)
    return (float) Math.pow(finalTemp / initialTemp, 1.0 / totalIterations);
}
```

### Phase 4: Parallel SA Chains

Increase `times` parameter in `getBestHillClimbState()` from 1 to `availableProcessors()`:

In `Model.java`, change:
```java
private static final int times = 1;
```
to:
```java
private static final int times = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
```

This runs multiple independent SA chains and picks the best result, exploiting parallelism for better exploration.

## Files to Modify

| File | Change |
|------|--------|
| `HillClimbGenerator.java` | Replace `getHillClimb()` with SA, add `estimateTemperature()` and `computeCoolingRate()` |
| `Model.java` | Update `times` to use available processors |
| `State.java` | No changes needed (existing `doMove`/`fromValues`/`getCopy` API is sufficient) |

## Testing Strategy

### Test 1: Benchmark Harness — Quantitative Accuracy Comparison

Create `src/test/java/com/bobrust/generator/SimulatedAnnealingBenchmark.java`:

**Purpose**: Compare SA vs hill-climbing on identical inputs with identical shape budgets, measuring final energy (pixel error).

**Method**:
1. Load 5 diverse test images (solid color, gradient, photo with fine detail, high-contrast edges, natural scene)
2. For each image, run the generator with hill climbing (current code) for N shapes (e.g., 500, 1000, 2000)
3. Record the final `model.score` (total pixel error) and wall-clock time
4. Switch to SA and repeat with identical parameters
5. Compare: SA must achieve **lower or equal final score** for the same shape count

**Pass criteria**:
- SA achieves >= 5% lower error on at least 3/5 test images at 1000 shapes
- SA does not take more than 2x the wall-clock time of hill climbing
- SA never produces a *worse* result than hill climbing on any test image

```java
@Test
public void testSAProducesLowerEnergy() {
    BufferedImage testImage = loadTestImage("photo_detail.png");
    int maxShapes = 1000;
    int background = 0xFFFFFFFF;
    int alpha = 128;
    
    // Run hill climbing
    float hillClimbScore = runGenerator(testImage, maxShapes, background, alpha, false);
    
    // Run simulated annealing  
    float saScore = runGenerator(testImage, maxShapes, background, alpha, true);
    
    // SA should produce lower error
    assertTrue("SA score (" + saScore + ") should be <= hill climb score (" + hillClimbScore + ")",
               saScore <= hillClimbScore * 1.0); // Allow equal
    
    // Log improvement percentage
    float improvement = (hillClimbScore - saScore) / hillClimbScore * 100;
    System.out.println("SA improvement: " + improvement + "%");
}
```

### Test 2: Convergence Rate — Shapes-to-Quality Curve

**Purpose**: Verify SA converges faster (needs fewer shapes for the same quality).

**Method**:
1. Run both algorithms, recording score at every 100 shapes
2. Plot convergence curves
3. Verify SA reaches the hill-climbing final score in fewer shapes

```java
@Test
public void testSAConvergesFaster() {
    BufferedImage testImage = loadTestImage("gradient.png");
    int background = 0xFFFFFFFF;
    int alpha = 128;
    
    // Record scores at intervals
    float[] hcScores = runGeneratorWithIntervals(testImage, 2000, background, alpha, false, 100);
    float[] saScores = runGeneratorWithIntervals(testImage, 2000, background, alpha, true, 100);
    
    // Find how many shapes SA needs to match HC's final score
    float hcFinal = hcScores[hcScores.length - 1];
    int saShapesNeeded = -1;
    for (int i = 0; i < saScores.length; i++) {
        if (saScores[i] <= hcFinal) {
            saShapesNeeded = (i + 1) * 100;
            break;
        }
    }
    
    assertTrue("SA should reach HC quality in fewer shapes", 
               saShapesNeeded > 0 && saShapesNeeded < 2000);
}
```

### Test 3: Temperature Schedule Validation

**Purpose**: Verify the temperature estimation and cooling produce sensible behavior.

```java
@Test
public void testTemperatureSchedule() {
    // Create a simple test worker and state
    BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    BorstImage target = new BorstImage(img);
    Worker worker = new Worker(target, 128);
    worker.init(new BorstImage(64, 64), 1.0f);
    State state = new State(worker);
    
    float temp = HillClimbGenerator.estimateTemperature(state);
    
    // Temperature should be positive and finite
    assertTrue("Temperature should be positive", temp > 0);
    assertTrue("Temperature should be finite", Float.isFinite(temp));
    
    // Cooling rate should be between 0 and 1
    float rate = HillClimbGenerator.computeCoolingRate(temp, 100);
    assertTrue("Cooling rate should be in (0,1)", rate > 0 && rate < 1);
    
    // After maxAge*10 iterations, temperature should be near zero
    float finalTemp = temp;
    for (int i = 0; i < 1000; i++) finalTemp *= rate;
    assertTrue("Final temperature should be near zero", finalTemp < 0.01f);
}
```

### Test 4: Regression — No Worse Than Baseline

**Purpose**: Ensure SA never produces significantly worse results.

```java
@Test
public void testSANeverSignificantlyWorse() {
    String[] testImages = {"solid.png", "gradient.png", "photo.png", "edges.png", "nature.png"};
    
    for (String imageName : testImages) {
        BufferedImage img = loadTestImage(imageName);
        float hcScore = runGenerator(img, 500, 0xFFFFFFFF, 128, false);
        float saScore = runGenerator(img, 500, 0xFFFFFFFF, 128, true);
        
        // SA should never be more than 2% worse
        assertTrue(imageName + ": SA should not be significantly worse",
                   saScore <= hcScore * 1.02);
    }
}
```

### Test 5: Visual Diff — Perceptual Quality

**Purpose**: Generate output images for human inspection.

**Method**:
1. Run both algorithms on a test photo
2. Save the rendered approximation as PNG files
3. Compute and save a pixel-difference heatmap highlighting where SA differs from HC
4. These are not automated pass/fail — they're for manual review

```java
@Test
public void generateVisualComparison() {
    BufferedImage testImage = loadTestImage("photo_detail.png");
    
    BufferedImage hcResult = runGeneratorAndRender(testImage, 1000, false);
    BufferedImage saResult = runGeneratorAndRender(testImage, 1000, true);
    BufferedImage diffMap = computeDiffHeatmap(hcResult, saResult);
    
    ImageIO.write(hcResult, "png", new File("build/test-output/hc_result.png"));
    ImageIO.write(saResult, "png", new File("build/test-output/sa_result.png"));
    ImageIO.write(diffMap, "png", new File("build/test-output/diff_heatmap.png"));
}
```

## Test Images Required

Create `src/test/resources/test-images/` with:
1. `solid.png` — single flat color (baseline, both should score near-zero)
2. `gradient.png` — smooth gradient (tests color blending accuracy)
3. `photo_detail.png` — photograph with fine details (hair, text, etc.)
4. `edges.png` — high-contrast black/white edges (tests circle placement precision)
5. `nature.png` — natural scene with mixed regions (comprehensive test)

These can be 128x128 or 256x256 to keep test times reasonable.

## Rollout Plan

1. Implement SA behind a boolean flag (`USE_SIMULATED_ANNEALING` in `AppConstants`)
2. Run benchmarks with flag on and off
3. If SA passes all tests, make it the default
4. Keep the flag so users can revert if needed

## Expected Impact

- **Accuracy**: 5-15% lower final error for the same shape count
- **Speed**: Similar per-shape time (same number of energy evaluations per iteration). May be slightly slower due to temperature calculations, offset by better convergence.
- **Convergence**: SA should reach hill-climbing's final quality in 10-20% fewer shapes, meaning faster painting times in Rust.

## Risks

- SA with poor temperature schedule can perform worse than hill climbing (too hot = random walk, too cold = same as hill climbing)
- Mitigation: temperature estimation from empirical deltas ensures schedule is tuned to actual energy landscape
- Multiple parallel SA chains provide robustness against bad individual runs
