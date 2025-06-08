package com.bobrust.generator;

import com.bobrust.robot.BobRustPainter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class CircleCache {
	private static final Logger LOGGER = LogManager.getLogger(BobRustPainter.class);

	private static final Scanline[] CIRCLE_0;
	private static final Scanline[] CIRCLE_1;
	private static final Scanline[] CIRCLE_2;
	private static final Scanline[] CIRCLE_3;
	private static final Scanline[] CIRCLE_4;
	private static final Scanline[] CIRCLE_5;
	
	public static final Scanline[][] CIRCLE_CACHE;
	public static final int[] CIRCLE_CACHE_LENGTH;

	// Default circle values
    public static final int DEFAULT_CIRCLE_0_VALUE = 3;
    public static final int DEFAULT_CIRCLE_1_VALUE = 6;
    public static final int DEFAULT_CIRCLE_2_VALUE = 12;
    public static final int DEFAULT_CIRCLE_3_VALUE = 25;
    public static final int DEFAULT_CIRCLE_4_VALUE = 50;
    public static final int DEFAULT_CIRCLE_5_VALUE = 100;

    static {
		CIRCLE_0 = generateCircle(DEFAULT_CIRCLE_0_VALUE);
		CIRCLE_1 = generateCircle(DEFAULT_CIRCLE_1_VALUE);
		CIRCLE_2 = generateCircle(DEFAULT_CIRCLE_2_VALUE);
		CIRCLE_3 = generateCircle(DEFAULT_CIRCLE_3_VALUE);
		CIRCLE_4 = generateCircle(DEFAULT_CIRCLE_4_VALUE);
		CIRCLE_5 = generateCircle(DEFAULT_CIRCLE_5_VALUE);
		CIRCLE_CACHE = new Scanline[][] {
			CIRCLE_0, CIRCLE_1, CIRCLE_2, CIRCLE_3, CIRCLE_4, CIRCLE_5
        };
		CIRCLE_CACHE_LENGTH = new int[CIRCLE_CACHE.length];
		for (int i = 0; i < CIRCLE_CACHE.length; i++) {
			CIRCLE_CACHE_LENGTH[i] = CIRCLE_CACHE[i].length;
		}
	}

	private static Scanline[] generateCircle(int size) {
		LOGGER.info("circle size " +size);
		boolean[] grid = new boolean[size * size];
		for (int i = 0; i < size * size; i++) {
			double px = (int) (i % size) + 0.5;
			double py = (int) (i / size) + 0.5;
			double x = (px / (double) size) * 2.0 - 1;
			double y = (py / (double) size) * 2.0 - 1;

			double magnitudeSqr = x * x + y * y;
			grid[i] = magnitudeSqr <= 1;
		}

		Scanline[] scanlines = new Scanline[size];
		for (int i = 0; i < size; i++) {
			int start = size;
			int end = 0;
			for (int j = 0; j < size; j++) {
				if (grid[i * size + j]) {
					start = Math.min(start, j);
					end = Math.max(end, j);
				}
			}

			if (start <= end) {
				int off = size / 2;
				scanlines[i] = new Scanline(i - off, start - off, end - off);
			}
		}
		return scanlines;
	}
}
