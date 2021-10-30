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
import com.bobrust.util.BobRustUtil;

public class BobRustDrawDialog {
	private static final Dimension REGULAR = new Dimension(320, 240);
	private static final Dimension MINIMIZED = new Dimension(120, 240);
	
	private final BobRustEditor gui;
	private final BobRustOverlay overlay;
	private final BobRustPalette rustPalette;
	private final BobRustPainter rustPainter;
	
	private final JDialog dialog;
	
	private final JPanel panel;
	private final JIntegerField maxShapesTextField;
	private final JIntegerField clickInterval;
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
		maxShapesTextField = new JIntegerField(0);
		maxShapesTextField.setPreferredSize(buttonSize);
		maxShapesTextField.setMaximumSize(buttonSize);
		maxShapesTextField.setMinimumSize(buttonSize);
		maxShapesTextField.setFocusable(true);
		maxShapesTextField.setAlignmentX(0.0f);
		maxShapesTextField.addActionListener((event) -> {
			int value = maxShapesTextField.getNumberValue();
			shapesSlider.setValue(value);
			overlay.setEstimatedGenerationLabel(value, gui.getSettingsMaxShapes());
			overlay.renderShapes(value);
			overlay.repaint();
		});
		panel.add(maxShapesTextField);
		
		JLabel lblNewLabel = new JLabel("1");
		lblNewLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
		panel.add(lblNewLabel);
		
		shapesSlider.setOpaque(false);
		shapesSlider.setMinimum(1);
		shapesSlider.setFocusable(false);
		shapesSlider.setAlignmentX(0.0f);
		shapesSlider.addChangeListener((event) -> {
			int value = shapesSlider.getValue();
			maxShapesTextField.setText(Integer.toString(value));
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
		clickInterval = new JIntegerField(gui.getSettingsClickInterval());
		clickInterval.setPreferredSize(buttonSize2);
		clickInterval.setMaximumSize(buttonSize2);
		clickInterval.setMinimumSize(buttonSize2);
		clickInterval.setFocusable(true);
		clickInterval.setMinimum(1);
		clickInterval.setMaximum(60);
		clickInterval.setAlignmentX(0.0f);
		clickInterval.addActionListener((event) -> {
			gui.setSettingsClickInterval(clickInterval.getNumberValue());
			overlay.setEstimatedGenerationLabel(maxShapesTextField.getNumberValue(), gui.getSettingsMaxShapes());
			overlay.repaint();
		});
		panel_1.add(clickInterval);
		
		btnCalculateExactTime = new JButton("Calculate Exact Time");
		btnCalculateExactTime.setFocusable(false);
		btnCalculateExactTime.addActionListener((event) -> {
			int count = maxShapesTextField.getNumberValue();
			
			BlobList list = BobRustUtil.convertToList(overlay.getBorstData().getModel(), count);
			list = BorstSorter.sort(list);
			int after = BobRustUtil.getScore(list);
			
			int totalClicks = after + count;
			
			int interval = (int)clickInterval.getNumberValue();
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
					
					int count = maxShapesTextField.getNumberValue();
					BlobList list = BorstSorter.sort(BobRustUtil.convertToList(overlay.getBorstData().getModel(), count));
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
			"""
			Could not find the color palette.<br>
			If you think this is a bug please create a new issue on the github.<br>
			<a href="#blank">https://github.com/Bob-Rust/Bob-Rust-Java/issues/new</a>
			"""
		);
		pane.addHyperlinkListener((e) -> {
			if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				gui.openIssueUrl();
			}
		});
		JOptionPane.showConfirmDialog(dialog, pane, "Could not find the palette", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean findColorPalette() {
		Rectangle screen = overlay.dialog.getBounds();
		
		// Check for bright red on the edge of the screen.
		try {
			BufferedImage screenshot = new Robot().createScreenCapture(screen);
			
			// ef4431
			int x = screen.width - 43;
			
			Point red_middle = null;
			for(int i = 0, lastNonRed = 0; i < screen.height; i++) {
				int red = (screenshot.getRGB(x, i) >> 16) & 0xff;
				
				// above 220.

				if(red < 220) {
					lastNonRed = i;
				}
				
				if(i - lastNonRed > 40) {
					// We found the circle probably.
					red_middle = new Point(x, i - 10);
					break;
				}
			}
			
			if(red_middle != null) {
				// 150x264
				overlay.colorRegion.setLocation(screen.width - 150, red_middle.y - 163 + 132 + 100);
				
				if(rustPalette.analyse(dialog, screenshot, new Point(screen.width - 150, red_middle.y - 163))) {
					// Found the palette.
					LogUtils.info("Found the color palette (%d, %d)", red_middle.x, red_middle.y);
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
		maxShapesTextField.setText(Integer.toString(value));
		lblMaximumShape.setText(Integer.toString(value));
		dialog.setLocation(point);
		dialog.setSize(REGULAR);
		dialog.setVisible(true);
		rustPalette.reset();
		
		try {
			gui.setSettingsClickInterval(Integer.parseInt(clickInterval.getText()));
		} catch(NumberFormatException e) {
			LogUtils.warn("Invalid click interval '%s'", clickInterval.getText());
			clickInterval.setText(Integer.toString(gui.getSettingsClickInterval()));
		}
	}
	
	public BobRustPalette getPalette() {
		return rustPalette;
	}
}
