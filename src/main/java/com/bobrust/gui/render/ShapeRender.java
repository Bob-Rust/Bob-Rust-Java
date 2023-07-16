package com.bobrust.gui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstUtils;

/**
 * Optimized class for rendering many circles
 * 
 * This class creates a cache for different draw steps and can then generate the image much faster.
 * 
 * @author HardCoded
 */
public class ShapeRender {
	/**
	 * Each element in this list is 'cacheInterval' shapes apart
	 */
	private final List<int[]> pixelBufferCache;
	private final int cacheInterval;
	private BufferedImage canvas;
	private int[] canvasPixels;
	
	public ShapeRender(int cacheInterval) {
		this.pixelBufferCache = new ArrayList<>();
		this.cacheInterval = cacheInterval;
	}
	
	public synchronized void reset() {
		pixelBufferCache.clear();
		canvas = null;
		canvasPixels = null;
	}
	
	public synchronized void createCanvas(int width, int height, int background) {
		reset();
		
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		canvasPixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		Arrays.fill(canvasPixels, background);
		
		pixelBufferCache.add(canvasPixels.clone());
	}
	
	public synchronized BufferedImage getImage(BorstGenerator.BorstData data, int shapes) {
		if (pixelBufferCache.isEmpty()) {
			return null;
		}
		
		int cacheIndex = shapes / cacheInterval;
		
		// The pixel buffer of the closest image
		int[] pixelBuffer;
		int startIndex;
		
		// If we do not have cached values up to this point
		if (pixelBufferCache.size() > cacheIndex) {
			startIndex = cacheIndex * cacheInterval;
			pixelBuffer = pixelBufferCache.get(cacheIndex);
		} else {
			startIndex = (pixelBufferCache.size() - 1) * cacheInterval;
			pixelBuffer = pixelBufferCache.get(pixelBufferCache.size() - 1);
		}
		
		// Copy the closest pixel buffer to the canvas
		System.arraycopy(pixelBuffer, 0, canvasPixels, 0, canvasPixels.length);
		
		if (startIndex == shapes) {
			// If the start index was the same as the cachedIndex we return
			return canvas;
		}
		
		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int i = startIndex; i < shapes; i++) {
			var blob = data.getBlobs().get(i);
			int alpha = BorstUtils.ALPHAS[blob.alphaIndex];
			
			g.setColor(new Color(blob.color | (alpha << 24), true));
			int cd = blob.size;
			if (blob.shapeIndex == 3) {
				cd *= 1.25;
				g.fillRect(blob.x - cd / 2, blob.y - cd / 2, cd, cd);
			} else {
				g.fillOval(blob.x - cd / 2, blob.y - cd / 2, cd, cd);
			}
			
			// Cache every 'cacheInterval' shapes.
			if ((i % cacheInterval) == cacheInterval - 1) {
				pixelBufferCache.add(canvasPixels.clone());
			}
		}
		g.dispose();
		
		return canvas;
	}
}
