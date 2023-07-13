package com.bobrust.gui;

import com.bobrust.gui.comp.JRoundPanel;
import com.bobrust.util.ResourceUtil;
import com.bobrust.util.data.RustConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * BobRust desktop overlay window.
 * This window will be the overlay that covers the game.
 * 
 * @author HardCoded
 */
public class BobRustDesktopOverlayNew extends JDialog {
	// This is 
	public BobRustDesktopOverlayNew() {
		super(null, "BobRust", ModalityType.APPLICATION_MODAL);
		
		setUndecorated(true);
		setBackground(new Color(0, true));
		
		JRoundPanel panel = new JRoundPanel();
		// panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setLayout(new BorderLayout());
		panel.setBorderRadius(25);
		// panel.setBackground(new Color(0x1a183f));
		// panel.setBackground(new Color(0xe5e4ed));
		panel.setBackground(new Color(0x1d1b43));
		
		panel.add(createHeader(), BorderLayout.NORTH);
		panel.add(createToolbar(), BorderLayout.CENTER);
		panel.add(createVersion(), BorderLayout.SOUTH);
		
		setSize(100, 500);
		setContentPane(panel);
	}
	
	private JPanel createHeader() {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(new Color(0x302d5b));
		
		WindowDragListener listener = new WindowDragListener();
		headerPanel.addMouseListener(listener);
		headerPanel.addMouseMotionListener(listener);
		
		Image image = ResourceUtil.loadImageFromResources("/ui/close_button.png");
		Image scaledImage = image.getScaledInstance(12, 12, Image.SCALE_SMOOTH);
		scaledImage = changeColor(scaledImage, 0xffffff);
		JLabel closeButton = new JLabel(new ImageIcon(scaledImage));
		closeButton.setBorder(new EmptyBorder(5, 5, 5, 10));
		headerPanel.add(closeButton, BorderLayout.EAST);
		
		return headerPanel;
	}
	
	private JPanel createToolbar() {
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setOpaque(false);
		
		int iconSize = 42;
		toolbarPanel.add(createButton("/ui/settings_icon.png", iconSize, 0xe0e0e0));
		toolbarPanel.add(createButton("/ui/select_image.png", iconSize, 0xe0e0e0));
		toolbarPanel.add(createButton("/ui/select_sign.png", iconSize, -1));
		toolbarPanel.add(createButton("/ui/select_icon.png", iconSize, 0xa2ad4b));
		toolbarPanel.add(createButton("/ui/select_icon.png", iconSize, 0x3bb1b1));
		
		return toolbarPanel;
	}
	
	private JComponent createButton(String iconPath, int size, int rgb) {
		Image icon = ResourceUtil.loadImageFromResources(iconPath);
		icon = icon.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		if (rgb != -1) {
			icon = changeColor(icon, rgb);
		}
		
		JRoundPanel panel = new JRoundPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorderRadius(10);
		panel.setBackground(new Color(0x302d5b));
		
		JLabel label = new JLabel(new ImageIcon(icon));
		label.setBorder(new EmptyBorder(8, 8, 8, 8));
		panel.add(label, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createVersion() {
		JLabel versionLabel = new JLabel("Version " + RustConstants.VERSION);
		versionLabel.setForeground(new Color(0xcbcbcb));
		// versionLabel.setForeground(new Color(0x000000));
		// TODO - Label font
		
		JPanel versionPanel = new JPanel();
		versionPanel.setOpaque(false);
		versionPanel.add(versionLabel);
		return versionPanel;
	}
	
	private BufferedImage changeColor(Image image, int rgb) {
		// Compute grayscale
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
	
	private static class WindowDragListener extends MouseAdapter {
		
	}
}
