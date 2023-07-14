package com.bobrust.robot.error;

import java.util.Objects;

public class PaintingInterrupted extends Exception {
	/**
	 * How many shapes had been drawn
	 */
	private final int drawnShapes;
	private final InterruptType interruptType;
	
	public PaintingInterrupted(int drawnShapes, InterruptType interruptType) {
		this.drawnShapes = drawnShapes;
		this.interruptType = Objects.requireNonNull(interruptType);
	}
	
	public int getDrawnShapes() {
		return drawnShapes;
	}
	
	public InterruptType getInterruptType() {
		return interruptType;
	}
	
	public enum InterruptType {
		/**
		 * This is used when the painting was interrupted because the mouse moved
		 */
		MouseMoved,
		
		/**
		 * This is used when the painting was interrupted because of Thread.interrupt()
		 */
		ThreadInterrupted
	}
}
