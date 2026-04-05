package com.bobrust.generator;

import com.bobrust.util.data.AppConstants;

import java.util.Random;

public class Circle {
	private final Worker worker;

	// Position
	public int x;
	public int y;

	// Radius
	public int r;

	public Circle(Worker worker) {
		this.worker = worker;
		this.randomize();
	}

	public Circle(Worker worker, int x, int y, int r) {
		this.worker = worker;
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public void mutateShape() {
		int w = worker.w - 1;
		int h = worker.h - 1;
		Random rnd = worker.getRandom();
		GradientMap gradientMap = AppConstants.USE_ADAPTIVE_SIZE ? worker.getGradientMap() : null;

		if (rnd.nextInt(3) == 0) {
			// Mutate position — scale perturbation by local gradient
			float scale = (gradientMap != null) ? gradientMap.getMutationScale(x, y) : 1.0f;
			int a = x + (int)(rnd.nextGaussian() * 16 * scale);
			int b = y + (int)(rnd.nextGaussian() * 16 * scale);
			x = BorstUtils.clampInt(a, 0, w);
			y = BorstUtils.clampInt(b, 0, h);
		} else {
			if (gradientMap != null) {
				// Use gradient-biased size selection during mutation too
				int sizeIdx = gradientMap.selectSizeIndex(rnd, x, y);
				r = BorstUtils.SIZES[sizeIdx];
			} else {
				int c = BorstUtils.getClosestSize(r + (int)(rnd.nextGaussian() * 16));
				r = BorstUtils.clampInt(c, 1, w);
			}
		}
	}

	public void randomize() {
		randomize(null);
	}

	/**
	 * Randomize circle position and size.
	 * When an {@link ErrorMap} is provided and error-guided placement is enabled,
	 * 80% of placements are biased toward high-error regions via importance
	 * sampling. The remaining 20% use uniform random placement for exploration.
	 *
	 * When adaptive size selection is enabled and a {@link GradientMap} is
	 * available, circle sizes are biased by local gradient: small circles
	 * near edges, large circles in smooth areas.
	 */
	public void randomize(ErrorMap errorMap) {
		Random rnd = worker.getRandom();
		if (errorMap != null && rnd.nextFloat() < 0.8f) {
			int[] pos = errorMap.samplePosition(rnd);
			this.x = pos[0];
			this.y = pos[1];
		} else {
			this.x = rnd.nextInt(worker.w);
			this.y = rnd.nextInt(worker.h);
		}

		GradientMap gradientMap = AppConstants.USE_ADAPTIVE_SIZE ? worker.getGradientMap() : null;
		if (gradientMap != null) {
			int sizeIdx = gradientMap.selectSizeIndex(rnd, this.x, this.y);
			this.r = BorstUtils.SIZES[sizeIdx];
		} else {
			this.r = BorstUtils.SIZES[rnd.nextInt(BorstUtils.SIZES.length)];
		}
	}

	public void fromValues(Circle shape) {
		this.r = shape.r;
		this.x = shape.x;
		this.y = shape.y;
	}
}