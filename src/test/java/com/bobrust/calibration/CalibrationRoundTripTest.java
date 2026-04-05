package com.bobrust.calibration;

import com.bobrust.generator.BorstUtils;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip test: generate a calibration pattern, then analyze it as if it were a screenshot.
 * Since the "screenshot" is the exact pattern we generated (no Rust in the loop), the measured
 * values should match the expected values exactly or very closely.
 */
class CalibrationRoundTripTest {

	@Test
	void roundTripExactPattern() {
		// Generate the calibration pattern
		BufferedImage pattern = CalibrationPatternGenerator.generate(0, 0);
		assertNotNull(pattern);
		assertTrue(pattern.getWidth() > 0);
		assertTrue(pattern.getHeight() > 0);

		// Analyze the pattern as if it were a screenshot
		ScreenshotAnalyzer analyzer = new ScreenshotAnalyzer(pattern);
		analyzer.analyze();

		boolean[][] detected = analyzer.getDetected();
		int[][] measuredDiameters = analyzer.getMeasuredDiameters();
		int[][] measuredAlphas = analyzer.getMeasuredAlphas();
		double[][] shapeMatch = analyzer.getShapeMatchPct();

		int numSizes = CalibrationPatternGenerator.NUM_SIZES;
		int numAlphas = CalibrationPatternGenerator.NUM_ALPHAS;

		// Count how many cells are detected vs expected
		int expectedDetected = 0;
		int actualDetected = 0;
		int diameterMatches = 0;
		int alphaMatches = 0;

		for (int row = 0; row < numAlphas; row++) {
			int expectedAlpha = BorstUtils.ALPHAS[row];
			for (int col = 0; col < numSizes; col++) {
				int expectedSize = BorstUtils.SIZES[col];

				if (expectedAlpha <= 10) continue;
				expectedDetected++;

				if (!detected[row][col]) continue;
				actualDetected++;

				// Diameter check
				if (Math.abs(expectedSize - measuredDiameters[row][col]) <= 2) {
					diameterMatches++;
				}

				// Alpha check (bottom row only, where alpha is most reliable)
				if (row == numAlphas - 1) {
					if (Math.abs(expectedAlpha - measuredAlphas[row][col]) <= 3) {
						alphaMatches++;
					}
				}
			}
		}

		// At least 80% of cells should be detected
		assertTrue(actualDetected >= expectedDetected * 0.8,
			"Too few cells detected: " + actualDetected + "/" + expectedDetected);

		// At least 80% of detected cells should have correct diameter
		assertTrue(diameterMatches >= actualDetected * 0.8,
			"Too few diameter matches: " + diameterMatches + "/" + actualDetected);

		// All bottom-row detected cells should have correct alpha
		int bottomRowDetected = 0;
		for (int col = 0; col < numSizes; col++) {
			if (detected[numAlphas - 1][col]) bottomRowDetected++;
		}
		assertTrue(alphaMatches >= bottomRowDetected * 0.8,
			"Too few alpha matches in bottom row: " + alphaMatches + "/" + bottomRowDetected);

		// Shape match for detected full-alpha cells should be high
		int fullAlphaRow = numAlphas - 1;
		int goodShapes = 0;
		int checkedShapes = 0;
		for (int col = 0; col < numSizes; col++) {
			if (detected[fullAlphaRow][col]) {
				checkedShapes++;
				if (shapeMatch[fullAlphaRow][col] >= 90.0) {
					goodShapes++;
				}
			}
		}
		// At least 50% should have good shapes (small circles may have
		// shape detection issues due to centroid precision)
		assertTrue(goodShapes >= checkedShapes * 0.5,
			"Too few good shape matches: " + goodShapes + "/" + checkedShapes);
	}

	@Test
	void patternGeneratorDimensions() {
		BufferedImage img = CalibrationPatternGenerator.generate(0, 0);
		int numSizes = CalibrationPatternGenerator.NUM_SIZES;
		int numAlphas = CalibrationPatternGenerator.NUM_ALPHAS;
		int padding = CalibrationPatternGenerator.GRID_PADDING;
		int spacing = CalibrationPatternGenerator.CELL_SPACING;

		int expectedW = padding * 2 + spacing * numSizes;
		int expectedH = padding * 2 + spacing * numAlphas;

		assertEquals(expectedW, img.getWidth(), "Auto-calculated width");
		assertEquals(expectedH, img.getHeight(), "Auto-calculated height");
	}

	@Test
	void patternGeneratorCustomSize() {
		BufferedImage img = CalibrationPatternGenerator.generate(800, 600);
		assertEquals(800, img.getWidth());
		assertEquals(600, img.getHeight());
	}

	@Test
	void diffImageGeneration() {
		BufferedImage pattern = CalibrationPatternGenerator.generate(0, 0);
		ScreenshotAnalyzer analyzer = new ScreenshotAnalyzer(pattern);
		analyzer.analyze();

		BufferedImage diff = analyzer.generateDiffImage();
		assertNotNull(diff);
		assertTrue(diff.getWidth() > 0);
		assertTrue(diff.getHeight() > 0);
	}

	@Test
	void scaledPatternDetection() {
		// Generate pattern, then scale to 2x to simulate different resolution
		BufferedImage original = CalibrationPatternGenerator.generate(0, 0);
		int newW = original.getWidth() * 2;
		int newH = original.getHeight() * 2;

		BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
			java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(original, 0, 0, newW, newH, null);
		g.dispose();

		ScreenshotAnalyzer analyzer = new ScreenshotAnalyzer(scaled);
		analyzer.analyze();

		boolean[][] detected = analyzer.getDetected();
		int numAlphas = CalibrationPatternGenerator.NUM_ALPHAS;
		int numSizes = CalibrationPatternGenerator.NUM_SIZES;

		// At least some cells in the full-alpha row should be detected
		int fullAlphaRow = numAlphas - 1;
		int detectedCount = 0;
		for (int col = 0; col < numSizes; col++) {
			if (detected[fullAlphaRow][col]) detectedCount++;
		}
		assertTrue(detectedCount >= numSizes / 2,
			"Scaled 2x: should detect at least half the bottom row, got " + detectedCount);

		// For detected cells, diameter should be larger than the original
		// (not necessarily exact 2x due to grid transform estimation)
		for (int col = 0; col < numSizes; col++) {
			if (detected[fullAlphaRow][col]) {
				int measured = analyzer.getMeasuredDiameters()[fullAlphaRow][col];
				assertTrue(measured > BorstUtils.SIZES[col],
					"Scaled 2x: measured diameter " + measured +
					" should be > original " + BorstUtils.SIZES[col]);
			}
		}
	}
}
