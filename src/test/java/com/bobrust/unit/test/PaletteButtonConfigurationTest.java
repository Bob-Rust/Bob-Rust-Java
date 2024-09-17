package com.bobrust.unit.test;

import com.bobrust.robot.BobRustPalette;
import com.bobrust.robot.BobRustPaletteGenerator;
import com.bobrust.robot.ButtonConfiguration;
import com.bobrust.util.RustWindowUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PaletteButtonConfigurationTest {
	public static final Pattern PATTERN = Pattern.compile("draw_([0-9]+).*");
	public static final int VERSION = 3; // Game UI Version
	
	/**
	 * Images used to perform automatic analysis on
	 */
	public static final List<BufferedImage> IMAGES = getImages();
	
	private static List<BufferedImage> getImages() {
		File ui = new File("src/test/resources/rust-ui");
		File[] files = ui.listFiles();
		if (files == null) {
			return List.of();
		}
		
		List<BufferedImage> images = new ArrayList<>();
		for (File file : files) {
			var match = PATTERN.matcher(file.getName());
			if (match.find() && Integer.toString(VERSION).equals(match.group(1))) {
				try {
					BufferedImage bi = ImageIO.read(file);
					if (bi != null) {
						images.add(bi);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return List.copyOf(images);
	}
	
	private static class DummyConfiguration extends GraphicsConfiguration {
		private final Rectangle rect = new Rectangle();
		
		public DummyConfiguration(int x, int y, int width, int height) {
			rect.setBounds(x, y, width, height);
		}
		
		public GraphicsDevice getDevice() { return null; }
		public ColorModel getColorModel() { return null; }
		public ColorModel getColorModel(int transparency) { return null; }
		public AffineTransform getDefaultTransform() { return null; }
		public AffineTransform getNormalizingTransform() { return null; }
		
		@Override
		public Rectangle getBounds() {
			return rect;
		}
	}
	
	/**
	 * Checks if the automatic palette generation works for different screen sizes
	 */
	@Disabled
	@Test
	public void automaticPaletteTest() {
		assertFalse(IMAGES.isEmpty(), "Automatic palette test was run with zero images");
		
		BobRustPalette palette = new BobRustPalette();
		for (BufferedImage image : IMAGES) {
			GraphicsConfiguration monitor = new DummyConfiguration(0, 0, image.getWidth(), image.getHeight());
			var config = BobRustPaletteGenerator.createAutomaticConfiguration(3, image);
			boolean valid = palette.initWith(image, monitor, config);
			
			if (!valid) {
				test(image, 0);
			}
			
			assertTrue(valid, "Monitor size %dx%d palette found".formatted(image.getWidth(), image.getHeight()));
		}
	}
	
	@Disabled
	@Test
	public void checkAutomaticPalette() {
		// test(IMAGES.get(0), 0);
		// test(IMAGES.get(1), 1);
	}
	
	private void test(BufferedImage source, int index) {
		var config = BobRustPaletteGenerator.createAutomaticV3(source.getWidth(), source.getHeight(), source);
		System.out.println(config.serialize());
		
		BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = copy.createGraphics();
		g.drawImage(source, 0, 0, null);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(3.0f));
		
		for (var field : config.getClass().getDeclaredFields()) {
			if ((field.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC)) != 0) {
				continue;
			}
			
			try {
				String name = field.getName();
				var value = (ButtonConfiguration.Coordinate) field.get(config);
				
				g.setColor(Color.red);
				g.drawRect(value.x() - 20, value.y() - 20, 40, 40);
				
				g.setColor(Color.black);
				g.fillOval(value.x() - 3, value.y() - 3, 6, 6);
				
				g.setColor(Color.white);
				g.fillOval(value.x() - 2, value.y() - 2, 4, 4);
				
				g.setColor(Color.white);
				g.drawString("" + name, value.x() - 20, value.y() - 20 - 6);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		g.dispose();
		
		double mul = (1080.0 / source.getHeight());
		var scaled = copy.getScaledInstance((int) (source.getWidth() * mul), 1080, Image.SCALE_DEFAULT);
		RustWindowUtil.showWarningMessage(null, new JLabel(new ImageIcon(scaled)), "Index " + index + " " + source.getWidth() + "x" + source.getHeight());
	}
}
