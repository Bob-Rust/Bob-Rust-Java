package com.bobrust.gui.render;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private final List<byte[]> pixelBufferCache;
	private final int cacheInterval;
	private BufferedImage canvas;
	private byte[] canvasPixels;
	
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
		
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = canvas.createGraphics();
		g.setColor(new Color(background));
		g.fillRect(0, 0, width, height);
		g.dispose();
		
		canvasPixels = ((DataBufferByte) canvas.getRaster().getDataBuffer()).getData();
		pixelBufferCache.add(canvasPixels.clone());
	}
	
	private BufferedImage createTest(BorstGenerator.BorstData data, int shapes) {
		System.arraycopy(pixelBufferCache.get(0), 0, canvasPixels, 0, canvasPixels.length);
		
		Random random = new Random(34);
		
		BlobList sorted = new BlobList(data.getBlobs());
		sorted = BorstSorter.sort(sorted);
		
		Graphics2D g = canvas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		double failRate = 0;
		int cd = sorted.get(0).size;
		int is = cd;
		
		int pc = sorted.get(0).color;
		int ps = pc;
		
		for (int i = 0; i < shapes; i++) {
			var blob = sorted.get(i);
			int alpha = BorstUtils.ALPHAS[blob.alphaIndex];
			
			if (is != blob.size) {
				is = blob.size;
				if (random.nextDouble() > failRate) {
					cd = is;
				}
			}
			
			if (ps != blob.color) {
				ps = blob.color;
				if (random.nextDouble() > failRate) {
					pc = ps;
				}
			}
			
			g.setColor(new Color(pc | (alpha << 24), true));
			if (blob.shapeIndex == 3) {
				cd *= 1.25;
				g.fillRect(blob.x - cd / 2, blob.y - cd / 2, cd, cd);
			} else {
				g.fillOval(blob.x - cd / 2, blob.y - cd / 2, cd, cd);
			}
		}
		g.dispose();
		
		return canvas;
	}
	
	public synchronized BufferedImage getImage(BorstGenerator.BorstData data, int shapes) {
		if (pixelBufferCache.isEmpty()) {
			return null;
		}
		
		//if (true) {
		// return createTest(data, shapes);
		//}
		
		int cacheIndex = shapes / cacheInterval;
		
		// The pixel buffer of the closest image
		byte[] pixelBuffer;
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
