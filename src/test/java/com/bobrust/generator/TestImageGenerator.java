package com.bobrust.generator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility to programmatically generate test images for benchmarks.
 * All images are 128x128 to keep test times reasonable.
 */
class TestImageGenerator {
	static final int SIZE = 128;

	/** Solid red image */
	static BufferedImage createSolid() {
		BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(200, 50, 50));
		g.fillRect(0, 0, SIZE, SIZE);
		g.dispose();
		return img;
	}

	/** Horizontal gradient from blue to green */
	static BufferedImage createGradient() {
		BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < SIZE; x++) {
			float t = x / (float) (SIZE - 1);
			int r = (int) (50 * (1 - t) + 50 * t);
			int g = (int) (50 * (1 - t) + 200 * t);
			int b = (int) (200 * (1 - t) + 50 * t);
			int rgb = 0xFF000000 | (r << 16) | (g << 8) | b;
			for (int y = 0; y < SIZE; y++) {
				img.setRGB(x, y, rgb);
			}
		}
		return img;
	}

	/** High-contrast black/white edges — checkerboard pattern */
	static BufferedImage createEdges() {
		BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		int blockSize = 16;
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				boolean white = ((x / blockSize) + (y / blockSize)) % 2 == 0;
				img.setRGB(x, y, white ? 0xFFFFFFFF : 0xFF000000);
			}
		}
		return img;
	}

	/** Simulated photo with fine detail — concentric circles of varying colors */
	static BufferedImage createPhotoDetail() {
		BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		int cx = SIZE / 2;
		int cy = SIZE / 2;
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
				int r = (int) (127 + 127 * Math.sin(dist * 0.3));
				int g = (int) (127 + 127 * Math.cos(dist * 0.5));
				int b = (int) (127 + 127 * Math.sin(dist * 0.7 + 1.0));
				img.setRGB(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
			}
		}
		return img;
	}

	/** Natural scene approximation — overlapping gradients and shapes */
	static BufferedImage createNature() {
		BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		// Sky gradient
		for (int y = 0; y < SIZE / 2; y++) {
			float t = y / (float) (SIZE / 2);
			g.setColor(new Color(
				(int) (100 + 100 * t),
				(int) (150 + 50 * t),
				(int) (255 - 50 * t)
			));
			g.drawLine(0, y, SIZE - 1, y);
		}
		// Ground
		g.setColor(new Color(80, 140, 50));
		g.fillRect(0, SIZE / 2, SIZE, SIZE / 2);
		// Tree trunk
		g.setColor(new Color(100, 70, 30));
		g.fillRect(55, 40, 18, 50);
		// Tree canopy
		g.setColor(new Color(30, 120, 30));
		g.fillOval(30, 10, 68, 50);
		// Sun
		g.setColor(new Color(255, 230, 80));
		g.fillOval(90, 5, 30, 30);
		g.dispose();
		return img;
	}

	/** Save all test images to the given directory */
	static void saveAll(File dir) throws IOException {
		dir.mkdirs();
		ImageIO.write(createSolid(), "png", new File(dir, "solid.png"));
		ImageIO.write(createGradient(), "png", new File(dir, "gradient.png"));
		ImageIO.write(createEdges(), "png", new File(dir, "edges.png"));
		ImageIO.write(createPhotoDetail(), "png", new File(dir, "photo_detail.png"));
		ImageIO.write(createNature(), "png", new File(dir, "nature.png"));
	}

	public static void main(String[] args) throws IOException {
		File dir = new File("src/test/resources/test-images");
		saveAll(dir);
		System.out.println("Test images generated in " + dir.getAbsolutePath());
	}
}
