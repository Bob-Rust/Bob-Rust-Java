package com.bobrust.robot;

/**
 * This class is used to fix screen scaling
 */
class ScreenPoint {
	private final ScreenDevice device;
	
	public double x;
	public double y;
	
	private ScreenPoint(ScreenDevice device, double x, double y) {
		this.device = device;
		this.x = x;
		this.y = y;
	}
	
	private ScreenPoint(ScreenDevice device) {
		this(device, 0, 0);
	}
	
	public ScreenPoint setPoint(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public double getX() {
		return device.getScreenX() + x * device.getScaledWidth();
	}
	
	public double getY() {
		return device.getScreenY() + y * device.getScaledHeight();
	}
	
	@Override
	public String toString() {
		return "{ x=%.4f, y=%.4f }".formatted(x, y);
	}
}
