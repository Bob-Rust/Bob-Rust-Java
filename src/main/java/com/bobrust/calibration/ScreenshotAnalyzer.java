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
 * Analyzes a screenshot of the calibration pattern painted in Rust to measure
 * actual circle diameters, alpha values, and circle shapes.
 *
 * The analyzer locates each cell in the 6x6 grid by searching for bright clusters
 * near expected positions (with tolerance for misalignment), then measures:
 * <ul>
 *   <li>Effective diameter (bounding box of painted pixels)</li>
 *   <li>Effective alpha (average brightness at circle center)</li>
 *   <li>Shape match percentage against Bob-Rust's scanline masks</li>
 * </ul>
 *
 * Usage: java -cp ... com.bobrust.calibration.ScreenshotAnalyzer screenshot.png [reference.png]
 */
public class ScreenshotAnalyzer {

	private static final int NUM_SIZES = CalibrationPatternGenerator.NUM_SIZES;
	private static final int NUM_ALPHAS = CalibrationPatternGenerator.NUM_ALPHAS;

	/** Pixel brightness threshold to consider a pixel "painted" (on dark background). */
	private static final int PAINT_THRESHOLD = 10;

	/** Search radius around expected cell center for centroid detection. */
	private static final int SEARCH_RADIUS = 55;

	/** Size of center sample region for alpha measurement. */
	private static final int ALPHA_SAMPLE_RADIUS = 1; // 3x3 region

	// --- Results ---

	/** Measured effective diameters [row][col] (max of width/height). */
	private final int[][] measuredDiameters;
	/** Measured effective alpha [row][col] from center sampling. */
	private final int[][] measuredAlphas;
	/** Shape match percentage [row][col] against expected scanline mask. */
	private final double[][] shapeMatchPct;
	/** Detected center X [row][col]. */
	private final int[][] detectedCX;
	/** Detected center Y [row][col]. */
	private final int[][] detectedCY;
	/** Whether a cell was successfully detected. */
	private final boolean[][] detected;

	private final BufferedImage screenshot;
	private final int imgW;
	private final int imgH;

	// Grid mapping: we compute the transform from reference grid to screenshot coordinates.
	private double scaleX = 1.0;
	private double scaleY = 1.0;
	private double offsetX = 0.0;
	private double offsetY = 0.0;

	public ScreenshotAnalyzer(BufferedImage screenshot) {
		this.screenshot = screenshot;
		this.imgW = screenshot.getWidth();
		this.imgH = screenshot.getHeight();

		this.measuredDiameters = new int[NUM_ALPHAS][NUM_SIZES];
		this.measuredAlphas = new int[NUM_ALPHAS][NUM_SIZES];
		this.shapeMatchPct = new double[NUM_ALPHAS][NUM_SIZES];
		this.detectedCX = new int[NUM_ALPHAS][NUM_SIZES];
		this.detectedCY = new int[NUM_ALPHAS][NUM_SIZES];
		this.detected = new boolean[NUM_ALPHAS][NUM_SIZES];
	}

	/**
	 * Run the full analysis.
	 */
	public void analyze() {
		estimateGridTransform();

		for (int row = 0; row < NUM_ALPHAS; row++) {
			for (int col = 0; col < NUM_SIZES; col++) {
				analyzeCell(row, col);
			}
		}
	}

	/**
	 * Estimate scale/offset from reference grid to screenshot using brightness projections.
	 *
	 * Strategy: project brightness onto X and Y axes to find column/row peaks.
	 * The bottom row (alpha=255) has the brightest circles, so it dominates the X projection.
	 * The rightmost column (size=100, largest) has the most pixel mass per cell.
	 *
	 * We find the first and last peaks in each projection and compute the transform.
	 */
	private void estimateGridTransform() {
		// X-projection: sum brightness per column
		long[] xProj = new long[imgW];
		// Y-projection: sum brightness per row
		long[] yProj = new long[imgH];

		for (int y = 0; y < imgH; y++) {
			for (int x = 0; x < imgW; x++) {
				int b = brightness(screenshot.getRGB(x, y));
				if (b > PAINT_THRESHOLD) {
					xProj[x] += b;
					yProj[y] += b;
				}
			}
		}

		// Find NUM_SIZES peaks in X projection and NUM_ALPHAS peaks in Y projection
		int[] xPeaks = findPeaks(xProj, NUM_SIZES);
		int[] yPeaks = findPeaks(yProj, NUM_ALPHAS);

		if (xPeaks != null && yPeaks != null && xPeaks.length >= 2 && yPeaks.length >= 2) {
			// Map first peak to first cell center, last peak to last cell center
			int refX0 = CalibrationPatternGenerator.getCellCenterX(0);
			int refX1 = CalibrationPatternGenerator.getCellCenterX(NUM_SIZES - 1);
			int refY0 = CalibrationPatternGenerator.getCellCenterY(0);
			int refY1 = CalibrationPatternGenerator.getCellCenterY(NUM_ALPHAS - 1);

			if (xPeaks[xPeaks.length - 1] != xPeaks[0]) {
				scaleX = (double)(xPeaks[xPeaks.length - 1] - xPeaks[0]) / (refX1 - refX0);
				offsetX = xPeaks[0] - refX0 * scaleX;
			}
			if (yPeaks[yPeaks.length - 1] != yPeaks[0]) {
				scaleY = (double)(yPeaks[yPeaks.length - 1] - yPeaks[0]) / (refY1 - refY0);
				offsetY = yPeaks[0] - refY0 * scaleY;
			}
		}
		// else: keep default 1:1 mapping
	}

	/**
	 * Find N peaks in a 1D projection by dividing into N equal bins and finding
	 * the weighted centroid within each bin.
	 */
	private int[] findPeaks(long[] proj, int numPeaks) {
		int len = proj.length;
		if (len == 0) return null;

		// Find the total energy to set a threshold
		long totalEnergy = 0;
		for (long v : proj) totalEnergy += v;
		if (totalEnergy == 0) return null;

		// Divide the array into numPeaks bins
		int[] peaks = new int[numPeaks];
		double binSize = (double) len / numPeaks;

		for (int i = 0; i < numPeaks; i++) {
			int binStart = (int)(i * binSize);
			int binEnd = (int)((i + 1) * binSize);
			binEnd = Math.min(binEnd, len);

			long sumPos = 0, sumWeight = 0;
			for (int j = binStart; j < binEnd; j++) {
				if (proj[j] > 0) {
					sumPos += (long) j * proj[j];
					sumWeight += proj[j];
				}
			}

			if (sumWeight > 0) {
				peaks[i] = (int)(sumPos / sumWeight);
			} else {
				// No energy in this bin; use bin center
				peaks[i] = (binStart + binEnd) / 2;
			}
		}

		return peaks;
	}

	/**
	 * Map a reference grid coordinate to screenshot coordinate.
	 */
	private int mapX(int refX) {
		return (int) Math.round(refX * scaleX + offsetX);
	}

	private int mapY(int refY) {
		return (int) Math.round(refY * scaleY + offsetY);
	}

	/**
	 * Analyze a single grid cell.
	 */
	private void analyzeCell(int row, int col) {
		int expectedCX = mapX(CalibrationPatternGenerator.getCellCenterX(col));
		int expectedCY = mapY(CalibrationPatternGenerator.getCellCenterY(row));

		// Find actual centroid near expected position
		int[] centroid = findCentroidNear(expectedCX, expectedCY, SEARCH_RADIUS);
		if (centroid == null) {
			detected[row][col] = false;
			return;
		}

		detected[row][col] = true;
		int cx = centroid[0];
		int cy = centroid[1];
		detectedCX[row][col] = cx;
		detectedCY[row][col] = cy;

		// Measure bounding box of painted pixels around centroid.
		// Limit search to half the cell spacing minus a margin to avoid bleeding
		// into neighboring cells.
		int halfCell = CalibrationPatternGenerator.CELL_SPACING / 2 - 5;
		int minPX = Integer.MAX_VALUE, maxPX = Integer.MIN_VALUE;
		int minPY = Integer.MAX_VALUE, maxPY = Integer.MIN_VALUE;

		for (int dy = -halfCell; dy <= halfCell; dy++) {
			for (int dx = -halfCell; dx <= halfCell; dx++) {
				int px = cx + dx;
				int py = cy + dy;
				if (px < 0 || px >= imgW || py < 0 || py >= imgH) continue;
				if (brightness(screenshot.getRGB(px, py)) > PAINT_THRESHOLD) {
					minPX = Math.min(minPX, px);
					maxPX = Math.max(maxPX, px);
					minPY = Math.min(minPY, py);
					maxPY = Math.max(maxPY, py);
				}
			}
		}

		if (minPX > maxPX) {
			measuredDiameters[row][col] = 0;
		} else {
			int w = maxPX - minPX + 1;
			int h = maxPY - minPY + 1;
			measuredDiameters[row][col] = Math.max(w, h);
		}

		// Measure alpha from center sample
		measuredAlphas[row][col] = sampleCenterBrightness(cx, cy);

		// Shape match
		shapeMatchPct[row][col] = computeShapeMatch(cx, cy, col);
	}

	/**
	 * Find the centroid of bright pixels near a given point.
	 */
	private int[] findCentroidNear(int cx, int cy, int radius) {
		long sumX = 0, sumY = 0, sumW = 0;

		int x1 = Math.max(0, cx - radius);
		int y1 = Math.max(0, cy - radius);
		int x2 = Math.min(imgW - 1, cx + radius);
		int y2 = Math.min(imgH - 1, cy + radius);

		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				int b = brightness(screenshot.getRGB(x, y));
				if (b > PAINT_THRESHOLD) {
					sumX += (long) x * b;
					sumY += (long) y * b;
					sumW += b;
				}
			}
		}

		if (sumW == 0) return null;
		return new int[]{(int)(sumX / sumW), (int)(sumY / sumW)};
	}

	/**
	 * Sample the average brightness of the center 3x3 region.
	 */
	private int sampleCenterBrightness(int cx, int cy) {
		long sum = 0;
		int count = 0;
		for (int dy = -ALPHA_SAMPLE_RADIUS; dy <= ALPHA_SAMPLE_RADIUS; dy++) {
			for (int dx = -ALPHA_SAMPLE_RADIUS; dx <= ALPHA_SAMPLE_RADIUS; dx++) {
				int px = cx + dx;
				int py = cy + dy;
				if (px >= 0 && px < imgW && py >= 0 && py < imgH) {
					sum += brightness(screenshot.getRGB(px, py));
					count++;
				}
			}
		}
		return count > 0 ? (int)(sum / count) : 0;
	}

	/**
	 * Compute the percentage of matching pixels between the screenshot circle
	 * and the expected scanline mask for a given size index.
	 */
	private double computeShapeMatch(int cx, int cy, int sizeIdx) {
		Scanline[] expected = CircleCache.CIRCLE_CACHE[sizeIdx];
		int size = BorstUtils.SIZES[sizeIdx];

		int totalExpected = 0;
		int matched = 0;

		// Count total expected pixels and check which ones are painted in the screenshot
		for (Scanline sl : expected) {
			for (int x = sl.x1; x <= sl.x2; x++) {
				totalExpected++;
				int px = cx + x;
				int py = cy + sl.y;
				if (px >= 0 && px < imgW && py >= 0 && py < imgH) {
					if (brightness(screenshot.getRGB(px, py)) > PAINT_THRESHOLD) {
						matched++;
					}
				}
			}
		}

		// Also count false positives: painted pixels NOT in the expected mask
		int halfSize = size / 2 + 2;
		int falsePositives = 0;
		int totalChecked = 0;
		for (int dy = -halfSize; dy <= halfSize; dy++) {
			for (int dx = -halfSize; dx <= halfSize; dx++) {
				int px = cx + dx;
				int py = cy + dy;
				if (px < 0 || px >= imgW || py < 0 || py >= imgH) continue;
				totalChecked++;
				boolean isPainted = brightness(screenshot.getRGB(px, py)) > PAINT_THRESHOLD;
				boolean isExpected = isInScanlines(expected, dx, dy);
				if (isPainted && !isExpected) {
					falsePositives++;
				}
			}
		}

		if (totalExpected == 0) return 0.0;
		// F1-like score: penalize both misses and false positives
		int errors = (totalExpected - matched) + falsePositives;
		int possible = totalExpected + falsePositives;
		return possible > 0 ? 100.0 * (possible - errors) / possible : 100.0;
	}

	/**
	 * Check if a relative offset (dx, dy) falls within any scanline.
	 */
	private static boolean isInScanlines(Scanline[] scanlines, int dx, int dy) {
		for (Scanline sl : scanlines) {
			if (sl.y == dy && dx >= sl.x1 && dx <= sl.x2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generate a diff image showing shape mismatches for each size.
	 * Green = correctly painted, Red = missing, Blue = false positive (extra painted pixel).
	 */
	public BufferedImage generateDiffImage() {
		// Use bottom row (full alpha) for clearest comparison
		int alphaRow = NUM_ALPHAS - 1;
		int maxSize = BorstUtils.SIZES[NUM_SIZES - 1];
		int cellSize = maxSize + 20;
		int diffW = cellSize * NUM_SIZES + 20;
		int diffH = cellSize + 40;

		BufferedImage diff = new BufferedImage(diffW, diffH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = diff.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, diffW, diffH);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

		for (int col = 0; col < NUM_SIZES; col++) {
			if (!detected[alphaRow][col]) continue;
			int cx = detectedCX[alphaRow][col];
			int cy = detectedCY[alphaRow][col];
			int size = BorstUtils.SIZES[col];
			int halfSize = size / 2 + 2;

			int drawBaseX = 10 + col * cellSize + cellSize / 2;
			int drawBaseY = 20 + cellSize / 2;

			Scanline[] expected = CircleCache.CIRCLE_CACHE[col];

			// Label
			g.setColor(Color.WHITE);
			String label = "s=" + size + " (" + String.format("%.0f%%", shapeMatchPct[alphaRow][col]) + ")";
			g.drawString(label, drawBaseX - 25, diffH - 5);

			for (int dy = -halfSize; dy <= halfSize; dy++) {
				for (int dx = -halfSize; dx <= halfSize; dx++) {
					int px = cx + dx;
					int py = cy + dy;
					boolean isPainted = false;
					if (px >= 0 && px < imgW && py >= 0 && py < imgH) {
						isPainted = brightness(screenshot.getRGB(px, py)) > PAINT_THRESHOLD;
					}
					boolean isExpected = isInScanlines(expected, dx, dy);

					Color c = null;
					if (isPainted && isExpected) {
						c = new Color(0, 200, 0);   // green: correct
					} else if (!isPainted && isExpected) {
						c = new Color(200, 0, 0);   // red: missing
					} else if (isPainted && !isExpected) {
						c = new Color(0, 0, 200);   // blue: false positive
					}

					if (c != null) {
						int drawX = drawBaseX + dx;
						int drawY = drawBaseY + dy;
						if (drawX >= 0 && drawX < diffW && drawY >= 0 && drawY < diffH) {
							diff.setRGB(drawX, drawY, c.getRGB());
						}
					}
				}
			}
		}

		g.dispose();
		return diff;
	}

	/**
	 * Print the analysis report to stdout.
	 */
	public void printReport() {
		System.out.println("=== Calibration Analysis Report ===");
		System.out.println();
		System.out.printf("Grid transform: scale=(%.3f, %.3f) offset=(%.1f, %.1f)%n",
			scaleX, scaleY, offsetX, offsetY);
		System.out.println();

		// Diameter table
		System.out.println("--- Measured Diameters (expected across top) ---");
		System.out.printf("%8s", "alpha\\sz");
		for (int col = 0; col < NUM_SIZES; col++) {
			System.out.printf("  %4d", BorstUtils.SIZES[col]);
		}
		System.out.println();

		for (int row = 0; row < NUM_ALPHAS; row++) {
			System.out.printf("  a=%-4d", BorstUtils.ALPHAS[row]);
			for (int col = 0; col < NUM_SIZES; col++) {
				if (detected[row][col]) {
					System.out.printf("  %4d", measuredDiameters[row][col]);
				} else {
					System.out.printf("  %4s", "-");
				}
			}
			System.out.println();
		}

		// Alpha table
		System.out.println();
		System.out.println("--- Measured Alpha (center brightness) ---");
		System.out.printf("%8s", "alpha\\sz");
		for (int col = 0; col < NUM_SIZES; col++) {
			System.out.printf("  %4d", BorstUtils.SIZES[col]);
		}
		System.out.println();

		for (int row = 0; row < NUM_ALPHAS; row++) {
			System.out.printf("  a=%-4d", BorstUtils.ALPHAS[row]);
			for (int col = 0; col < NUM_SIZES; col++) {
				if (detected[row][col]) {
					System.out.printf("  %4d", measuredAlphas[row][col]);
				} else {
					System.out.printf("  %4s", "-");
				}
			}
			System.out.println();
		}

		// Shape match table (bottom row only — full alpha)
		System.out.println();
		System.out.println("--- Shape Match % (row with alpha=255) ---");
		int alphaRow = NUM_ALPHAS - 1;
		for (int col = 0; col < NUM_SIZES; col++) {
			if (detected[alphaRow][col]) {
				System.out.printf("  size=%3d: %.1f%% match%n",
					BorstUtils.SIZES[col], shapeMatchPct[alphaRow][col]);
			}
		}

		// Suggested corrections
		System.out.println();
		System.out.println("--- Suggested Corrections ---");
		printSuggestedSizes();
		printSuggestedAlphas();
		printJavaSnippet();
	}

	private void printSuggestedSizes() {
		// Use the bottom row (alpha=255) for most reliable diameter measurement
		int row = NUM_ALPHAS - 1;
		System.out.print("Suggested SIZES: { ");
		for (int col = 0; col < NUM_SIZES; col++) {
			if (col > 0) System.out.print(", ");
			if (detected[row][col] && measuredDiameters[row][col] > 0) {
				System.out.print(measuredDiameters[row][col]);
			} else {
				System.out.print(BorstUtils.SIZES[col] + "?");
			}
		}
		System.out.println(" }");

		System.out.print("Current SIZES:   { ");
		for (int col = 0; col < NUM_SIZES; col++) {
			if (col > 0) System.out.print(", ");
			System.out.print(BorstUtils.SIZES[col]);
		}
		System.out.println(" }");
	}

	private void printSuggestedAlphas() {
		// Use the rightmost column (largest size) for most reliable alpha measurement
		int col = NUM_SIZES - 1;
		System.out.print("Suggested ALPHAS: { ");
		for (int row = 0; row < NUM_ALPHAS; row++) {
			if (row > 0) System.out.print(", ");
			if (detected[row][col] && measuredAlphas[row][col] > 0) {
				System.out.print(measuredAlphas[row][col]);
			} else {
				System.out.print(BorstUtils.ALPHAS[row] + "?");
			}
		}
		System.out.println(" }");

		System.out.print("Current ALPHAS:  { ");
		for (int row = 0; row < NUM_ALPHAS; row++) {
			if (row > 0) System.out.print(", ");
			System.out.print(BorstUtils.ALPHAS[row]);
		}
		System.out.println(" }");
	}

	private void printJavaSnippet() {
		int sizeRow = NUM_ALPHAS - 1;
		int alphaCol = NUM_SIZES - 1;

		System.out.println();
		System.out.println("--- Copy-paste Java snippet ---");
		System.out.println();

		// Sizes
		StringBuilder sb = new StringBuilder("public static final int[] SIZES = { ");
		for (int col = 0; col < NUM_SIZES; col++) {
			if (col > 0) sb.append(", ");
			if (detected[sizeRow][col] && measuredDiameters[sizeRow][col] > 0) {
				sb.append(measuredDiameters[sizeRow][col]);
			} else {
				sb.append(BorstUtils.SIZES[col]);
			}
		}
		sb.append(" };");
		System.out.println(sb);

		// Alphas
		sb = new StringBuilder("public static final int[] ALPHAS = { ");
		for (int row = 0; row < NUM_ALPHAS; row++) {
			if (row > 0) sb.append(", ");
			if (detected[row][alphaCol] && measuredAlphas[row][alphaCol] > 0) {
				sb.append(measuredAlphas[row][alphaCol]);
			} else {
				sb.append(BorstUtils.ALPHAS[row]);
			}
		}
		sb.append(" };");
		System.out.println(sb);
	}

	/**
	 * Get the measured diameters array (for testing).
	 */
	public int[][] getMeasuredDiameters() { return measuredDiameters; }

	/**
	 * Get the measured alphas array (for testing).
	 */
	public int[][] getMeasuredAlphas() { return measuredAlphas; }

	/**
	 * Get the shape match percentages (for testing).
	 */
	public double[][] getShapeMatchPct() { return shapeMatchPct; }

	/**
	 * Get detection flags (for testing).
	 */
	public boolean[][] getDetected() { return detected; }

	private static int brightness(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return (r + g + b) / 3;
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage: ScreenshotAnalyzer <screenshot.png> [diff_output.png]");
			System.exit(1);
		}

		String screenshotPath = args[0];
		String diffOutputPath = args.length > 1 ? args[1] : "calibration_diff.png";

		System.out.println("Loading screenshot: " + screenshotPath);
		BufferedImage screenshot = ImageIO.read(new File(screenshotPath));
		if (screenshot == null) {
			System.err.println("Failed to load image: " + screenshotPath);
			System.exit(1);
		}

		System.out.println("Screenshot size: " + screenshot.getWidth() + " x " + screenshot.getHeight());
		System.out.println("Analyzing...");
		System.out.println();

		ScreenshotAnalyzer analyzer = new ScreenshotAnalyzer(screenshot);
		analyzer.analyze();
		analyzer.printReport();

		// Save diff image
		BufferedImage diff = analyzer.generateDiffImage();
		File diffFile = new File(diffOutputPath);
		ImageIO.write(diff, "PNG", diffFile);
		System.out.println();
		System.out.println("Diff image saved to: " + diffFile.getAbsolutePath());
	}
}
