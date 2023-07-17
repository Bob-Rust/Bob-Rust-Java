package com.bobrust.generator.color;

import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.io.*;

import javax.imageio.ImageIO;

/**
 * Generates the ICC lookup table for the color correcting part of the
 * application.
 * 
 * <pre>
 *   BufferedImage image;
 *   image = applyICC_cmyk(image);
 *   image = applyLutConversion(image);
 *   return image;
 * </pre>
 * 
 * @author HardCoded
 */
public class ICC_ColorGenerator {
	private static int lerp(int a, int b, double index) {
		int ar = (a >> 16) & 0xff;
		int ag = (a >> 8) & 0xff;
		int ab = a & 0xff;
		
		int br = (b >> 16) & 0xff;
		int bg = (b >> 8) & 0xff;
		int bb = b & 0xff;
		
		int dr = (int)((br - ar) * index + ar);
		int dg = (int)((bg - ag) * index + ag);
		int db = (int)((bb - ab) * index + ab);
		
		dr = (dr < 0 ? 0:(dr > 255 ? 255:dr));
		dg = (dg < 0 ? 0:(dg > 255 ? 255:dg));
		db = (db < 0 ? 0:(db > 255 ? 255:db));
		
		return (dr << 16) | (dg << 8) | db;
	}
	
	/*
	private static int[] createConversionLUT_old() {
		int[] lut = new int[256 * 256 * 256];
		
		int left = 0x3eb8ff;
		int right = 0x777aff;
		
		// This lut will make.
		// Blue more cyan
		// Cyan more light
		// Magenta less light
		
		for(int i = 0, len = lut.length; i < len; i++) {
			int ri = (i >> 16) & 255;
			int gi = (i >> 8) & 255;
			int bi = i & 255;
			
			int r = ri;
			int g = gi;
			int b = lerp(0, lerp(left, right, (ri / 255.0) * (ri / 255.0)), bi / 255.0);
			
			// blue green
			int bg = (b >> 8) & 0xff;
			int br = (b >> 16) & 0xff;
			g = Math.max(g, bg);
			
			// Remove some of the red component
			r -= br;
			r = (r < 0 ? 0:(r > 255 ? 255:r));
			
			// Only keep blue component
			b = b & 0xff;
			
			// put values between 15 and 245
			r = ((int)((r - 15) * (230.0 / 255.0)) + 15);
			// Make green and blue brighter
			g = ((int)((g - 15) * (230.0 / 255.0)) + 25);
			b = ((int)((b - 15) * (230.0 / 255.0)) + 20);
			
			lut[i] = (r << 16) | (g << 8) | b | 0xff000000;
		}
		
		return lut;
	}
	*/
	
	/*
	private static int[] createConversionLut_256() {
		int[] lut = new int[256 * 256 * 256];
		
		int left = 0x3eb8ff;
		int right = 0x777aff;
		
		// This lut will make.
		// Blue more cyan
		// Cyan more light
		// Magenta less light
		// Brightest cyan should be '28a6c0'
		// Brightest blue should be '1e98bf'
		// 
		
		for(int i = 0, len = lut.length; i < len; i++) {
			int ri = (i >> 16) & 255;
			int gi = (i >> 8) & 255;
			int bi = i & 255;
			
			int r = ri;
			int g = gi;
			int b = lerp(0, lerp(left, right, ri / 255.0), bi / 255.0);
			
			// blue green
			int bg = (b >> 8) & 0xff;
//			int br = (b >> 16) & 0xff;
//			g = lerp(((int)(bg * ((255 - gi) / 255.0)) + g) / 2, g, (gi / 255.0));
			g = Math.max(g, bg);
			
			// Only keep blue component
			b = b & 0xff;
			
			// Remove some of the red component
//			r -= br ;
//			r = (r < 0 ? 0:(r > 255 ? 255:r));
			
			// put values between 15 and 245
//			r = ((int)((r - 15) * (230.0 / 255.0)) + 15);
			// Make green and blue brighter
//			g = ((int)((g - 15) * (230.0 / 255.0)) + 25);
//			b = ((int)((b - 15) * (230.0 / 255.0)) + 20);
			lut[i] = (r << 16) | (g << 8) | b | 0xff000000;
		}
		
		return lut;
	}
	*/
	
	@SuppressWarnings("unused")
	private static int curve(int value) {
		// Will aproximate the curve (x * 0.92 + 20) but makes it
		// aproach zero instead of 20.
		final double exponent = 0.9;
		
		value = (int)(Math.pow(value, exponent) * Math.pow(255, 1 - exponent));
		value = (value < 0 ? 0:(value > 255 ? 255:value));
		return value;
	}
	
	private static int[] createConversionLut(int bits) {
		final int size = (1 << bits) * (1 << bits) * (1 << bits);
		final int mask = (1 << bits) - 1;
		final int r_shift = bits * 2;
		final int g_shift = bits;
		final int d_shift = 8 - bits;
		
		int[] lut = new int[size];
		int left = 0x3eb8ff;
		int right = 0x777aff;
		
		for(int i = 0, len = lut.length; i < len; i++) {
			int ri = (i >> r_shift) & mask;
			int gi = (i >> g_shift) & mask;
			int bi = i & mask;
			
			int r = ri;
			int g = gi;
			int b = lerp(0, lerp(left, right, ri / (double)mask), bi / (double)mask);
			
			// Blue green mixing.
			int bg = ((b >> 8) & 255) >> d_shift;
			g = Math.max(g, bg);
			
			// Only keep blue component.
			b = (b & 255) >> d_shift;
			
			r = r << d_shift;
			g = g << d_shift;
			b = b << d_shift;
			
			// Make darker colors more dark.
//			r = curve(r);
//			g = curve(g);
//			b = curve(b);
			
			lut[i] = (r << 16) | (g << 8) | (b) | 0xff000000;
		}
		
		return lut;
	}
	
	public static BufferedImage createICCconversionLut(int bits) {
		final int size = (1 << bits) * (1 << bits) * (1 << bits);
		final int width = 1 << ((bits * 3 + 1) / 2);
		final int height = size / width;
		final int mask = (1 << bits) - 1;
		
		final int r_shift = bits * 2;
		final int g_shift = bits;
		final int d_shift = 8 - bits;
		
		final int[] conversionLut = createConversionLut(bits);
		
		BufferedImage iccCmyk = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] src = ((DataBufferInt)iccCmyk.getRaster().getDataBuffer()).getData();
		
		for(int i = 0, len = src.length; i < len; i++) {
			int r = ((i >> r_shift) & mask) << d_shift;
			int g = ((i >> g_shift) & mask) << d_shift;
			int b = (i & mask) << d_shift;
			src[i] = (r << 16) | (g << 8) | (b) | 0xff000000;
		}
		
		ICC_Profile sRGB_to_CMYK = null;
		try(InputStream stream = ICC_ColorGenerator.class.getResourceAsStream("/profiles/cmyk.icm")) {
			sRGB_to_CMYK = ICC_Profile.getInstance(stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		ColorConvertOp CMYK_CONVERT_OP = new ColorConvertOp(new ICC_Profile[] { sRGB_to_CMYK }, null);
		CMYK_CONVERT_OP.filter(iccCmyk, iccCmyk);
		for(int i = 0, len = src.length; i < len; i++) {
			int col = src[i];
			int cr = ((col >> 16) & 255) >> d_shift;
			int cg = ((col >> 8) & 255) >> d_shift;
			int cb = (col & 255) >> d_shift;
			src[i] = conversionLut[(cr << r_shift) | (cg << g_shift) | (cb)] | 0xff000000;
		}
		
		return iccCmyk;
	}
	
	public static void main(String[] args) {
		BufferedImage lut = createICCconversionLut(7);
		
		try(FileOutputStream stream = new FileOutputStream(new File("src/main/resources/profiles/cmyk.png"))) {
			ImageIO.write(lut, "png", stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
