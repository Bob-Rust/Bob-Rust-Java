package com.bobrust.generator;

class Scanline {
	public int y;
	public int x1;
	public int x2; // inclusive
	
	public Scanline(int y, int x1, int x2) {
		this.y = y;
		this.x1 = x1;
		this.x2 = x2;
	}
}