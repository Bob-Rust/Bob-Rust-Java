package com.bobrust.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;

import com.bobrust.settings.Settings;
import com.bobrust.util.data.RustConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.gui.BobRustDesktopOverlay;
import com.bobrust.util.RustUtil;
import com.bobrust.util.Sign;

public class BobRustPainter {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPainter.class);
	
	// The maximum distance the mouse can be from the correct position
	private static final double MAXIMUM_DISPLACEMENT = 10;
	
	private final BobRustDesktopOverlay overlay;
	private final BobRustPalette palette;
	private volatile int clickIndex;
	private int displayX;
	private int displayY;
	private double widthDelta;
	private double heightDelta;
	
	public BobRustPainter(BobRustDesktopOverlay overlay, BobRustPalette palette) {
		this.overlay = overlay;
		this.palette = palette;
	}
	
	// TODO: All points should be described as an percentage of the screen
	
	public BlobList generateDebugDrawList() {
		// DEBUG_DRAWN_COLORS
		BlobList list = new BlobList();
		
		int xo = 128;
		int yo = 128;
		
		// Draw shape sizes
		for (int shape = 0; shape < 2; shape++) {
			for (int size = 0; size < 6; size++) {
				int x = size * 64 + xo;
				int y = shape * 64 + yo;
				list.add(Blob.get(x, y, BorstUtils.SIZES[size], 0,
					shape == 0
						? RustConstants.CIRCLE_SHAPE
						: RustConstants.SQUARE_SHAPE, 5));
			}
		}
		
		// Draw alpha
		for (int alpha = 0; alpha < 6; alpha++) {
			int x = (alpha) * 64 + xo;
			int y = 3 * 64 + yo;
			
			list.add(Blob.get(x, y, BorstUtils.SIZES[5], 0, RustConstants.SQUARE_SHAPE, alpha));
		}
		
		// Draw colors
		int maxWidth = 16;
		for (int color = 0; color < BorstUtils.COLORS.length; color++) {
			int x = (color % maxWidth) * 64 + (7) * 64 + xo;
			int y = (color / maxWidth) * 64 + yo;
			list.add(Blob.get(x, y, BorstUtils.SIZES[5], BorstUtils.COLORS[color].rgb, RustConstants.SQUARE_SHAPE, 5));
		}
		
		return list;
	}
	
	public boolean startDrawing(BlobList list) throws Exception {
		return startDrawing(list, RustConstants.CIRCLE_SHAPE);
	}
	
	public boolean startDrawing(BlobList list, int shape) throws Exception {
		if (list.size() < 1) {
			return true;
		}

		GraphicsConfiguration gc = overlay.getMonitorConfiguration();
		
		{
			GraphicsDevice gd = gc.getDevice();
			Rectangle bounds = gc.getBounds();
			
			displayX = bounds.x;
			displayY = bounds.y;
			widthDelta = bounds.getWidth() / (double)gd.getDisplayMode().getWidth();
			heightDelta = bounds.getHeight() / (double)gd.getDisplayMode().getHeight();
		}
		Robot robot = new Robot(gc.getDevice());
		
		Rectangle canvas = overlay.getCanvasArea();
		Sign signType = Settings.SettingsSign.get();
		
		int clickInterval = Settings.SettingsClickInterval.get();
		int alphaSetting = Settings.SettingsAlpha.get();
		int shapeSetting = shape;
		int delayPerCycle = (int) (1000.0 / clickInterval);
		double autoDelay = 1000.0 / (clickInterval * 3.0);
		int autosaveInterval = Settings.SettingsAutosaveInterval.get();
		
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
			// TODO: This could be made using a better algorithm.
			//       The painter should not touch the render thread
			long start = System.nanoTime();
			int msDelay = delayPerCycle;
			while (true) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// Make sure we keep the interrupted status
					Thread.currentThread().interrupt();
					break;
				}
				
				if (clickIndex > 0) {
					long time = System.nanoTime() - start;
					msDelay = (int)((time / 1000000.0) / (double)clickIndex);
				}
				
				overlay.setRemainingTime(clickIndex, score, msDelay * (score - clickIndex));
				overlay.repaint();
			}
		}, "Drawing Update Thread");
		guiUpdateThread.setDaemon(true);
		guiUpdateThread.start();
		
		int signWidth = signType.getWidth();
		int signHeight = signType.getHeight();
		
		try {
			// Last fields
			Point lastPoint = new Point(0, 0);
			int lastColor = -1;
			int lastSize = -1;
			
			// Debug
			int lastShape = -1;
			int lastAlpha = -1;
			
			{
				Blob first = blobList.get(0);
				
				// Select first color to prevent exception
				clickPoint(robot, palette.getColorButton(BorstUtils.getClosestColor(first.color)), 4, 50);
				lastColor = first.colorIndex;
			}
			
			for (int i = 0, actions = 1; i < count; i++, actions++) {
				Blob blob = blobList.get(i);
				
				// Change the size
				if (lastSize != blob.sizeIndex) {
					clickSize(robot, palette.getSizeButton(blob.sizeIndex), 20, autoDelay);
					lastSize = blob.sizeIndex;
					actions++;
				}
				
				// Change the color
				if (lastColor != blob.colorIndex) {
					if (!clickColor(robot, palette.getColorButton(BorstUtils.getClosestColor(blob.color)), 5, autoDelay)) {
						// LOGGER.warn("Potentially failed to change color! Will still keep try drawing");
					}
					
					lastColor = blob.colorIndex;
					actions++;
				}
				
				// Change the shape (Only ever used in debug)
				if (RustConstants.DEBUG_DRAWN_COLORS && lastShape != blob.shapeIndex) {
					clickSize(robot, palette.getShapeButton(blob.shapeIndex), 20, autoDelay);
					lastShape = blob.shapeIndex;
					actions++;
				}
				
				if (RustConstants.DEBUG_DRAWN_COLORS && lastAlpha != blob.alphaIndex) {
					clickSize(robot, palette.getAlphaButton(blob.alphaIndex), 20, autoDelay);
					lastAlpha = blob.alphaIndex;
					actions++;
				}
				
				double dx = blob.x / (double) signWidth;
				double dy = blob.y / (double) signHeight;
				double tx = dx * canvas.width + canvas.x;
				double ty = dy * canvas.height + canvas.y;
				int sx = (int)tx + displayX;
				int sy = (int)ty + displayY;
				
				lastPoint.setLocation(sx, sy);
				clickPointScaled(robot, lastPoint, autoDelay);
				
				if ((i % autosaveInterval) == 0) {
					clickPoint(robot, palette.getSaveButton(), autoDelay);
					actions++;
				}
				
				this.clickIndex = actions;
			}

			this.clickIndex = score;
			
			// Make sure that we save the painting
			{
				clickPoint(robot, palette.getSaveButton(), 4, autoDelay);
			}
		} finally {
			// Interrupt the update thread and join
			guiUpdateThread.interrupt();
			guiUpdateThread.join();
			
			// Make sure we update the remaining time
			overlay.setRemainingTime(clickIndex, score, 0);
			overlay.repaint();
		}
		
		return true;
	}
	
	private Point transformPoint(Point point) {
		return new Point(
			displayX + (int)((point.x - displayX) * widthDelta),
			displayY + (int)((point.y - displayY) * heightDelta)
		);
	}
	
	private void clickPoint(Robot robot, Point point, int times, double delay) {
		for (int i = 0; i < times; i++) {
			clickPoint(robot, point, delay);
		}
	}
	
	/**
	 * Click a point on the screen with a scaled point
	 */
	private void clickPointScaled(Robot robot, Point point, double delay) {
		double time = System.nanoTime() / 1000000.0;
		
		robot.mouseMove(point.x, point.y);
		addTimeDelay(time + delay);
		
		Color before = robot.getPixelColor(point.x, point.y);
		
		int maxAttempts = 3;
		do {
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			addTimeDelay(time + delay * 2.0);
			
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			addTimeDelay(time + delay * 3.0);
			
			if (true) {
				Color after = robot.getPixelColor(point.x, point.y);
				if (!before.equals(after)) {
					break;
				}
				
				addTimeDelay(time + delay);
			} else {
				break;
			}
		} while (maxAttempts-- > 0);
		
		if (maxAttempts == 0) {
			LOGGER.warn("Potentially failed to paint color! Will still keep try drawing");
		}
		
		double distance = point.distance(MouseInfo.getPointerInfo().getLocation());
		if (distance > MAXIMUM_DISPLACEMENT) {
			throw new IllegalStateException("Mouse moved during the operation");
		}
		
		//time = (System.nanoTime() / 1000000.0) - time;
		//System.out.printf("Time: took %.4f, wanted: %.4f\n", time, delay * 3.0);
	}
	
	private void clickPoint(Robot robot, Point point, double delay) {
		point = transformPoint(point);
		
		double time = System.nanoTime() / 1000000.0;
		
		robot.mouseMove(point.x, point.y);
		addTimeDelay(time + delay);
		
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		addTimeDelay(time + delay * 2.0);

		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		addTimeDelay(time + delay * 3.0);
		
		double distance = point.distance(MouseInfo.getPointerInfo().getLocation());
		if (distance > MAXIMUM_DISPLACEMENT) {
			throw new IllegalStateException("Mouse moved during the operation");
		}
		
		//time = (System.nanoTime() / 1000000.0) - time;
		//System.out.printf("Time: took %.4f, wanted: %.4f\n", time, delay * 3.0);
	}
	
	private void clickSize(Robot robot, Point point, int maxAttempts, double delay) {
		// Make sure that we press the size
		while (maxAttempts-- > 0) {
			clickPoint(robot, point, delay);
			
			Color after = robot.getPixelColor(point.x, point.y + 8);
			if (after.getGreen() > 120) {
				return;
			}
		}
		
		//throw new IllegalStateException("Failed to select size");
	}
	
	private boolean clickColor(Robot robot, Point point, int maxAttempts, double delay) {
		Point colorPreview = palette.getColorPreview();
		Color before = robot.getPixelColor(colorPreview.x, colorPreview.y);
		
		// Make sure that we press the size
		while (maxAttempts-- > 0) {
			clickPoint(robot, point, delay);
			
			Color after = robot.getPixelColor(colorPreview.x, colorPreview.y);
			if (!before.equals(after)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean clickColorTest(Robot robot, Point point, int maxAttempts, double delay) {
		Point colorPreview = palette.getColorPreview();
		Color before = robot.getPixelColor(colorPreview.x, colorPreview.y);
		
		// Make sure that we press the size
		while(maxAttempts-- > 0) {
			clickPointScaled(robot, point, delay);
			
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
