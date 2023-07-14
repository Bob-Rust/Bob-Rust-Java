package com.bobrust.generator.sorter;

import java.util.Objects;

import com.bobrust.generator.BorstUtils;

public class Blob {
	public final int x;
	public final int y;
	public final int size;
	public final int color;
	public final int alpha;
	
	public final int colorIndex;
	public final int sizeIndex;
	public final int alphaIndex;
	public final int shapeIndex;
	
	private final int hash;
	
	protected Blob(int x, int y, int size, int color, int alpha, int shape) {
		this.x = x;
		this.y = y;
		this.colorIndex = BorstUtils.getClosestColorIndex(color);
		this.sizeIndex = BorstUtils.getClosestSizeIndex(size);
		this.alphaIndex = BorstUtils.getClosestAlphaIndex(alpha);
		
		this.size = BorstUtils.SIZES[this.sizeIndex];
		this.color = BorstUtils.COLORS[this.colorIndex].rgb;
		this.alpha = BorstUtils.ALPHAS[this.alphaIndex];
		this.shapeIndex = shape;
		
		this.hash = Objects.hash(x, y, sizeIndex, colorIndex, alphaIndex, shapeIndex);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	public static Blob of(int x, int y, int size, int color, int alpha, int shape) {
		return new Blob(x, y, size, color, alpha, shape);
	}
	
	@Override
	public String toString() {
		return "{ x: %d, y: %d, size: %d, color: #%06x, shape: %s }".formatted(
			x,
			y,
			size,
			color,
			switch (shapeIndex) {
				case 0 -> "Soft Halo";
				case 2 -> "Hard Halo";
				case 3 -> "Square";
				default -> "Circle"; // 1
			}
		);
	}
}
