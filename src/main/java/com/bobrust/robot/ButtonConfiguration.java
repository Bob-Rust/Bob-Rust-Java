package com.bobrust.robot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;

/**
 * A class containing all button configuration data needed to draw
 */
public class ButtonConfiguration {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	// Main buttons
	public Coordinate clearCanvas = Coordinate.INVALID;
	public Coordinate saveImageToDesktop = Coordinate.INVALID;
	public Coordinate saveImage = Coordinate.INVALID;
	public Coordinate clearCanvasRotation = Coordinate.INVALID;
	
	// Tool buttons
	public Coordinate tool_paintBrush = Coordinate.INVALID;
	
	// Brush buttons
	public Coordinate brush_circle = Coordinate.INVALID;
	public Coordinate brush_square = Coordinate.INVALID;
	
	// Pixel positions of the size 1 and 32
	public Coordinate size_1 = Coordinate.INVALID;
	public Coordinate size_32 = Coordinate.INVALID;
	
	// Pixel position of the opacity 0 and 1
	public Coordinate opacity_0 = Coordinate.INVALID;
	public Coordinate opacity_1 = Coordinate.INVALID;
	
	// Color buttons
	public Coordinate color_topLeft = Coordinate.INVALID;
	public Coordinate color_topRight = Coordinate.INVALID;
	
	// Misc
	/**
	 * Point that can be clicked to gain focus from the game
	 */
	public Coordinate focus = Coordinate.INVALID;
	public Coordinate colorPreview = Coordinate.INVALID;
	
	/**
	 * Coordinate (x,y) that should be expanded to the screen size used
	 */
	public record Coordinate(double x, double y, boolean valid) {
		public static final Coordinate INVALID = new Coordinate(0, 0, false);
		
		public Coordinate(double x, double y) {
			this(x, y, true);
		}
		
		public Point with(GraphicsConfiguration config) {
			var rect = config.getBounds();
			return new Point(
				(int) (x * rect.getWidth()),
				(int) (y * rect.getHeight())
			);
		}
	}
	
	public String serialize() {
		return GSON.toJson(this);
	}
	
	public static ButtonConfiguration deserialize(String json) {
		return GSON.fromJson(json, ButtonConfiguration.class);
	}
}
