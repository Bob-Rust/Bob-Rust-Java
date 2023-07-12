package com.bobrust.util;

import java.awt.Color;

public class Sign {
	private final String name;
	private final int width;
	private final int height;
	private final Color averageColor;
	
	public Sign(String name, int width, int height, Color averageColor) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.averageColor = averageColor;
	}
	
	public String getName() {
		return name;
	}
	
	public Color getAverageColor() {
		return averageColor;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public String toString() {
		return "Sign { name: '%s', width: %d, height: %d }".formatted(name, width, height);
	}
}
