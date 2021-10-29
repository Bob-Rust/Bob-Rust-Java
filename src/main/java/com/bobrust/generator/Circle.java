package com.bobrust.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Circle {
	private final Worker worker;
	
	// position
	public int x;
	public int y;
	
	// radius
	public int r;
	
	public Circle(Worker worker) {
		this.worker = worker;
		Random rnd = worker.rnd;
		this.x = rnd.nextInt(worker.w);
		this.y = rnd.nextInt(worker.h);
		this.r = BorstUtils.SIZES[rnd.nextInt(BorstUtils.SIZES.length)];
	}
	
	public Circle(Worker worker, int x, int y, int r) {
		this.worker = worker;
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public void Mutate() {
		int w = worker.w - 1;
		int h = worker.h - 1;
		Random rnd = worker.rnd;
		
		if(rnd.nextInt(3) == 0) {
			int a = x + (int)(rnd.nextGaussian() * 16); // NormFloat64
			int b = y + (int)(rnd.nextGaussian() * 16); // NormFloat64
			x = BorstUtils.clampInt(a, 0, w);
			y = BorstUtils.clampInt(b, 0, h);
		} else {
			int c = BorstUtils.getClosestSize(r + (int)(rnd.nextGaussian() * 16)); // NormFloat64
			r = BorstUtils.clampInt(c, 1, w);
		}
	}
	

	public Scanline[] Rasterize() {
		int w = worker.w;
		int h = worker.h;
		
		int cache_index = BorstUtils.getClosestSizeIndex(r);
		Scanline[] LINES = CircleCache.CIRCLE_CACHE[cache_index];
		int LENGTH = CircleCache.CIRCLE_CACHE_LENGTH[cache_index];
		
		List<Scanline> list = new ArrayList<>(LENGTH);
		for(int i = 0; i < LENGTH; i++) {
			Scanline line = LINES[i];
			int yy = line.y + y;
			if(yy < 0) {
				continue;
			}
			
			if(yy >= h) {
				break;
			}
			
			int x1 = line.x1 + x;
			int x2 = line.x2 + x;
			x1 = (x1 <  0) ? 0:x1;
			x2 = (x2 >= w) ? (w - 1):x2;
			list.add(new Scanline(yy, x1, x2));
		}
		
		return list.toArray(Scanline[]::new);
	}
}