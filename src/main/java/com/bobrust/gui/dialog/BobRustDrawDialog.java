package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import com.bobrust.settings.Settings;
import com.bobrust.util.RustConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.gui.BobRustDesktopOverlay;
import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.robot.BobRustPainter;
import com.bobrust.robot.BobRustPalette;
import com.bobrust.util.RustUtil;
import com.bobrust.util.RustWindowUtil;
import com.bobrust.util.UrlUtils;

public class BobRustDrawDialog {
	private static final Logger LOGGER = LogManager.getLogger(BobRustDrawDialog.class);
	private static final Dimension REGULAR = new Dimension(320, 240);
	private static final Dimension MINIMIZED = new Dimension(120, 240);
	
	private final BobRustDesktopOverlay overlay;
	private final BobRustPalette rustPalette;
	private final BobRustPainter rustPainter;
	
	private final JDialog dialog;
	private final JDialog parentDialog;
	
	private final JPanel panel;
	private final JIntegerField maxShapesField;
	private final JIntegerField clickIntervalField;
	private final JSlider shapesSlider;
	private final JLabel lblMaximumShape;
	private final JButton btnCalculateExactTime;
	private final JButton btnSelectColorPalette;
	private final JButton btnStartDrawing;
	
	private boolean isPainting;
	
	public BobRustDrawDialog(BobRustDesktopOverlay overlay, JDialog parent) {
		this.parentDialog = parent;
		this.overlay = overlay;
		this.rustPalette = new BobRustPalette();
		this.rustPainter = new BobRustPainter(overlay, rustPalette);
		
		dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_DRAWDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		dialog.setResizable(false);
		dialog.setSize(REGULAR);
		dialog.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		rootPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dialog.getContentPane().add(rootPanel);
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		
		JLabel lblShapeCount = new JLabel(RustUI.getString(Type.ACTION_SHAPECOUNT_LABEL));
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
			overlay.setEstimatedGenerationLabel(value, Settings.SettingsMaxShapes.get());
			overlay.setRenderPreviewShapes(value);
			overlay.repaint();
		});
		panel.add(maxShapesField);
		
		JLabel lblMinimumShape = new JLabel("1");
		lblMinimumShape.setBorder(new EmptyBorder(0, 10, 0, 5));
		panel.add(lblMinimumShape);
		
		shapesSlider.setOpaque(false);
		shapesSlider.setMinimum(1);
		shapesSlider.setFocusable(false);
		shapesSlider.setAlignmentX(0.0f);
		shapesSlider.addChangeListener((event) -> {
			int value = shapesSlider.getValue();
			maxShapesField.setText(Integer.toString(value));
			overlay.setEstimatedGenerationLabel(value, Settings.SettingsMaxShapes.get());
			overlay.setRenderPreviewShapes(value);
			overlay.repaint();
		});
		panel.add(shapesSlider);
		
		lblMaximumShape = new JLabel("1");
		lblMaximumShape.setBorder(new EmptyBorder(0, 5, 0, 10));
		panel.add(lblMaximumShape);
		
		JLabel lblClickInterval = new JLabel(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_LABEL));
		lblClickInterval.setToolTipText(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_TOOLTIP));
		rootPanel.add(lblClickInterval);
		
		JPanel panel_1 = new JPanel();
		panel_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		rootPanel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		Dimension buttonSize2 = new Dimension(60, 20);
		clickIntervalField = new JIntegerField(Settings.SettingsClickInterval.get());
		clickIntervalField.setPreferredSize(buttonSize2);
		clickIntervalField.setMaximumSize(buttonSize2);
		clickIntervalField.setMinimumSize(buttonSize2);
		clickIntervalField.setFocusable(true);
		clickIntervalField.setMinimum(1);
		clickIntervalField.setMaximum(60);
		clickIntervalField.setAlignmentX(0.0f);
		clickIntervalField.addActionListener((event) -> {
			Settings.SettingsClickInterval.set(clickIntervalField.getNumberValue());
			overlay.setEstimatedGenerationLabel(maxShapesField.getNumberValue(), Settings.SettingsMaxShapes.get());
			overlay.repaint();
		});
		panel_1.add(clickIntervalField);
		
		btnCalculateExactTime = new JButton(RustUI.getString(Type.ACTION_CALCULATEEXACTTIME_BUTTON));
		btnCalculateExactTime.setFocusable(false);
		btnCalculateExactTime.addActionListener((event) -> {
			int count = maxShapesField.getNumberValue();
			
			BlobList list = RustUtil.convertToList(overlay.getBorstData().getModel(), count);
			list = BorstSorter.sort(list);
			int after = RustUtil.getScore(list);
			
			int totalClicks = after + count;
			
			int interval = (int) clickIntervalField.getNumberValue();
			overlay.setExactGenerationLabel((long)(totalClicks * (1000.0 / (double)interval)));
			overlay.repaint();
		});
		rootPanel.add(btnCalculateExactTime);
		
		btnStartDrawing = new JButton(RustUI.getString(Type.ACTION_STARTDRAWING_BUTTON));
		btnSelectColorPalette = new JButton(RustUI.getString(Type.ACTION_SELECTCOLORPALETTE_BUTTON));
		btnSelectColorPalette.setFocusable(false);
		btnSelectColorPalette.addActionListener((event) -> {
			btnStartDrawing.setEnabled(false);
			
			if (findColorPalette()) {
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
			isPainting = true;
			
			Thread thread = new Thread(() -> {
				try {
					Point p = parentDialog.getLocation();
					dialog.setLocation(p.x, p.y);
					dialog.setSize(MINIMIZED);
					overlay.setHideRegions();
					
					int count = maxShapesField.getNumberValue();
					BlobList list;
					if (RustConstants.DEBUG_DRAWN_COLORS) {
						list = rustPainter.generateDebugDrawList();
					} else {
						list = BorstSorter.sort(RustUtil.convertToList(overlay.getBorstData().getModel(), count));
					}
					
					if (!rustPainter.startDrawing(list)) {
						LOGGER.warn("The user stopped the drawing process early");
					}
				} catch (IllegalStateException e) {
					LOGGER.warn("The user stopped the drawing process early");
					LOGGER.warn("Message: {}", e.getMessage());
				} catch (Exception e) {
					LOGGER.throwing(e);
				} finally {
					isPainting = false;
					btnStartDrawing.setEnabled(true);
					dialog.setLocation(previous_location);
					dialog.setSize(REGULAR);
				}
			}, "BobRustDrawing Thread");
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
		JOptionPane.showConfirmDialog(dialog, pane, RustUI.getString(Type.ACTION_PALETTEWARNINGDIALOG_TITLE), JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean findColorPalette() {
		// The bounds of the screen
		Rectangle screenBounds = parentDialog.getBounds();
		
		// Check for bright red on the edge of the screen
		try {
			// TODO: Get the true size of the screen
			BufferedImage screenshot = RustWindowUtil.captureScreenshot(overlay.getMonitorConfiguration());
			Point paletteLocation = rustPalette.findPalette(screenshot);
			
			if (paletteLocation != null) {
				overlay.colorRegion.setLocation(paletteLocation.x, paletteLocation.y + 132 + 100);
				Point paletteScreenLocation = new Point(screenBounds.x + paletteLocation.x, screenBounds.y + paletteLocation.y);
				
				if (rustPalette.analyse(dialog, screenshot, screenBounds, paletteScreenLocation)) {
					// Found the palette
					LOGGER.info("Found the color palette ({}, {})", paletteScreenLocation.x, paletteScreenLocation.y);
					overlay.repaint();
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LOGGER.warn("User needs to manually select the color palette");
		return false;
	}
	
	public boolean isVisible() {
		return dialog.isVisible();
	}

	public boolean isPainting() {
		return isPainting;
	}
	
	public void openDialog(Point point) {
		// Force the user to reset the palette
		btnStartDrawing.setEnabled(false);
		rustPalette.reset();
		
		// Clear the overlay
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
			Settings.SettingsClickInterval.set(Integer.parseInt(clickIntervalField.getText()));
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid click interval '{}'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(Settings.SettingsClickInterval.get()));
		}
	}
	
	public BobRustPalette getPalette() {
		return rustPalette;
	}
}
