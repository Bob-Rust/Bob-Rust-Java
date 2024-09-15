package com.bobrust.robot;

import java.awt.*;

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
