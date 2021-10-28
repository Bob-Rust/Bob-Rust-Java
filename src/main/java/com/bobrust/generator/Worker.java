package com.bobrust.generator;

import java.util.Random;

class Worker {
	private BorstImage buffer;
	private BorstImage target;
	private BorstImage current;
	
	public int w;
	public int h;
	public Random rnd;
	public float score = 0;
	public int counter = 0;
	public final int alpha;

	public Worker(BorstImage target, int alpha) {
		this.w = target.width;
		this.h = target.height;
		this.target = target;
		this.buffer = new BorstImage(w, h);
		this.rnd = new Random(0);
		this.alpha = alpha;
	}

	public void Init(BorstImage current, float score) {
		this.current = current;
		this.score = score;
		this.counter = 0;
	}
	
	public float Energy(Scanline[] lines) {
		this.counter++;
		
		// Because this is called in parallel we could mess up the buffer? by writing to it twize?
		BorstColor color = BorstCore.computeColor(target, current, lines, alpha);

		// Set the buffers region on the lines to the current image
		BorstCore.copyLines_replaceRegion(buffer, current, lines);

		// Draw the lines with the new color to the buffer
		BorstCore.drawLines(buffer, color, lines, alpha);
		
		// Get the difference over the drawn region compared to the current with the lines
		return BorstCore.differencePartial(target, current, buffer, score, lines);
	}
}
