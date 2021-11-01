package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.logging.LogUtils;
import com.bobrust.robot.BobRustPainter;
import com.bobrust.robot.BobRustPalette;
import com.bobrust.util.RustUtil;
import com.bobrust.util.UrlUtils;

public class BobRustDrawDialog {
	private static final Dimension REGULAR = new Dimension(320, 240);
	private static final Dimension MINIMIZED = new Dimension(120, 240);
	
	private final BobRustEditor gui;
	private final BobRustOverlay overlay;
	private final BobRustPalette rustPalette;
	private final BobRustPainter rustPainter;
	
	private final JDialog dialog;
	
	private final JPanel panel;
	private final JIntegerField maxShapesField;
	private final JIntegerField clickIntervalField;
	private final JSlider shapesSlider;
	private final JLabel lblMaximumShape;
	private final JButton btnCalculateExactTime;
	private final JButton btnSelectColorPalette;
	private final JButton btnStartDrawing;
	
	public BobRustDrawDialog(BobRustEditor gui, BobRustOverlay overlay, JDialog parent) {
		this.gui = gui;
		this.overlay = overlay;
		this.rustPalette = new BobRustPalette();
		this.rustPainter = new BobRustPainter(gui, overlay, rustPalette);
		
		dialog = new JDialog(parent, "Draw Settings", ModalityType.APPLICATION_MODAL);
		dialog.setResizable(false);
		dialog.setSize(REGULAR);
		dialog.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		rootPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dialog.getContentPane().add(rootPanel);
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		
		JLabel lblShapeCount = new JLabel("Shape Count");
		rootPanel.add(lblShapeCount);
		
		panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rootPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		Dimension buttonSize = new Dimension(60, 20);
		shapesSlider = new JSlider();
		maxShapesField = new JIntegerField(0);
		maxShapesField.setPreferredSize(buttonSize);
		maxShapesField.setMaximumSize(buttonSize);
		maxShapesField.setMinimumSize(buttonSize);
		maxShapesField.setFocusable(true);
		maxShapesField.setAlignmentX(0.0f);
		maxShapesField.addActionListener((event) -> {
			int value = maxShapesField.getNumberValue();
			shapesSlider.setValue(value);
			overlay.setEstimatedGenerationLabel(value, gui.getSettingsMaxShapes());
			overlay.renderShapes(value);
			overlay.repaint();
		});
		panel.add(maxShapesField);
		
		JLabel lblNewLabel = new JLabel("1");
		lblNewLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
		panel.add(lblNewLabel);
		
		shapesSlider.setOpaque(false);
		shapesSlider.setMinimum(1);
		shapesSlider.setFocusable(false);
		shapesSlider.setAlignmentX(0.0f);
		shapesSlider.addChangeListener((event) -> {
			int value = shapesSlider.getValue();
			maxShapesField.setText(Integer.toString(value));
			overlay.setEstimatedGenerationLabel(value, gui.getSettingsMaxShapes());
			overlay.renderShapes(value);
			overlay.repaint();
		});
		panel.add(shapesSlider);
		
		lblMaximumShape = new JLabel("1");
		lblMaximumShape.setBorder(new EmptyBorder(0, 5, 0, 10));
		panel.add(lblMaximumShape);
		
		JLabel lblClickInterval = new JLabel("Clicks per second");
		lblClickInterval.setToolTipText("The amount of clicks each second");
		rootPanel.add(lblClickInterval);
		
		JPanel panel_1 = new JPanel();
		panel_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		rootPanel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		Dimension buttonSize2 = new Dimension(60, 20);
		clickIntervalField = new JIntegerField(gui.getSettingsClickInterval());
		clickIntervalField.setPreferredSize(buttonSize2);
		clickIntervalField.setMaximumSize(buttonSize2);
		clickIntervalField.setMinimumSize(buttonSize2);
		clickIntervalField.setFocusable(true);
		clickIntervalField.setMinimum(1);
		clickIntervalField.setMaximum(60);
		clickIntervalField.setAlignmentX(0.0f);
		clickIntervalField.addActionListener((event) -> {
			gui.setSettingsClickInterval(clickIntervalField.getNumberValue());
			overlay.setEstimatedGenerationLabel(maxShapesField.getNumberValue(), gui.getSettingsMaxShapes());
			overlay.repaint();
		});
		panel_1.add(clickIntervalField);
		
		btnCalculateExactTime = new JButton("Calculate Exact Time");
		btnCalculateExactTime.setFocusable(false);
		btnCalculateExactTime.addActionListener((event) -> {
			int count = maxShapesField.getNumberValue();
			
			BlobList list = RustUtil.convertToList(overlay.getBorstData().getModel(), count);
			list = BorstSorter.sort(list);
			int after = RustUtil.getScore(list);
			
			int totalClicks = after + count;
			
			int interval = (int)clickIntervalField.getNumberValue();
			overlay.setExactGenerationLabel((long)(totalClicks * (1000.0 / (double)interval)));
			overlay.repaint();
		});
		rootPanel.add(btnCalculateExactTime);
		
		btnStartDrawing = new JButton("Start Drawing");
		btnSelectColorPalette = new JButton("Select Color Palette");
		btnSelectColorPalette.setFocusable(false);
		btnSelectColorPalette.addActionListener((event) -> {
			btnStartDrawing.setEnabled(false);
			
			if(findColorPalette()) {
				btnStartDrawing.setEnabled(true);
			} else {
				showPaletteWarning();
			}
		});
		rootPanel.add(btnSelectColorPalette);
		
		btnStartDrawing.setFocusable(false);
		btnStartDrawing.setEnabled(false);
		btnStartDrawing.addActionListener((event) -> {
			Point previous_location = dialog.getLocation();
			btnStartDrawing.setEnabled(false);
			
			Thread thread = new Thread(() -> {
				try {
					Point p = overlay.dialog.getLocation();
					dialog.setLocation(p.x, p.y);
					dialog.setSize(MINIMIZED);
					overlay.setHideRegions(true);
					
					int count = maxShapesField.getNumberValue();
					BlobList list = BorstSorter.sort(RustUtil.convertToList(overlay.getBorstData().getModel(), count));
					if(!rustPainter.startDrawing(list)) {
						LogUtils.warn("The user stoped the drawing process early");
					}
				} catch(IllegalStateException e) {
					LogUtils.warn("The user stoped the drawing process early");
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					btnStartDrawing.setEnabled(true);
					dialog.setLocation(previous_location);
					dialog.setSize(REGULAR);
				}
			});
			thread.setDaemon(true);
			thread.start();
		});
		rootPanel.add(btnStartDrawing);
	}
	
	private void showPaletteWarning() {
		JEditorPane pane = new JEditorPane("text/html", "");
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		pane.setText(
			"Could not find the color palette.<br>" +
			"If you think this is a bug please take a screenshot and create a new issue on the github.<br>" +
			"<a href=\"#blank\">https://github.com/Bob-Rust/Bob-Rust-Java/issues/new</a>"
		);
		pane.addHyperlinkListener((e) -> {
			if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				UrlUtils.openIssueUrl();
			}
		});
		JOptionPane.showConfirmDialog(dialog, pane, "Could not find the palette", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean findColorPalette() {
		// The bounds of the screen.
		Rectangle screen = overlay.dialog.getBounds();
		
		// Check for bright red on the edge of the screen.
		try {
			BufferedImage screenshot = new Robot().createScreenCapture(screen);
			Point paletteLocation = rustPalette.findPalette(screenshot);
			
			if(paletteLocation != null) {
				overlay.colorRegion.setLocation(paletteLocation.x, paletteLocation.y + 132 + 100);
				Point paletteScreenLocation = new Point(screen.x + paletteLocation.x, screen.y + paletteLocation.y);
				
				if(rustPalette.analyse(dialog, screenshot, screen.getLocation(), paletteScreenLocation)) {
					// Found the palette.
					LogUtils.info("Found the color palette (%d, %d)", paletteScreenLocation.x, paletteScreenLocation.y);
					overlay.repaint();
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		LogUtils.warn("User needs to manually select the color palette");
		return false;
	}
	
	public void openDialog(Point point) {
		// Force the user to reset the palette.
		btnStartDrawing.setEnabled(false);
		rustPalette.reset();
		
		// Clear the overlay.
		overlay.repaint();
		
		int value = overlay.getBorstData().getIndex();
		shapesSlider.setMaximum(value);
		shapesSlider.setValue(value);
		maxShapesField.setText(Integer.toString(value));
		lblMaximumShape.setText(Integer.toString(value));
		dialog.setLocation(point);
		dialog.setSize(REGULAR);
		dialog.setVisible(true);
		rustPalette.reset();
		
		try {
			gui.setSettingsClickInterval(Integer.parseInt(clickIntervalField.getText()));
		} catch(NumberFormatException e) {
			LogUtils.warn("Invalid click interval '%s'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(gui.getSettingsClickInterval()));
		}
	}
	
	public BobRustPalette getPalette() {
		return rustPalette;
	}
}
