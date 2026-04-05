package com.bobrust.generator;

import java.util.Random;

/**
 * Spatial error map that tracks per-cell error across the image and supports
 * importance sampling to bias circle placement toward high-error regions.
 *
 * The image is divided into a coarse grid (e.g. 32x32). Each cell stores the
 * sum of squared per-pixel error for its region. An alias table enables O(1)
 * weighted random sampling from the grid.
 */
public class ErrorMap {
	private static final int DEFAULT_GRID_DIM = 32;

	final int gridWidth;
	final int gridHeight;
	final int cellWidth;
	final int cellHeight;
	final int imageWidth;
	final int imageHeight;
	final float[] cellErrors;

	// Alias table fields for O(1) weighted sampling
	private int[] alias;
	private float[] prob;
	private boolean tableValid;

	public ErrorMap(int imageWidth, int imageHeight) {
		this(imageWidth, imageHeight, DEFAULT_GRID_DIM, DEFAULT_GRID_DIM);
	}

	public ErrorMap(int imageWidth, int imageHeight, int gridWidth, int gridHeight) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.cellWidth = Math.max(1, (imageWidth + gridWidth - 1) / gridWidth);
		this.cellHeight = Math.max(1, (imageHeight + gridHeight - 1) / gridHeight);
		this.cellErrors = new float[gridWidth * gridHeight];
		this.alias = new int[gridWidth * gridHeight];
		this.prob = new float[gridWidth * gridHeight];
		this.tableValid = false;
	}

	/**
	 * Compute the full error map from scratch given target and current images.
	 */
	public void computeFull(BorstImage target, BorstImage current) {
		int w = target.width;
		int h = target.height;
		int n = gridWidth * gridHeight;
		for (int i = 0; i < n; i++) {
			cellErrors[i] = 0;
		}

		for (int py = 0; py < h; py++) {
			int gy = py / cellHeight;
			if (gy >= gridHeight) gy = gridHeight - 1;
			int rowOffset = py * w;
			for (int px = 0; px < w; px++) {
				int gx = px / cellWidth;
				if (gx >= gridWidth) gx = gridWidth - 1;

				int tt = target.pixels[rowOffset + px];
				int cc = current.pixels[rowOffset + px];

				int dr = ((tt >>> 16) & 0xff) - ((cc >>> 16) & 0xff);
				int dg = ((tt >>> 8) & 0xff) - ((cc >>> 8) & 0xff);
				int db = (tt & 0xff) - (cc & 0xff);

				cellErrors[gy * gridWidth + gx] += dr * dr + dg * dg + db * db;
			}
		}

		tableValid = false;
	}

	/**
	 * Incrementally update the error map after a circle was drawn.
	 * Only recomputes cells that overlap the circle's bounding box.
	 */
	public void updateIncremental(BorstImage target, BorstImage current, int cx, int cy, int cacheIndex) {
		Scanline[] lines = CircleCache.CIRCLE_CACHE[cacheIndex];
		int w = target.width;
		int h = target.height;

		// Find bounding box of affected grid cells
		int minGx = Integer.MAX_VALUE, maxGx = Integer.MIN_VALUE;
		int minGy = Integer.MAX_VALUE, maxGy = Integer.MIN_VALUE;
		for (Scanline line : lines) {
			int py = line.y + cy;
			if (py < 0 || py >= h) continue;
			int xs = Math.max(line.x1 + cx, 0);
			int xe = Math.min(line.x2 + cx, w - 1);
			if (xs > xe) continue;

			int gy = Math.min(py / cellHeight, gridHeight - 1);
			int gx0 = Math.min(xs / cellWidth, gridWidth - 1);
			int gx1 = Math.min(xe / cellWidth, gridWidth - 1);

			minGy = Math.min(minGy, gy);
			maxGy = Math.max(maxGy, gy);
			minGx = Math.min(minGx, gx0);
			maxGx = Math.max(maxGx, gx1);
		}

		if (minGx > maxGx || minGy > maxGy) return;

		// Recompute only the affected cells
		for (int gy = minGy; gy <= maxGy; gy++) {
			int pyStart = gy * cellHeight;
			int pyEnd = Math.min(pyStart + cellHeight, h);
			for (int gx = minGx; gx <= maxGx; gx++) {
				int pxStart = gx * cellWidth;
				int pxEnd = Math.min(pxStart + cellWidth, w);

				float error = 0;
				for (int py = pyStart; py < pyEnd; py++) {
					int rowOffset = py * w;
					for (int px = pxStart; px < pxEnd; px++) {
						int tt = target.pixels[rowOffset + px];
						int cc = current.pixels[rowOffset + px];

						int dr = ((tt >>> 16) & 0xff) - ((cc >>> 16) & 0xff);
						int dg = ((tt >>> 8) & 0xff) - ((cc >>> 8) & 0xff);
						int db = (tt & 0xff) - (cc & 0xff);

						error += dr * dr + dg * dg + db * db;
					}
				}
				cellErrors[gy * gridWidth + gx] = error;
			}
		}

		tableValid = false;
	}

	/**
	 * Build the alias table for O(1) weighted sampling.
	 * Uses Vose's alias method.
	 */
	private void buildAliasTable() {
		int n = cellErrors.length;
		float totalError = 0;
		for (int i = 0; i < n; i++) {
			totalError += cellErrors[i];
		}

		if (totalError <= 0) {
			// Uniform distribution fallback
			for (int i = 0; i < n; i++) {
				prob[i] = 1.0f;
				alias[i] = i;
			}
			tableValid = true;
			return;
		}

		float[] scaled = new float[n];
		for (int i = 0; i < n; i++) {
			scaled[i] = cellErrors[i] * n / totalError;
		}

		// Partition into small and large
		int[] small = new int[n];
		int[] large = new int[n];
		int smallCount = 0, largeCount = 0;

		for (int i = 0; i < n; i++) {
			if (scaled[i] < 1.0f) {
				small[smallCount++] = i;
			} else {
				large[largeCount++] = i;
			}
		}

		while (smallCount > 0 && largeCount > 0) {
			int s = small[--smallCount];
			int l = large[--largeCount];

			prob[s] = scaled[s];
			alias[s] = l;

			scaled[l] = (scaled[l] + scaled[s]) - 1.0f;
			if (scaled[l] < 1.0f) {
				small[smallCount++] = l;
			} else {
				large[largeCount++] = l;
			}
		}

		while (largeCount > 0) {
			prob[large[--largeCount]] = 1.0f;
		}
		while (smallCount > 0) {
			prob[small[--smallCount]] = 1.0f;
		}

		tableValid = true;
	}

	/**
	 * Sample a pixel position biased toward high-error regions.
	 * Uses the alias table for O(1) cell selection, then uniform
	 * random within the selected cell.
	 */
	public int[] samplePosition(Random rnd) {
		if (!tableValid) {
			buildAliasTable();
		}

		int n = cellErrors.length;
		int cell;
		int idx = rnd.nextInt(n);
		if (rnd.nextFloat() < prob[idx]) {
			cell = idx;
		} else {
			cell = alias[idx];
		}

		int gx = cell % gridWidth;
		int gy = cell / gridWidth;

		int pxStart = gx * cellWidth;
		int pyStart = gy * cellHeight;

		int px = pxStart + rnd.nextInt(Math.min(cellWidth, imageWidth - pxStart));
		int py = pyStart + rnd.nextInt(Math.min(cellHeight, imageHeight - pyStart));

		return new int[]{px, py};
	}
}
