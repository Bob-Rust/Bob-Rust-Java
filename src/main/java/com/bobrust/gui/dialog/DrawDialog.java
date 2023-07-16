package com.bobrust.gui.dialog;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstUtils;
import com.bobrust.generator.Model;
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
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	private static final Dimension REGULAR = new Dimension(320, 200);
	private static final Dimension MINIMIZED = new Dimension(120, 40);
	
	private final BobRustPalette rustPalette;
	private final BobRustPainter rustPainter;
	private final RegionSelectionDialog selectionDialog;
	
	private final ScreenDrawDialog parent;
	
	private final JTextField maxShapesField;
	private final JIntegerField clickIntervalField;
	final JSlider shapesSlider;
	
	private final JLabel minShapeLabel;
	private final JLabel maxShapeLabel;
	
	// Borst stuff
	private final BorstGenerator borstGenerator;
	private GraphicsConfiguration monitor;
	private Model previousBorstModel;
	private int drawnShapes;
	private BlobList previouslyUsed = new BlobList(); // We need to continue using these instructions
	
	// TODO: This field should become final
	private BorstGenerator.BorstData lastData;
	
	public DrawDialog(ScreenDrawDialog parent) {
		super(parent, "Draw Settings", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		this.rustPalette = new BobRustPalette();
		this.rustPainter = new BobRustPainter(rustPalette);
		this.borstGenerator = new BorstGenerator(this::onBorstData);
		this.selectionDialog = new RegionSelectionDialog(this, false);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(AppConstants.DIALOG_ICON);
		setResizable(false);
		setSize(REGULAR);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				clickIntervalField.requestFocus();
			}
		});
		
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
		maxShapesField = new JTextField("1");
		maxShapesField.setMaximumSize(buttonSize);
		maxShapesField.setPreferredSize(buttonSize);
		maxShapesField.addActionListener((event) -> {
			int value;
			try {
				value = Integer.parseInt(maxShapesField.getText());
			} catch (NumberFormatException e) {
				Toolkit.getDefaultToolkit().beep();
				
				value = shapesSlider.getValue();
				maxShapesField.setText(Integer.toString(value));
			}
			
			shapesSlider.setValue(value);
			parent.repaint();
		});
		panel.add(maxShapesField);
		
		minShapeLabel = new JLabel("1");
		minShapeLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
		panel.add(minShapeLabel);
		
		shapesSlider.setMinimum(1);
		shapesSlider.setUI(new BasicSliderUI(shapesSlider) {
			@Override
			public void paintFocus(Graphics g) {
				// don't paint focus
			}
		});
		shapesSlider.setOpaque(false);
		shapesSlider.addChangeListener((event) -> {
			int value = shapesSlider.getValue();
			if (!maxShapesField.hasFocus()) {
				maxShapesField.setText(Integer.toString(value));
			}
			
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
		clickIntervalField.setFocusable(true);
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
			int count = shapesSlider.getValue();
			
			// TODO: Make sure that lastData is not null
			// TODO: Synchronize on object first
			
			int actions = RustUtil.getScore(BorstSorter.sort(RustUtil.convertToList(lastData, 0, count)), 0);
			int totalClicks = actions + count;
			
			System.out.println("Actions: " + totalClicks);
			int interval = clickIntervalField.getNumberValue();
			parent.topPanel.setExactGenerationLabel((long)(totalClicks * (1000.0 / (double)interval)));
		});
		rootPanel.add(exactTimeButton);
		
		JButton changeAreaButton = new JButton("Update canvas area");
		changeAreaButton.setFocusable(false);
		changeAreaButton.addActionListener((event) -> {
			parent.setAlwaysOnTop(true);
			parent.repaint();
			parent.updateCanvasRect(selectionDialog.openDialog(monitor, false, null, parent.canvasRect).selection());
			parent.setAlwaysOnTop(false);
			parent.repaint();
		});
		rootPanel.add(changeAreaButton);
		
		JButton colorPaletteButton = new JButton("Select Color Palette And Draw");
		colorPaletteButton.setFocusable(false);
		colorPaletteButton.addActionListener((event) -> {
			if (findColorPalette()) {
				previousBorstModel = borstGenerator.stop();
				
				Point previous_location = getLocation();
				startDrawingAction(previous_location);
			} else {
				showPaletteWarning();
			}
		});
		rootPanel.add(colorPaletteButton);
	}
	
	private void startDrawingAction(Point previous_location) {
		parent.setAlwaysOnTop(true);
		parent.repaint();
		setAlwaysOnTop(true);
		start = -1;
		
		Thread thread = new Thread(() -> {
			int offsetShapes = 0;
			try {
				setLocation(monitor.getBounds().getLocation());
				setSize(MINIMIZED);
				
				int count = shapesSlider.getValue();
				BlobList list;
				
				// TODO: Make sure 'lastData' is not null
				synchronized (lastData) {
					// TODO: 'previouslyUsed' should start at 'drawnShapes'
					int previouslyComputed = previouslyUsed.size();
					if (previouslyComputed <= drawnShapes || previouslyComputed <= count) {
						// Fill the previouslyUsed list with new data
						int missing = count - previouslyComputed;
						BlobList missingList = BorstSorter.sort(RustUtil.convertToList(lastData, count - missing, missing));
						previouslyUsed.getList().addAll(missingList.getList());
					}
					
					
					list = new BlobList();
					list.assign(previouslyUsed.getList(), drawnShapes, count - drawnShapes);
					// System.out.println(previouslyUsed.size() + ", " + drawnShapes + ", " + list.size());
					// System.out.println(lastData.getBlobs().size());
					// count = lastData.getBlobs().size();
					// list = BorstSorter.sort(RustUtil.convertToList(lastData, drawnShapes, count));
				}
				
				updateTimeRemaining(0, count - drawnShapes);
				
				start = -1;
				if (!rustPainter.startDrawing(monitor, parent.parent.getCanvasRect(), list, 0, this::updateTimeRemaining)) {
					LOGGER.warn("The user stopped the drawing process early");
				}
			} catch (PaintingInterrupted e) {
				LOGGER.warn("The user stopped the drawing process early");
				LOGGER.warn("Type   : {}", e.getInterruptType());
				LOGGER.warn("Shapes : {}", e.getDrawnShapes());
				offsetShapes = e.getDrawnShapes();
			} catch (Exception e) {
				LOGGER.throwing(e);
			} finally {
				parent.setAlwaysOnTop(false);
				setAlwaysOnTop(false);
				parent.repaint();
				
				setLocation(previous_location);
				setSize(REGULAR);
				
				// Start generation again
				startGeneration(offsetShapes);
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
		} else if (now - start > 50000000) { // 50 ms
			msDelay = ((now - start) / 1000000.0) / index;
		}
		
		parent.topPanel.setDrawnShapes(drawnShapes + index, drawnShapes + length, (int) (msDelay * (length - index)));
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
	
	private void startGeneration(int offset) {
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
		
		drawnShapes += offset;
		minShapeLabel.setText(Integer.toString(drawnShapes + 1));
		shapesSlider.setMinimum(drawnShapes + 1);
		
		if (borstGenerator.start(
			previousBorstModel,
			scaled,
			Settings.SettingsMaxShapes.get(),
			Settings.EditorCallbackInterval.get(),
			bgColor.getRGB(),
			BorstUtils.ALPHAS[Settings.SettingsAlpha.get()]
		)) {
			if (previousBorstModel == null) {
				parent.shapeRender.reset();
				parent.shapeRender.createCanvas(scaled.getWidth(), scaled.getHeight(), bgColor.getRGB());
			}
		}
	}
	
	private void onBorstData(BorstGenerator.BorstData data) {
		lastData = data;
		parent.repaint();
		
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
	
	public void openDialog(GraphicsConfiguration monitor, Point point) {
		this.monitor = monitor;
		
		// Force the user to reset the palette
		previousBorstModel = null;
		rustPalette.reset();
		previouslyUsed.reset();
		drawnShapes = 0;
		
		// Update old graphics
		parent.shapeRender.reset();
		parent.repaint();
		
		// Before we block
		startGeneration(0);
		
		setLocation(point);
		setSize(REGULAR);
		setVisible(true);
		borstGenerator.stop();
		
		try {
			Settings.SettingsClickInterval.set(Integer.parseInt(clickIntervalField.getText()));
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid click interval '{}'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(Settings.SettingsClickInterval.get()));
		}
	}
}
