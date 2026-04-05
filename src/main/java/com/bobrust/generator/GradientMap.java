package com.bobrust.generator;

import java.util.Random;

/**
 * Precomputed Sobel gradient magnitude map of the target image, downsampled
 * to a coarse grid. Provides normalized [0,1] gradient values that indicate
 * edge density at any pixel position.
 *
 * High gradient = edges/detail, low gradient = smooth regions.
 *
 * Used by adaptive size selection (Proposal 3) to bias circle sizes:
 * small circles near edges, large circles in smooth areas.
 */
public class GradientMap {
	private static final int DEFAULT_GRID_DIM = 32;

	final int gridWidth;
	final int gridHeight;
	final int cellWidth;
	final int cellHeight;
	final int imageWidth;
	final int imageHeight;

	/** Normalized gradient values per grid cell, range [0,1]. */
	final float[] cellGradients;

	public GradientMap(int imageWidth, int imageHeight) {
		this(imageWidth, imageHeight, DEFAULT_GRID_DIM, DEFAULT_GRID_DIM);
	}

	public GradientMap(int imageWidth, int imageHeight, int gridWidth, int gridHeight) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.cellWidth = Math.max(1, (imageWidth + gridWidth - 1) / gridWidth);
		this.cellHeight = Math.max(1, (imageHeight + gridHeight - 1) / gridHeight);
		this.cellGradients = new float[gridWidth * gridHeight];
	}

	/**
	 * Compute the gradient map from the target image using Sobel operators.
	 * This is a one-time cost at initialization.
	 */
	public void compute(BorstImage target) {
		int w = target.width;
		int h = target.height;

		// Convert to grayscale luminance
		float[] gray = new float[w * h];
		for (int i = 0; i < w * h; i++) {
			int px = target.pixels[i];
			int r = (px >>> 16) & 0xff;
			int g = (px >>> 8) & 0xff;
			int b = px & 0xff;
			gray[i] = 0.299f * r + 0.587f * g + 0.114f * b;
		}

		// Compute Sobel gradient magnitude per pixel, accumulate into grid cells
		float[] cellSums = new float[gridWidth * gridHeight];
		int[] cellCounts = new int[gridWidth * gridHeight];

		for (int y = 1; y < h - 1; y++) {
			int gy = Math.min(y / cellHeight, gridHeight - 1);
			for (int x = 1; x < w - 1; x++) {
				int gx = Math.min(x / cellWidth, gridWidth - 1);

				// Sobel X kernel: [-1 0 1; -2 0 2; -1 0 1]
				float sx = -gray[(y - 1) * w + (x - 1)] + gray[(y - 1) * w + (x + 1)]
						 - 2 * gray[y * w + (x - 1)]    + 2 * gray[y * w + (x + 1)]
						 - gray[(y + 1) * w + (x - 1)]  + gray[(y + 1) * w + (x + 1)];

				// Sobel Y kernel: [-1 -2 -1; 0 0 0; 1 2 1]
				float sy = -gray[(y - 1) * w + (x - 1)] - 2 * gray[(y - 1) * w + x] - gray[(y - 1) * w + (x + 1)]
						 + gray[(y + 1) * w + (x - 1)]  + 2 * gray[(y + 1) * w + x] + gray[(y + 1) * w + (x + 1)];

				float magnitude = (float) Math.sqrt(sx * sx + sy * sy);

				int cellIdx = gy * gridWidth + gx;
				cellSums[cellIdx] += magnitude;
				cellCounts[cellIdx]++;
			}
		}

		// Compute average gradient per cell
		float maxGradient = 0;
		for (int i = 0; i < cellGradients.length; i++) {
			if (cellCounts[i] > 0) {
				cellGradients[i] = cellSums[i] / cellCounts[i];
			} else {
				cellGradients[i] = 0;
			}
			maxGradient = Math.max(maxGradient, cellGradients[i]);
		}

		// Normalize to [0, 1]
		if (maxGradient > 0) {
			for (int i = 0; i < cellGradients.length; i++) {
				cellGradients[i] /= maxGradient;
			}
		}
	}

	/**
	 * Get the normalized gradient value [0,1] at the given pixel position.
	 * 0 = smooth area, 1 = strongest edge.
	 */
	public float getGradient(int x, int y) {
		int gx = Math.min(x / cellWidth, gridWidth - 1);
		int gy = Math.min(y / cellHeight, gridHeight - 1);
		if (gx < 0) gx = 0;
		if (gy < 0) gy = 0;
		return cellGradients[gy * gridWidth + gx];
	}

	/**
	 * Select a size index from SIZES weighted by local gradient.
	 * High gradient favors small sizes (low indices), low gradient favors large sizes.
	 *
	 * @param rnd random source
	 * @param x pixel x position
	 * @param y pixel y position
	 * @return index into BorstUtils.SIZES
	 */
	public int selectSizeIndex(Random rnd, int x, int y) {
		float gradient = getGradient(x, y);
		int numSizes = BorstUtils.SIZES.length;
		float[] weights = new float[numSizes];
		float totalWeight = 0;

		for (int i = 0; i < numSizes; i++) {
			float sizeNorm = (float) i / (numSizes - 1); // 0=smallest, 1=largest
			// High gradient -> prefer small (low sizeNorm), low gradient -> prefer large
			weights[i] = (float) Math.exp(-4.0 * Math.abs(sizeNorm - (1.0 - gradient)));
			totalWeight += weights[i];
		}

		// Weighted random selection
		float r = rnd.nextFloat() * totalWeight;
		float cumulative = 0;
		for (int i = 0; i < numSizes; i++) {
			cumulative += weights[i];
			if (r <= cumulative) {
				return i;
			}
		}
		return numSizes - 1; // fallback
	}

	/**
	 * Get the position perturbation scale based on local gradient.
	 * Near edges (high gradient): small perturbations for fine-tuning.
	 * In smooth areas (low gradient): large perturbations for broad exploration.
	 *
	 * @return scale factor for Gaussian perturbation (range roughly [0.25, 1.0])
	 */
	public float getMutationScale(int x, int y) {
		float gradient = getGradient(x, y);
		// High gradient -> small scale (0.25), low gradient -> large scale (1.0)
		return 1.0f - 0.75f * gradient;
	}
}
