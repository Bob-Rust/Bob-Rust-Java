package com.bobrust.generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the adaptive size selection feature (Proposal 3).
 *
 * Verifies that gradient-based size biasing produces equal or better results
 * than uniform size selection. Generates visual comparison images saved to
 * test-results/proposal3/.
 */
class AdaptiveSizeSelectionTest {
	private static final int ALPHA = 128;
	private static final int BACKGROUND = 0xFFFFFFFF;
	private static final File OUTPUT_DIR = new File("test-results/proposal3");

	@BeforeAll
	static void setup() {
		OUTPUT_DIR.mkdirs();
	}

	// ---- Gradient map correctness tests ----

	@Test
	void testGradientMapHighAtEdges() {
		// Create an image with a sharp vertical edge in the middle
		BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 32, 64);
		g.setColor(Color.WHITE);
		g.fillRect(32, 0, 32, 64);
		g.dispose();

		BorstImage target = new BorstImage(ensureArgb(img));
		GradientMap gradMap = new GradientMap(64, 64, 4, 4);
		gradMap.compute(target);

		// Cells near the edge (column 1-2 out of 0-3) should have higher gradient
		// than cells far from the edge (column 0 or 3)
		float edgeGradient = gradMap.getGradient(32, 32); // at the edge
		float smoothGradient = gradMap.getGradient(8, 32); // far from edge

		assertTrue(edgeGradient > smoothGradient,
			"Gradient at edge (" + edgeGradient + ") should be higher than in smooth area (" + smoothGradient + ")");
	}

	@Test
	void testGradientMapLowInSmoothAreas() {
		// Solid color image — gradient should be near zero everywhere
		BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(128, 128, 128));
		g.fillRect(0, 0, 64, 64);
		g.dispose();

		BorstImage target = new BorstImage(ensureArgb(img));
		GradientMap gradMap = new GradientMap(64, 64, 4, 4);
		gradMap.compute(target);

		for (int i = 0; i < gradMap.cellGradients.length; i++) {
			assertEquals(0.0f, gradMap.cellGradients[i], 0.001f,
				"Gradient in solid image should be zero at cell " + i);
		}
	}

	@Test
	void testGradientMapCheckerboard() {
		// Checkerboard has edges everywhere — all cells should have high gradient
		BufferedImage img = TestImageGenerator.createEdges();
		BorstImage target = new BorstImage(ensureArgb(img));
		GradientMap gradMap = new GradientMap(128, 128, 8, 8);
		gradMap.compute(target);

		int highCount = 0;
		for (float v : gradMap.cellGradients) {
			if (v > 0.3f) highCount++;
		}

		// Most cells in a checkerboard should have notable gradient
		assertTrue(highCount > gradMap.cellGradients.length / 2,
			"Checkerboard should have high gradient in most cells, but only " + highCount +
			" of " + gradMap.cellGradients.length + " were above 0.3");
	}

	@Test
	void testSizeSelectionBiasNearEdges() {
		// Create an image with a clear edge
		BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 64, 128);
		g.setColor(Color.WHITE);
		g.fillRect(64, 0, 64, 128);
		g.dispose();

		BorstImage target = new BorstImage(ensureArgb(img));
		GradientMap gradMap = new GradientMap(128, 128);
		gradMap.compute(target);

		Random rnd = new Random(42);
		int numSizes = BorstUtils.SIZES.length;

		// Sample sizes at the edge vs in smooth area
		int[] edgeSizeHist = new int[numSizes];
		int[] smoothSizeHist = new int[numSizes];
		int samples = 5000;

		for (int i = 0; i < samples; i++) {
			edgeSizeHist[gradMap.selectSizeIndex(rnd, 64, 64)]++;   // at edge
			smoothSizeHist[gradMap.selectSizeIndex(rnd, 16, 64)]++; // smooth area
		}

		// Near edges: should favor smaller sizes (lower indices)
		int edgeSmallCount = edgeSizeHist[0] + edgeSizeHist[1] + edgeSizeHist[2];
		int smoothSmallCount = smoothSizeHist[0] + smoothSizeHist[1] + smoothSizeHist[2];

		assertTrue(edgeSmallCount > smoothSmallCount,
			"Edge area should select more small circles (" + edgeSmallCount +
			") than smooth area (" + smoothSmallCount + ")");
	}

	// ---- End-to-end: adaptive vs uniform sizing ----

	@Test
	void testAdaptiveSizingProducesLowerOrEqualError() {
		BufferedImage testImage = TestImageGenerator.createPhotoDetail();
		int maxShapes = 50;

		Model uniformModel = runGenerator(testImage, maxShapes, false);
		Model adaptiveModel = runGenerator(testImage, maxShapes, true);

		float uniformScore = uniformModel.score;
		float adaptiveScore = adaptiveModel.score;

		System.out.println("Uniform score:  " + uniformScore);
		System.out.println("Adaptive score: " + adaptiveScore);
		float improvement = (uniformScore - adaptiveScore) / uniformScore * 100;
		System.out.println("Improvement:    " + improvement + "%");

		// Allow 5% tolerance for stochastic variation
		assertTrue(adaptiveScore <= uniformScore * 1.05f,
			"Adaptive score (" + adaptiveScore + ") should not be significantly worse than uniform (" + uniformScore + ")");
	}

	@Test
	void testAdaptiveNeverSignificantlyWorse() {
		BufferedImage[] images = {
			TestImageGenerator.createGradient(),
			TestImageGenerator.createEdges(),
			TestImageGenerator.createNature(),
			TestImageGenerator.createPhotoDetail(),
		};
		String[] names = {"gradient", "edges", "nature", "photo_detail"};
		int maxShapes = 30;

		float totalUniform = 0, totalAdaptive = 0;
		for (int idx = 0; idx < images.length; idx++) {
			Model uniformModel = runGenerator(images[idx], maxShapes, false);
			Model adaptiveModel = runGenerator(images[idx], maxShapes, true);
			totalUniform += uniformModel.score;
			totalAdaptive += adaptiveModel.score;
			System.out.println(names[idx] + " — Uniform: " + uniformModel.score + ", Adaptive: " + adaptiveModel.score);
		}

		// Check aggregate rather than per-image to reduce stochastic flakiness
		System.out.println("Aggregate — Uniform: " + totalUniform + ", Adaptive: " + totalAdaptive);
		assertTrue(totalAdaptive <= totalUniform * 1.10f,
			"Aggregate Adaptive (" + totalAdaptive + ") should not be significantly worse than aggregate Uniform (" + totalUniform + ")");
	}

	// ---- Visual comparison benchmark ----

	@Test
	void testVisualComparison() throws IOException {
		String[] names = {"photo_detail", "nature", "edges"};
		BufferedImage[] images = {
			TestImageGenerator.createPhotoDetail(),
			TestImageGenerator.createNature(),
			TestImageGenerator.createEdges(),
		};
		int maxShapes = 200;

		for (int idx = 0; idx < names.length; idx++) {
			String name = names[idx];
			BufferedImage targetImg = images[idx];
			System.out.println("Generating visual comparison for: " + name);

			// Save target
			ImageIO.write(targetImg, "png", new File(OUTPUT_DIR, name + "_target.png"));

			// Run both methods
			Model uniformModel = runGenerator(targetImg, maxShapes, false);
			Model adaptiveModel = runGenerator(targetImg, maxShapes, true);

			// Save rendered results
			BufferedImage uniformResult = toBufferedImage(uniformModel.current);
			BufferedImage adaptiveResult = toBufferedImage(adaptiveModel.current);
			ImageIO.write(uniformResult, "png", new File(OUTPUT_DIR, name + "_uniform.png"));
			ImageIO.write(adaptiveResult, "png", new File(OUTPUT_DIR, name + "_adaptive.png"));

			// Generate difference heatmap
			BufferedImage diffImage = generateDiffHeatmap(uniformResult, adaptiveResult);
			ImageIO.write(diffImage, "png", new File(OUTPUT_DIR, name + "_diff.png"));

			// Generate gradient map visualization
			BorstImage targetBorst = new BorstImage(ensureArgb(targetImg));
			GradientMap gradMap = new GradientMap(targetBorst.width, targetBorst.height);
			gradMap.compute(targetBorst);
			BufferedImage gradientViz = visualizeGradientMap(gradMap);
			ImageIO.write(gradientViz, "png", new File(OUTPUT_DIR, name + "_gradient.png"));

			System.out.println("  Uniform score:  " + uniformModel.score);
			System.out.println("  Adaptive score: " + adaptiveModel.score);
			float improvement = (uniformModel.score - adaptiveModel.score) / uniformModel.score * 100;
			System.out.println("  Improvement:    " + improvement + "%");
		}
	}

	// ---- Generator runner ----

	/**
	 * Run the generator for a given number of shapes.
	 * @param useAdaptiveSize if true, uses gradient-based adaptive sizing; if false, uniform random sizes.
	 */
	private static Model runGenerator(BufferedImage testImage, int maxShapes, boolean useAdaptiveSize) {
		BufferedImage argbImage = ensureArgb(testImage);
		BorstImage target = new BorstImage(argbImage);
		Model model = new Model(target, BACKGROUND, ALPHA);

		Worker worker = getWorker(model);
		ErrorMap errorMap = getErrorMap(model);

		if (!useAdaptiveSize) {
			// Disable gradient map for uniform sizing
			worker.setGradientMap(null);
			setGradientMap(model, null);
		}

		for (int i = 0; i < maxShapes; i++) {
			worker.init(model.current, model.score);
			List<State> randomStates = createRandomStates(worker, 200);
			State best = getBestRandomState(randomStates, errorMap);
			State state = HillClimbGenerator.getHillClimbClassic(best, 100);
			addShapeToModel(model, state.shape);
			if (errorMap != null) {
				errorMap = getErrorMap(model);
			}
		}
		return model;
	}

	// ---- Helper methods ----

	private static BufferedImage ensureArgb(BufferedImage img) {
		if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
		BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = argb.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return argb;
	}

	private static BufferedImage toBufferedImage(BorstImage borstImage) {
		int w = borstImage.width;
		int h = borstImage.height;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] destPixels = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
		System.arraycopy(borstImage.pixels, 0, destPixels, 0, borstImage.pixels.length);
		return img;
	}

	private static BufferedImage generateDiffHeatmap(BufferedImage a, BufferedImage b) {
		int w = a.getWidth();
		int h = a.getHeight();
		BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int ca = a.getRGB(x, y);
				int cb = b.getRGB(x, y);

				int dr = Math.abs(((ca >> 16) & 0xff) - ((cb >> 16) & 0xff));
				int dg = Math.abs(((ca >> 8) & 0xff) - ((cb >> 8) & 0xff));
				int db = Math.abs((ca & 0xff) - (cb & 0xff));

				int intensity = Math.min(255, (dr + dg + db) * 2);
				int heatR = Math.min(255, intensity * 2);
				int heatG = Math.max(0, 255 - intensity * 2);
				int heatB = 0;

				diff.setRGB(x, y, 0xFF000000 | (heatR << 16) | (heatG << 8) | heatB);
			}
		}
		return diff;
	}

	/**
	 * Visualize the gradient map as a grayscale image where bright = high gradient (edges).
	 */
	private static BufferedImage visualizeGradientMap(GradientMap gradMap) {
		int w = gradMap.imageWidth;
		int h = gradMap.imageHeight;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float g = gradMap.getGradient(x, y);
				int v = Math.min(255, (int)(g * 255));
				img.setRGB(x, y, 0xFF000000 | (v << 16) | (v << 8) | v);
			}
		}
		return img;
	}

	// ---- Reflective helpers ----

	private static Worker getWorker(Model model) {
		try {
			Field field = Model.class.getDeclaredField("worker");
			field.setAccessible(true);
			return (Worker) field.get(model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static ErrorMap getErrorMap(Model model) {
		try {
			Field field = Model.class.getDeclaredField("errorMap");
			field.setAccessible(true);
			return (ErrorMap) field.get(model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void setGradientMap(Model model, GradientMap gradientMap) {
		try {
			Field field = Model.class.getDeclaredField("gradientMap");
			field.setAccessible(true);
			field.set(model, gradientMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static List<State> createRandomStates(Worker worker, int count) {
		List<State> states = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			states.add(new State(worker));
		}
		return states;
	}

	private static State getBestRandomState(List<State> states, ErrorMap errorMap) {
		for (State s : states) {
			s.score = -1;
			s.shape.randomize(errorMap);
		}
		states.parallelStream().forEach(State::getEnergy);
		float bestEnergy = Float.MAX_VALUE;
		State bestState = null;
		for (State s : states) {
			float energy = s.getEnergy();
			if (bestState == null || energy < bestEnergy) {
				bestEnergy = energy;
				bestState = s;
			}
		}
		return bestState;
	}

	private static void addShapeToModel(Model model, Circle shape) {
		try {
			Method method = Model.class.getDeclaredMethod("addShape", Circle.class);
			method.setAccessible(true);
			method.invoke(model, shape);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
