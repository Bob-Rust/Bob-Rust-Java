package com.bobrust.generator;

public class BorstColor {
	public final int r;
	public final int g;
	public final int b;
	public final int a;
	public final int rgba;
	
	public BorstColor(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.rgba = (a << 24) | (r << 16) | (g << 8) | (b);
	}
}
