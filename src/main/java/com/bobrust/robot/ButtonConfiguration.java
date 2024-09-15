package com.bobrust.robot;

import com.google.gson.*;

import java.awt.*;
import java.util.Objects;

/**
 * A class containing all button configuration data needed to draw
 */
public class ButtonConfiguration {
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
	
	/**
	 * Update this configuration with values
	 */
	public void update(ButtonConfiguration config) {
		Objects.requireNonNull(config, "ButtonConfiguration was updated with a null value");
		
		this.clearCanvas        = Objects.requireNonNullElse(config.clearCanvas,     DEFAULT);
		this.saveToDesktop      = Objects.requireNonNullElse(config.saveToDesktop,   DEFAULT);
		this.saveImage          = Objects.requireNonNullElse(config.saveImage,       DEFAULT);
		this.clearRotation      = Objects.requireNonNullElse(config.clearRotation,   DEFAULT);
		this.tool_paintBrush    = Objects.requireNonNullElse(config.tool_paintBrush, DEFAULT);
		this.brush_circle       = Objects.requireNonNullElse(config.brush_circle,    DEFAULT);
		this.brush_square       = Objects.requireNonNullElse(config.brush_square,    DEFAULT);
		this.size_1             = Objects.requireNonNullElse(config.size_1,          DEFAULT);
		this.size_32            = Objects.requireNonNullElse(config.size_32,         DEFAULT);
		this.opacity_0          = Objects.requireNonNullElse(config.opacity_0,       DEFAULT);
		this.opacity_1          = Objects.requireNonNullElse(config.opacity_1,       DEFAULT);
		this.color_topLeft      = Objects.requireNonNullElse(config.color_topLeft,   DEFAULT);
		this.color_botRight     = Objects.requireNonNullElse(config.color_botRight,  DEFAULT);
		this.focus              = Objects.requireNonNullElse(config.focus,           DEFAULT);
		this.colorPreview       = Objects.requireNonNullElse(config.colorPreview,    DEFAULT);
	}
	
	/**
	 * Coordinate (x,y) that should be expanded to the screen size used
	 */
	public record Coordinate(int x, int y, boolean valid) {
		public Coordinate(int x, int y) {
			this(x, y, true);
		}
		
		public static Coordinate from(double x, double y) {
			return new Coordinate((int) x, (int) y, true);
		}
		
		public Point with(GraphicsConfiguration config) {
			return new Point(
				(int) (x),
				(int) (y)
			);
		}
		
		private static final int FLAG_IS_BOT   = 1;
		private static final int FLAG_IS_RIGHT = 2;
		
		public static final int SIDE_TOP_LEFT  = 0,
								SIDE_TOP_RIGHT = FLAG_IS_RIGHT,
								SIDE_BOT_LEFT  = FLAG_IS_BOT,
								SIDE_BOT_RIGHT = FLAG_IS_BOT | FLAG_IS_RIGHT;
		
		/**
		 * Calculate the coordinate from the specified position in the source to the target
		 */
		public static Coordinate fromSide(double x, double y, int flags, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
			// Height is the coordinate modifier
			if ((flags & FLAG_IS_RIGHT) != 0) {
				// 374 from the right side when 1080
				// 187 from the right side when 540
				
				x = targetWidth - (x * targetHeight / (double) sourceHeight);
			} else {
				x = (x / (double) sourceWidth) * targetWidth;
			}
			
			if ((flags & FLAG_IS_BOT) != 0) {
				y = sourceHeight - y;
			}
			
			// Transform
			y = (y / (double) sourceHeight) * targetHeight;
			
			// Calculate the new coordinates
			int nx = (int) x;
			int ny = (int) y;
			return new Coordinate(nx, ny, true);
		}
	}
	
	public String serialize() {
		return GSON.toJson(this);
	}
	
	public static ButtonConfiguration deserialize(String json) {
		return GSON.fromJson(json, ButtonConfiguration.class);
	}
}
