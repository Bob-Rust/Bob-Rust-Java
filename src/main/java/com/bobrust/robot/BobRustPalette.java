package com.bobrust.robot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import com.bobrust.util.data.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.BorstUtils;

/**
 * Used to analyse the gui of rust to get information about the game
 * @author HardCoded
 */
public class BobRustPalette {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPalette.class);
	private final Map<BorstColor, Point> colorMap;
	private Map<BorstColor, Point> colorMapCopy;

	// Button configuration map
	private GraphicsConfiguration monitor;
	private ButtonConfiguration buttonConfig;
	public void setButtonConfig(ButtonConfiguration config)
	{
		buttonConfig=config;
	}
	public Rectangle paletteRect;
	public void setPaletteRect(Rectangle r)

	{
		this.paletteRect=r;
	}
	public BobRustPalette() {
		colorMap = new HashMap<>();
	}
	private final static int GRID_ROWS = 16;  // 16 rows
	private final static int GRID_COLS = 4;   // 4 columns
	public boolean initWith(BufferedImage screenshot, GraphicsConfiguration monitor) {
		int startX = 0;
		int startY = 0;
		int endX = screenshot.getWidth();
		int endY = screenshot.getHeight();

		// Calculate the size of each block in the 4x16 grid
		int blockWidth = (endX - startX) / GRID_COLS;
		int blockHeight = (endY - startY) / GRID_ROWS;

		// Extract colors from each block and adjust coordinates based on the paletteRect position
		for (int x = 0; x < GRID_COLS; x++) {
			for (int y = 0; y < GRID_ROWS; y++) {
				// Get the center of each color block in the palette screenshot
				int x_pos = startX + x * blockWidth + blockWidth / 2;
				int y_pos = startY + y * blockHeight + blockHeight / 2;

				// Adjust the position to be relative to the full screen
				int adjustedX = x_pos + paletteRect.x;
				int adjustedY = y_pos + paletteRect.y;

				LOGGER.info("Sampling color at adjusted position: ({}, {})", adjustedX, adjustedY);
				int rgb = screenshot.getRGB(x_pos, y_pos);
				BorstColor color = BorstUtils.getClosestColor(rgb);
				Point localPoint = new Point(adjustedX, adjustedY);
				colorMap.putIfAbsent(color, localPoint);
				LOGGER.info("Detected color: {} at adjusted position: {}", color, localPoint);
			}
		}

		// Check if all colors were found
		for (BorstColor color : BorstUtils.COLORS) {
			if (!colorMap.containsKey(color)) {
				LOGGER.warn("Could not find all colors in the color palette. Found {}/{} colors", colorMap.size(), BorstUtils.COLORS.length);
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unused")
	private void debugGenerateColorMap(Set<Integer> colors) {
		StringBuilder sb = new StringBuilder();
		sb.append("public static final BorstColor[] COLORS = {\n");
		for (int rgb : colors) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >>  8) & 0xff;
			int b = (rgb      ) & 0xff;
			sb.append("    new BorstColor(").append(r)
					.append(", ").append(g).append(", ").append(b)
					.append("),\n");
		}
		sb.append("};");
		LOGGER.info(sb);
	}

	public Map<BorstColor, Point> getColorMap() {
		return colorMapCopy;
	}

	public void reset() {
		colorMap.clear();
		colorMapCopy = null;
	}

	public Point getColorPreview() {
		return buttonConfig.colorPreview.with(monitor);
	}

	public Point getFocusPoint() {
		return buttonConfig.focus.with(monitor);
	}

	public Point getClearButton() {
		return buttonConfig.clearCanvas.with(monitor);
	}

	public Point getSaveButton() {
		return buttonConfig.saveImage.with(monitor);
	}

	public Point getAlphaButton(int index) {
		Point a = buttonConfig.opacity_0.with(monitor);
		Point b = buttonConfig.opacity_1.with(monitor);

		// 256 on this span
		double step = (b.x - a.x) / 256.0;

		// TODO: Check if the opacity is the same as in the precomputed array
		Point point = new Point(a.x + (int) (step * BorstUtils.ALPHAS[index]), a.y);
		return point;
	}

	public Point getSizeButton(int index) {
		Point a = buttonConfig.size_1.with(monitor);
		Point b = buttonConfig.size_32.with(monitor);

		int x_pos=a.x+(int)((b.x-a.x)*(BorstUtils.SIZES[index]/100.0));
		return new Point(x_pos,a.y);
	}

	public Point getShapeButton(int index) {
		return switch (index) {
			case AppConstants.CIRCLE_SHAPE -> buttonConfig.brush_circle.with(monitor);
			case AppConstants.SQUARE_SHAPE -> buttonConfig.brush_square.with(monitor);
			default -> throw new RuntimeException("Invalid shape button index = " + index);
		};
	}

	public Point getColorButton(BorstColor color) {
		return colorMap.get(color);
	}
}
