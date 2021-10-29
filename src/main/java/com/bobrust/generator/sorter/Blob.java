package com.bobrust.generator.sorter;

import java.util.Objects;

import com.bobrust.generator.BorstUtils;

public class Blob {
	public final int x;
	public final int y;
	public final int size;
	public final int color;
	public final int colorIndex;
	public final int sizeIndex;
	private final int hash;
	
	protected Blob(int x, int y, int size, int color) {
		this.x = x;
		this.y = y;
		this.colorIndex = BorstUtils.getClosestColorIndex(color);
		this.sizeIndex = BorstUtils.getClosestSizeIndex(size);
		this.size = BorstUtils.SIZES[this.sizeIndex];
		this.color = BorstUtils.COLORS[this.colorIndex].rgb;
		this.hash = Objects.hash(this.x, this.y, this.size, this.color, this.colorIndex, this.sizeIndex);
	}
	
	public int getSizeIndex() {
		return sizeIndex;
	}
	
	public int getColorIndex() {
		return colorIndex;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	public static Blob get(int x, int y, int size, int color) {
		return new Blob(x, y, size, color);
	}
	
	@Override
	public String toString() {
		return "{ x: %d, y: %d, size: %d, color: #%06x }".formatted(x, y, size, color);
	}
}
