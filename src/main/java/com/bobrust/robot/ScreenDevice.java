package com.bobrust.robot;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

class ScreenDevice {
	private int displayX;
	private int displayY;
	private double deviceWidth;
	private double deviceHeight;
	private double scaledWidth;
	private double scaledHeight;
	
	private ScreenDevice(GraphicsConfiguration gc) {
		update(gc);
	}
	
	public void update(GraphicsConfiguration gc) {
		Rectangle screenRect = gc.getBounds();
		GraphicsDevice gd = gc.getDevice();
		
		this.displayX = screenRect.x;
		this.displayY = screenRect.y;
		this.deviceWidth = gd.getDisplayMode().getWidth();
		this.deviceHeight = gd.getDisplayMode().getHeight();
		this.scaledWidth = screenRect.getWidth();
		this.scaledHeight = screenRect.getHeight();
	}

	/**
	 * Returns the x position of this screen device.
	 */
	public int getScreenX() {
		return displayX;
	}
	
	/**
	 * Returns the y position of this screen device.
	 */
	public int getScreenY() {
		return displayY;
	}
	
	/**
	 * Returns the true width of this screen device.
	 */
	public double getDeviceWidth() {
		return deviceWidth;
	}

	/**
	 * Returns the true height of this screen device.
	 */
	public double getDeviceHeight() {
		return deviceHeight;
	}
	
	/**
	 * Returns the scaled width of this screen device.
	 */
	public double getScaledWidth() {
		return scaledWidth;
	}

	/**
	 * Returns the scaled height of this screen device.
	 */
	public double getScaledHeight() {
		return scaledHeight;
	}
}
