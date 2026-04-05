package com.bobrust.generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Proposal 6: Progressive Multi-Resolution Generation.
 *
 * Verifies that multi-resolution generation produces reasonable quality,
 * benchmarks timing vs single-resolution, and generates before/after
 * comparison images in test-results/proposal6/.
 */
class ProgressiveResolutionTest {
    private static final int ALPHA = 128;
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int MAX_SHAPES = 100;
    private static final File OUTPUT_DIR = new File("test-results/proposal6");

    @BeforeAll
    static void setup() {
        OUTPUT_DIR.mkdirs();
    }

    // ---- Test 1: Multi-res model produces valid output ----

    @Test
    void testMultiResModelProducesValidOutput() {
        BufferedImage img = TestImageGenerator.createPhotoDetail();
        BorstImage target = new BorstImage(ensureArgb(img));

        MultiResModel multiRes = new MultiResModel(target, BACKGROUND, ALPHA);

        for (int i = 0; i < MAX_SHAPES; i++) {
            multiRes.processStep(i, MAX_SHAPES);
        }

        Model fullModel = multiRes.getFullResModel();

        // Should have produced the expected number of shapes
        assertEquals(MAX_SHAPES, fullModel.shapes.size(),
            "Full-res model should have " + MAX_SHAPES + " shapes");

        // Score should have improved from initial
        float initialScore = BorstCore.differenceFull(target,
            createBackground(target.width, target.height));
        assertTrue(fullModel.getScore() < initialScore,
            "Score should improve: initial=" + initialScore + " final=" + fullModel.getScore());

        System.out.println("Multi-res final score: " + fullModel.getScore());
    }

    // ---- Test 2: Single-res vs multi-res quality comparison ----

    @Test
    void testQualityComparisonAndImages() throws IOException {
        String[] imageNames = {"photo_detail", "nature"};
        BufferedImage[] images = {
            TestImageGenerator.createPhotoDetail(),
            TestImageGenerator.createNature()
        };

        for (int idx = 0; idx < images.length; idx++) {
            BufferedImage img = ensureArgb(images[idx]);
            BorstImage target = new BorstImage(img);
            String name = imageNames[idx];

            // --- Single resolution ---
            Model singleRes = new Model(target, BACKGROUND, ALPHA);
            long singleStart = System.nanoTime();
            for (int i = 0; i < MAX_SHAPES; i++) {
                singleRes.processStep();
            }
            long singleTime = System.nanoTime() - singleStart;
            float singleScore = singleRes.getScore();

            // --- Multi resolution ---
            MultiResModel multiRes = new MultiResModel(target, BACKGROUND, ALPHA);
            long multiStart = System.nanoTime();
            for (int i = 0; i < MAX_SHAPES; i++) {
                multiRes.processStep(i, MAX_SHAPES);
            }
            long multiTime = System.nanoTime() - multiStart;
            float multiScore = multiRes.getFullResModel().getScore();

            double singleMs = singleTime / 1_000_000.0;
            double multiMs = multiTime / 1_000_000.0;
            double speedup = (double) singleTime / multiTime;

            System.out.println(name + ":");
            System.out.println("  Single-res: score=" + singleScore +
                " time=" + String.format("%.0f", singleMs) + "ms");
            System.out.println("  Multi-res:  score=" + multiScore +
                " time=" + String.format("%.0f", multiMs) + "ms");
            System.out.println("  Speedup:    " + String.format("%.2fx", speedup));

            // Save comparison images
            ImageIO.write(img, "png", new File(OUTPUT_DIR, name + "_target.png"));
            ImageIO.write(singleRes.current.image, "png",
                new File(OUTPUT_DIR, name + "_single_res.png"));
            ImageIO.write(multiRes.getFullResModel().current.image, "png",
                new File(OUTPUT_DIR, name + "_multi_res.png"));

            // Generate difference image (amplified 4x for visibility)
            BufferedImage diff = generateDiffImage(
                singleRes.current.image,
                multiRes.getFullResModel().current.image);
            ImageIO.write(diff, "png", new File(OUTPUT_DIR, name + "_diff.png"));

            // Multi-res quality should be within reasonable range of single-res
            // (allow up to 30% worse score since early shapes are optimized at lower resolution)
            assertTrue(multiScore <= singleScore * 1.30f,
                name + ": Multi-res score (" + multiScore +
                ") should not be dramatically worse than single-res (" + singleScore + ")");
        }
    }

    // ---- Test 3: Resolution level selection is correct ----

    @Test
    void testResolutionLevelSelection() {
        BufferedImage img = TestImageGenerator.createSolid();
        BorstImage target = new BorstImage(ensureArgb(img));

        MultiResModel multiRes = new MultiResModel(target, BACKGROUND, ALPHA);

        // Track shapes added at each level by checking model shape counts
        int level0Before = multiRes.getModel(0).shapes.size();
        int level1Before = multiRes.getModel(1).shapes.size();
        int level2Before = multiRes.getModel(2).shapes.size();

        int totalShapes = 100;
        // First 10 shapes should use level 2 (quarter)
        for (int i = 0; i < 10; i++) {
            multiRes.processStep(i, totalShapes);
        }

        // Level 2 should have generated 10 shapes
        assertEquals(10, multiRes.getModel(2).shapes.size() - level2Before,
            "Level 2 should have 10 shapes after first 10% of generation");

        // Next 30 shapes should use level 1 (half)
        for (int i = 10; i < 40; i++) {
            multiRes.processStep(i, totalShapes);
        }

        // Level 1 should have generated 30 new shapes (plus 10 propagated from level 2)
        int level1Shapes = multiRes.getModel(1).shapes.size() - level1Before;
        assertEquals(40, level1Shapes,
            "Level 1 should have 40 shapes (30 generated + 10 propagated)");

        // Remaining 60 shapes at full resolution
        for (int i = 40; i < totalShapes; i++) {
            multiRes.processStep(i, totalShapes);
        }

        // Full-res model should have all shapes
        assertEquals(totalShapes, multiRes.getModel(0).shapes.size() - level0Before,
            "Level 0 should have all " + totalShapes + " shapes");
    }

    // ---- Test 4: Circle scaling between levels ----

    @Test
    void testShapePropagation() {
        BufferedImage img = TestImageGenerator.createGradient();
        BorstImage target = new BorstImage(ensureArgb(img));

        MultiResModel multiRes = new MultiResModel(target, BACKGROUND, ALPHA);

        // Generate a single shape at quarter resolution
        multiRes.processStep(0, 100);

        // Shape should have been propagated to half-res and full-res
        assertTrue(multiRes.getModel(2).shapes.size() >= 1,
            "Quarter-res model should have at least 1 shape");
        assertTrue(multiRes.getModel(1).shapes.size() >= 1,
            "Half-res model should have at least 1 propagated shape");
        assertTrue(multiRes.getModel(0).shapes.size() >= 1,
            "Full-res model should have at least 1 propagated shape");
    }

    // ---- Test 5: Timing benchmark ----

    @Test
    void testTimingBenchmark() {
        BufferedImage img = TestImageGenerator.createPhotoDetail();
        BorstImage target = new BorstImage(ensureArgb(img));
        int shapes = 50;

        // Warm up
        Model warmup = new Model(target, BACKGROUND, ALPHA);
        for (int i = 0; i < 10; i++) warmup.processStep();

        // Single resolution
        Model singleRes = new Model(target, BACKGROUND, ALPHA);
        long singleStart = System.nanoTime();
        for (int i = 0; i < shapes; i++) singleRes.processStep();
        long singleNs = System.nanoTime() - singleStart;

        // Multi resolution
        MultiResModel multiRes = new MultiResModel(target, BACKGROUND, ALPHA);
        long multiStart = System.nanoTime();
        for (int i = 0; i < shapes; i++) multiRes.processStep(i, shapes);
        long multiNs = System.nanoTime() - multiStart;

        System.out.println("Timing (" + shapes + " shapes):");
        System.out.println("  Single: " + String.format("%.0f", singleNs / 1e6) + " ms");
        System.out.println("  Multi:  " + String.format("%.0f", multiNs / 1e6) + " ms");
        System.out.println("  Ratio:  " + String.format("%.2fx", (double) singleNs / multiNs));

        // Multi-res should not be catastrophically slower (overhead of managing 3 models)
        assertTrue(multiNs < singleNs * 3,
            "Multi-res should not be more than 3x slower than single-res");
    }

    // ---- Helpers ----

    private static BufferedImage ensureArgb(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
        BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return argb;
    }

    private static BorstImage createBackground(int w, int h) {
        BorstImage bg = new BorstImage(w, h);
        Arrays.fill(bg.pixels, BACKGROUND);
        return bg;
    }

    /**
     * Generate a difference image (amplified 4x) between two images.
     */
    private static BufferedImage generateDiffImage(BufferedImage a, BufferedImage b) {
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

                // Amplify 4x
                dr = Math.min(255, dr * 4);
                dg = Math.min(255, dg * 4);
                db = Math.min(255, db * 4);

                diff.setRGB(x, y, 0xFF000000 | (dr << 16) | (dg << 8) | db);
            }
        }

        return diff;
    }
}
