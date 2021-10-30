package com.bobrust.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.Circle;
import com.bobrust.generator.Model;

// TODO: Sometimes generates the wrong cache.. Try fix this!
public class BobRustShapeRender {
	/**
	 * Each element in this list is 'cacheInterval' shapes apart
	 */
	private final List<int[]> list;
	private final int cacheInterval;
	private BufferedImage canvas;
	private int[] canvasPixels;
	
	public BobRustShapeRender(int cacheInterval) {
		this.list = new ArrayList<>();
		this.cacheInterval = cacheInterval;
	}
	
	public int getCacheInterval() {
		return cacheInterval;
	}
	
	public synchronized void reset() {
		list.clear();
		canvas = null;
		canvasPixels = null;
	}
	
	public synchronized void createCanvas(int width, int height, int background) {
		reset();
		
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		canvasPixels = ((DataBufferInt)canvas.getRaster().getDataBuffer()).getData();
		Arrays.fill(canvasPixels, background);
		list.add(canvasPixels.clone());
	}
	
	public synchronized boolean hasCanvas() {
		return canvas != null;
	}
	
	public synchronized BufferedImage getImage(Model model, int shapes) {
		int cacheIndex = shapes / cacheInterval;
		
		// The pixel buffer of the closest image.
		int[] pixelBuffer;
		int startIndex;
		// If we do not have cached values up to this point
		if(list.size() > cacheIndex) {
			startIndex = cacheIndex * cacheInterval;
			pixelBuffer = list.get(cacheIndex);
		} else {
			startIndex = (list.size() - 1) * cacheInterval;
			pixelBuffer = list.get(list.size() - 1);
		}
		
		// Copy the closest pixel buffer to the canvas.
		System.arraycopy(pixelBuffer, 0, canvasPixels, 0, canvasPixels.length);
		
		if(startIndex == shapes) {
			// If the start index was the same as the cachedIndex we return.
			return canvas;
		}
		
		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for(int i = startIndex; i < shapes; i++) {
			Circle circle = model.shapes.get(i);
			BorstColor color = model.colors.get(i);
			
			g.setColor(new Color(color.rgb | (model.alpha << 24), true));
			int cd = (circle.r - 1) * 2 + 1;
			g.fillOval(circle.x - cd / 2, circle.y - cd / 2, cd, cd);
			
			// Cache every 'cacheInterval' shapes
			if((i % cacheInterval) == 499) {
				list.add(canvasPixels.clone());
			}
		}
		g.dispose();
		
		return canvas;
	}
}
