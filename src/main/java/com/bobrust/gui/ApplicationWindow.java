package com.bobrust.gui;

import com.bobrust.gui.comp.JRoundPanel;
import com.bobrust.gui.dialog.*;
import com.bobrust.settings.Settings;
import com.bobrust.util.ResourceUtil;
import com.bobrust.util.data.RustConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Application window.
 * 
 * Adds a toolbar and shows the current version of the app
 * 
 * @author HardCoded
 */
public class ApplicationWindow extends JDialog {
	private final RustFileDialog fileDialog;
	private final SignPickerDialog signPickerDialog;
	private final SettingsDialog settingsDialog;
	private final DrawDialogNew drawDialog;
	
	public ApplicationWindow() {
		super(null, "BobRust", ModalityType.APPLICATION_MODAL);
		
		setIconImage(RustConstants.DIALOG_ICON);
		//setUndecorated(true);
		setFocusable(true);
		// setBackground(new Color(0, true));
		
		// Setup member variables
		fileDialog = new RustFileDialog();
		signPickerDialog = new SignPickerDialog(this);
		settingsDialog = new SettingsDialog(this);
		drawDialog = new DrawDialogNew(this);
		
		// Setup overlay UI
		JRoundPanel panel = new JRoundPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorderRadius(0);
		// panel.setBackground(new Color(0xe5e4ed));
		panel.setBackground(new Color(0x1d1b43));
		
		// panel.add(createHeader(), BorderLayout.NORTH);
		panel.add(createToolbar(), BorderLayout.CENTER);
		panel.add(createVersion(), BorderLayout.SOUTH);
		
		setSize(0, 520);
		// setSize(410, 110 + 30);
		setContentPane(panel);
	}
	
	private JPanel createHeader() {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(new Color(0x302d5b));
		
		WindowDragListener listener = new WindowDragListener(this);
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
		// toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.PAGE_AXIS));
		toolbarPanel.setOpaque(false);
		
		int iconSize = 42;
		toolbarPanel.add(createButton("/ui/settings_icon.png", iconSize, 0xe0e0e0, e -> {
			settingsDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/upload_image.png", iconSize, 0xe0e0e0, e -> {
			fileDialog.open(this, new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()), "test", Settings.EditorImageDirectory.get());
		}));
		toolbarPanel.add(createButton("/ui/canvas_icon.png", iconSize, 0xe0e0e0, e -> {
			signPickerDialog.openSignDialog(toolbarPanel.getLocationOnScreen());
			Settings.SettingsSign.set(signPickerDialog.getSelectedSign());
		}));
		toolbarPanel.add(createButton("/ui/crop_canvas.png", iconSize, 0xe0e0e0, e -> {}));
		toolbarPanel.add(createButton("/ui/image_select_icon.png", iconSize, 0xe0e0e0, e -> {}));
		// toolbarPanel.add(createButton("/ui/select_icon.png", iconSize, 0x3bb1b1, e -> {}));
		toolbarPanel.add(createButton("/ui/draw_icon.png", iconSize, 0x606060, e -> {
			drawDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/sponsor_icon.png", iconSize, 0xdb61a2, e -> {}));
		
		return toolbarPanel;
	}
	
	private JComponent createButton(String iconPath, int size, int rgb, ActionListener action) {
		Image icon = ResourceUtil.loadImageFromResources(iconPath);
		icon = icon.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		if (rgb != -1) {
			icon = changeColor(icon, rgb);
		}
		
		JRoundPanel panel = new JRoundPanel();
		panel.setLayout(new BorderLayout());
		// panel.setBorder(new EmptyBorder(5, 5, 0, 0));
		panel.setBorderRadius(10);
		panel.setBackground(new Color(0x302d5b));
		// panel.setMaximumSize(new Dimension(68, 63));
		
		JButton button = new JButton(new ImageIcon(icon));
		button.setBorder(new EmptyBorder(8, 8, 8, 8));
		button.addActionListener(action);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				panel.setBackground(new Color(0x413D73));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				panel.setBackground(new Color(0x302d5b));
			}
		});
		panel.add(button, BorderLayout.CENTER);
		
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
	
	/*
	private static class WindowDragListener extends MouseAdapter {
		private final Point dragStart = new Point();
		private final Window window;
		private boolean shouldDrag;
		
		public WindowDragListener(Window window) {
			this.window = window;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			shouldDrag = e.getButton() == MouseEvent.BUTTON1;
			
			Point mouse = e.getLocationOnScreen();
			Point window = this.window.getLocationOnScreen();
			dragStart.setLocation(
				mouse.x - window.x,
				mouse.y - window.y
			);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!shouldDrag) {
				return;
			}
			
			window.setLocation(
				e.getXOnScreen() - dragStart.x,
				e.getYOnScreen() - dragStart.y
			);
		}
	}
	*/
}
