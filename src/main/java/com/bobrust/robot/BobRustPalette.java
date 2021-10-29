package com.bobrust.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.BorstUtils;
import com.bobrust.logging.LogUtils;

/**
 * Used to analyse the gui of rust to get information about the game
 * @author HardCoded
 */
public class BobRustPalette {
	private final Map<BorstColor, Point> colorMap;
	private Point panel_offset;
	
	public BobRustPalette() {
		colorMap = new HashMap<>();
	}
	
	public synchronized boolean analyse(JDialog dialog, BufferedImage bi, Point panel_offset) {
		panel_offset = new Point(panel_offset.x, panel_offset.y - 152);
		this.panel_offset = panel_offset;
		this.colorMap.clear();
		
		Rectangle rect = new Rectangle(panel_offset.x, panel_offset.y, 150, 525);
		BufferedImage image;
		try {
			image = bi.getSubimage(rect.x, rect.y, rect.width, rect.height);
		} catch(Exception e) {
			return false;
		}
		
		for(int x = 0; x < 4; x++) {
			for(int y = 0; y < 8; y++) {
				int x_pos = 27 + x * 32;
				int y_pos = 180 + y * 30;
				
				int rgb = image.getRGB(x_pos, y_pos);
				BorstColor color = BorstUtils.getClosestColor(rgb);
				colorMap.putIfAbsent(color, point(x_pos, y_pos));
			}
		}
		
		for(BorstColor color : BorstUtils.COLORS) {
			if(!colorMap.containsKey(color)) {
				LogUtils.warn("Could not find all colors in the color palette. Found %d/20 colors", colorMap.size());
				return false;
			}
		}
		
		int dialogResult = JOptionPane.showConfirmDialog(dialog, new JLabel(new ImageIcon(image)), "Is this the color palette?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(dialogResult == JOptionPane.YES_OPTION) {
			// We got the color palette
			return true;
		} else {
			return false;
		}
	}
	
	protected String dumpMap() {
		StringBuilder sb = new StringBuilder();
		sb.append("const colors = [\n");
		for(Map.Entry<BorstColor, Point> entry : colorMap.entrySet()) {
			BorstColor color = entry.getKey();
			Point point = entry.getValue();
			
			sb.append("    { x: %d, y: %d, color: [ %d, %d, %d ] },\n".formatted(
				point.x - panel_offset.x,
				point.y - panel_offset.y,
				
				color.r,
				color.g,
				color.b
			));
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	public synchronized Map<BorstColor, Point> getColorMap() {
		return Map.copyOf(colorMap);
	}
	
	public void reset() {
		colorMap.clear();
		panel_offset = null;
	}
	
	private Point point(int x, int y) {
		return new Point(panel_offset.x + x, panel_offset.y + y);
	}
	
	/**
	 * Returns a spot were the bot can press without changing any state of the game.
	 */
	public Point getFocusPoint() {
		return point(12, 24);
	}
	
	@Deprecated
	public Point getColorPreview() {
		// 1704, 1012
		// -66, 735
		return point(-66, 735);
	}
	
	public Point getClearButton() {
		return point(55, 24);
	}
	
	public Point getSaveButton() {
		return point(95, 24);
	}
	
	public Point getUpdateButton() {
		return point(75, 469);
	}
	
	public Point getCancelButton() {
		return point(75, 505);
	}
	
	public Point getAlphaButton(int index) {
		Point[] array = getOpacityButtons();
		return array[index];
	}
	
	public Point getSizeButton(int index) {
		Point[] array = getSizeButtons();
		return array[index];
	}
	
	public Point getShapeButton(int index) {
		Point[] array = getShapeButtons();
		return array[index];
	}
	
	public Point getColorButton(BorstColor color) {
		return colorMap.get(color);
	}
	
	private Point[] getOpacityButtons() {
		return new Point[] {
			point( 22, 138),
			point( 43, 138),
			point( 64, 138),
			point( 85, 138),
			point(106, 138),
			point(127, 138),
		};
	}
	
	private Point[] getSizeButtons() {
		return new Point[] {
			point( 25, 62),
			point( 45, 62),
			point( 65, 62),
			point( 85, 62),
			point(105, 62),
			point(125, 62),
		};
	}
	
	private Point[] getShapeButtons() {
		return new Point[] {
			point( 27, 100), // Soft halo
			point( 59, 100), // Circle
			point( 91, 100), // Strong halo
			point(123, 100), // Square
		};
	}
}
