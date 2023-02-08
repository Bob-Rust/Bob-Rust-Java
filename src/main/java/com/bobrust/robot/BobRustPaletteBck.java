package com.bobrust.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.BorstUtils;

/**
 * Used to analyse the gui of rust to get information about the game
 * @author HardCoded
 */
public class BobRustPaletteBck {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPaletteBck.class);
	private final Map<BorstColor, Point> colorMap;
	private Map<BorstColor, Point> colorMapCopy;
	private Point panel_offset;
	private Point preview_middle;
	
	public BobRustPaletteBck() {
		colorMap = new HashMap<>();
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
		
		int x = screen_width - 43;
		
		Point red_middle = null;
		for(int i = 0, lastNonRed = 0; i < screen_height; i++) {
			int red = (screenshot.getRGB(x, i) >> 16) & 0xff;
			
			if(red < 220) {
				lastNonRed = i;
			}
			
			if(i - lastNonRed > 40) {
				// We found the circle probably.
				red_middle = new Point(x, i - 10);
				break;
			}
		}
		
		if(red_middle != null) {
			// 150x264
			int point_x = screen_width - 150;
			int point_y = red_middle.y - 163;
			return new Point(point_x, point_y);
		}
		
		return null;
	}
	
	public synchronized boolean analyse(JDialog dialog, BufferedImage bi, Rectangle screenBounds, Point screenOffset) {
		this.panel_offset = new Point(screenOffset.x, screenOffset.y - 152);
		// (-215, -67) from screen corner
		this.preview_middle = new Point(screenBounds.x + screenBounds.width - 215, screenBounds.y + screenBounds.height - 67);
		this.colorMap.clear();
		
		Point screenLocation = screenBounds.getLocation();
		Rectangle rect = new Rectangle(panel_offset.x - screenLocation.x, panel_offset.y - screenLocation.y, 150, 525);
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
				LOGGER.warn("Could not find all colors in the color palette. Found {}/20 colors", colorMap.size());
				return false;
			}
		}
		this.colorMapCopy = Map.copyOf(colorMap);
		
		int dialogResult = JOptionPane.showConfirmDialog(dialog, new JLabel(new ImageIcon(image)), "Is this the color palette?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(dialogResult == JOptionPane.YES_OPTION) {
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
		opacityButtons = null;
		shapeButtons = null;
		sizeButtons = null;
		
		focusPoint = null;
		saveButton = null;
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
		return new Point(panel_offset.x + x, panel_offset.y + y);
	}
	
	// Returns a spot were the bot can press without changing any state of the game
	private Point focusPoint;
	public Point getFocusPoint() {
		if(focusPoint == null) {
			focusPoint = point(12, 24);
		}
		return focusPoint;
	}
	
	public Point getColorPreview() {
		return preview_middle;
	}
	
//	public Point getClearButton() {
//		return point(55, 24);
//	}
	
	private Point saveButton;
	public Point getSaveButton() {
		if(saveButton == null) {
			saveButton = point(95, 24);
		}
		return saveButton;
	}
	
//	public Point getUpdateButton() {
//		return point(75, 469);
//	}
//	
//	public Point getCancelButton() {
//		return point(75, 505);
//	}
	
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
	
	private Point[] opacityButtons;
	private Point[] getOpacityButtons() {
		if(opacityButtons == null) {
			opacityButtons = new Point[] {
				point( 22, 138),
				point( 43, 138),
				point( 64, 138),
				point( 85, 138),
				point(106, 138),
				point(127, 138),
			};
		}
		return opacityButtons;
	}
	
	private Point[] sizeButtons;
	private Point[] getSizeButtons() {
		if(sizeButtons == null) {
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
		if(shapeButtons == null) {
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
