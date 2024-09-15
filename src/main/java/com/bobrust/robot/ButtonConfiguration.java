package com.bobrust.robot;

import com.google.gson.*;

import java.awt.*;

/**
 * A class containing all button configuration data needed to draw
 */
public class ButtonConfiguration {
	public ButtonConfiguration()
	{

	}
	private static final Coordinate DEFAULT = new Coordinate(0, 0, false);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	// Main buttons
	public Coordinate clearCanvas        = DEFAULT;
	public Coordinate saveToDesktop      = DEFAULT;
	public Coordinate saveImage          = DEFAULT;
	public Coordinate clearRotation      = DEFAULT;

	// Tool buttons
	public Coordinate tool_paintBrush    = DEFAULT;

	// Brush buttons
	public Coordinate brush_circle       = DEFAULT;
	public Coordinate brush_square       = DEFAULT;

	// Pixel positions of the size 1 and 32
	public Coordinate size_1             = DEFAULT;
	public Coordinate size_32            = DEFAULT;

	// Pixel position of the opacity 0 and 1
	public Coordinate opacity_0          = DEFAULT;
	public Coordinate opacity_1          = DEFAULT;

	// Color buttons
	public Coordinate color_topLeft      = DEFAULT;
	public Coordinate color_botRight     = DEFAULT;

	// Misc
	/**
	 * Point that can be clicked to gain focus from the game
	 */
	public Coordinate focus              = DEFAULT;
	public Coordinate colorPreview       = DEFAULT;


	public String serialize() {
		return GSON.toJson(this);
	}

	public static ButtonConfiguration deserialize(String json) {
		return GSON.fromJson(json, ButtonConfiguration.class);
	}
}
