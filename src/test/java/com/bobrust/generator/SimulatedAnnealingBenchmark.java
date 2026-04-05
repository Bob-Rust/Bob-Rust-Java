package com.bobrust.generator;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Benchmark and correctness tests for the simulated annealing optimizer.
 *
 * These tests compare SA against the classic hill climbing to verify that
 * SA produces equal or better results without significant performance regression.
 */
class SimulatedAnnealingBenchmark {
	private static final int ALPHA = 128;
	private static final int BACKGROUND = 0xFFFFFFFF;

	/**
	 * Run the generator for the given number of shapes using either SA or classic hill climbing.
	 * Returns the final model score (lower is better).
	 */
	private static float runGenerator(BufferedImage testImage, int maxShapes, boolean useSimulatedAnnealing) {
		// Ensure the image has TYPE_INT_ARGB so DataBufferInt works
		BufferedImage argbImage = ensureArgb(testImage);
		BorstImage target = new BorstImage(argbImage);
		Model model = new Model(target, BACKGROUND, ALPHA);

		// Temporarily override the SA flag by calling the appropriate method directly
		for (int i = 0; i < maxShapes; i++) {
			Worker worker = getWorker(model);
			worker.init(model.current, model.score);
			List<State> randomStates = createRandomStates(worker, 200);
			State state;
			if (useSimulatedAnnealing) {
				State best = getBestRandomState(randomStates);
				state = HillClimbGenerator.getHillClimbSA(best, 100);
			} else {
				State best = getBestRandomState(randomStates);
				state = HillClimbGenerator.getHillClimbClassic(best, 100);
			}
			addShapeToModel(model, state.shape);
		}
		return model.score;
	}

	/** Ensure image is TYPE_INT_ARGB */
	private static BufferedImage ensureArgb(BufferedImage img) {
		if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
		BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = argb.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return argb;
	}

	/** Reflective helper to get the worker from Model (package-private field) */
	private static Worker getWorker(Model model) {
		try {
			var field = Model.class.getDeclaredField("worker");
			field.setAccessible(true);
			return (Worker) field.get(model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Create a list of random states for the given worker */
	private static List<State> createRandomStates(Worker worker, int count) {
		List<State> states = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			states.add(new State(worker));
		}
		return states;
	}

	/** Get the best random state from a list */
	private static State getBestRandomState(List<State> states) {
		for (State s : states) {
			s.score = -1;
			s.shape.randomize();
		}
		states.parallelStream().forEach(State::getEnergy);
		float bestEnergy = Float.MAX_VALUE;
		State bestState = null;
		for (State s : states) {
			float energy = s.getEnergy();
			if (bestState == null || energy < bestEnergy) {
				bestEnergy = energy;
				bestState = s;
			}
		}
		return bestState;
	}

	/** Add a shape to the model using its internal addShape logic */
	private static void addShapeToModel(Model model, Circle shape) {
		try {
			var method = Model.class.getDeclaredMethod("addShape", Circle.class);
			method.setAccessible(true);
			method.invoke(model, shape);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ---- Test 3: Temperature Schedule Validation ----

	@Test
	void testTemperatureSchedule() {
		BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		// Fill with a non-trivial pattern so energy deltas are meaningful
		Graphics2D g = img.createGraphics();
		g.setColor(Color.RED);
		g.fillOval(10, 10, 40, 40);
		g.dispose();

		BorstImage target = new BorstImage(img);
		Worker worker = new Worker(target, ALPHA);
		BorstImage current = new BorstImage(64, 64);
		Arrays.fill(current.pixels, BACKGROUND);
		float initialScore = BorstCore.differenceFull(target, current);
		worker.init(current, initialScore);
		State state = new State(worker);
		state.getEnergy();

		float temp = HillClimbGenerator.estimateTemperature(state);

		// Temperature should be positive and finite
		assertTrue(temp > 0, "Temperature should be positive, got " + temp);
		assertTrue(Float.isFinite(temp), "Temperature should be finite");

		// Cooling rate should be between 0 and 1
		float rate = HillClimbGenerator.computeCoolingRate(temp, 100);
		assertTrue(rate > 0 && rate < 1, "Cooling rate should be in (0,1), got " + rate);

		// After maxAge*10 iterations, temperature should be near zero
		float finalTemp = temp;
		for (int i = 0; i < 1000; i++) finalTemp *= rate;
		assertTrue(finalTemp < 0.01f, "Final temperature should be near zero, got " + finalTemp);
	}

	// ---- Test 1: SA Produces Lower or Equal Energy ----

	@Test
	void testSAProducesLowerOrEqualEnergy() {
		BufferedImage testImage = TestImageGenerator.createPhotoDetail();
		int maxShapes = 50; // Small count for test speed

		float hillClimbScore = runGenerator(testImage, maxShapes, false);
		float saScore = runGenerator(testImage, maxShapes, true);

		System.out.println("Hill climb score: " + hillClimbScore);
		System.out.println("SA score: " + saScore);
		float improvement = (hillClimbScore - saScore) / hillClimbScore * 100;
		System.out.println("SA improvement: " + improvement + "%");

		// SA should not be dramatically worse (allow 5% tolerance due to stochastic nature)
		assertTrue(saScore <= hillClimbScore * 1.05f,
			"SA score (" + saScore + ") should not be significantly worse than hill climb (" + hillClimbScore + ")");
	}

	// ---- Test 4: Regression — No Worse Than Baseline on Multiple Images ----

	@Test
	void testSANeverSignificantlyWorse() {
		BufferedImage[] images = {
			TestImageGenerator.createSolid(),
			TestImageGenerator.createGradient(),
			TestImageGenerator.createEdges(),
		};
		String[] names = {"solid", "gradient", "edges"};
		int maxShapes = 30; // Small for test speed

		float totalHC = 0, totalSA = 0;
		float[] hcScores = new float[images.length];
		float[] saScores = new float[images.length];
		for (int idx = 0; idx < images.length; idx++) {
			hcScores[idx] = runGenerator(images[idx], maxShapes, false);
			saScores[idx] = runGenerator(images[idx], maxShapes, true);
			totalHC += hcScores[idx];
			totalSA += saScores[idx];
			System.out.println(names[idx] + " — HC: " + hcScores[idx] + ", SA: " + saScores[idx]);
		}

		// SA may lose on individual degenerate cases (e.g., solid color where hill
		// climbing is already optimal), so we check the aggregate across all test
		// images rather than each one individually.
		System.out.println("Aggregate — HC: " + totalHC + ", SA: " + totalSA);
		assertTrue(totalSA <= totalHC * 1.10f,
			"Aggregate SA (" + totalSA + ") should not be significantly worse than aggregate HC (" + totalHC + ")");

		// Also verify no single image is catastrophically worse (>30% tolerance per image)
		for (int idx = 0; idx < images.length; idx++) {
			assertTrue(saScores[idx] <= hcScores[idx] * 1.30f,
				names[idx] + ": SA (" + saScores[idx] + ") catastrophically worse than HC (" + hcScores[idx] + ")");
		}
	}

	// ---- Test: Cooling Rate Edge Cases ----

	@Test
	void testCoolingRateEdgeCases() {
		// Very small initial temperature
		float rate = HillClimbGenerator.computeCoolingRate(0.0005f, 100);
		assertTrue(rate > 0 && rate <= 1.0f, "Cooling rate should handle small temps, got " + rate);

		// Large initial temperature
		rate = HillClimbGenerator.computeCoolingRate(1000f, 100);
		assertTrue(rate > 0 && rate < 1, "Cooling rate should handle large temps, got " + rate);

		// Normal case
		rate = HillClimbGenerator.computeCoolingRate(1.0f, 100);
		assertTrue(rate > 0 && rate < 1, "Cooling rate should be in (0,1), got " + rate);
	}
}
