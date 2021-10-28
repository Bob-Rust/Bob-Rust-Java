package com.bobrust.generator;

import java.awt.image.BufferedImage;

public class BorstSettings {
	// A path to the image that should be loaded
	public String ImagePath;
	
	// How many shapes to generate before it sends
	// the data back to the caller.
	public int CallbackShapes = 100;
	
	// The max amount of shapes to generate.
	public int MaxShapes = 4000;
	
	// The default background color of the canvas.
	public int Background = 0xff000000;
	
	// The default alpha value.
	public int Alpha = 2;
	
	// The width of the image
	public int Width = 0;
	
	// The height of the image
	public int Height = 0;
	
	public BufferedImage DirectImage;
}
