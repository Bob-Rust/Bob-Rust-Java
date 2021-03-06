package com.bobrust.gui.comp;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

import javax.swing.JPanel;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.util.RustConstants;

@SuppressWarnings("serial")
public class JRandomPanel extends JPanel {
	private final BobRustEditor gui;
	private TexturePaint randomPaint;
	private Color lastToolbarColor;
	
	public JRandomPanel(BobRustEditor gui) {
		this.gui = gui;
	}
	
	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		
		if(RustConstants.ENABLE_RANDOM_BACKGROUND) {
			if(!gui.getEditorToolbarColor().equals(lastToolbarColor) || randomPaint == null) {
				lastToolbarColor = gui.getEditorToolbarColor();
				BufferedImage randomImage = getRandomImage(128, 128, lastToolbarColor.darker(), 0, 120, 0);
				randomPaint = new TexturePaint(randomImage, new Rectangle2D.Double(0, 0, 128, 128));
			}
			
			Graphics2D g = (Graphics2D)gr;
			Paint oldPaint = g.getPaint();
			g.setPaint(randomPaint);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setPaint(oldPaint);
		}
	}
	
	private static BufferedImage getRandomImage(int width, int height, Color color, int lowest, int highest, long seed) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		int rgb = color.getRGB() & 0xffffff;
		int distance = highest - lowest;
		
		Random random = new Random(seed);
		for(int i = 0, len = pixels.length; i < len; i++) {
			int col = (int)(random.nextGaussian() * random.nextGaussian() * distance) + lowest;
			col = col < lowest ? lowest:(col > highest ? highest:col);
			pixels[i] = rgb | (col << 24);
		}
		
		return image;
	}
}
