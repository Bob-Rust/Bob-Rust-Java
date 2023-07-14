package com.bobrust.gui.dialog;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstSettings;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.robot.BobRustPainter;
import com.bobrust.robot.BobRustPalette;
import com.bobrust.robot.error.PaintingInterrupted;
import com.bobrust.settings.Settings;
import com.bobrust.util.*;
import com.bobrust.util.data.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: The draw dialog is going to start the borst generator and update the UI to see the new changes
//       The draw dialog should have a way to keep drawing even if you need to stop. If the process stopped
//       the user should be able to select the canvas again and all shapes should be properly updated
//       The image rect should not be able to be updated inside the draw dialog

// TODO: 1. Show the generation live on the screen
//          1. The user should not be able to hide it but to minimize in case you want to do stuff in the game
//       2. If the generation is paused the user should be able to play it for were it last left of
//          1. The user will need access to the canvas region mover
//          2. The image region should move relative to the canvas region
//          3. The draw script should continue with the new values
public class DrawDialog extends JDialog {
	private static final Logger LOGGER = LogManager.getLogger(DrawDialog.class);
	private static final Dimension REGULAR = new Dimension(320, 240);
	private static final Dimension MINIMIZED = new Dimension(120, 40);
	
	private final BobRustPalette rustPalette;
	private final BobRustPainter rustPainter;
	
	private final ScreenDrawDialog parent;
	
	private final JIntegerField maxShapesField;
	private final JIntegerField clickIntervalField;
	final JSlider shapesSlider;
	private final JLabel maxShapeLabel;
	private final JButton btnSelectColorPalette;
	private final JButton btnStartDrawing;
	
	// Borst stuff
	private final BorstGenerator borstGenerator;
	private final BorstSettings borstSettings;
	
	// TODO: This field should become final
	private BorstGenerator.BorstData lastData;
	
	public DrawDialog(ScreenDrawDialog parent) {
		super(parent, "Draw Settings", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		this.rustPalette = new BobRustPalette();
		this.rustPainter = new BobRustPainter(rustPalette);
		this.borstSettings = new BorstSettings();
		this.borstGenerator = new BorstGenerator(borstSettings, this::onBorstData);
		
		setIconImage(AppConstants.DIALOG_ICON);
		setResizable(false);
		setSize(REGULAR);
		
		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
		rootPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		setContentPane(rootPanel);
		
		rootPanel.add(new JLabel("Shape Count"));
		
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		rootPanel.add(panel);
		
		Dimension buttonSize = new Dimension(60, 20);
		shapesSlider = new JSlider();
		maxShapesField = new JIntegerField(0, 0, 999999);
		maxShapesField.setMaximumSize(buttonSize);
		maxShapesField.setPreferredSize(buttonSize);
		maxShapesField.addActionListener((event) -> {
			int value = maxShapesField.getNumberValue();
			shapesSlider.setValue(value);
			// overlay.setEstimatedGenerationLabel(value, Settings.SettingsMaxShapes.get());
			// overlay.setRenderPreviewShapes(value);
			
			parent.repaint();
		});
		panel.add(maxShapesField);
		
		JLabel minShapeLabel = new JLabel("1");
		minShapeLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
		panel.add(minShapeLabel);
		
		shapesSlider.setMinimum(1);
		shapesSlider.setFocusable(false);
		shapesSlider.setOpaque(false);
		shapesSlider.addChangeListener((event) -> {
			int value = shapesSlider.getValue();
			if (value > shapesSlider.getMaximum()) {
				System.out.println("!!!!");
			}
			maxShapesField.setText(Integer.toString(value));
			// overlay.setEstimatedGenerationLabel(value, Settings.SettingsMaxShapes.get());
			// overlay.setRenderPreviewShapes(value);
			
			parent.topPanel.setGeneratedShapes(shapesSlider.getValue(), lastData.getIndex());
			parent.repaint();
		});
		panel.add(shapesSlider);
		
		maxShapeLabel = new JLabel("1");
		maxShapeLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
		panel.add(maxShapeLabel);
		
		JLabel clickIntervalLabel = new JLabel("Clicks per second");
		clickIntervalLabel.setToolTipText("The amount of clicks per second");
		rootPanel.add(clickIntervalLabel);
		
		JPanel clickIntervalPanel = new JPanel();
		clickIntervalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rootPanel.add(clickIntervalPanel);
		clickIntervalPanel.setLayout(new BoxLayout(clickIntervalPanel, BoxLayout.X_AXIS));
		
		Dimension buttonSize2 = new Dimension(60, 20);
		clickIntervalField = new JIntegerField(
			Settings.SettingsClickInterval.get(),
			Settings.SettingsClickInterval.getMin(),
			Settings.SettingsClickInterval.getMax()
		);
		clickIntervalField.setPreferredSize(buttonSize2);
		clickIntervalField.setMaximumSize(buttonSize2);
		clickIntervalField.setMinimumSize(buttonSize2);
		clickIntervalField.setAlignmentX(0.0f);
		clickIntervalField.addActionListener((event) -> {
			Settings.SettingsClickInterval.set(clickIntervalField.getNumberValue());
			// overlay.setEstimatedGenerationLabel(maxShapesField.getNumberValue(), Settings.SettingsMaxShapes.get());
			// overlay.repaint();
		});
		clickIntervalPanel.add(clickIntervalField);
		
		JButton exactTimeButton = new JButton("Calculate Exact Time");
		exactTimeButton.setFocusable(false);
		exactTimeButton.addActionListener((event) -> {
			int count = maxShapesField.getNumberValue();
			
			// TODO: Make sure that lastData is not null
			// TODO: Synchronize on object first
			
			int actions = 0;
			int groups = 1000; // Low enough to not freeze for a long time for 200k shapes
			for (int i = 0; i < count; i += groups) {
				actions += RustUtil.getScore(BorstSorter.sort(RustUtil.convertToList(lastData, i, groups)), 0);
			}
			
			int totalClicks = actions + count;
			
			System.out.println("Actions: " + totalClicks);
			int interval = clickIntervalField.getNumberValue();
			parent.topPanel.setExactGenerationLabel((long)(totalClicks * (1000.0 / (double)interval)));
		});
		rootPanel.add(exactTimeButton);
		
		btnStartDrawing = new JButton("Start Drawing");
		btnSelectColorPalette = new JButton("Select Color Palette");
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
			Point previous_location = getLocation();
			btnStartDrawing.setEnabled(false);
			
			startDrawingAction(previous_location);
		});
		rootPanel.add(btnStartDrawing);
	}
	
	private void startDrawingAction(Point previous_location) {
		parent.setAlwaysOnTop(true);
		parent.repaint();
		setAlwaysOnTop(true);
		start = -1;
		
		Thread thread = new Thread(() -> {
			try {
				setLocation(monitor.getBounds().getLocation());
				setSize(MINIMIZED);
				
				int count = maxShapesField.getNumberValue();
				BlobList list;
				
				// TODO: Make sure 'lastData' is not null
				synchronized (lastData) {
					System.out.println(lastData.getBlobs().size());
					count = lastData.getBlobs().size();
					list = BorstSorter.sort(RustUtil.convertToList(lastData, 0, count));
				}
				
				if (!rustPainter.startDrawing(monitor, parent.parent.getCanvasRect(), list, 0, this::updateTimeRemaining)) {
					LOGGER.warn("The user stopped the drawing process early");
				}
			} catch (PaintingInterrupted e) {
				LOGGER.warn("The user stopped the drawing process early");
				LOGGER.warn("Type : {}", e.getInterruptType());
				LOGGER.warn("Shapes: {}", e.getDrawnShapes());
			} catch (Exception e) {
				LOGGER.throwing(e);
			} finally {
				parent.setAlwaysOnTop(false);
				setAlwaysOnTop(false);
				
				btnStartDrawing.setEnabled(true);
				setLocation(previous_location);
				setSize(REGULAR);
			}
		}, "BobRustDrawing Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	private long start = -1;
	private double msDelay = 0;
	private void updateTimeRemaining(int index, int length) {
		long now = System.nanoTime();
		if (start == -1) {
			start = now;
			msDelay = (1000.0 / Settings.SettingsClickInterval.get());
		} else if (now - start > 50000000) {
			// 50 ms has passed
			msDelay = ((now - start) / 1000000.0) / index;
		}
		
		parent.topPanel.setDrawnShapes(index, length, (int) (msDelay * (length - index)));
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
			if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
				UrlUtils.openIssueUrl();
			}
		});
		JOptionPane.showMessageDialog(this, pane, "Could not find the palette", JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean findColorPalette() {
		// The bounds of the screen
		Rectangle screenBounds = monitor.getBounds();
		
		// Take a screenshot
		BufferedImage screenshot = RustWindowUtil.captureScreenshot(monitor);
		if (screenshot == null) {
			LOGGER.info("Failed to take screenshot. Was null");
			return false;
		}
		
		// Check for bright red on the edge of the screen
		Point paletteLocation = rustPalette.findPalette(screenshot);
		if (paletteLocation == null) {
			return false;
		}
		
		try {
			// overlay.colorRegion.setLocation(paletteLocation.x, paletteLocation.y + 132 + 100);
			Point paletteScreenLocation = new Point(screenBounds.x + paletteLocation.x, screenBounds.y + paletteLocation.y);
			
			if (rustPalette.analyse(this, screenshot, screenBounds, paletteScreenLocation)) {
				// Found the palette
				LOGGER.info("Found the color palette ({}, {})", paletteScreenLocation.x, paletteScreenLocation.y);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LOGGER.warn("User needs to manually select the color palette");
		return false;
	}
	
	private void startGeneration() {
		Sign signType = Settings.SettingsSign.get();
		Color bgColor = Settings.getSettingsBackgroundCalculated();
		
		BufferedImage scaled = RustImageUtil.getScaledInstance(
			parent.parent.getDrawImage(),
			parent.parent.getCanvasRect(),
			parent.parent.getImageRect(),
			signType.getWidth(),
			signType.getHeight(),
			bgColor,
			Settings.SettingsScaling.get()
		);
		
		// Apply the ICC cmyk lut filter
		if (Settings.SettingsUseICCConversion.get()) {
			scaled = RustImageUtil.applyFilters(scaled);
		}
		
		BorstSettings settings = borstSettings;
		settings.MaxShapes = Settings.SettingsMaxShapes.get();
		settings.Alpha = Settings.SettingsAlpha.get();
		settings.CallbackInterval = Settings.EditorCallbackInterval.get();
		settings.Background = bgColor.getRGB();
		settings.DirectImage = scaled;
		
		if (borstGenerator.start()) {
			parent.shapeRender.reset();
			parent.shapeRender.createCanvas(scaled.getWidth(), scaled.getHeight(), bgColor.getRGB());
		}
	}
	
	private void onBorstData(BorstGenerator.BorstData data) {
		lastData = data;
		parent.repaint();
		
		// TODO: There exists a weird beep sound sometimes when max is changed!
		// Follow maximum
		shapesSlider.setMinimum(1);
		if (shapesSlider.getValue() == shapesSlider.getMaximum()) {
			shapesSlider.setMaximum(data.getIndex());
			shapesSlider.setValue(data.getIndex());
		} else {
			shapesSlider.setMaximum(data.getIndex());
		}
		
		parent.topPanel.setGeneratedShapes(shapesSlider.getValue(), data.getIndex());
		maxShapeLabel.setText(Integer.toString(data.getIndex()));
	}
	
	public BorstGenerator.BorstData getBorstData() {
		return lastData;
	}
	
	private GraphicsConfiguration monitor;
	public void openDialog(GraphicsConfiguration monitor, Point point) {
		this.monitor = monitor;
		
		// Force the user to reset the palette
		btnStartDrawing.setEnabled(false);
		rustPalette.reset();
		
		// Update old graphics
		parent.shapeRender.reset();
		parent.repaint();
		
		// Before we block
		startGeneration();
		
		setLocation(point);
		setSize(REGULAR);
		setVisible(true);
		dispose();
		
		try {
			// TODO: Make this not throw
			borstGenerator.stop();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		try {
			Settings.SettingsClickInterval.set(Integer.parseInt(clickIntervalField.getText()));
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid click interval '{}'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(Settings.SettingsClickInterval.get()));
		}
	}
}
