package com.bobrust.generator;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class Worker {
	private final BorstImage target;
	private BorstImage current;
	public final int alpha;

	public final int w;
	public final int h;
	public float score;
	private final AtomicInteger counter = new AtomicInteger();
	private ErrorMap errorMap;
	private GradientMap gradientMap;

	public Worker(BorstImage target, int alpha) {
		this.w = target.width;
		this.h = target.height;
		this.target = target;
		this.alpha = alpha;
	}

	/** Returns the error map, or null if error-guided placement is disabled. */
	public ErrorMap getErrorMap() {
		return errorMap;
	}

	/** Sets the error map (called from Model when error-guided placement is enabled). */
	public void setErrorMap(ErrorMap errorMap) {
		this.errorMap = errorMap;
	}

	/** Returns the gradient map, or null if adaptive sizing is disabled. */
	public GradientMap getGradientMap() {
		return gradientMap;
	}

	/** Sets the gradient map (called from Model when adaptive sizing is enabled). */
	public void setGradientMap(GradientMap gradientMap) {
		this.gradientMap = gradientMap;
	}

	/**
	 * Returns a thread-local Random instance for use in parallel operations.
	 * This avoids lock contention on a shared Random instance.
	 */
	public Random getRandom() {
		return ThreadLocalRandom.current();
	}

	public void init(BorstImage current, float score) {
		this.current = current;
		this.score = score;
		this.counter.set(0);
	}

	public float getEnergy(Circle circle) {
		this.counter.incrementAndGet();
		int cache_index = BorstUtils.getClosestSizeIndex(circle.r);
		return BorstCore.differencePartialThread(target, current, score, alpha, cache_index, circle.x, circle.y);
	}

	public int getCounter() {
		return counter.get();
	}
}
