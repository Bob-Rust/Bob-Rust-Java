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
	
	public BobRustPalette() {
		colorMap = new HashMap<>();
	}
	
	public synchronized boolean initWith(BufferedImage screenshot, GraphicsConfiguration monitor) {
		this.monitor = Objects.requireNonNull(monitor);
		this.buttonConfig = BobRustPaletteGenerator.createAutomaticConfiguration(3, screenshot);
		
		Point a = this.buttonConfig.color_topLeft.with(monitor);
		Point b = this.buttonConfig.color_botRight.with(monitor);
		
		var rect = monitor.getBounds();
		final int color_width  = 4;
		final int color_height = 16;
		for (int x = 0; x < color_width; x++) {
			for (int y = 0; y < color_height; y++) {
				int x_pos = a.x + ((b.x - a.x) * x) / (color_width - 1);
				int y_pos = a.y + ((b.y - a.y) * y) / (color_height - 1);
				
				int rgb = screenshot.getRGB(x_pos, y_pos);
				BorstColor color = BorstUtils.getClosestColor(rgb);
				
				Point localPoint = new Point(x_pos, y_pos);
				localPoint.translate(rect.x, rect.y);
				
				colorMap.putIfAbsent(color, localPoint);
			}
		}
		
		/*
		{
			var image = screenshot.getSubimage(a.x, a.y, b.x - a.x, b.y - a.y);
			Graphics2D g = image.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.setColor(Color.white);
			for (Map.Entry<BorstColor, Point> entry : colorMap.entrySet()) {
				Point point = entry.getValue();
				Point sc = new Point(point.x, point.y);
				sc.translate(-rect.x, -rect.y);
				g.drawOval(sc.x - 15, sc.y - 7, 30, 15);
			}
			g.dispose();
			
			javax.swing.JOptionPane.showConfirmDialog(
				null,
				new javax.swing.JLabel(new javax.swing.ImageIcon(image)),
				"Debug Image Palette",
				javax.swing.JOptionPane.OK_CANCEL_OPTION,
				javax.swing.JOptionPane.PLAIN_MESSAGE
			);
		}
		*/
		
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
		
		// TODO: Check if the size is the same as the precomputed array
		double step = (b.x - a.x) / (double) (BorstUtils.SIZES.length - 1);
		return new Point(
			a.x + (int) (step * index),
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
