package com.bobrust.generator;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BorstImage {
	public final BufferedImage image;
	public final int[] pixels;
	public final int width;
	public final int height;
	
	public BorstImage(BufferedImage image) {
		this.image = image;
		this.pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	public BorstImage(int width, int height) {
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Creates a new BorstImage that has no internal bufferedImage.
	 * @param pixels the pixel buffer
	 */
	public BorstImage(int[] pixels, int width) {
		this.image = null;
		this.pixels = pixels;
		this.width = width;
		this.height = pixels.length / width;
	}

	public BorstImage createCopy() {
		BorstImage copy = new BorstImage(width, height);
		System.arraycopy(pixels, 0, copy.pixels, 0, pixels.length);
		return copy;
	}
	
	public static BorstImage loadBorstImage(String path, int target_width, int target_height) throws IOException {
		BufferedImage image = ImageIO.read(new File(path));
		
		if(target_width == 0) {
			target_width = image.getWidth();
		}
		
		if(target_height == 0) {
			target_height = image.getHeight();
		}
		
		BufferedImage result = new BufferedImage(target_width, target_height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, target_width, target_height, null);
		g.dispose();
		
		return new BorstImage(result);
	}
	
	public static BorstImage loadBorstImage(String path) throws IOException {
		return new BorstImage(ImageIO.read(new File(path)));
	}

	public void draw(BorstImage image) {
		System.arraycopy(image.pixels, 0, pixels, 0, pixels.length);
	}
}
