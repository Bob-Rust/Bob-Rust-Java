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
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the error-guided circle placement feature.
 *
 * Compares uniform random placement against error-guided placement to verify
 * that biasing toward high-error regions produces equal or better results.
 * Generates visual comparison images saved to build/test-output/.
 */
class ErrorGuidedPlacementTest {
	private static final int ALPHA = 128;
	private static final int BACKGROUND = 0xFFFFFFFF;
	private static final File OUTPUT_DIR = new File("build/test-output");

	@BeforeAll
	static void setup() {
		OUTPUT_DIR.mkdirs();
	}

	// ---- Core test runner ----

	/**
	 * Run the generator for a given number of shapes.
	 * @param useErrorGuided if true, uses error-guided placement; if false, uniform random.
	 * @return the final Model (with score and rendered current image).
	 */
	private static Model runGenerator(BufferedImage testImage, int maxShapes, boolean useErrorGuided) {
		BufferedImage argbImage = ensureArgb(testImage);
		BorstImage target = new BorstImage(argbImage);
		Model model = new Model(target, BACKGROUND, ALPHA);

		// Override the error map on the worker based on what we want to test
		Worker worker = getWorker(model);
		ErrorMap errorMap = useErrorGuided ? getErrorMap(model) : null;
		if (!useErrorGuided) {
			worker.setErrorMap(null);
			setErrorMap(model, null);
		}

		for (int i = 0; i < maxShapes; i++) {
			worker.init(model.current, model.score);
			List<State> randomStates = createRandomStates(worker, 200);
			State best = getBestRandomState(randomStates, errorMap);
			State state = HillClimbGenerator.getHillClimbClassic(best, 100);
			addShapeToModel(model, state.shape);
			// Re-fetch the error map as it gets updated after addShape
			if (useErrorGuided) {
				errorMap = getErrorMap(model);
			}
		}
		return model;
	}

	// ---- Test: error-guided produces lower or equal error ----

	@Test
	void testErrorGuidedProducesLowerOrEqualError() {
		BufferedImage testImage = TestImageGenerator.createPhotoDetail();
		int maxShapes = 50;

		Model uniformModel = runGenerator(testImage, maxShapes, false);
		Model guidedModel = runGenerator(testImage, maxShapes, true);

		float uniformScore = uniformModel.score;
		float guidedScore = guidedModel.score;

		System.out.println("Uniform score: " + uniformScore);
		System.out.println("Guided score:  " + guidedScore);
		float improvement = (uniformScore - guidedScore) / uniformScore * 100;
		System.out.println("Improvement:   " + improvement + "%");

		// Allow 5% tolerance for stochastic variation
		assertTrue(guidedScore <= uniformScore * 1.05f,
			"Guided score (" + guidedScore + ") should not be significantly worse than uniform (" + uniformScore + ")");
	}

	@Test
	void testErrorGuidedNeverSignificantlyWorse() {
		BufferedImage[] images = {
			TestImageGenerator.createSolid(),
			TestImageGenerator.createGradient(),
			TestImageGenerator.createEdges(),
			TestImageGenerator.createNature(),
		};
		String[] names = {"solid", "gradient", "edges", "nature"};
		int maxShapes = 30;

		float totalUniform = 0, totalGuided = 0;
		for (int idx = 0; idx < images.length; idx++) {
			Model uniformModel = runGenerator(images[idx], maxShapes, false);
			Model guidedModel = runGenerator(images[idx], maxShapes, true);
			totalUniform += uniformModel.score;
			totalGuided += guidedModel.score;
			System.out.println(names[idx] + " — Uniform: " + uniformModel.score + ", Guided: " + guidedModel.score);
		}

		// Check aggregate rather than per-image to reduce stochastic flakiness
		// (small shape counts on small images produce high variance)
		System.out.println("Aggregate — Uniform: " + totalUniform + ", Guided: " + totalGuided);
		assertTrue(totalGuided <= totalUniform * 1.10f,
			"Aggregate Guided (" + totalGuided + ") should not be significantly worse than aggregate Uniform (" + totalUniform + ")");
	}

	// ---- Test: ErrorMap correctness ----

	@Test
	void testErrorMapBasicCorrectness() {
		BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.RED);
		g.fillRect(0, 0, 32, 64); // left half red
		g.setColor(Color.WHITE);
		g.fillRect(32, 0, 32, 64); // right half white
		g.dispose();

		BorstImage target = new BorstImage(ensureArgb(img));
		BorstImage current = new BorstImage(64, 64);
		Arrays.fill(current.pixels, 0xFFFFFFFF); // all white

		ErrorMap map = new ErrorMap(64, 64, 2, 1); // 2 columns, 1 row
		map.computeFull(target, current);

		// Left cells should have much higher error (red vs white)
		// Right cells should have near-zero error (white vs white)
		float leftError = map.cellErrors[0];
		float rightError = map.cellErrors[1];

		assertTrue(leftError > rightError * 10,
			"Left (red vs white) error (" + leftError + ") should be much higher than right (white vs white) error (" + rightError + ")");
	}

	@Test
	void testErrorMapSamplingBias() {
		// Create an image that is white everywhere except a small red patch
		BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 128, 128);
		g.setColor(Color.RED);
		g.fillRect(0, 0, 32, 32); // top-left corner is red
		g.dispose();

		BorstImage target = new BorstImage(ensureArgb(img));
		BorstImage current = new BorstImage(128, 128);
		Arrays.fill(current.pixels, 0xFFFFFFFF);

		ErrorMap map = new ErrorMap(128, 128);
		map.computeFull(target, current);

		// Sample many positions and verify bias toward top-left
		java.util.Random rnd = new java.util.Random(42);
		int topLeftCount = 0;
		int totalSamples = 10000;
		for (int i = 0; i < totalSamples; i++) {
			int[] pos = map.samplePosition(rnd);
			if (pos[0] < 32 && pos[1] < 32) {
				topLeftCount++;
			}
		}

		// The top-left quadrant is 1/16 of the image area but has almost all the error.
		// With error-guided sampling, it should receive >50% of samples.
		float topLeftFraction = topLeftCount / (float) totalSamples;
		assertTrue(topLeftFraction > 0.5f,
			"Error-guided sampling should heavily favor the high-error region, but only " +
			(topLeftFraction * 100) + "% of samples hit the top-left corner");
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
			Model guidedModel = runGenerator(targetImg, maxShapes, true);

			// Save rendered results
			BufferedImage uniformResult = toBufferedImage(uniformModel.current);
			BufferedImage guidedResult = toBufferedImage(guidedModel.current);
			ImageIO.write(uniformResult, "png", new File(OUTPUT_DIR, name + "_uniform_200shapes.png"));
			ImageIO.write(guidedResult, "png", new File(OUTPUT_DIR, name + "_guided_200shapes.png"));

			// Generate difference heatmap between the two results
			BufferedImage diffImage = generateDiffHeatmap(uniformResult, guidedResult);
			ImageIO.write(diffImage, "png", new File(OUTPUT_DIR, name + "_diff.png"));

			System.out.println("  Uniform score: " + uniformModel.score);
			System.out.println("  Guided score:  " + guidedModel.score);
			float improvement = (uniformModel.score - guidedModel.score) / uniformModel.score * 100;
			System.out.println("  Improvement:   " + improvement + "%");
		}
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

	/**
	 * Generate a heatmap showing absolute difference between two images.
	 * Brighter = more different.
	 */
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

				// Scale up for visibility and map to a heat color
				int intensity = Math.min(255, (dr + dg + db) * 2);
				int heatR = Math.min(255, intensity * 2);
				int heatG = Math.max(0, 255 - intensity * 2);
				int heatB = 0;

				diff.setRGB(x, y, 0xFF000000 | (heatR << 16) | (heatG << 8) | heatB);
			}
		}
		return diff;
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

	private static void setErrorMap(Model model, ErrorMap errorMap) {
		try {
			Field field = Model.class.getDeclaredField("errorMap");
			field.setAccessible(true);
			field.set(model, errorMap);
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
