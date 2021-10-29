package com.bobrust.generator;

public class BorstColor {
	public final int r;
	public final int g;
	public final int b;
	public final int rgb;
	
	public BorstColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.rgb = (r << 16) | (g << 8) | (b);
	}
	
	@Override
	public int hashCode() {
		return rgb;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof BorstColor)) return false;
		return rgb == obj.hashCode();
	}
}
