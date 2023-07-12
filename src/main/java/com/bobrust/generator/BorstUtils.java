package com.bobrust.generator;

public class BorstUtils {
	// 9, 19, 60, 84, 98, 100
	// 23, 48, 153, 214, 250, 255
	// public static final int[] ALPHAS = { 9, 18, 72, 136, 218, 255 };
	// public static final int[] ALPHAS = { 23, 48, 153, 214, 250, 255 };
	public static final int[] ALPHAS = { 23, 48, 140, 190, 230, 255 };
	public static final int[] SIZES = CircleCache.CIRCLE_CACHE_LENGTH; // { 1, 2, 4, 6, 10, 13 };
	
	public static final BorstColor[] COLORS = {
		new BorstColor(0, 0, 0),
		new BorstColor(52, 33, 21),
		new BorstColor(52, 45, 21),
		new BorstColor(46, 51, 21),
		new BorstColor(33, 51, 21),
		new BorstColor(21, 51, 21),
		new BorstColor(21, 51, 33),
		new BorstColor(21, 51, 46),
		new BorstColor(21, 45, 52),
		new BorstColor(21, 33, 52),
		new BorstColor(21, 20, 52),
		new BorstColor(33, 20, 52),
		new BorstColor(46, 20, 52),
		new BorstColor(52, 20, 46),
		new BorstColor(52, 20, 33),
		new BorstColor(52, 20, 21),
		new BorstColor(115, 116, 115),
		new BorstColor(102, 65, 41),
		new BorstColor(102, 90, 41),
		new BorstColor(90, 102, 41),
		new BorstColor(66, 102, 41),
		new BorstColor(41, 102, 41),
		new BorstColor(41, 102, 66),
		new BorstColor(41, 102, 90),
		new BorstColor(41, 90, 102),
		new BorstColor(41, 65, 102),
		new BorstColor(41, 41, 102),
		new BorstColor(66, 41, 102),
		new BorstColor(90, 41, 102),
		new BorstColor(102, 41, 90),
		new BorstColor(102, 41, 66),
		new BorstColor(102, 41, 41),
		new BorstColor(192, 192, 192),
		new BorstColor(255, 134, 52),
		new BorstColor(255, 215, 52),
		new BorstColor(214, 255, 52),
		new BorstColor(132, 255, 52),
		new BorstColor(52, 255, 52),
		new BorstColor(52, 255, 132),
		new BorstColor(52, 255, 214),
		new BorstColor(52, 215, 255),
		new BorstColor(52, 134, 255),
		new BorstColor(52, 51, 255),
		new BorstColor(132, 51, 255),
		new BorstColor(214, 51, 255),
		new BorstColor(255, 51, 214),
		new BorstColor(255, 51, 132),
		new BorstColor(255, 51, 52),
		new BorstColor(255, 255, 255),
		new BorstColor(255, 178, 126),
		new BorstColor(255, 230, 126),
		new BorstColor(231, 255, 126),
		new BorstColor(178, 255, 126),
		new BorstColor(129, 255, 126),
		new BorstColor(126, 255, 178),
		new BorstColor(126, 255, 228),
		new BorstColor(126, 230, 255),
		new BorstColor(126, 178, 255),
		new BorstColor(129, 127, 255),
		new BorstColor(178, 127, 255),
		new BorstColor(231, 127, 255),
		new BorstColor(255, 127, 228),
		new BorstColor(255, 127, 178),
		new BorstColor(255, 127, 126),
	};
	
	// Precomputed lookup tables
	private static final NumberLookup AlphaLookup = new NumberLookup(ALPHAS);
	private static final NumberLookup SizeLookup = new NumberLookup(SIZES);
	
	public static int getClosestAlpha(int alpha) {
		return AlphaLookup.getClosestValue(alpha);
	}
	
	public static int getClosestAlphaIndex(int alpha) {
		return AlphaLookup.getClosestIndex(alpha);
	}
	
	public static int getClosestSize(int size) {
		return SizeLookup.getClosestValue(size);
	}
	
	public static int getClosestSizeIndex(int size) {
		return SizeLookup.getClosestIndex(size);
	}
	
	public static BorstColor getClosestColor(int color) {
		return COLORS[getClosestColorIndex(color)];
	}
	
	public static int getClosestColorIndex(int color) {
		double current_diff = 0;
		int result = 0;
		
		int b_r = (color >> 16) & 0xff;
		int b_g = (color >>  8) & 0xff;
		int b_b = (color      ) & 0xff;
		for (int i = 0, len = COLORS.length; i < len; i++) {
			BorstColor a = COLORS[i];
			// Weighted
			double rd = (a.r - b_r);
			double gd = (a.g - b_g);
			double bd = (a.b - b_b);
			double diff = rd * rd + gd * gd + bd * bd;
			
			if (i == 0 || current_diff > diff) {
				current_diff = diff;
				result = i;
			}
		}
		
		return result;
	}
	
	public static int clampInt(int value, int min, int max) {
		return (value < min ? min : (value > max ? max : value));
	}
	
	private static class NumberLookup {
		private final int[] valueLookup;
		private final int[] indexLookup;
		private final int max;
		public NumberLookup(int[] values) {
			if (values == null || values.length == 0) {
				throw new NullPointerException("Cannot create an empty lookup table");
			}
			
			// Calculate min / max
			int max = Integer.MIN_VALUE;
			for (int value : values) {
				max = Math.max(max, value);
			}
			
			this.max = max;
			this.valueLookup = new int[max + 1];
			this.indexLookup = new int[max + 1];
			
			// Calculate lookups
			for (int i = 0; i < max + 1; i++) {
				int index = closestIndex(i, max, values);
				this.indexLookup[i] = index;
				this.valueLookup[i] = values[index];
			}
		}
		
		public int getClosestIndex(int value) {
			return indexLookup[Math.max(0, Math.min(max, value))];
		}
		
		public int getClosestValue(int value) {
			return valueLookup[Math.max(0, Math.min(max, value))];
		}
		
		private int closestIndex(int value, int maxValue, int[] array) {
			int current_diff = maxValue * maxValue;
			int result = -1;
			for (int i = 0, len = array.length; i < len; i++) {
				int diff = Math.abs(value - array[i]);
				if (current_diff > diff) {
					current_diff = diff;
					result = i;
				}
			}
			
			return result;
		}
	}
}
