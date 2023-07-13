package com.bobrust.gui.comp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class JToolbarButton extends JRoundPanel {
	private final JButton button;
	private Color background;
	
	public JToolbarButton(Image icon, int rgb) {
		background = getBackground();
		
		setLayout(new BorderLayout());
		setBorderRadius(10);
		
		button = new JButton();
		button.setBorder(new EmptyBorder(8, 8, 8, 8));
		button.setToolTipText("This is a tooltip");
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEnabled()) {
					JToolbarButton.super.setBackground(background.brighter());
					// setBackground(new Color(0x413D73));
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (isEnabled()) {
					JToolbarButton.super.setBackground(background);
					// setBackground(new Color(0x302d5b));
				}
			}
		});
		add(button, BorderLayout.CENTER);
		
		setToolbarIcon(icon, new Color(rgb));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
		
		if (!enabled) {
			super.setBackground(background.darker());
		}
	}
	
	public void addActionListener(ActionListener actionListener) {
		button.addActionListener(actionListener);
	}
	
	public void setToolbarIcon(Image icon, Color color) {
		Image enableIcon = changeColor(icon, color.getRGB());
		Image disableIcon = changeColor(icon, color.darker().darker().darker().getRGB());
		button.setIcon(new ImageIcon(enableIcon));
		button.setDisabledIcon(new ImageIcon(disableIcon));
	}
	
	@Override
	public void setBackground(Color bg) {
		background = bg;
		
		if (isEnabled()) {
			super.setBackground(bg);
		} else {
			super.setBackground(bg.darker());
		}
	}
	
	public static BufferedImage changeColor(Image image, int rgb) {
		BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = result.createGraphics();
		gr.drawImage(image, 0, 0, null);
		gr.dispose();
		
		int[] pixels = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
		
		int tr = (rgb >> 16) & 0xff;
		int tg = (rgb >> 8) & 0xff;
		int tb = rgb & 0xff;
		
		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int a = argb >>> 24;
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = argb & 0xff;
			
			double gray = ((r + g + b) / 3.0) / 255.0;
			
			int nr = (int) (gray * tr);
			int ng = (int) (gray * tg);
			int nb = (int) (gray * tb);
			
			nr = Math.max(0, Math.min(255, nr));
			ng = Math.max(0, Math.min(255, ng));
			nb = Math.max(0, Math.min(255, nb));
			pixels[i] = a << 24 | nr << 16 | ng << 8 | nb;
		}
		
		return result;
	}
	
}
