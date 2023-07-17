package com.bobrust.generator;

class BorstCore {
	static BorstColor computeColor(BorstImage target, BorstImage current, int alpha, int size, int x_offset, int y_offset) {
		long rsum_1 = 0;
		long gsum_1 = 0;
		long bsum_1 = 0;
		
		long rsum_2 = 0;
		long gsum_2 = 0;
		long bsum_2 = 0;
		
		int count = 0;
		int w = target.width;
		int h = target.height;
		
		final Scanline[] lines = CircleCache.CIRCLE_CACHE[size];
		final int len = lines.length;
		for (int i = 0; i < len; i++) {
			Scanline line = lines[i];
			int y = line.y + y_offset;
			if (y < 0 || y >= h) {
				continue;
			}
			
			int xs = Math.max(line.x1 + x_offset, 0);
			int xe = Math.min(line.x2 + x_offset, w - 1);
			int idx = y * w;
			
			for (int x = xs; x <= xe; x++) {
				int tt = target.pixels[idx + x];
				int cc = current.pixels[idx + x];
				
				rsum_1 += (tt >>> 16) & 0xff;
				gsum_1 += (tt >>>  8) & 0xff;
				bsum_1 += (tt       ) & 0xff;
				
				rsum_2 += (cc >>> 16) & 0xff;
				gsum_2 += (cc >>>  8) & 0xff;
				bsum_2 += (cc       ) & 0xff;
			}
			
			count += (line.x2 - line.x1 + 1);
		}
		
		int pd = 65280 / alpha; // (255 << 8) / alpha;
		long rsum = (rsum_1 - rsum_2) * pd + (rsum_2 << 8);
		long gsum = (gsum_1 - gsum_2) * pd + (gsum_2 << 8);
		long bsum = (bsum_1 - bsum_2) * pd + (bsum_2 << 8);
		
		int r = (int)(rsum / (double)count) >> 8;
		int g = (int)(gsum / (double)count) >> 8;
		int b = (int)(bsum / (double)count) >> 8;
		r = BorstUtils.clampInt(r, 0, 255);
		g = BorstUtils.clampInt(g, 0, 255);
		b = BorstUtils.clampInt(b, 0, 255);
		
		return BorstUtils.getClosestColor((alpha << 24) | (r << 16) | (g << 8) | (b));
	}
	
	static void drawLines(BorstImage im, BorstColor c, int alpha, int size, int x_offset, int y_offset) {
		int cr = c.r * alpha;
		int cg = c.g * alpha;
		int cb = c.b * alpha;
		int pa = 255 - alpha;
		int w = im.width;
		int h = im.height;
		
		final Scanline[] lines = CircleCache.CIRCLE_CACHE[size];
		final int len = lines.length;
		for (int i = 0; i < len; i++) {
			Scanline line = lines[i];
			int y = line.y + y_offset;
			if (y < 0 || y >= h) {
				continue;
			}
			
			int xs = Math.max(line.x1 + x_offset, 0);
			int xe = Math.min(line.x2 + x_offset, w - 1);
			int idx = y * w;
			
			for (int x = xs; x <= xe; x++) {
				int a = im.pixels[idx + x];
				int a_a = (a >>> 24) & 0xff;
				int a_r = (a >>> 16) & 0xff;
				int a_g = (a >>>  8) & 0xff;
				int a_b = (a       ) & 0xff;
				
				int ar = (cr + (a_r * pa)) >>> 8;
				int ag = (cg + (a_g * pa)) >>> 8;
				int ab = (cb + (a_b * pa)) >>> 8;
				int aa = 255 - (((255 - a_a) * pa) >>> 8);
				
				im.pixels[idx + x] = (aa << 24) | (ar << 16) | (ag << 8) | (ab);
			}
		}
	}
	
	static float differenceFull(BorstImage a, BorstImage b) {
		final int w = a.width;
		final int h = a.height;
		
		long total = 0;
		
		final int length = w * h;
		for(int i = 0; i < length; i++) {
			int aa = a.pixels[i];
			int bb = b.pixels[i];
			
			int aa_a = (aa >>> 24) & 0xff;
			int aa_r = (aa >>> 16) & 0xff;
			int aa_g = (aa >>>  8) & 0xff;
			int aa_b = (aa       ) & 0xff;
			
			int bb_a = (bb >>> 24) & 0xff;
			int bb_r = (bb >>> 16) & 0xff;
			int bb_g = (bb >>>  8) & 0xff;
			int bb_b = (bb       ) & 0xff;
			
			int da = aa_a - bb_a;
			int dr = aa_r - bb_r;
			int dg = aa_g - bb_g;
			int db = aa_b - bb_b;
			
			total += (dr*dr + dg*dg + db*db + da*da);
		}

		return (float)(Math.sqrt(total / (w * h * 4.0)) / 255.0);
	}
	
	static float differencePartial(BorstImage target, BorstImage before, BorstImage after, float score, int size, int x_offset, int y_offset) {
		int w = target.width;
		int h = target.height;
		double denom = (w * h * 4.0);
		long total = (long)(Math.pow(score * 255, 2) * denom);
		
		final Scanline[] lines = CircleCache.CIRCLE_CACHE[size];
		final int len = lines.length;
		for (int i = 0; i < len; i++) {
			Scanline line = lines[i];
			int y = line.y + y_offset;
			if (y < 0 || y >= h) {
				continue;
			}
			
			int xs = Math.max(line.x1 + x_offset, 0);
			int xe = Math.min(line.x2 + x_offset, w - 1);
			int idx = y * w;
			
			for (int x = xs; x <= xe; x++) {
				int tt = target.pixels[idx + x];
				int bb = before.pixels[idx + x];
				int aa = after.pixels[idx + x];
				
				int tt_a = (tt >>> 24) & 0xff;
				int tt_r = (tt >>> 16) & 0xff;
				int tt_g = (tt >>>  8) & 0xff;
				int tt_b = (tt       ) & 0xff;
				
				int aa_a = (aa >>> 24) & 0xff;
				int aa_r = (aa >>> 16) & 0xff;
				int aa_g = (aa >>>  8) & 0xff;
				int aa_b = (aa       ) & 0xff;
				
				int bb_a = (bb >>> 24) & 0xff;
				int bb_r = (bb >>> 16) & 0xff;
				int bb_g = (bb >>>  8) & 0xff;
				int bb_b = (bb       ) & 0xff;
				
				int da1 = tt_a - bb_a;
				int dr1 = tt_r - bb_r;
				int dg1 = tt_g - bb_g;
				int db1 = tt_b - bb_b;
				
				int da2 = tt_a - aa_a;
				int dr2 = tt_r - aa_r;
				int dg2 = tt_g - aa_g;
				int db2 = tt_b - aa_b;
				
				total -= (long)(dr1*dr1 + dg1*dg1 + db1*db1 + da1*da1);
				total += (long)(dr2*dr2 + dg2*dg2 + db2*db2 + da2*da2);
			}
		}
		
		return (float)(Math.sqrt(total / denom) / 255.0);
	}
	
	static float differencePartialThread(BorstImage target, BorstImage before, float score, int alpha, int size, int x_offset, int y_offset) {
		BorstColor color = BorstCore.computeColor(target, before, alpha, size, x_offset, y_offset);
		
		final int h = target.height;
		final int w = target.width;
		
		final double denom = (w * h * 4.0);
		long total = (long)(Math.pow(score * 255, 2) * denom);
		
		final int cr = color.r * alpha;
		final int cg = color.g * alpha;
		final int cb = color.b * alpha;
		final int pa = 255 - alpha;
		
		final Scanline[] lines = CircleCache.CIRCLE_CACHE[size];
		final int len = lines.length;
		
		for (int i = 0; i < len; i++) {
			Scanline line = lines[i];
			int y = line.y + y_offset;
			if (y < 0 || y >= h) {
				continue;
			}
			
			int xs = Math.max(line.x1 + x_offset, 0);
			int xe = Math.min(line.x2 + x_offset, w - 1);
			int idx = y * w;
			
			for (int x = xs; x <= xe; x++) {
				int tt = target.pixels[idx + x];
				int bb = before.pixels[idx + x];
				
				int bb_a = (bb >>> 24) & 0xff;
				int bb_r = (bb >>> 16) & 0xff;
				int bb_g = (bb >>>  8) & 0xff;
				int bb_b = (bb       ) & 0xff;
				
				int aa_r = (cr + (bb_r * pa)) >>> 8;
				int aa_g = (cg + (bb_g * pa)) >>> 8;
				int aa_b = (cb + (bb_b * pa)) >>> 8;
				int aa_a = 255 - (((255 - bb_a) * pa) >>> 8);
				
				int tt_a = (tt >>> 24) & 0xff;
				int tt_r = (tt >>> 16) & 0xff;
				int tt_g = (tt >>>  8) & 0xff;
				int tt_b = (tt       ) & 0xff;
				
				int da1 = tt_a - bb_a;
				int dr1 = tt_r - bb_r;
				int dg1 = tt_g - bb_g;
				int db1 = tt_b - bb_b;
				
				int da2 = tt_a - aa_a;
				int dr2 = tt_r - aa_r;
				int dg2 = tt_g - aa_g;
				int db2 = tt_b - aa_b;
				
				total -= (long)(dr1*dr1 + dg1*dg1 + db1*db1 + da1*da1);
				total += (long)(dr2*dr2 + dg2*dg2 + db2*db2 + da2*da2);
			}
		}
		
		return (float)(Math.sqrt(total / denom) / 255.0);
	}
}
