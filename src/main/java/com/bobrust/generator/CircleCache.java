package com.bobrust.generator;

class CircleCache {	
	private static final Scanline[] CIRCLE_0 = { // 1
		new Scanline(0, 0, 0)
	};
	
	private static final Scanline[] CIRCLE_1 = { // 3
		new Scanline(-1, -1, 1),
		new Scanline(0, -1, 1),
		new Scanline(1, -1, 1)
	};
	
	private static final Scanline[] CIRCLE_2 = { // 7
		new Scanline(-3, -1, 1),
		new Scanline(-2, -2, 2),
		new Scanline(-1, -3, 3),
		new Scanline(0, -3, 3),
		new Scanline(1, -3, 3),
		new Scanline(2, -2, 2),
		new Scanline(3, -1, 1)
	};
	
	private static final Scanline[] CIRCLE_3 = { // 11
		new Scanline(-5, -2, 2),
		new Scanline(-4, -3, 3),
		new Scanline(-3, -4, 4),
		new Scanline(-2, -5, 5),
		new Scanline(-1, -5, 5),
		new Scanline(0, -5, 5),
		new Scanline(1, -5, 5),
		new Scanline(2, -5, 5),
		new Scanline(3, -4, 4),
		new Scanline(4, -3, 3),
		new Scanline(5, -2, 2)
	};
	
	private static final Scanline[] CIRCLE_4 = { // 19
		new Scanline(-9, -3, 3),
		new Scanline(-8, -5, 5),
		new Scanline(-7, -6, 6),
		new Scanline(-6, -7, 7),
		new Scanline(-5, -8, 8),
		new Scanline(-4, -8, 8),
		new Scanline(-3, -9, 9),
		new Scanline(-2, -9, 9),
		new Scanline(-1, -9, 9),
		new Scanline(0, -9, 9),
		new Scanline(1, -9, 9),
		new Scanline(2, -9, 9),
		new Scanline(3, -9, 9),
		new Scanline(4, -8, 8),
		new Scanline(5, -8, 8),
		new Scanline(6, -7, 7),
		new Scanline(7, -6, 6),
		new Scanline(8, -5, 5),
		new Scanline(9, -3, 3)
	};
	
	private static final Scanline[] CIRCLE_5 = { // 25
		new Scanline(-12, -3, 3),
		new Scanline(-11, -5, 5),
		new Scanline(-10, -7, 7),
		new Scanline(-9, -8, 8),
		new Scanline(-8, -9, 9),
		new Scanline(-7, -10, 10),
		new Scanline(-6, -10, 10),
		new Scanline(-5, -11, 11),
		new Scanline(-4, -11, 11),
		new Scanline(-3, -12, 12),
		new Scanline(-2, -12, 12),
		new Scanline(-1, -12, 12),
		new Scanline(0, -12, 12),
		new Scanline(1, -12, 12),
		new Scanline(2, -12, 12),
		new Scanline(3, -12, 12),
		new Scanline(4, -11, 11),
		new Scanline(5, -11, 11),
		new Scanline(6, -10, 10),
		new Scanline(7, -10, 10),
		new Scanline(8, -9, 9),
		new Scanline(9, -8, 8),
		new Scanline(10, -7, 7),
		new Scanline(11, -5, 5),
		new Scanline(12, -3, 3)
	};
	
	public static final int[] CIRCLE_CACHE_LENGTH = { 1, 3, 7, 11, 19, 25 };
	public static final Scanline[][] CIRCLE_CACHE = { CIRCLE_0, CIRCLE_1, CIRCLE_2, CIRCLE_3, CIRCLE_4, CIRCLE_5 };
}
