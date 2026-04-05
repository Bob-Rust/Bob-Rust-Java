package com.bobrust.generator;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Progressive multi-resolution model for shape generation.
 *
 * Uses a resolution pyramid:
 * <ul>
 *   <li>Level 2: quarter resolution (first 10% of shapes)</li>
 *   <li>Level 1: half resolution (next 30% of shapes)</li>
 *   <li>Level 0: full resolution (remaining 60% of shapes)</li>
 * </ul>
 *
 * Shapes generated at lower resolutions are scaled and propagated to all
 * finer resolution levels, so the full-resolution model stays in sync.
 */
public class MultiResModel {
    /** Models at each resolution level: [0]=full, [1]=half, [2]=quarter */
    private final Model[] levels;

    /** Dimensions at each level */
    private final int[][] dims;

    /** The full-resolution target image */
    private final BorstImage fullTarget;

    private final int backgroundRGB;
    private final int alpha;
    private int shapesAdded;

    /**
     * Create a multi-resolution model.
     *
     * @param target      the full-resolution target image
     * @param backgroundRGB background color
     * @param alpha       alpha value for blending
     */
    public MultiResModel(BorstImage target, int backgroundRGB, int alpha) {
        this.fullTarget = target;
        this.backgroundRGB = backgroundRGB;
        this.alpha = alpha;
        this.shapesAdded = 0;

        int fw = target.width;
        int fh = target.height;

        dims = new int[][] {
            { fw, fh },                              // Level 0: full
            { Math.max(1, fw / 2), Math.max(1, fh / 2) },  // Level 1: half
            { Math.max(1, fw / 4), Math.max(1, fh / 4) },  // Level 2: quarter
        };

        levels = new Model[3];
        levels[0] = new Model(target, backgroundRGB, alpha);
        levels[1] = new Model(scaleImage(target, dims[1][0], dims[1][1]), backgroundRGB, alpha);
        levels[2] = new Model(scaleImage(target, dims[2][0], dims[2][1]), backgroundRGB, alpha);
    }

    /**
     * Process one shape at the appropriate resolution level.
     *
     * @param currentShape current shape index (0-based)
     * @param maxShapes    total number of shapes to generate
     * @return the counter from the worker (number of energy evaluations)
     */
    public int processStep(int currentShape, int maxShapes) {
        float progress = (float) currentShape / maxShapes;
        int level;
        if (progress < 0.10f) {
            level = 2; // Quarter resolution
        } else if (progress < 0.40f) {
            level = 1; // Half resolution
        } else {
            level = 0; // Full resolution
        }

        // Run generation at selected level
        int n = levels[level].processStep();

        // Get the shape that was just added
        Circle shape = levels[level].shapes.get(levels[level].shapes.size() - 1);

        // Propagate the shape to all finer levels
        for (int i = level - 1; i >= 0; i--) {
            Circle scaled = scaleCircle(shape, level, i);
            levels[i].addExternalShape(scaled);
        }

        shapesAdded++;
        return n;
    }

    /**
     * Scale a circle from one resolution level to another.
     */
    private Circle scaleCircle(Circle shape, int fromLevel, int toLevel) {
        float scaleX = (float) dims[toLevel][0] / dims[fromLevel][0];
        float scaleY = (float) dims[toLevel][1] / dims[fromLevel][1];

        int newX = BorstUtils.clampInt(Math.round(shape.x * scaleX), 0, dims[toLevel][0] - 1);
        int newY = BorstUtils.clampInt(Math.round(shape.y * scaleY), 0, dims[toLevel][1] - 1);

        // Scale the radius and snap to nearest valid size
        int scaledR = Math.round(shape.r * scaleX);
        int newR = BorstUtils.getClosestSize(scaledR);

        // Create a new circle in the target level's worker
        // We need to access the worker through the model
        return new Circle(getWorker(levels[toLevel]), newX, newY, newR);
    }

    /**
     * Get the full-resolution model (level 0).
     */
    public Model getFullResModel() {
        return levels[0];
    }

    /**
     * Get the model at a specific level.
     */
    public Model getModel(int level) {
        return levels[level];
    }

    /**
     * Get the number of shapes added so far.
     */
    public int getShapesAdded() {
        return shapesAdded;
    }

    /**
     * Scale a BorstImage to a new size.
     */
    private static BorstImage scaleImage(BorstImage source, int newWidth, int newHeight) {
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source.image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return new BorstImage(scaled);
    }

    /**
     * Get the Worker from a Model via package-private accessor.
     */
    private static Worker getWorker(Model model) {
        return model.getWorker();
    }
}
