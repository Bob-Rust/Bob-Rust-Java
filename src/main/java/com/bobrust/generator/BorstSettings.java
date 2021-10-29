package com.bobrust.generator;

import java.awt.image.BufferedImage;

public class BorstSettings {
	// The update interval for the callback.
	public int CallbackInterval = 100;
	
	// The max amount of shapes to generate.
	public int MaxShapes = 4000;
	
	// The default background color of the canvas.
	public int Background = 0;
	
	// The default alpha value.
	public int Alpha = 2;
	
	// The direct image reference.
	public BufferedImage DirectImage;
}
