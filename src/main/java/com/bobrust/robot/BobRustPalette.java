package com.bobrust.robot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.*;

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
	
	@Deprecated
	private Point panel_offset;
	
	@Deprecated
	private Point preview_middle;
	
	// Button configuration map
	private GraphicsConfiguration monitor;
	private ButtonConfiguration buttonConfig;
	
	public BobRustPalette() {
		colorMap = new HashMap<>();
	}
	
	public void setData(GraphicsConfiguration monitor, ButtonConfiguration config) {
		this.monitor = Objects.requireNonNull(monitor);
		this.buttonConfig = Objects.requireNonNull(config);
	}
	
	/**
	 * Find the palette in the provided screenshot.
	 * 
	 * TODO: Make this method more general and work for more inputs.
	 *       Where the color palette might not even be to the right of the screen.
	 */
	public Point findPalette(BufferedImage screenshot) {
		int screen_width = screenshot.getWidth();
		int screen_height = screenshot.getHeight();
		
		int x = screen_width - 60;
		
		Point palette_marker = null;
		for (int i = 0; i < screen_height - 45; i++) {
			// 15 per box
			
			if ((screenshot.getRGB(x, i) & 0xffffff) == 0xc0c0c0
			&& (screenshot.getRGB(x, i + 15) & 0xffffff) == 0xff8634
			&& (screenshot.getRGB(x, i + 30) & 0xffffff) == 0xffd734) {
				palette_marker = new Point(x, i);
				break;
			}
		}
		
		if (palette_marker != null) {
			int point_x = screen_width - 150;
			int point_y = palette_marker.y - 15;
			return new Point(point_x, point_y);
		}
		
		return null;
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
	
	public Map<BorstColor, Point> getColorMap() {
		return colorMapCopy;
	}
	
	public void reset() {
		colorMap.clear();
		colorMapCopy = null;
		preview_middle = null;
		panel_offset = null;
		shapeButtons = null;
		sizeButtons = null;
	}
	
	public boolean hasPalette() {
		/*
		 * Because the color map is the only required value
		 * we can assume that if the map has enough elements
		 * then we have the palette.
		 */
		return colorMap.size() == BorstUtils.COLORS.length;
	}
	
	private Point point(int x, int y) {
		// TODO: Use non screen relative points.
		return new Point(panel_offset.x + x, panel_offset.y + y);
	}
	
	public Point getColorPreview() {
		return buttonConfig.colorPreview.with(monitor);
	}
	
	// Returns a spot were the bot can press without changing any state of the game
	public Point getFocusPoint() {
		// point(12, 24);
		return buttonConfig.focus.with(monitor);
	}
	
	public Point getClearButton() {
		// point(55, 24);
		return buttonConfig.clearCanvas.with(monitor);
	}
	
	public Point getSaveButton() {
		// point(95, 24);
		return buttonConfig.saveImage.with(monitor);
	}
	
//	public Point getUpdateButton() {
//		// point(75, 469);
//		return buttonConfig.saveImage;
//	}
	
	public Point getAlphaButtonOld(int index) {
		Point a = buttonConfig.opacity_0.with(monitor);
		Point b = buttonConfig.opacity_1.with(monitor);
		
		// 256 on this span
		double step = (b.x - a.x) / 256.0;
		
		return new Point(
			a.x + (int) (step * BorstUtils.ALPHAS[index]),
			a.y
		);
		
		// Point[] array = getOpacityButtons();
		// return array[index];
	}
	
	public Point getSizeButtonOld(int index) {
		// TODO: Fix
		Point[] array = getSizeButtons();
		return array[index];
	}
	
	public Point getShapeButtonOld(int index) {
		// TODO: Fix
		Point[] array = getShapeButtons();
		return array[index];
	}
	
	public Point getColorButton(BorstColor color) {
		// TODO: Fix
		return colorMap.get(color);
	}
	
//	private Point[] opacityButtons;
//	private Point[] getOpacityButtons() {
//		if (opacityButtons == null) {
//			opacityButtons = new Point[] {
//				point( 22, 138),
//				point( 43, 138),
//				point( 64, 138),
//				point( 85, 138),
//				point(106, 138),
//				point(127, 138),
//			};
//		}
//		return opacityButtons;
//	}
	
	private Point[] sizeButtons;
	private Point[] getSizeButtons() {
		if (sizeButtons == null) {
			sizeButtons = new Point[] {
				point( 25, 62),
				point( 45, 62),
				point( 65, 62),
				point( 85, 62),
				point(105, 62),
				point(125, 62),
			};
		}
		return sizeButtons;
	}
	
	private Point[] shapeButtons;
	private Point[] getShapeButtons() {
		if (shapeButtons == null) {
			shapeButtons = new Point[] {
				point( 27, 100), // Soft halo
				point( 59, 100), // Circle
				point( 91, 100), // Strong halo
				point(123, 100), // Square
			};
		}
		return shapeButtons;
	}
}
