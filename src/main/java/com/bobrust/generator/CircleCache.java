package com.bobrust.generator;

class CircleCache {	
	private static final Scanline[] CIRCLE_0 = generateCircle(1);
	private static final Scanline[] CIRCLE_1 = generateCircle(4);
	private static final Scanline[] CIRCLE_2 = generateCircle(8);
	private static final Scanline[] CIRCLE_3 = generateCircle(12);
	private static final Scanline[] CIRCLE_4 = generateCircle(19);
	private static final Scanline[] CIRCLE_5 = generateCircle(25); // Might be larger
	
	public static final Scanline[][] CIRCLE_CACHE = { CIRCLE_0, CIRCLE_1, CIRCLE_2, CIRCLE_3, CIRCLE_4, CIRCLE_5 };
	public static final int[] CIRCLE_CACHE_LENGTH = {
		CIRCLE_CACHE[0].length,
		CIRCLE_CACHE[1].length,
		CIRCLE_CACHE[2].length,
		CIRCLE_CACHE[3].length,
		CIRCLE_CACHE[4].length,
		CIRCLE_CACHE[5].length
	};
	
	/*public static void main(String[] args) {
		// 1, 4, 8, 12, 19, 25
		// 18 full match but 19 matches area
		// 24 full match but 25 matches area
		generateCircle(24);
	}*/
	
	private static Scanline[] generateCircle(int size) {
		boolean[] grid = new boolean[size * size];
		for (int i = 0; i < size * size; i++) {
			double px = (int) (i % size) + 0.5;
			double py = (int) (i / size) + 0.5;
			double x = (px / (double) size) * 2.0 - 1;
			double y = (py / (double) size) * 2.0 - 1;
			
			double magnitudeSqr = x*x + y*y;
			grid[i] = magnitudeSqr <= 1;
		}
		
		/*{
			BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
			for (int i = 0; i < grid.length; i++) {
				pixels[i] = grid[i] ? 0x000000 : 0xffffff;
			}
			
			DebugUtil.debugShowImage(bi, 13);
		}*/
		
		Scanline[] scanlines = new Scanline[size];
		for (int i = 0; i < size; i++) {
			int start = size;
			int end = 0;
			for (int j = 0; j < size; j++) {
				if (grid[i * size + j]) {
					start = Math.min(start, j);
					end = Math.max(end, j );
				}
			}
			
			if (start <= end) {
				int off = size / 2;
				scanlines[i] = new Scanline(i - off, start - off, end - off);
			}
		}
		
		/*{
			int off = size / 2;
			BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
			Arrays.fill(pixels, 0xffffff);
			for (Scanline line : scanlines) {
				for (int x = line.x1; x <= line.x2; x++) {
					pixels[(line.y + off) * size + (x + off)] = 0x000000;
				}
			}
			DebugUtil.debugShowImage(bi, 13);
		}*/
		
		return scanlines;
	}
}
