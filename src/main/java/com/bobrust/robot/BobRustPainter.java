package com.bobrust.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.function.BiConsumer;

import com.bobrust.robot.error.PaintingInterrupted;
import com.bobrust.settings.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.util.Sign;

public class BobRustPainter {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPainter.class);
	
	// The maximum distance the mouse can be from the correct position
	private static final double MAXIMUM_DISPLACEMENT = 10;
	
	private final BobRustPalette palette;
	private int displayX;
	private int displayY;
	private double widthDelta;
	private double heightDelta;
	
	// Exception
	private int drawnShapes;
	
	public BobRustPainter(BobRustPalette palette) {
		this.palette = palette;
	}
	
	public boolean startDrawing(GraphicsConfiguration monitor, Rectangle canvasArea, BlobList list, BiConsumer<Integer, Integer> renderCallback) throws PaintingInterrupted {
		// Reset values
		this.drawnShapes = 0;
		
		if (list.size() < 1) {
			return true;
		}
		
		Robot robot;
		try {
			robot = new Robot(monitor.getDevice());
		} catch (AWTException e) {
			return false;
		}
		
		{
			GraphicsDevice gd = monitor.getDevice();
			Rectangle bounds = monitor.getBounds();
			
			displayX = bounds.x;
			displayY = bounds.y;
			widthDelta = bounds.getWidth() / (double)gd.getDisplayMode().getWidth();
			heightDelta = bounds.getHeight() / (double)gd.getDisplayMode().getHeight();
		}
		
		Sign signType = Settings.SettingsSign.get();
		
		int clickInterval = Settings.SettingsClickInterval.get();
		double autoDelay = 1000.0 / (clickInterval * 3.0);
		int autosaveInterval = Settings.SettingsAutosaveInterval.get();
		
		// Configure the robot
		robot.setAutoDelay(0);
		List<Blob> blobList = list.getList();
		int count = blobList.size();
		int signWidth = signType.getWidth();
		int signHeight = signType.getHeight();
		
		// Last fields
		Point lastPoint = new Point(0, 0);
		int lastColor;
		int lastSize;
		int lastAlpha;
		int lastShape;
		
		{
			Blob startBlob = blobList.get(0);
			
			// Make sure that we have selected the game
			clickPoint(robot, palette.getFocusPoint(), 4, 50);
			
			// Select first color to prevent exception
			clickPoint(robot, palette.getColorButton(BorstUtils.getClosestColor(startBlob.color)), 4, 50);
			clickPoint(robot, palette.getSizeButton(startBlob.sizeIndex), 4, 50);
			clickPoint(robot, palette.getAlphaButton(startBlob.alphaIndex), 4, 50);
			clickPoint(robot, palette.getShapeButton(startBlob.shapeIndex), 4, 50);
			
			// Fill in last color information
			lastColor = startBlob.colorIndex;
			lastSize = startBlob.sizeIndex;
			lastAlpha = startBlob.alphaIndex;
			lastShape = startBlob.shapeIndex;
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
				if (!clickColor(robot, palette.getColorButton(BorstUtils.getClosestColor(blob.color)), 8, autoDelay)) {
					// LOGGER.warn("Potentially failed to change color! Will still keep try drawing");
				}
				
				lastColor = blob.colorIndex;
				actions++;
			}
			
			// Change the alpha
			if (lastAlpha != blob.alphaIndex) {
				clickSize(robot, palette.getAlphaButton(blob.alphaIndex), 20, autoDelay);
				lastAlpha = blob.alphaIndex;
				actions++;
			}
			
			// Change the shape
			if (lastShape != blob.shapeIndex) {
				clickSize(robot, palette.getShapeButton(blob.shapeIndex), 20, autoDelay);
				lastShape = blob.shapeIndex;
				actions++;
			}
			
			// Blob coordinates to sign coordinates
			double dx = blob.x / (double) signWidth;
			double dy = blob.y / (double) signHeight;
			
			// Sign coordinates to canvas coordinates
			double tx = dx * canvasArea.width + canvasArea.x;
			double ty = dy * canvasArea.height + canvasArea.y;
			
			// Canvas coordinates to screen coordinates
			int sx = (int) tx + displayX;
			int sy = (int) ty + displayY;
			
			lastPoint.setLocation(sx, sy);
			clickPointScaled(robot, lastPoint, autoDelay);
			
			if ((i % autosaveInterval) == 0) {
				clickPoint(robot, palette.getSaveButton(), autoDelay);
				actions++;
			}
			
			drawnShapes += 1;
			renderCallback.accept(drawnShapes, count);
		}
		
		// Make sure that we save the painting
		clickPoint(robot, palette.getSaveButton(), 4, autoDelay);
		
		// Return the result
		throw new PaintingInterrupted(drawnShapes, PaintingInterrupted.InterruptType.PaintingFinished);
	}
	
	private Point transformPoint(Point point) {
		return new Point(
			displayX + (int) ((point.x - displayX) * widthDelta),
			displayY + (int) ((point.y - displayY) * heightDelta)
		);
	}
	
	private void clickPoint(Robot robot, Point point, int times, double delay) throws PaintingInterrupted {
		for (int i = 0; i < times; i++) {
			clickPoint(robot, point, delay);
		}
	}
	
	/**
	 * Click a point on the screen with a scaled point
	 */
	private void clickPointScaled(Robot robot, Point point, double delay) throws PaintingInterrupted {
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
			
			Color after = robot.getPixelColor(point.x, point.y);
			if (!before.equals(after)) {
				break;
			}
			
			addTimeDelay(time + delay);
		} while (maxAttempts-- > 0);
		
		if (maxAttempts == 0) {
			LOGGER.warn("Potentially failed to paint color! Will still keep try drawing");
		}
		
		// TODO: This can return null for scaled monitors!
		double distance = point.distance(MouseInfo.getPointerInfo().getLocation());
		if (distance > MAXIMUM_DISPLACEMENT) {
			throw new PaintingInterrupted(drawnShapes, PaintingInterrupted.InterruptType.MouseMoved);
		}
	}
	
	private void clickPoint(Robot robot, Point point, double delay) throws PaintingInterrupted {
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
			throw new PaintingInterrupted(drawnShapes, PaintingInterrupted.InterruptType.MouseMoved);
		}
	}
	
	private void clickSize(Robot robot, Point point, int maxAttempts, double delay) throws PaintingInterrupted {
		// Make sure that we press the size
		while (maxAttempts-- > 0) {
			clickPoint(robot, point, delay);
			
			Color after = robot.getPixelColor(point.x, point.y + 8);
			if (after.getGreen() > 120) {
				return;
			}
		}
	}
	
	private boolean clickColor(Robot robot, Point point, int maxAttempts, double delay) throws PaintingInterrupted {
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
	
	private boolean clickColorTest(Robot robot, Point point, int maxAttempts, double delay) throws PaintingInterrupted {
		Point colorPreview = palette.getColorPreview();
		Color before = robot.getPixelColor(colorPreview.x, colorPreview.y);
		
		// Make sure that we press the size
		while (maxAttempts-- > 0) {
			clickPointScaled(robot, point, delay);
			
			Color after = robot.getPixelColor(colorPreview.x, colorPreview.y);
			if (!before.equals(after)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * This method is used to provide a more accurate timing than {@code Robot.setAutoDelay}.
	 */
	private void addTimeDelay(double expected) throws PaintingInterrupted {
		double time = expected - (System.nanoTime() / 1000000.0);
		if (time < 0) return;
		
		try {
			Thread.sleep(Math.round(time));
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
			throw new PaintingInterrupted(drawnShapes, PaintingInterrupted.InterruptType.ThreadInterrupted);
		}
	}
}
