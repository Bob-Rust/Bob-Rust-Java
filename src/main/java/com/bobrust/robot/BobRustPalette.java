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
	private final static int GRID_ROWS = 16;  // 16 rows
	private final static int GRID_COLS = 4;   // 4 columns

	private final Map<BorstColor, Point> colorMap;
	private Map<BorstColor, Point> colorMapCopy;
	
	// Button configuration map
	private GraphicsConfiguration monitor;
	private ButtonConfiguration buttonConfig;

	public BobRustPalette() {
		this.colorMap = new HashMap<>();
	}

	public synchronized boolean initWith(BufferedImage screenshot, GraphicsConfiguration monitor, ButtonConfiguration config) {
		this.monitor = Objects.requireNonNull(monitor);
		this.buttonConfig = Objects.requireNonNull(config);
		
		Point a = config.color_topLeft.with(monitor);
		Point b = config.color_botRight.with(monitor);
		
		var rect = monitor.getBounds();
		for (int x = 0; x < GRID_COLS; x++) {
			for (int y = 0; y < GRID_ROWS; y++) {
				int x_pos = a.x + ((b.x - a.x) * x) / (GRID_COLS - 1);
				int y_pos = a.y + ((b.y - a.y) * y) / (GRID_ROWS - 1);
				
				int rgb = screenshot.getRGB(x_pos, y_pos);
				BorstColor color = BorstUtils.getClosestColor(rgb);
				
				Point localPoint = new Point(x_pos, y_pos);
				localPoint.translate(rect.x, rect.y);
				
				colorMap.putIfAbsent(color, localPoint);
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
		return new Point(
			a.x + (int) (step * BorstUtils.ALPHAS[index]),
			a.y
		);
	}

	public Point getSizeButton(int index) {
		Point a = buttonConfig.size_1.with(monitor);
		Point b = buttonConfig.size_32.with(monitor);

		int x_pos = a.x + (int)((b.x - a.x) * (BorstUtils.SIZES[index] / 100.0));
		return new Point(
			x_pos,
			a.y
		);
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
