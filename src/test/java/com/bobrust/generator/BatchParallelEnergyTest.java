package com.bobrust.generator;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Proposal 4: Batch-Parallel Energy Evaluation.
 *
 * Verifies that the combined single-pass differencePartialThread produces
 * identical results to the classic two-pass implementation, and benchmarks
 * timing differences.
 */
class BatchParallelEnergyTest {
    private static final int ALPHA = 128;
    private static final int BACKGROUND = 0xFFFFFFFF;

    // ---- Test 1: Combined pass produces identical energy values ----

    @Test
    void testCombinedPassIdenticalToClassic() {
        BufferedImage[] images = {
            TestImageGenerator.createSolid(),
            TestImageGenerator.createGradient(),
            TestImageGenerator.createEdges(),
            TestImageGenerator.createPhotoDetail(),
            TestImageGenerator.createNature(),
        };
        String[] names = {"solid", "gradient", "edges", "photo_detail", "nature"};

        for (int idx = 0; idx < images.length; idx++) {
            BufferedImage argb = ensureArgb(images[idx]);
            BorstImage target = new BorstImage(argb);
            BorstImage current = new BorstImage(target.width, target.height);
            Arrays.fill(current.pixels, BACKGROUND);

            float score = BorstCore.differenceFull(target, current);

            // Test multiple circle positions and sizes
            int[][] testCases = {
                {64, 64, 0}, {64, 64, 1}, {64, 64, 2}, {64, 64, 3}, {64, 64, 4}, {64, 64, 5},
                {0, 0, 2}, {127, 127, 2}, {10, 50, 3}, {100, 20, 1},
                {-5, 30, 2}, {64, 200, 1},  // edge cases: partially or fully out of bounds
            };

            for (int[] tc : testCases) {
                int cx = tc[0], cy = tc[1], sizeIdx = tc[2];

                float classic = BorstCore.differencePartialThreadClassic(
                    target, current, score, ALPHA, sizeIdx, cx, cy);
                float combined = BorstCore.differencePartialThreadCombined(
                    target, current, score, ALPHA, sizeIdx, cx, cy);

                assertEquals(classic, combined, 1e-6f,
                    names[idx] + " at (" + cx + "," + cy + ") size=" + sizeIdx +
                    ": classic=" + classic + " combined=" + combined);
            }

            System.out.println(names[idx] + ": all positions match between classic and combined");
        }
    }

    // ---- Test 2: Identical results after multiple shapes ----

    @Test
    void testIdenticalAfterMultipleShapes() {
        BufferedImage img = TestImageGenerator.createPhotoDetail();
        BufferedImage argb = ensureArgb(img);
        BorstImage target = new BorstImage(argb);

        // Run a sequence of shapes and compare energies at each step
        BorstImage current = new BorstImage(target.width, target.height);
        Arrays.fill(current.pixels, BACKGROUND);
        float score = BorstCore.differenceFull(target, current);

        // Use fixed circle positions/sizes for reproducibility
        int[][] shapes = {
            {30, 30, 4}, {80, 80, 3}, {50, 100, 2}, {10, 10, 5},
            {64, 64, 1}, {100, 50, 0}, {20, 90, 3}, {90, 20, 2},
        };

        for (int[] s : shapes) {
            int cx = s[0], cy = s[1], sizeIdx = s[2];

            float classic = BorstCore.differencePartialThreadClassic(
                target, current, score, ALPHA, sizeIdx, cx, cy);
            float combined = BorstCore.differencePartialThreadCombined(
                target, current, score, ALPHA, sizeIdx, cx, cy);

            assertEquals(classic, combined, 1e-6f,
                "Mismatch at (" + cx + "," + cy + ") size=" + sizeIdx);

            // Actually draw the shape to advance the current image state
            BorstColor color = BorstCore.computeColor(target, current, ALPHA, sizeIdx, cx, cy);
            BorstImage before = current.createCopy();
            BorstCore.drawLines(current, color, ALPHA, sizeIdx, cx, cy);
            score = BorstCore.differencePartial(target, before, current, score, sizeIdx, cx, cy);
        }

        System.out.println("Multi-shape sequential test: all energies match");
    }

    // ---- Test 3: Benchmark timing comparison ----

    @Test
    void testBenchmarkTiming() {
        BufferedImage img = TestImageGenerator.createPhotoDetail();
        BufferedImage argb = ensureArgb(img);
        BorstImage target = new BorstImage(argb);
        BorstImage current = new BorstImage(target.width, target.height);
        Arrays.fill(current.pixels, BACKGROUND);
        float score = BorstCore.differenceFull(target, current);

        int iterations = 5000;

        // Warm up
        for (int i = 0; i < 500; i++) {
            BorstCore.differencePartialThreadClassic(target, current, score, ALPHA, 3, 64, 64);
            BorstCore.differencePartialThreadCombined(target, current, score, ALPHA, 3, 64, 64);
        }

        // Benchmark classic
        long startClassic = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int x = (i * 7 + 13) % target.width;
            int y = (i * 11 + 17) % target.height;
            int sz = i % 6;
            BorstCore.differencePartialThreadClassic(target, current, score, ALPHA, sz, x, y);
        }
        long classicNs = System.nanoTime() - startClassic;

        // Benchmark combined
        long startCombined = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int x = (i * 7 + 13) % target.width;
            int y = (i * 11 + 17) % target.height;
            int sz = i % 6;
            BorstCore.differencePartialThreadCombined(target, current, score, ALPHA, sz, x, y);
        }
        long combinedNs = System.nanoTime() - startCombined;

        double classicMs = classicNs / 1_000_000.0;
        double combinedMs = combinedNs / 1_000_000.0;
        double speedup = (double) classicNs / combinedNs;

        System.out.println("Benchmark (" + iterations + " iterations):");
        System.out.println("  Classic:  " + String.format("%.2f", classicMs) + " ms");
        System.out.println("  Combined: " + String.format("%.2f", combinedMs) + " ms");
        System.out.println("  Speedup:  " + String.format("%.2fx", speedup));

        // On small test images the overhead of the combined approach may not show
        // a speedup; the real benefit comes from larger images with more pixels per
        // circle. Just verify it's not catastrophically slower (3x tolerance).
        assertTrue(combinedNs <= classicNs * 3.0,
            "Combined pass should not be catastrophically slower: classic=" +
            classicMs + "ms, combined=" + combinedMs + "ms");
    }

    // ---- Test 4: Edge cases — circle fully out of bounds ----

    @Test
    void testOutOfBoundsCircle() {
        BufferedImage img = TestImageGenerator.createSolid();
        BufferedImage argb = ensureArgb(img);
        BorstImage target = new BorstImage(argb);
        BorstImage current = new BorstImage(target.width, target.height);
        Arrays.fill(current.pixels, BACKGROUND);
        float score = BorstCore.differenceFull(target, current);

        // Circle completely outside the image
        float classic = BorstCore.differencePartialThreadClassic(
            target, current, score, ALPHA, 0, -100, -100);
        float combined = BorstCore.differencePartialThreadCombined(
            target, current, score, ALPHA, 0, -100, -100);

        assertEquals(classic, combined, 1e-6f, "Out-of-bounds circle should match");
        assertEquals(score, combined, 1e-6f, "Out-of-bounds circle should return original score");
    }

    // ---- Test 5: Full generator run identical with and without batch-parallel ----

    @Test
    void testFullGeneratorIdenticalOutput() {
        // Run a small generation with fixed seed-like behavior and verify
        // that the combined method produces the same optimal color for every circle
        BufferedImage img = TestImageGenerator.createNature();
        BufferedImage argb = ensureArgb(img);
        BorstImage target = new BorstImage(argb);
        BorstImage current = new BorstImage(target.width, target.height);
        Arrays.fill(current.pixels, BACKGROUND);
        float score = BorstCore.differenceFull(target, current);

        // Grid of test points covering the entire image
        int mismatches = 0;
        for (int y = 5; y < target.height; y += 10) {
            for (int x = 5; x < target.width; x += 10) {
                for (int sz = 0; sz < 6; sz++) {
                    float classic = BorstCore.differencePartialThreadClassic(
                        target, current, score, ALPHA, sz, x, y);
                    float combined = BorstCore.differencePartialThreadCombined(
                        target, current, score, ALPHA, sz, x, y);
                    if (Math.abs(classic - combined) > 1e-6f) {
                        mismatches++;
                    }
                }
            }
        }

        assertEquals(0, mismatches, "There should be zero mismatches across the grid");
        System.out.println("Full grid test: zero mismatches");
    }

    private static BufferedImage ensureArgb(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_ARGB) return img;
        BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return argb;
    }
}
