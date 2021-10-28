package com.bobrust.generator;

public class BorstUtils {
	public static final int[] ALPHAS = { 9, 18, 72, 136, 218, 255 };
	public static final int[] SIZES = { 1, 2, 4, 6, 10, 13 };
	
	public static final int COMMON_OPACITY = 72;
	public static final BorstColor[] COLORS = {
		new BorstColor(46, 204, 112, COMMON_OPACITY),
		new BorstColor(22, 161, 132, COMMON_OPACITY),
		new BorstColor(52, 153, 218, COMMON_OPACITY),
		new BorstColor(241, 195, 16, COMMON_OPACITY),
		new BorstColor(143, 69, 173, COMMON_OPACITY),
		new BorstColor(153, 163, 162, COMMON_OPACITY),
		new BorstColor(52, 73, 93, COMMON_OPACITY),
		new BorstColor(46, 158, 135, COMMON_OPACITY),
		new BorstColor(30, 224, 24, COMMON_OPACITY),
		new BorstColor(176, 122, 195, COMMON_OPACITY),
		new BorstColor(231, 127, 33, COMMON_OPACITY),
		new BorstColor(236, 240, 241, COMMON_OPACITY),
		new BorstColor(38, 174, 96, COMMON_OPACITY),
		new BorstColor(33, 203, 241, COMMON_OPACITY),
		new BorstColor(126, 77, 41, COMMON_OPACITY),
		new BorstColor(239, 68, 49, COMMON_OPACITY),
		new BorstColor(74, 212, 188, COMMON_OPACITY),
		new BorstColor(69, 48, 33, COMMON_OPACITY),
		new BorstColor(49, 49, 49, COMMON_OPACITY),
		new BorstColor(1, 2, 1, COMMON_OPACITY)
	};
	
	// Precomputed lookup tables
	private static final int[] ALPHA_INDEX_LOOKUP_TABLE;
	private static final int[] ALPHA_VALUE_LOOKUP_TABLE;
	private static final int[] SIZE_INDEX_LOOKUP_TABLE;
	private static final int[] SIZE_VALUE_LOOKUP_TABLE;
	
	public static int getClosestAlpha(int alpha) {
		return ALPHA_VALUE_LOOKUP_TABLE[alpha < 0 ? 0:(alpha > 255 ? 255:alpha)]; 
	}
	
	public static int getClosestAlphaIndex(int alpha) {
		return ALPHA_INDEX_LOOKUP_TABLE[alpha < 0 ? 0:(alpha > 255 ? 255:alpha)]; 
	}
	
	public static int getClosestSize(int size) {
		return SIZE_VALUE_LOOKUP_TABLE[size < 0 ? 0:(size > 13 ? 13:size)]; 
	}
	
	public static int getClosestSizeIndex(int size) {
		return SIZE_INDEX_LOOKUP_TABLE[size < 0 ? 0:(size > 13 ? 13:size)]; 
	}
	
	public static BorstColor getClosestColor(int color) {
		return COLORS[getClosestColorIndex(color)];
	}
	
	public static int getClosestColorIndex(int color) {
		int current_diff = 0;
		int result = 0;
		
		int b_r = (color >> 16) & 0xff;
		int b_g = (color >>  8) & 0xff;
		int b_b = (color      ) & 0xff;
		for(int i = 0, len = COLORS.length; i < len; i++) {
			BorstColor a = COLORS[i];
			int diff = (a.r - b_r) * (a.r - b_r) + (a.g - b_g) * (a.g - b_g) + (a.b - b_b) * (a.b - b_b);
			
			if(i == 0 || current_diff > diff) {
				current_diff = diff;
				result = i;
			}
		}
	
		return result;
	}
	
	public static int clampInt(int value, int min, int max) {
		return (value < min ? min:(value > max ? max:value));
	}
	
	// Lookup table generators
	private static int getClosestAlphaIndex0(int alpha) {
		int current_diff = 65535;
		int result = 0;
		
		for(int i = 0, len = ALPHAS.length; i < len; i++) {
			int diff = Math.abs(ALPHAS[i] - alpha);
			if(current_diff > diff) {
				current_diff = diff;
				result = i;
			}
		}
		
		return result;
	}
	
	private static int getClosestSizeIndex0(int size) {
		int current_diff = 65535;
		int result = 0;
		for(int i = 0, len = SIZES.length; i < len; i++) {
			int diff = Math.abs(size - SIZES[i]);
			if(current_diff > diff) {
				current_diff = diff;
				result = i;
			}
		}
	
		return result;
	}
	
	static {
		ALPHA_INDEX_LOOKUP_TABLE = new int[256];
		for(int i = 0; i < 13; i++) ALPHA_INDEX_LOOKUP_TABLE[i] = getClosestAlphaIndex0(i);
		
		ALPHA_VALUE_LOOKUP_TABLE = new int[256];
		for(int i = 0; i < 13; i++) ALPHA_VALUE_LOOKUP_TABLE[i] = ALPHAS[getClosestAlphaIndex0(i)];
		
		SIZE_INDEX_LOOKUP_TABLE = new int[14];
		for(int i = 0; i < 13; i++) SIZE_INDEX_LOOKUP_TABLE[i] = getClosestSizeIndex0(i);
		
		SIZE_VALUE_LOOKUP_TABLE = new int[14];
		for(int i = 0; i < 13; i++) SIZE_VALUE_LOOKUP_TABLE[i] = SIZES[getClosestSizeIndex0(i)];
		
	}
}
