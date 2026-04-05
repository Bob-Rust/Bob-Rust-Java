package com.bobrust.calibration;

import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.CircleCache;
import com.bobrust.generator.Scanline;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Generates a calibration reference pattern for measuring actual circle sizes and alpha values
 * as painted by the Rust game.
 *
 * The pattern is a 6x6 grid: 6 circle sizes (columns) x 6 alpha values (rows).
 * All circles are white on a black background. The user paints this in Rust, takes a screenshot,
 * and feeds it to {@link ScreenshotAnalyzer}.
 *
 * Usage: java -cp ... com.bobrust.calibration.CalibrationPatternGenerator [output.png] [width] [height]
 */
public class CalibrationPatternGenerator {

	/** Number of size levels (columns). */
	public static final int NUM_SIZES = BorstUtils.SIZES.length;
	/** Number of alpha levels (rows). */
	public static final int NUM_ALPHAS = BorstUtils.ALPHAS.length;

	/** Padding around the entire grid in pixels. */
	public static final int GRID_PADDING = 8;
	/** Spacing between cell centers. Must be > largest circle diameter. */
	public static final int CELL_SPACING = 110;

	/**
	 * Generate the calibration pattern image.
	 *
	 * @param width  image width (0 = auto-calculate)
	 * @param height image height (0 = auto-calculate)
	 * @return the generated calibration image
	 */
	public static BufferedImage generate(int width, int height) {
		int gridW = GRID_PADDING * 2 + CELL_SPACING * NUM_SIZES;
		int gridH = GRID_PADDING * 2 + CELL_SPACING * NUM_ALPHAS;

		if (width <= 0) width = gridW;
		if (height <= 0) height = gridH;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// Black background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		// Draw grid labels — use very dim color (below paint threshold of 10)
		// so they don't interfere with circle detection
		g.setColor(new Color(8, 8, 8));
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));

		// Column labels (sizes)
		for (int col = 0; col < NUM_SIZES; col++) {
			int cx = GRID_PADDING + CELL_SPACING / 2 + col * CELL_SPACING;
			String label = "s=" + BorstUtils.SIZES[col];
			FontMetrics fm = g.getFontMetrics();
			g.drawString(label, cx - fm.stringWidth(label) / 2, GRID_PADDING - 1);
		}

		// Row labels (alphas)
		for (int row = 0; row < NUM_ALPHAS; row++) {
			int cy = GRID_PADDING + CELL_SPACING / 2 + row * CELL_SPACING;
			String label = "a=" + BorstUtils.ALPHAS[row];
			g.drawString(label, 1, cy + 3);
		}

		g.dispose();

		// Draw circles using the same scanline approach as the engine
		for (int row = 0; row < NUM_ALPHAS; row++) {
			int alpha = BorstUtils.ALPHAS[row];
			for (int col = 0; col < NUM_SIZES; col++) {
				int sizeIdx = col;
				int cx = GRID_PADDING + CELL_SPACING / 2 + col * CELL_SPACING;
				int cy = GRID_PADDING + CELL_SPACING / 2 + row * CELL_SPACING;

				drawCircleScanlines(image, cx, cy, sizeIdx, alpha);
			}
		}

		return image;
	}

	/**
	 * Draw a circle using the exact same scanline mask from CircleCache.
	 */
	private static void drawCircleScanlines(BufferedImage image, int cx, int cy, int sizeIdx, int alpha) {
		Scanline[] scanlines = CircleCache.CIRCLE_CACHE[sizeIdx];
		int w = image.getWidth();
		int h = image.getHeight();

		// White with given alpha, composited onto black background:
		// result = alpha/255 * 255 = alpha
		int grey = alpha;
		int argb = (0xFF << 24) | (grey << 16) | (grey << 8) | grey;

		for (Scanline sl : scanlines) {
			int py = cy + sl.y;
			if (py < 0 || py >= h) continue;
			for (int x = sl.x1; x <= sl.x2; x++) {
				int px = cx + x;
				if (px < 0 || px >= w) continue;
				image.setRGB(px, py, argb);
			}
		}
	}

	/**
	 * Get the center coordinates of a grid cell (col=sizeIndex, row=alphaIndex).
	 */
	public static int getCellCenterX(int col) {
		return GRID_PADDING + CELL_SPACING / 2 + col * CELL_SPACING;
	}

	public static int getCellCenterY(int row) {
		return GRID_PADDING + CELL_SPACING / 2 + row * CELL_SPACING;
	}

	public static void main(String[] args) throws IOException {
		String outputPath = args.length > 0 ? args[0] : "calibration_pattern.png";
		int width = args.length > 1 ? Integer.parseInt(args[1]) : 0;
		int height = args.length > 2 ? Integer.parseInt(args[2]) : 0;

		BufferedImage image = generate(width, height);
		File outFile = new File(outputPath);
		ImageIO.write(image, "PNG", outFile);

		System.out.println("Calibration pattern saved to: " + outFile.getAbsolutePath());
		System.out.println("Image size: " + image.getWidth() + " x " + image.getHeight());
		System.out.println();
		System.out.println("Grid layout: " + NUM_SIZES + " columns (sizes) x " + NUM_ALPHAS + " rows (alphas)");
		System.out.println("Sizes:  " + java.util.Arrays.toString(BorstUtils.SIZES));
		System.out.println("Alphas: " + java.util.Arrays.toString(BorstUtils.ALPHAS));
		System.out.println();
		System.out.println("Next steps:");
		System.out.println("  1. Use this image as the paint source in Bob-Rust (or paint manually in Rust)");
		System.out.println("  2. Take a screenshot of the painted result on a Rust sign");
		System.out.println("  3. Run: ScreenshotAnalyzer <screenshot.png>");
	}
}
