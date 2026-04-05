package com.bobrust.generator.sorter;

import com.bobrust.util.data.AppConstants;

/**
 * 2-opt local search optimizer for paint ordering.
 *
 * Takes the greedy output from {@link BorstSorter} and applies 2-opt
 * improvements to reduce total cost, which is a weighted sum of palette
 * changes and Euclidean cursor travel distance.
 *
 * The cost function:
 *   cost(a, b) = W_palette * paletteChanges(a, b) + W_distance * normalizedDistance(a, b)
 */
public class TwoOptOptimizer {

    /**
     * The maximum Euclidean distance used for normalization.
     * For a typical sign this is the diagonal of the canvas.
     */
    private final float maxDistance;

    public TwoOptOptimizer(int canvasWidth, int canvasHeight) {
        this.maxDistance = (float) Math.sqrt(
            (double) canvasWidth * canvasWidth + (double) canvasHeight * canvasHeight);
    }

    /**
     * Compute the cost of transitioning from blob a to blob b.
     */
    public double cost(Blob a, Blob b) {
        int paletteChanges = countPaletteChanges(a, b);
        float distance = euclideanDistance(a, b);
        float normalizedDist = (maxDistance > 0) ? distance / maxDistance : 0;

        return AppConstants.TSP_W_PALETTE * paletteChanges
             + AppConstants.TSP_W_DISTANCE * normalizedDist;
    }

    /**
     * Count the number of palette interaction changes between two consecutive blobs.
     * Each differing attribute (size, color, alpha, shape) requires a click action.
     */
    public static int countPaletteChanges(Blob a, Blob b) {
        int changes = 0;
        if (a.sizeIndex != b.sizeIndex) changes++;
        if (a.colorIndex != b.colorIndex) changes++;
        if (a.alphaIndex != b.alphaIndex) changes++;
        if (a.shapeIndex != b.shapeIndex) changes++;
        return changes;
    }

    /**
     * Euclidean distance between the centers of two blobs.
     */
    public static float euclideanDistance(Blob a, Blob b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return (float) Math.sqrt((double) dx * dx + (double) dy * dy);
    }

    /**
     * Compute the total route cost for an ordered array of blobs.
     */
    public double totalCost(Blob[] blobs) {
        if (blobs.length <= 1) return 0;
        double total = 0;
        for (int i = 0; i < blobs.length - 1; i++) {
            total += cost(blobs[i], blobs[i + 1]);
        }
        return total;
    }

    /**
     * Apply 2-opt local search to improve the ordering.
     * For each pair of edges (i, i+1) and (j, j+1), checks if reversing
     * the segment [i+1..j] reduces total cost.
     *
     * @param blobs the ordered blob array to optimize in-place
     * @return the optimized array (same reference)
     */
    public Blob[] optimize(Blob[] blobs) {
        if (blobs.length <= 3) return blobs;

        boolean improved = true;
        int maxIterations = 100; // Safety limit to prevent excessive optimization time
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;

            for (int i = 0; i < blobs.length - 2; i++) {
                for (int j = i + 2; j < blobs.length - 1; j++) {
                    // Current edges: (i, i+1) and (j, j+1)
                    // Proposed: reverse segment [i+1..j]
                    // New edges: (i, j) and (i+1, j+1)
                    double oldCost = cost(blobs[i], blobs[i + 1])
                                   + cost(blobs[j], blobs[j + 1]);
                    double newCost = cost(blobs[i], blobs[j])
                                   + cost(blobs[i + 1], blobs[j + 1]);

                    if (newCost < oldCost - 1e-10) {
                        // Reverse the segment [i+1..j]
                        reverse(blobs, i + 1, j);
                        improved = true;
                    }
                }
            }
        }

        return blobs;
    }

    /**
     * Apply 2-opt optimization to a BlobList and return a new optimized BlobList.
     */
    public BlobList optimize(BlobList sorted) {
        Blob[] blobs = sorted.getList().toArray(new Blob[0]);
        optimize(blobs);
        BlobList result = new BlobList();
        for (Blob b : blobs) {
            result.add(b);
        }
        return result;
    }

    /**
     * Reverse the sub-array blobs[start..end] inclusive.
     */
    private static void reverse(Blob[] blobs, int start, int end) {
        while (start < end) {
            Blob temp = blobs[start];
            blobs[start] = blobs[end];
            blobs[end] = temp;
            start++;
            end--;
        }
    }
}
