package com.bobrust.gui.dialog;

import com.bobrust.gui.ApplicationWindow;
import com.bobrust.gui.OverlayTopPanel;
import com.bobrust.gui.render.ShapeRender;
import com.bobrust.util.data.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenDrawDialog extends JDialog {
	private final DrawDialog drawDialog;
	
	// TODO: Make these private
	final OverlayTopPanel topPanel;
	final ShapeRender shapeRender;
	final ApplicationWindow parent;
	
	public ScreenDrawDialog(ApplicationWindow parent) {
		super(null, "", ModalityType.MODELESS);
		setIconImage(AppConstants.DIALOG_ICON);
		setUndecorated(true);
		setBackground(new Color(0, true));
		
		this.parent = parent;
		this.topPanel = new OverlayTopPanel();
		this.shapeRender = new ShapeRender(500);
		this.drawDialog = new DrawDialog(this);
		
		setLayout(new BorderLayout());
		topPanel.setPreferredSize(new Dimension(0, 50));
		add(topPanel, BorderLayout.NORTH);
		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.translate(0, -50);
				ScreenDrawDialog.this.paintComponent((Graphics2D) g);
				g.translate(0, 50);
			}
		};
		panel.setOpaque(false);
		add(panel, BorderLayout.CENTER);
	}
	
	public void openDialog(GraphicsConfiguration monitor, Point location) {
		setBounds(monitor.getBounds());
		parent.setVisible(false);
		setVisible(true);
		drawDialog.openDialog(monitor, location);
		System.out.println("Sdd A");
		parent.setVisible(true);
		System.out.println("Sdd B");
		dispose();
		System.out.println("Sdd C");
	}
	
	private final BasicStroke backgroundStroke = new BasicStroke(3);
	
	public void paintComponent(Graphics2D g) {
		if (isAlwaysOnTop()) {
			// Don't draw when we are always on top
			return;
		}
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		var canvasRect = parent.getCanvasRect();
		var imageRect = parent.getImageRect();
		var oldStroke = g.getStroke();
		
		g.setStroke(backgroundStroke);
		g.setColor(Color.darkGray);
		g.drawRoundRect(canvasRect.x - 1, canvasRect.y - 1, canvasRect.width + 1, canvasRect.height + 1, 2, 2);
		g.drawRoundRect(imageRect.x - 1, imageRect.y - 1, imageRect.width + 1, imageRect.height + 1, 2, 2);
		g.setStroke(oldStroke);
		
		g.setColor(Color.white);
		g.drawRoundRect(canvasRect.x - 1, canvasRect.y - 1, canvasRect.width + 1, canvasRect.height + 1, 2, 2);
		
		g.setColor(Color.cyan);
		g.drawRoundRect(imageRect.x - 1, imageRect.y - 1, imageRect.width + 1, imageRect.height + 1, 2, 2);
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(0.5f));
		// g.drawImage(drawImage, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
		
		var data = drawDialog.getBorstData();
		// System.out.println("data: " + data);
		if (data != null) {
			synchronized (data) {
				int shapes = data.getBlobs().size();
				shapes = Math.min(drawDialog.shapesSlider.getValue(), shapes);
				
				BufferedImage shapeImage = shapeRender.getImage(data, shapes);
				g.drawImage(shapeImage, canvasRect.x, canvasRect.y, canvasRect.width, canvasRect.height, null);
			}
		}
	}
}
