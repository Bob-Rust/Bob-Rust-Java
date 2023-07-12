package com.bobrust.generator.color;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ICC_ColorVisualizer {
	private static final File WALLPAPER_FOLDER = new File(System.getProperty("user.home"), "downloads/Wallpapers");
	private static final int MAX_WIDTH = 800;
	private static final int MAX_HEIGHT = 500;
	private static final int bits = 7;
	private static final int[] iccCmykLut;
	
	static {
		iccCmykLut = ((DataBufferInt)ICC_ColorGenerator.createICCconversionLut(bits).getRaster().getDataBuffer()).getData().clone();
	}
	
	private static BufferedImage getImage(File name) {
		try (FileInputStream stream = new FileInputStream(name)) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void showImage(BufferedImage image) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
	}
	
	private static BufferedImage getImageScreen(String name) {
		return getImageScreen(new File(WALLPAPER_FOLDER, name), MAX_WIDTH, MAX_HEIGHT);
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage getImageScreen(BufferedImage image) {
		return getImageScreen(image, MAX_WIDTH, MAX_HEIGHT);
	}
	
	private static BufferedImage getImageScreen(File path, int max_width, int max_height) {
		return getImageScreen(getImage(path), max_width, max_height);
	}
	
	private static BufferedImage getImageScreen(BufferedImage bi, int max_width, int max_height) {
		int width = bi.getWidth();
		int height = bi.getHeight();
		int bwi = width;
		int bhe = height;
		
		if (width > max_width || height > max_height) {
			double he = max_height;
			double wi = width * (max_height / (double) height);
			
			if (wi > max_width) {
				wi = max_width;
				he = height * (max_width / (double) width);
			}

			bhe = (int) he;
			bwi = (int) wi;
		}
		
		BufferedImage image = new BufferedImage(bwi, bhe, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(bi, 0, 0, bwi, bhe, null);
		g.dispose();
		bi = image;
		
		return bi;
	}
	
	public static void main(String[] args) {
		BufferedImage image;
		image = getImageScreen("spectrum.png");
		image = getImageScreen("images.jpg");
//		image = getImageScreen("color.png");
//		image = getImageScreen("windows.png");
//		image = getImageScreen("windows_magenta.png");
//		image = getImageScreen("rgb-hsv.png");
		
		showImage(image);
		showImage(applyFilters(image));
	}
	
	@SuppressWarnings("unused")
	private static BufferedImage convertPixelBuffer(int[] src, int size) {
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		System.arraycopy(src, 0, pixels, 0, src.length);
		return image;
	}
	
	public static BufferedImage applyFilters(BufferedImage scaled) {
		BufferedImage image = new BufferedImage(scaled.getWidth(), scaled.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(scaled, 0, 0, null);
		g.dispose();
		
		final int r_shift = bits * 2;
		final int g_shift = bits;
		final int d_shift = 8 - bits;
		
		int[] src = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		for(int i = 0, len = src.length; i < len; i++) {
			int col = src[i] & 0xffffff;
			int cr = ((col >> 16) & 255) >> d_shift;
			int cg = ((col >> 8) & 255) >> d_shift;
			int cb = (col & 255) >> d_shift;
			int cidx = (cr << r_shift) | (cg << g_shift) | cb;
			src[i] = iccCmykLut[cidx] | (col & 0xff000000);
		}
		
		return image;
	}
}
