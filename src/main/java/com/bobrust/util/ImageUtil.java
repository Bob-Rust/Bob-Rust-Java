package com.bobrust.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.bobrust.settings.data.ScalingType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Check different ways to convert colors
 * 
 * RGB: simplest but skips blue
 * HSB, HSL: does not look good
 * RGB with IndexColorModel: Works but produces artifacts.
 * 
 * LUT: does seem to work but has problem with dark blue and magenta. (Research more)
 * 
 * @author HardCoded
 */
public class ImageUtil {
	private static final Logger LOGGER = LogManager.getLogger(ImageUtil.class);
	private static final int[] iccCmykLut;
	private static final int bits;
	
	static {
		int[] lut = null;
		try (InputStream stream = ImageUtil.class.getResourceAsStream("/profiles/cmyk.png")) {
			if (stream == null) {
				throw new NullPointerException("Resource was not found");
			}
			
			BufferedImage cmykLut = ImageIO.read(stream);
			int w = cmykLut.getWidth();
			int h = cmykLut.getHeight();
			lut = new int[w * h];
			cmykLut.getRGB(0, 0, w, h, lut, 0, w);
		} catch (Exception e) {
			LOGGER.error("Error loading cmyk lut", e);
			LOGGER.throwing(e);
			e.printStackTrace();
		}
		
		iccCmykLut = lut;
		bits = Integer.numberOfTrailingZeros(lut.length) / 3;
	}
	
	public static BufferedImage applyFilters(Image scaled) {
		// Create a new Image that has a backing int buffer
		BufferedImage image = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(scaled, 0, 0, null);
		g.dispose();
		
		final int r_shift = bits * 2;
		final int g_shift = bits;
		final int d_shift = 8 - bits;
		
		final int[] src = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		final int[] lut = iccCmykLut;
		for (int i = 0, len = src.length; i < len; i++) {
			int col = src[i] & 0xffffff;
			int cr = ((col >> 16) & 255) >> d_shift;
			int cg = ((col >> 8) & 255) >> d_shift;
			int cb = (col & 255) >> d_shift;
			int lut_idx = (cr << r_shift) | (cg << g_shift) | cb;
			src[i] = lut[lut_idx] | col;
		}
		
		return image;
	}
	
	public static BufferedImage getScaledInstance(Image source, Rectangle canvasRect, Rectangle imageRect, int width, int height, Color bgColor, ScalingType scalingType) {
		Object hint = switch (scalingType) {
			case Nearest -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
			case Bilinear -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
			case Bicubic -> RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		};
		
		int dst_x1 = ((imageRect.x - canvasRect.x) * width) / canvasRect.width;
		int dst_y1 = ((imageRect.y - canvasRect.y) * height) / canvasRect.height;
		int dst_x2 = dst_x1 + (imageRect.width * width) / canvasRect.width;
		int dst_y2 = dst_y1 + (imageRect.height * height) / canvasRect.height;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);
		g.drawImage(source, dst_x1, dst_y1, dst_x2, dst_y2, 0, 0, source.getWidth(null), source.getHeight(null), null);
		g.dispose();
		
		return image;
	}
	
	public static Image getSmoothScaledInstance(Image image, int width, int height) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();
		return result;
	}
}
