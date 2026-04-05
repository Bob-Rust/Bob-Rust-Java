# Bob-Rust-Java Codebase Analysis

## Project Overview

Bob-Rust-Java is an automated painting tool for the video game **Rust**. It takes an input image, approximates it as a series of colored circles (blobs) using a hill-climbing optimization algorithm, then automates the mouse to paint those blobs onto in-game signs using Rust's built-in painting UI. Think of it as a "Bob Ross" bot for Rust signs.

## Architecture

The project is structured into five main packages:

### 1. `com.bobrust.generator` — Core Image Approximation Engine

This is the computational heart of the application. It implements a **hill-climbing stochastic optimization** algorithm to approximate a target image using a fixed palette of 64 colors, 6 circle sizes, and 6 alpha (opacity) levels — matching the constraints of Rust's in-game painting tools.

**Key classes:**

- **`Model`** — The central model that holds the target image, the current approximation, and orchestrates shape placement. Each call to `processStep()` adds one optimized circle to the approximation.

- **`HillClimbGenerator`** — Implements the optimization loop:
  1. Generates 1000 random candidate circle placements (`State` objects)
  2. Evaluates each candidate in parallel using `parallelStream()`
  3. Takes the best candidate and performs hill-climbing mutations (up to 100 age / 4096 max iterations)
  4. Returns the best-optimized shape placement

- **`State`** / **`Circle`** / **`Worker`** — State encapsulates a circle placement + its score. Circle holds position (x,y) and radius (r). Worker provides the energy (difference) evaluation function and shared context (target image, current image, score).

- **`BorstCore`** — Low-level pixel manipulation functions:
  - `computeColor()` — Determines the optimal color for a circle at a given position by computing the weighted average difference between target and current pixels, then snapping to the nearest palette color
  - `drawLines()` — Alpha-composites a colored circle onto an image using scanlines
  - `differenceFull()` — Computes RGBA root-mean-square error between two images
  - `differencePartial()` — Efficiently updates the score by only recalculating pixels within the circle's bounds
  - `differencePartialThread()` — Combines color computation and partial difference in one pass (used for parallel evaluation)

- **`CircleCache`** — Pre-computes 6 circle rasterizations as arrays of `Scanline` objects. Each circle size (3, 6, 12, 25, 50, 100 pixels diameter) is stored as horizontal scanlines for efficient iteration.

- **`BorstUtils`** — Defines the Rust color palette (64 `BorstColor` values in 4 rows of 16), alpha values, size values, and provides nearest-neighbor lookup via precomputed lookup tables (`NumberLookup`).

- **`BorstGenerator`** — Thread management wrapper. Runs the generation on a daemon thread, provides callbacks at configurable intervals, and supports stop/resume.

- **`BorstImage`** — Thin wrapper around `BufferedImage` that exposes raw `int[]` pixel data for direct manipulation (avoiding per-pixel method call overhead).

### 2. `com.bobrust.generator.sorter` — Blob Sorting for Paint Efficiency

After generation, blobs must be sorted to minimize the number of UI interactions (color/size/opacity changes) when painting. The sorter:

- **`BorstSorter`** — Uses a **quadtree** (`QTree`) to efficiently find overlapping circles, then applies a greedy algorithm:
  1. Builds intersection maps using the quadtree
  2. Creates a cache indexed by `(sizeIndex, colorIndex)` for O(1) lookup of same-property blobs
  3. Greedily selects the next blob that: (a) matches the current size/color, and (b) doesn't overlap with any undrawn blob that would need to be drawn first
  
  This dramatically reduces the number of tool changes during painting. Data is sorted in groups of `MAX_SORT_GROUP` (1000) for memory efficiency.

- **`BlobList`** / **`IntList`** / **`Blob`** — Custom collection types. `IntList` is a primitive int list to avoid boxing overhead. `Blob` is an immutable record of a circle with all indices precomputed.

### 3. `com.bobrust.robot` — Automated Painting via `java.awt.Robot`

- **`BobRustPainter`** — Drives the actual painting process:
  1. Iterates through the sorted blob list
  2. For each blob, clicks UI elements to change size/color/opacity/shape as needed
  3. Clicks the canvas at the correct position to draw the circle
  4. Includes safety checks: if the mouse is moved by the user (displacement > 10px), painting is interrupted
  5. Periodically auto-saves via the in-game save button

- **`BobRustPalette`** — Analyzes a screenshot to locate the Rust painting UI elements:
  - Scans a 4x16 grid within the color palette region to identify each of the 64 colors
  - Maps `BorstColor` objects to screen `Point` coordinates
  - Provides button coordinates for size slider, opacity slider, shape buttons, etc.

- **`BobRustPaletteGenerator`** — Automatically calculates button positions based on screen resolution. Uses proportional scaling from a reference 1920x1080 layout, accounting for Rust's height-based aspect ratio scaling.

- **`ButtonConfiguration`** — Serializable configuration of all button positions. Supports manual calibration and JSON persistence via Gson.

### 4. `com.bobrust.gui` — Swing-based User Interface

- **`ApplicationWindow`** — Main toolbar with buttons for: settings, image import, sign type selection, canvas area selection, image area selection, button setup, and draw
- **`ScreenDrawDialog`** — Full-screen transparent overlay that shows the generation preview on top of the Rust game window
- **`DrawDialog`** — Controls for shape count slider, click speed, exact time calculation, and the "draw" action trigger
- **`RegionSelectionDialog`** — Allows selecting rectangular regions on screen (for canvas area, image area, palette area) with a resize handle UI
- **`SettingsDialog`** — Auto-generated settings UI from annotated `Settings` interface fields
- **`ShapeRender`** — Cached rendering of blob data to `BufferedImage`, with periodic pixel buffer snapshots for efficient seek/scrub on the shape slider

### 5. `com.bobrust.settings` — Configuration System

Uses a reflection-based system where `Settings` interface fields are annotated with `@GuiElement` to auto-generate UI. Settings are persisted to `bobrust.properties`. Types include `IntType`, `BoolType`, `ColorType`, `EnumType`, `SignType`, `SizeType`, and `StringType`.

## Data Flow

1. **User imports image** → stored as `drawImage` in `ApplicationWindow`
2. **User selects regions** → canvas area and image area rectangles stored
3. **User opens draw dialog** → `DrawDialog.startGeneration()`:
   - Scales image to sign dimensions using selected scaling type
   - Optionally applies ICC CMYK color profile LUT
   - Creates `Model` with target image, background color, and alpha
   - Starts `BorstGenerator` thread
4. **Generation loop** → `Model.processStep()` → `HillClimbGenerator.getBestHillClimbState()`:
   - 1000 random states evaluated in parallel
   - Best state hill-climbed to local optimum
   - Circle added to model, score updated
   - Callback fires at intervals to update preview
5. **User triggers painting** → `DrawDialog.startDrawingAction()`:
   - Stops generator, sorts blobs via `BorstSorter`
   - `BobRustPainter.startDrawing()` takes over mouse control
   - Iterates sorted blobs, clicking UI elements and canvas

## Algorithm Details

### Hill-Climbing Optimization

The core algorithm is a variant of **primitive image approximation** (similar to the "Primitive" project by Michael Fogleman):

1. **Random sampling**: Generate N random circle placements
2. **Energy evaluation**: For each, compute how much the image improves by adding that circle at that position with the optimal color
3. **Selection**: Take the best candidate
4. **Local search**: Mutate (position jitter via Gaussian, size change) and keep improvements
5. **Commit**: Add the final circle to the model

The "energy" is the RMSE between target and current+candidate images. The `differencePartialThread` function is key — it computes the partial score update in O(circle_area) instead of O(image_area).

### Color Matching

Colors are matched to the nearest palette entry using Euclidean distance in RGB space. The `computeColor` function calculates the ideal color for a circle by:
1. Summing target and current pixel values within the circle
2. Applying alpha-weighted inverse to find what color, when alpha-blended, would produce the target
3. Snapping to the nearest palette color

---

## Bugs Found

### Bug 1: `CircleCache.CIRCLE_CACHE` Indexed by Size Value, Not Cache Index

**Location**: `CircleCache.java` lines 35-37, used in `BorstCore.java`

The `CIRCLE_CACHE` array has 6 elements indexed 0-5, but `BorstCore` and `Worker` use it via `BorstUtils.getClosestSizeIndex()` which returns the index into the `SIZES` array. However, `CIRCLE_CACHE_LENGTH` stores the scanline counts per cache entry (not the circle diameter), while `BorstUtils.SIZES` is set to `CIRCLE_CACHE_LENGTH`. This means `SIZES = {3, 6, 12, 25, 50, 100}` (the scanline array lengths = the circle diameters), which actually works correctly by coincidence since `generateCircle(size)` produces exactly `size` scanlines.

However, there is a **null pointer risk**: `CircleCache.generateCircle()` can produce `null` entries in the `Scanline[]` array when a row of the circle grid has no filled pixels (the top/bottom rows). These null scanlines are then iterated in `BorstCore.computeColor()`, `drawLines()`, `differencePartial()`, and `differencePartialThread()` without null checks, which would cause `NullPointerException`.

**Severity**: Medium. In practice the circle rasterization usually fills all rows, but for very small circles or edge cases, nulls could occur.

### Bug 2: Thread Safety Issue in `Worker.counter`

**Location**: `Worker.java` line 14, `Model.java` line 75

`Worker.counter` is incremented by `getEnergy()` which is called from parallel streams in `HillClimbGenerator.getBestRandomState()`. The counter is a non-volatile, non-atomic `int` field, leading to data races. The final value read in `Model.processStep()` may be incorrect.

**Severity**: Low. The counter is only used for debug logging, not for correctness.

### Bug 3: `BorstColor.equals()` Uses `hashCode()` Instead of Direct Field Comparison

**Location**: `BorstColor.java` lines 17-21

```java
public boolean equals(Object obj) {
    if(!(obj instanceof BorstColor)) return false;
    return rgb == obj.hashCode();
}
```

This calls `obj.hashCode()` which works because `hashCode()` returns `rgb`, but it violates the contract — it should cast to `BorstColor` and compare the `rgb` field directly. If any subclass overrides `hashCode()`, this would break.

**Severity**: Low. Works correctly but is a code smell.

### Bug 4: `Random` with Fixed Seed in `Worker`

**Location**: `Worker.java` line 20

```java
this.rnd = new Random(0);
```

The `Random` instance uses a fixed seed of 0 and is shared across parallel stream operations (via `Circle.randomize()` and `Circle.mutateShape()`). `java.util.Random` is thread-safe but contended — all parallel threads will serialize on the same lock, reducing parallelism benefit.

**Severity**: Medium. Significantly reduces the effectiveness of parallel evaluation.

### Bug 5: Potential Division by Zero in `BorstCore.computeColor()`

**Location**: `BorstCore.java` line 50

If `count` is 0 (no pixels in the circle are within the image bounds), the division `rsum / (double)count` will produce `NaN`/`Infinity`. The subsequent `clampInt` won't catch `NaN` since `NaN` comparisons return false.

**Severity**: Medium. Can occur when a circle is entirely outside the image bounds.

### Bug 6: `PaintingInterrupted` Used for Normal Flow Control

**Location**: `BobRustPainter.java` line 173

```java
throw new PaintingInterrupted(drawnShapes, PaintingInterrupted.InterruptType.PaintingFinished);
```

The method throws an exception to signal successful completion. This is an anti-pattern — exceptions should not be used for normal control flow.

**Severity**: Low. Code smell, not a runtime issue.

### Bug 7: Static Mutable Field `map` in `BorstSorter`

**Location**: `BorstSorter.java` line 136

```java
private static IntList[] map;
```

This static field is written and read during sorting. If two sorts run concurrently, they would corrupt each other's data.

**Severity**: Medium. Unlikely in current usage but dangerous.

### Bug 8: `drawSetupButton` Never Disabled

**Location**: `ApplicationWindow.java`

The `drawSetupButton` is created but never disabled like the other buttons (`canvasAreaButton`, `imageAreaButton`, `drawButton`). The user can click "Setup Buttons" before importing an image or selecting regions.

**Severity**: Low. UI issue.

### Bug 9: Double `addTimeDelay` Bug in `clickPointScaledDrawColor`

**Location**: `BobRustPainter.java` lines 193-228

The `time` variable is set once at method entry, but `addTimeDelay` is called multiple times with `time + delay`, `time + delay * 2`, `time + delay * 3`. Inside the retry loop, these use the same `time` base, meaning retries don't actually wait — the expected time is already in the past after the first iteration.

**Severity**: Medium. Can cause rapid-fire clicks during retries, potentially missing paint operations.

---

## Improvement Recommendations

### Performance Improvements

1. **Use `ThreadLocalRandom` instead of shared `Random(0)`** — Eliminates lock contention in parallel streams
2. **Add null checks for scanlines** in `CircleCache` — Filter out null entries during generation
3. **Use `AtomicInteger` for `Worker.counter`** if accuracy matters
4. **Pre-compute color distance table** — The 64-color palette is fixed; a 256x256x256 LUT (16MB) or a smaller quantized LUT could replace the linear scan in `getClosestColorIndex()`

### Code Quality Improvements

1. **Fix `BorstColor.equals()`** — Use proper cast and field comparison
2. **Remove exception-as-flow-control** in `BobRustPainter` — Use a return value or status enum
3. **Make `BorstSorter.map` an instance field** — Eliminate static mutable state
4. **Add `@Override` annotations** consistently
5. **Remove trailing semicolon** on `Model.java` class declaration

### Feature Improvements

1. **Neon sign support** — Signs are defined but not all are exposed in the UI
2. **Progress persistence** — Save/load generation state to resume later
3. **Undo support** — Allow removing the last N shapes
