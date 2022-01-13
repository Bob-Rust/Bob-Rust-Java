package com.bobrust.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.BobRustDesktopOverlay;
import com.bobrust.util.RustUtil;
import com.bobrust.util.Sign;

public class BobRustPainter {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPainter.class);
	// This is the index of the circle shape
	private static final int CIRCLE_SHAPE = 1;
	// The maximum distance the mouse can be from the correct position
	private static final double MAXIMUM_DISPLACEMENT = 10;
	
	private final BobRustEditor gui;
	private final BobRustDesktopOverlay overlay;
	private final BobRustPalette palette;
	private volatile int clickIndex;
	
	public BobRustPainter(BobRustEditor gui, BobRustDesktopOverlay overlay, BobRustPalette palette) {
		this.gui = gui;
		this.overlay = overlay;
		this.palette = palette;
	}
	
	public boolean startDrawing(BlobList list) throws Exception {
		if(list.size() < 1) return true;
		
		Robot robot = new Robot();
		
		Rectangle canvas = overlay.getCanvasArea();
		Rectangle screen = overlay.getScreenLocation();
		Sign signType = gui.getSettingsSign();
		
		int clickInterval = gui.getSettingsClickInterval();
		int alphaSetting = gui.getSettingsAlpha();
		int shapeSetting = CIRCLE_SHAPE;
		int delayPerCycle = (int)(1000.0 / clickInterval);
		double autoDelay = 1000.0 / (clickInterval * 3.0);
		int autosaveInterval = gui.getSettingsAutosaveInterval();
		
		this.clickIndex = 0;
		
		// Configure the robot
		robot.setAutoDelay(0);
		List<Blob> blobList = list.getList();
		int count = blobList.size();
		
		// Calculate the total amount of presses needed
		// For each autosave press there is one more click
		int score = RustUtil.getScore(list) + count + (count / (autosaveInterval < 1 ? 1:autosaveInterval));
		
		{
			// Make sure that we have selected the game
			clickPoint(robot, palette.getFocusPoint(), 4, 50);
			
			// Make sure that we have selected the correct alpha
			clickPoint(robot, palette.getAlphaButton(alphaSetting), 4, 50);
			
			// Make sure that we have selected the correct shape
			clickPoint(robot, palette.getShapeButton(shapeSetting), 4, 50);
		}
		
		// We create an update thread because repainting graphics
		// could be an expensive operation. And because we only need
		// the current thread to click on the screen we want to minimize
		// the amount of noise that rendering components could generate.
		Thread guiUpdateThread = new Thread(() -> {
			long start = System.nanoTime();
			int msDelay = delayPerCycle;
			while(true) {
				try {
					Thread.sleep(50);
				} catch(InterruptedException e) {
					// Make sure we keep the interupted status
					Thread.currentThread().interrupt();
					break;
				}
				
				if(clickIndex > 0) {
					long time = System.nanoTime() - start;
					msDelay = (int)((time / 1000000.0) / (double)clickIndex);
				}
				
				overlay.setRemainingTime(clickIndex, score, msDelay * (score - clickIndex));
				overlay.repaint();
			}
		}, "Drawing Update Thread");
		guiUpdateThread.setDaemon(true);
		guiUpdateThread.start();
		
		try {
			// Last fields
			Point lastPoint = new Point(0, 0);
			int lastColor = -1;
			int lastSize = -1;
			
			{
				Blob first = blobList.get(0);
				
				// Select first color to prevent exception
				clickPoint(robot, palette.getColorButton(BorstUtils.getClosestColor(first.color)), 4, 50);
				lastColor = first.colorIndex;
			}
			
			for(int i = 0, l = 1; i < count; i++, l++) {
				Blob blob = blobList.get(i);
				
				// Change the size
				if(lastSize != blob.sizeIndex) {
					clickSize(robot, palette.getSizeButton(blob.sizeIndex), 20, autoDelay);
					lastSize = blob.sizeIndex;
					l++;
				}
				
				// Change the color
				if(lastColor != blob.colorIndex) {
					if(!clickColor(robot, palette.getColorButton(BorstUtils.getClosestColor(blob.color)), 20, autoDelay)) {
						LOGGER.warn("Potentially failed to change color! Will still keep try drawing");
					}
					
					lastColor = blob.colorIndex;
					l++;
				}
				
				double dx = blob.x / (double)signType.width;
				double dy = blob.y / (double)signType.height;
				double tx = dx * canvas.width + canvas.x;
				double ty = dy * canvas.height + canvas.y;
				int sx = (int)tx + screen.x;
				int sy = (int)ty + screen.y;
				
				lastPoint.setLocation(sx, sy);
				clickPoint(robot, lastPoint, autoDelay);
				
				if((i % autosaveInterval) == 0) {
					clickPoint(robot, palette.getSaveButton(), autoDelay);
					l++;
				}
				
				this.clickIndex = l;
			}

			this.clickIndex = score;
			
			// Make sure that we save the painting
			{
				clickPoint(robot, palette.getSaveButton(), 4, autoDelay);
			}
		} finally {
			// Interupt the update thread and join
			guiUpdateThread.interrupt();
			guiUpdateThread.join();
			
			// Make sure we update the remaining time
			overlay.setRemainingTime(clickIndex, score, 0);
			overlay.repaint();
		}
		
		return true;
	}
	
	private void clickPoint(Robot robot, Point point, int times, double delay) {
		for(int i = 0; i < times; i++) {
			clickPoint(robot, point, delay);
		}
	}
	
	private void clickPoint(Robot robot, Point point, double delay) {
		double time = System.nanoTime() / 1000000.0;
		
		robot.mouseMove(point.x, point.y);
		addTimeDelay(time + delay);
		
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		addTimeDelay(time + delay * 2.0);
		
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		addTimeDelay(time + delay * 3.0);
		
		double distance = point.distance(MouseInfo.getPointerInfo().getLocation());
		if(distance > MAXIMUM_DISPLACEMENT) {
			throw new IllegalStateException("Mouse moved during the operation");
		}
		
		//time = (System.nanoTime() / 1000000.0) - time;
		//System.out.printf("Time: took %.4f, wanted: %.4f\n", time, delay * 3.0);
	}
	
	private void clickSize(Robot robot, Point point, int maxAttempts, double delay) {
		// Make sure that we press the size
		while(maxAttempts-- > 0) {
			clickPoint(robot, point, delay);
			
			Color after = robot.getPixelColor(point.x, point.y + 8);
			if(after.getGreen() > 120) {
				return;
			}
		}
		
		throw new IllegalStateException("Failed to select size");
	}
	
	private boolean clickColor(Robot robot, Point point, int maxAttempts, double delay) {
		Point colorPreview = palette.getColorPreview();
		Color before = robot.getPixelColor(colorPreview.x, colorPreview.y);
		
		// Make sure that we press the size
		while(maxAttempts-- > 0) {
			clickPoint(robot, point, delay);
			
			Color after = robot.getPixelColor(colorPreview.x, colorPreview.y);
			if(!before.equals(after)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This method is used to provide a more accurate timing than {@code Robot.setAutoDelay}.
	 */
	private void addTimeDelay(double expected) {
		double time = expected - (System.nanoTime() / 1000000.0);
		if(time < 0) return;
		
		long millis = (long)time;
		int nanos = (int)((time - millis) * 10000000);
		if(nanos > 999999) nanos = 999999;
		if(nanos < 0) nanos = 0;
		
		try {
			Thread.sleep(millis, nanos);
		} catch(final InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}
}
