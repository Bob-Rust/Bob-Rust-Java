package com.bobrust.robot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import com.bobrust.util.data.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.BorstUtils;

import com.bobrust.robot.ButtonConfiguration.*;

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
		this.buttonConfig = createAutomaticConfiguration(3, screenshot);
		
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
		
		for (BorstColor color : BorstUtils.COLORS) {
			if (!colorMap.containsKey(color)) {
				LOGGER.warn("Could not find all colors in the color palette. Found {}/{} colors", colorMap.size(), BorstUtils.COLORS.length);
				return false;
			}
		}
		
		return true;
	}
	
	public static ButtonConfiguration createAutomaticConfiguration(int version, BufferedImage screenshot) {
		// Only supported version
		return createAutomaticV3(screenshot);
	}
	
	public static ButtonConfiguration createAutomaticV3(BufferedImage screenshot) {
		int screen_width = screenshot.getWidth();
		int screen_height = screenshot.getHeight();
		
		// Rust keep the height aspect ratio this means we can figure out almost exact point's on the screen
		
		// Delete button starts ( 24, 24), size (72, 72) on a (1080 height)
		// Next   button starts (120, 24), size (72, 72) offset (96, 0)
		// Next   button starts (216, 24), size (72, 72) offset (96, 0)
		
		ButtonConfiguration config = new ButtonConfiguration();
		
		final double btnOffset = 96;
		config.clearCanvas     = Coordinate.fromSide(24 + 36 + btnOffset * 0, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.saveToDesktop   = Coordinate.fromSide(24 + 36 + btnOffset * 1, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.saveImage       = Coordinate.fromSide(24 + 36 + btnOffset * 2, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.clearRotation   = Coordinate.fromSide(24 + 36 + btnOffset * 5, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		
		// The right toolbar is size (374, 1080) on (1080 height)
		config.tool_paintBrush = Coordinate.fromSide(374 - 140, 84 , Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.brush_circle    = Coordinate.fromSide(374 - 144, 214, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.brush_square    = Coordinate.fromSide(374 - 192, 214, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.size_1          = Coordinate.fromSide(374 - 140, 283, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.size_32         = Coordinate.fromSide(374 - 289, 283, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.opacity_0       = Coordinate.fromSide(374 - 140, 368, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.opacity_1       = Coordinate.fromSide(374 - 289, 368, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.color_topLeft   = Coordinate.fromSide(374 - 62 , 481, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.color_botRight  = Coordinate.fromSide(374 - 308, 889, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		
		config.focus           = Coordinate.fromSide(474, 102, Coordinate.SIDE_BOT_RIGHT, 1920, 1080, screen_width, screen_height);
		config.colorPreview    = config.focus;
		
		return config;
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
	
	// TODO: Remove this code
	/*
	@Deprecated
	public synchronized boolean analyse(JDialog dialog, BufferedImage bi, Rectangle screenBounds, Point screenOffset) {
		this.panel_offset = new Point(screenOffset.x, screenOffset.y - 152);
		// (-215, -67) from screen corner
		this.preview_middle = new Point(screenBounds.x + screenBounds.width - 215, screenBounds.y + screenBounds.height - 67);
		this.colorMap.clear();
		
		Point screenLocation = screenBounds.getLocation();
		Rectangle rect = new Rectangle(panel_offset.x - screenLocation.x, panel_offset.y - screenLocation.y, 150, 570);
		BufferedImage image;
		try {
			image = bi.getSubimage(rect.x, rect.y, rect.width, rect.height);
		} catch (Exception e) {
			return false;
		}
		
		// 128x240 (4x16 colors)
		// tiles   (16 x 32)
		// Set<Integer> colors = new LinkedHashSet<>();
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 16; y++) {
				int x_pos = 27 + x * 32;
				int y_pos = 172 + y * 15;
				
				int rgb = image.getRGB(x_pos, y_pos);
				// colors.add(rgb);
				BorstColor color = BorstUtils.getClosestColor(rgb);
				colorMap.putIfAbsent(color, point(x_pos, y_pos));
			}
		}
		// debugGenerateColorMap(colors);
		
		{
			Point zero = point(0, 0);
			Graphics2D g = image.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.setColor(Color.white);
			for (Map.Entry<BorstColor, Point> entry : colorMap.entrySet()) {
				Point point = entry.getValue();
				Point sc = new Point(point.x, point.y);
				g.drawOval(sc.x - 15 - zero.x, sc.y - 7 - zero.y, 30, 15);
			}
			g.dispose();
			// JOptionPane.showConfirmDialog(dialog, new JLabel(new ImageIcon(test)), "Debug Image Palette", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		}
		
		for (BorstColor color : BorstUtils.COLORS) {
			if (!colorMap.containsKey(color)) {
				LOGGER.warn("Could not find all colors in the color palette. Found {}/{} colors", colorMap.size(), BorstUtils.COLORS.length);
				return false;
			}
		}
		
		this.colorMapCopy = Map.copyOf(colorMap);
		
		int dialogResult = JOptionPane.showConfirmDialog(dialog, new JLabel(new ImageIcon(image)), "Is this the color palette?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (dialogResult == JOptionPane.YES_OPTION) {
			// We got the color palette
			return true;
		} else {
			return false;
		}
	}
	*/
	
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
