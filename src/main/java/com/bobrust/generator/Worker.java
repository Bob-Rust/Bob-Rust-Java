package com.bobrust.generator;

import java.util.Random;

class Worker {
	private final BorstImage target;
	private BorstImage current;
	public final Random rnd;
	public final int alpha;
	
	public final int w;
	public final int h;
	public float score;
	public int counter;

	public Worker(BorstImage target, int alpha) {
		this.w = target.width;
		this.h = target.height;
		this.target = target;
		this.rnd = new Random(0);
		this.alpha = alpha;
	}

	public void init(BorstImage current, float score) {
		this.current = current;
		this.score = score;
		this.counter = 0;
	}
	
	public float getEnergy(Scanline[] lines) {
		this.counter++;
		
		return BorstCore.differencePartialThread(target, current, score, alpha, lines);
	}
}
