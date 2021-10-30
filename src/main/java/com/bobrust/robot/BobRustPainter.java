package com.bobrust.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;

import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.BobRustOverlay;
import com.bobrust.gui.Sign;
import com.bobrust.util.BobRustUtil;

public class BobRustPainter {
	// This is the index of the circle shape.
	private static final int CIRCLE_SHAPE = 1;
	// The maximum distance the mouse can be from the correct position.
	private static final double MAXIMUM_DISPLACEMENT = 10;
	
	private final BobRustEditor gui;
	private final BobRustOverlay overlay;
	private final BobRustPalette palette;
	
	public BobRustPainter(BobRustEditor gui, BobRustOverlay overlay, BobRustPalette palette) {
		this.gui = gui;
		this.overlay = overlay;
		this.palette = palette;
	}
	
	public boolean startDrawing(BlobList list) throws Exception {
		Robot robot = new Robot();
		
		// overlay.setRemainingTime(0, 0, 0);
		Rectangle canvas = overlay.getCanvasArea();
		Rectangle screen = overlay.getScreenLocation();
		Sign signType = gui.getSettingsSign();
		
		int clickInterval = gui.getSettingsClickInterval();
		int alphaSetting = gui.getSettingsAlpha();
		int shapeSetting = CIRCLE_SHAPE;
		int ms_delay = (int)(1000.0 / clickInterval);
		int ms_auto_delay = (int)(1000.0 / (clickInterval * 3));
		int autosaveInterval = gui.getSettingsAutosaveInterval();
		
		// Configure the robot.
		robot.setAutoDelay(ms_auto_delay);
		List<Blob> blobList = list.getList();
		int count = blobList.size();
		
		// Calculate the total amount of presses needed
		int score = BobRustUtil.getScore(list) + count;
		
		{
			// Make sure that we have selected the game.
			clickPoint(robot, palette.getFocusPoint(), 4);
			
			// Make sure that we have selected the correct alpha.
			clickPoint(robot, palette.getAlphaButton(alphaSetting), 4);
			
			// Make sure that we have selected the correct shape
			clickPoint(robot, palette.getShapeButton(shapeSetting), 4);
		}
		
		// Last fields.
		int lastColor = -1;
		int lastSize = -1;
		for(int i = 0, l = 1; i < count; i++, l++) {
			Blob blob = blobList.get(i);
			
			// Change the size.
			if(lastSize != blob.sizeIndex) {
				clickPoint(robot, palette.getSizeButton(blob.sizeIndex));
				lastSize = blob.sizeIndex;
				l++;
			}
			
			// Change the color.
			if(lastColor != blob.colorIndex) {
				clickPoint(robot, palette.getColorButton(BorstUtils.getClosestColor(blob.color)));
				lastColor = blob.colorIndex;
				l++;
			}
			
			double dx = blob.x / (double)signType.width;
			double dy = blob.y / (double)signType.height;
			
			double tx = dx * canvas.width + canvas.x;
			double ty = dy * canvas.height + canvas.y;
			
			int sx = (int)tx + screen.x;
			int sy = (int)ty + screen.y;
			
			clickPoint(robot, new Point(sx, sy));
			
			overlay.setRemainingTime(l, score, ms_delay * (count - i));
			overlay.repaint();
			
			if((i % autosaveInterval) == 0) {
				clickPoint(robot, palette.getSaveButton());
			}
		}
		
		// Make sure that we save the painting.
		{
			clickPoint(robot, palette.getSaveButton(), 4);
		}
		
		return true;
	}
	
	private void clickPoint(Robot robot, Point point) {
		robot.mouseMove(point.x, point.y);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		double distance = point.distance(MouseInfo.getPointerInfo().getLocation());
		if(distance > MAXIMUM_DISPLACEMENT) {
			throw new IllegalStateException("Mouse moved during the operation");
		}
	}
	
	private void clickPoint(Robot robot, Point point, int times) {
		for(int i = 0; i < times; i++) {
			clickPoint(robot, point);
		}
	}
	
}
