package com.bobrust.gui.dialog;

import com.bobrust.gui.ApplicationWindow;
import com.bobrust.gui.OverlayTopPanel;
import com.bobrust.gui.render.ShapeRender;
import com.bobrust.util.data.AppConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenDrawDialog extends JDialog {
	public final DrawDialog drawDialog;
	
	// TODO: Make these private
	final OverlayTopPanel topPanel;
	final ShapeRender shapeRender;
	final ApplicationWindow parent;
	
	Rectangle canvasRect = new Rectangle();
	Rectangle imageRect = new Rectangle();
	
	public ScreenDrawDialog(ApplicationWindow parent) {
		super(null, "BobRust - Draw", ModalityType.MODELESS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(AppConstants.DIALOG_ICON);
		setUndecorated(true);
		setBackground(new Color(0, true));
		
		this.parent = parent;
		this.topPanel = new OverlayTopPanel();
		this.shapeRender = new ShapeRender(2000);
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
		// TODO: During testing this sometimes froze the application!
		
		// Create local variables for user selected areas
		canvasRect.setRect(parent.getCanvasRect());
		imageRect.setRect(parent.getImageRect());
		
		// Open the draw dialog
		setBounds(monitor.getBounds());
		parent.setVisible(false);
		setVisible(true);
		drawDialog.openDialog(monitor, location);
		parent.setVisible(true);
		dispose();
		
		shapeRender.reset();
	}
	
	private double remap(double value, double a, double b, double c, double d) {
		return c + (d - c) / (b - a) * (value - a);
	}
	
	void updateCanvasRect(Rectangle updated) {
		// Update the canvas rectangle
		
		var oc = parent.getCanvasRect();
		var oi = parent.getImageRect();
		
		double nx0 = remap(
			oi.x,
			oc.x, oc.x + oc.width,
			updated.x, updated.x + updated.width);
		double ny0 = remap(
			oi.y,
			oc.y, oc.y + oc.height,
			updated.y, updated.y + updated.height);
		
		double nx1 = remap(
			oi.x + oi.width,
			oc.x, oc.x + oc.width,
			updated.x, updated.x + updated.width);
		double ny1 = remap(
			oi.y + oi.height,
			oc.y, oc.y + oc.height,
			updated.y, updated.y + updated.height);
		
		double w = nx1 - nx0;
		double h = ny1 - ny0;
		imageRect.setRect(nx0, ny0, w, h);
		canvasRect.setRect(updated);
	}
	
	private final BasicStroke backgroundStroke = new BasicStroke(3);
	
	public void paintComponent(Graphics2D g) {
		if (isAlwaysOnTop()) {
			// Don't draw when we are always on top
			return;
		}
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		var oldStroke = g.getStroke();
		
		g.setStroke(backgroundStroke);
		g.setColor(Color.darkGray);
		g.drawRoundRect(canvasRect.x - 1, canvasRect.y - 1, canvasRect.width + 1, canvasRect.height + 1, 2, 2);
		g.drawRoundRect(imageRect.x - 1, imageRect.y - 1, imageRect.width + 1, imageRect.height + 1, 2, 2);
		g.setStroke(oldStroke);
		
		g.setColor(Color.white);
		g.drawRoundRect(canvasRect.x - 1, canvasRect.y - 1, canvasRect.width + 1, canvasRect.height + 1, 2, 2);
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(0.5f));
		// g.drawImage(drawImage, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
		
		synchronized (drawDialog.borstGenerator.data) {
			final var data = drawDialog.borstGenerator.data;
			int shapes = data.getBlobs().size();
			shapes = Math.min(drawDialog.shapesSlider.getValue(), shapes);
			
			BufferedImage shapeImage = shapeRender.getImage(data, shapes);
			if (shapeImage != null) {
				g.drawImage(shapeImage, canvasRect.x, canvasRect.y, canvasRect.width, canvasRect.height, null);
			}
		}
		
		g.setColor(Color.cyan);
		g.drawRoundRect(imageRect.x - 1, imageRect.y - 1, imageRect.width + 1, imageRect.height + 1, 2, 2);
	}
}
