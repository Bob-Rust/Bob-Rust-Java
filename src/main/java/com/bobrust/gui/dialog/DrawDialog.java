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
import org.apache.logging.log4j.Level;
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

public class DrawDialog extends JDialog {
	private static final Logger LOGGER = LogManager.getLogger(DrawDialog.class);
	private static final Dimension REGULAR = new Dimension(320, 200);
	private static final Dimension MINIMIZED = new Dimension(120, 40);
	
	private BobRustPalette rustPalette;
	private BobRustPainter rustPainter;
	private final RegionSelectionDialog selectionDialog;
	
	private final ScreenDrawDialog parent;
	
	private final JTextField maxShapesField;
	private final JIntegerField clickIntervalField;
	final JSlider shapesSlider;
	
	private final JLabel minShapeLabel;
	private final JLabel maxShapeLabel;
	
	// Borst stuff
	private GraphicsConfiguration monitor;
	private Model previousBorstModel;
	private int drawnShapes;
	private final BlobList previouslyUsed = new BlobList(); // We need to continue using these instructions
	final BorstGenerator borstGenerator;
	Rectangle paletteRect ;
	public void setPalette(BobRustPalette p)
	{
		rustPalette=p;
		this.rustPainter = new BobRustPainter(rustPalette);
	}
	public void setPaletteRect(Rectangle r)
	{
		paletteRect=r;
	}
	public DrawDialog(ScreenDrawDialog parent) {
		super(parent, "Draw Settings", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
	;
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
			
			parent.topPanel.setGeneratedShapes(shapesSlider.getValue(), borstGenerator.data.getIndex());
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
		});
		clickIntervalPanel.add(clickIntervalField);
		
		JButton exactTimeButton = new JButton("Calculate Exact Time");
		exactTimeButton.setFocusable(false);
		exactTimeButton.addActionListener((event) -> {
			int count = shapesSlider.getValue();
			int actions;
			synchronized (borstGenerator.data) {
				 actions = RustUtil.getScore(BorstSorter.sort(RustUtil.convertToList(borstGenerator.data, 0, count)));
			}
			int totalClicks = actions + count;
			
			int interval = Settings.SettingsClickInterval.get();
			parent.topPanel.setExactGenerationLabel((long) (totalClicks * (1000.0 / (double)interval)));
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
		//setVisible(false);
		if(rustPalette==null)
		{
			rustPalette = new BobRustPalette();
			rustPalette.setButtonConfig(parent.parent.config);
			rustPalette.setPaletteRect(paletteRect);

		}
		Thread thread = new Thread(() -> {
			int offsetShapes = 0;
			try {
				setLocation(monitor.getBounds().getLocation());
				setSize(MINIMIZED);
				
				int count = shapesSlider.getValue();
				BlobList list;
				
				synchronized (borstGenerator.data) {
					// TODO: 'previouslyUsed' should start at 'drawnShapes'
					int previouslyComputed = previouslyUsed.size();
					if (previouslyComputed <= drawnShapes || previouslyComputed <= count) {
						// Fill the previouslyUsed list with new data
						int missing = count - previouslyComputed;
						BlobList missingList = BorstSorter.sort(RustUtil.convertToList(borstGenerator.data, count - missing, missing));
						previouslyUsed.getList().addAll(missingList.getList());
					}
				}
				
				list = new BlobList();
				list.assign(previouslyUsed.getList(), drawnShapes, count - drawnShapes);
				updateTimeRemaining(0, count - drawnShapes);
				
				start = -1;
				LOGGER.info("Start drawing");
				LOGGER.info("- Alpha Index    : {}", Settings.SettingsAlpha.get());
				LOGGER.info("- Click Interval : {}", Settings.SettingsClickInterval.get());
				LOGGER.info("- Scaling        : {}", Settings.SettingsScaling.get());
				LOGGER.info("- Sign Type      : {}", Settings.SettingsSign.get().getName());
				if (!rustPainter.startDrawing(monitor, parent.canvasRect, list, this::updateTimeRemaining)) {
					LOGGER.warn("The user stopped the drawing process early");
				}
			} catch (PaintingInterrupted e) {
				boolean finished = e.getInterruptType() == PaintingInterrupted.InterruptType.PaintingFinished;
				Level level = finished
					? Level.INFO
					: Level.WARN;
				LOGGER.log(level, finished
					? "Painting process finished"
					: "The user stopped the drawing process early");
				LOGGER.log(level, "- Type   : {}", e.getInterruptType());
				LOGGER.log(level, "- Shapes : {}", e.getDrawnShapes());
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
	public static BufferedImage captureImageFromRect(Rectangle rect) {
		try {
			Robot robot = new Robot();

			BufferedImage screenCapture = robot.createScreenCapture(rect);

			return screenCapture;
		} catch (AWTException e) {
			e.printStackTrace();
			return null;
		}
	}
	private boolean findColorPalette() {
		// Take a screenshot
		BufferedImage screenshot =captureImageFromRect(paletteRect);
		if (screenshot == null) {
			LOGGER.warn("Failed to take screenshot. Was null");
			return false;
		}
		
		if (!rustPalette.initWith(screenshot, monitor)) {
			LOGGER.warn("User needs to manually select the color palette");
			return false;
		}
		
		LOGGER.info("Found the color palette");
		return true;
	}
	
	private void startGeneration(int offset) {
		Sign signType = Settings.SettingsSign.get();
		Color bgColor = Settings.getSettingsBackgroundCalculated();
		
		BufferedImage scaled = ImageUtil.getScaledInstance(
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
			scaled = ImageUtil.applyFilters(scaled);
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
	
	public void openDialog(GraphicsConfiguration monitor, Point point) {
		this.monitor = monitor;
if(rustPalette==null)
{
	rustPalette=parent.parent.palette;
	//rustPalette.setButtonConfig(parent.parent.config);
}
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
	}
}
