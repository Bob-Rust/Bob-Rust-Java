package com.bobrust.generator;

import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.generator.sorter.TwoOptOptimizer;
import com.bobrust.util.data.AppConstants;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Proposal 5: Paint Order Optimization with 2-opt TSP heuristic.
 *
 * Verifies that 2-opt reduces total cost vs. greedy-only ordering,
 * preserves the same set of shapes (no shapes lost or duplicated),
 * and runs in acceptable time.
 */
class TwoOptOptimizerTest {
    private static final int CANVAS_SIZE = 512;

    // ---- Test 1: 2-opt reduces or maintains total cost vs greedy ----

    @Test
    void testTwoOptReducesCost() {
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);
        Random rnd = new Random(42);

        // Generate a set of random blobs
        BlobList input = generateRandomBlobs(rnd, 200);

        // Sort with greedy (BorstSorter)
        BlobList greedy = BorstSorter.sort(input, CANVAS_SIZE);

        // Extract greedy order, compute cost before 2-opt
        Blob[] greedyArray = greedy.getList().toArray(new Blob[0]);
        double greedyCost = optimizer.totalCost(greedyArray);

        // Apply 2-opt
        Blob[] optimized = greedyArray.clone();
        optimizer.optimize(optimized);
        double optimizedCost = optimizer.totalCost(optimized);

        System.out.println("Greedy cost:    " + String.format("%.4f", greedyCost));
        System.out.println("2-opt cost:     " + String.format("%.4f", optimizedCost));
        System.out.println("Improvement:    " + String.format("%.2f%%",
            (greedyCost - optimizedCost) / greedyCost * 100));

        // 2-opt should not increase cost
        assertTrue(optimizedCost <= greedyCost + 1e-6,
            "2-opt should not increase cost: greedy=" + greedyCost + " optimized=" + optimizedCost);
    }

    // ---- Test 2: All shapes are preserved (no duplicates, no losses) ----

    @Test
    void testAllShapesPreserved() {
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);
        Random rnd = new Random(123);

        BlobList input = generateRandomBlobs(rnd, 100);
        BlobList greedy = BorstSorter.sort(input, CANVAS_SIZE);

        Blob[] before = greedy.getList().toArray(new Blob[0]);
        Blob[] after = before.clone();
        optimizer.optimize(after);

        assertEquals(before.length, after.length, "Same number of blobs");

        // Count occurrences by hashCode
        java.util.Map<Integer, Integer> beforeCounts = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> afterCounts = new java.util.HashMap<>();
        for (Blob b : before) beforeCounts.merge(b.hashCode(), 1, Integer::sum);
        for (Blob b : after) afterCounts.merge(b.hashCode(), 1, Integer::sum);

        assertEquals(beforeCounts, afterCounts, "Same blobs before and after 2-opt");
    }

    // ---- Test 3: Cost function correctness ----

    @Test
    void testCostFunction() {
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);

        // Two identical blobs at the same position: cost should be 0
        Blob a = Blob.of(100, 100, 12, BorstUtils.COLORS[5].rgb, 128, AppConstants.CIRCLE_SHAPE);
        Blob same = Blob.of(100, 100, 12, BorstUtils.COLORS[5].rgb, 128, AppConstants.CIRCLE_SHAPE);
        double costSame = optimizer.cost(a, same);
        assertEquals(0.0, costSame, 1e-6, "Same blob should have zero cost");

        // Different color only
        Blob diffColor = Blob.of(100, 100, 12, BorstUtils.COLORS[10].rgb, 128, AppConstants.CIRCLE_SHAPE);
        double costDiffColor = optimizer.cost(a, diffColor);
        assertTrue(costDiffColor > 0, "Different color should have positive cost");

        // Different position only (same attributes)
        Blob diffPos = Blob.of(400, 400, 12, BorstUtils.COLORS[5].rgb, 128, AppConstants.CIRCLE_SHAPE);
        double costDiffPos = optimizer.cost(a, diffPos);
        assertTrue(costDiffPos > 0, "Different position should have positive cost");

        // Different everything: should have higher cost than just one difference
        Blob diffAll = Blob.of(400, 400, 50, BorstUtils.COLORS[10].rgb, 255, AppConstants.SQUARE_SHAPE);
        double costDiffAll = optimizer.cost(a, diffAll);
        assertTrue(costDiffAll > costDiffColor, "All-different should cost more than color-only");
        assertTrue(costDiffAll > costDiffPos, "All-different should cost more than position-only");
    }

    // ---- Test 4: Palette change counting ----

    @Test
    void testPaletteChangeCount() {
        Blob a = Blob.of(50, 50, 12, BorstUtils.COLORS[0].rgb, 128, AppConstants.CIRCLE_SHAPE);

        // Same everything
        Blob same = Blob.of(50, 50, 12, BorstUtils.COLORS[0].rgb, 128, AppConstants.CIRCLE_SHAPE);
        assertEquals(0, TwoOptOptimizer.countPaletteChanges(a, same));

        // Different size
        Blob diffSize = Blob.of(50, 50, 50, BorstUtils.COLORS[0].rgb, 128, AppConstants.CIRCLE_SHAPE);
        assertEquals(1, TwoOptOptimizer.countPaletteChanges(a, diffSize));

        // Different color + size
        Blob diffTwo = Blob.of(50, 50, 50, BorstUtils.COLORS[5].rgb, 128, AppConstants.CIRCLE_SHAPE);
        assertEquals(2, TwoOptOptimizer.countPaletteChanges(a, diffTwo));

        // All different
        Blob diffAll = Blob.of(50, 50, 50, BorstUtils.COLORS[5].rgb, 255, AppConstants.SQUARE_SHAPE);
        assertEquals(4, TwoOptOptimizer.countPaletteChanges(a, diffAll));
    }

    // ---- Test 5: Edge cases ----

    @Test
    void testEdgeCases() {
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);

        // Empty list
        BlobList empty = new BlobList();
        Blob[] emptyArr = new Blob[0];
        assertEquals(0.0, optimizer.totalCost(emptyArr));

        // Single blob
        Blob[] single = { Blob.of(50, 50, 12, 0, 128, AppConstants.CIRCLE_SHAPE) };
        assertEquals(0.0, optimizer.totalCost(single));
        optimizer.optimize(single); // should not crash

        // Two blobs
        Blob[] two = {
            Blob.of(50, 50, 12, 0, 128, AppConstants.CIRCLE_SHAPE),
            Blob.of(200, 200, 50, BorstUtils.COLORS[10].rgb, 255, AppConstants.CIRCLE_SHAPE)
        };
        double cost2 = optimizer.totalCost(two);
        assertTrue(cost2 > 0);
        optimizer.optimize(two); // should not crash
    }

    // ---- Test 6: Performance — optimization runs within time budget ----

    @Test
    void testOptimizationPerformance() {
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);
        Random rnd = new Random(999);

        // Test with a moderate number of blobs
        BlobList input = generateRandomBlobs(rnd, 500);
        BlobList greedy = BorstSorter.sort(input, CANVAS_SIZE);
        Blob[] blobs = greedy.getList().toArray(new Blob[0]);

        long start = System.nanoTime();
        optimizer.optimize(blobs);
        long elapsed = System.nanoTime() - start;
        double elapsedMs = elapsed / 1_000_000.0;

        System.out.println("2-opt on 500 blobs: " + String.format("%.2f", elapsedMs) + " ms");

        // Should complete within 5 seconds (generous bound for CI environments)
        assertTrue(elapsedMs < 5000, "2-opt should complete within 5s, took " + elapsedMs + "ms");
    }

    // ---- Test 7: Integration with BorstSorter (end-to-end) ----

    @Test
    void testIntegrationWithBorstSorter() {
        Random rnd = new Random(777);
        BlobList input = generateRandomBlobs(rnd, 100);

        // BorstSorter.sort should now include 2-opt when USE_TSP_OPTIMIZATION is true
        BlobList sorted = BorstSorter.sort(input, CANVAS_SIZE);

        // Basic sanity: same number of blobs
        assertEquals(input.size(), sorted.size(), "Sorted list should have same size as input");

        // Compute cost
        TwoOptOptimizer optimizer = new TwoOptOptimizer(CANVAS_SIZE, CANVAS_SIZE);
        Blob[] sortedArr = sorted.getList().toArray(new Blob[0]);
        double cost = optimizer.totalCost(sortedArr);
        System.out.println("End-to-end sorted cost: " + String.format("%.4f", cost));

        // Cost should be finite and non-negative
        assertTrue(cost >= 0 && Double.isFinite(cost));
    }

    // ---- Helper: generate random blobs ----

    private static BlobList generateRandomBlobs(Random rnd, int count) {
        BlobList list = new BlobList();
        for (int i = 0; i < count; i++) {
            int x = rnd.nextInt(CANVAS_SIZE);
            int y = rnd.nextInt(CANVAS_SIZE);
            int sizeIdx = rnd.nextInt(BorstUtils.SIZES.length);
            int colorIdx = rnd.nextInt(BorstUtils.COLORS.length);
            int alphaIdx = rnd.nextInt(BorstUtils.ALPHAS.length);
            int shape = rnd.nextBoolean() ? AppConstants.CIRCLE_SHAPE : AppConstants.SQUARE_SHAPE;
            list.add(Blob.of(x, y, BorstUtils.SIZES[sizeIdx],
                BorstUtils.COLORS[colorIdx].rgb, BorstUtils.ALPHAS[alphaIdx], shape));
        }
        return list;
    }
}
