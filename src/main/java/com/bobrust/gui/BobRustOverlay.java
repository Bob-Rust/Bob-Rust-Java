package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.BorstSettings;
import com.bobrust.generator.Model;
import com.bobrust.gui.comp.JRandomPanel;
import com.bobrust.gui.comp.JStyledButton;
import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.gui.dialog.BobRustDrawDialog;
import com.bobrust.gui.dialog.BobRustMonitorPicker;
import com.bobrust.lang.RustTranslator;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.logging.LogUtils;
import com.bobrust.robot.BobRustPalette;
import com.bobrust.util.RustImageUtil;
import com.bobrust.util.Sign;
import com.bobrust.util.UrlUtils;

/**
 * Overlay window that will cover the entire screen
 * 
 * @author HardCoded
 */
@SuppressWarnings("serial")
public class BobRustOverlay extends JPanel {
	private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(146, 376);
	private static final int RECTANGLE_SELECTION_SIZE = 10;
	private static final int SHAPE_CACHE_INTERVAL = 500;
	private static final int ESTIMATE_DELAY_OFFSET = 14;
	private static final int BORDER_SIZE = 3;
	
	public final JDialog dialog;
	private final BobRustEditor gui;
	
	private final Rectangle canvasRegion = new Rectangle(0, 0, 0, 0);
	private final Rectangle imageRegion = new Rectangle(0, 0, 0, 0);
	
	// Used when drawing the selection box.
	public final Point colorRegion = new Point(0, 0);
	private final Point dragStart = new Point(0, 0);
	private final Point dragEnd = new Point(0, 0);
	
	private final BobRustMonitorPicker monitorPicker;
	private final BobRustSettingsDialog settingsGui;
	private final BobRustDrawDialog drawDialog;
	private final BobRustShapeRender shapeRender;
	
	private final JStyledButton btnSelectMonitor;
	private final JStyledToggleButton btnHideRegions;
	private final JStyledToggleButton btnSelectCanvasRegion;
	private final JStyledToggleButton btnSelectImageRegion;
	private final JStyledButton btnDrawImage;
	
	private final JStyledButton btnOpenImage;
	private final JStyledButton btnStartGenerate;
	private final JStyledButton btnPauseGenerate;
	private final JStyledButton btnResetGenerate;
	
	private final JStyledButton btnOptions;
	private final JStyledButton btnMaximize;
	private final JRandomPanel actionPanel;
	
	private final java.util.List<JLabel> labels = new ArrayList<>();
	private final JLabel generationLabel;
	private final JLabel generationInfo;
	private final JPanel topBarPanel;
	private final JPanel regionsPanel;
	private final JPanel painterPanel;
	
	private ResizeOption resizeOption = ResizeOption.NONE;
	private OverlayType action = OverlayType.NONE;
	
	private BufferedImage modelImage;
	private BufferedImage image;
	private boolean isFullscreen;
	private BorstData lastData;
	
	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		private Point originPoint = new Point(0, 0);
		private Rectangle original = new Rectangle();
		private Rectangle rectangle = new Rectangle();
		private boolean isToolbarArea;
		
		@Override
		public void mousePressed(MouseEvent e) {
			isToolbarArea = e.getX() < 137 - RECTANGLE_SELECTION_SIZE;
			if(isToolbarArea) {
				return;
			}
			
			originPoint = new Point(e.getPoint());
			if(originPoint.x < 137) {
				originPoint.x = 137;
			}
			
			updateResizeOption(e.getPoint());
			modifyRectangle(e.getPoint());
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(isToolbarArea) {
				return;
			}
			
			updateResizeOption(e.getPoint());
			repaint();
		}
		
		private void updateResizeOption(Point mouse) {
			if(action == OverlayType.SELECT_CANVAS_REGION) {
				rectangle = canvasRegion;
			} else if(action == OverlayType.SELECT_IMAGE_REGION) {
				rectangle = imageRegion;
			} else {
				// Return if the action was not canvas or image.
				return;
			}
			
			// Keep a copy of the original.
			original.setBounds(rectangle);
			
			Rectangle larger = new Rectangle(rectangle);
			larger.setBounds(
				rectangle.x - RECTANGLE_SELECTION_SIZE,
				rectangle.y - RECTANGLE_SELECTION_SIZE,
				rectangle.width + RECTANGLE_SELECTION_SIZE * 2,
				rectangle.height + RECTANGLE_SELECTION_SIZE * 2
			);
			
			if(!larger.contains(mouse)) {
				resizeOption = ResizeOption.ALL;
			} else {
				boolean top = Math.abs(rectangle.y - mouse.y) < RECTANGLE_SELECTION_SIZE;
				boolean right = Math.abs(rectangle.x + rectangle.width - mouse.x) < RECTANGLE_SELECTION_SIZE;
				boolean bottom  = Math.abs(rectangle.y + rectangle.height - mouse.y) < RECTANGLE_SELECTION_SIZE;
				boolean left = Math.abs(rectangle.x - mouse.x) < RECTANGLE_SELECTION_SIZE;
				
				if(top) {
					resizeOption = right ? ResizeOption.TOP_RIGHT:(left ? ResizeOption.TOP_LEFT:ResizeOption.TOP);
				} else if(bottom) {
					resizeOption = right ? ResizeOption.BOTTOM_RIGHT:(left ? ResizeOption.BOTTOM_LEFT:ResizeOption.BOTTOM);
				} else {
					resizeOption = right ? ResizeOption.RIGHT:(left ? ResizeOption.LEFT:ResizeOption.ALL);
				}
			}
		}
		
		private void modifyRectangle(Point point) {
			if(point.x < 137) {
				// Do not allow intersection with the toolbar area.
				point.x = 137;
			}
			
			Point topLeft = new Point(original.x, original.y);
			Point bottomRight = new Point(original.x + original.width, original.y + original.height);
			
			switch(resizeOption) {
				case ALL -> {
					topLeft = originPoint;
					bottomRight = point;
				}
				default -> {
					if(resizeOption.top) topLeft.y = point.y;
					if(resizeOption.right) bottomRight.x = point.x;
					if(resizeOption.bottom) bottomRight.y = point.y;
					if(resizeOption.left) topLeft.x = point.x;
				}
			}
			
			dragStart.x = topLeft.x;
			dragStart.y = topLeft.y;
			dragEnd.x = bottomRight.x;
			dragEnd.y = bottomRight.y;
			
			int x = Math.min(topLeft.x, bottomRight.x);
			int y = Math.min(topLeft.y, bottomRight.y);
			int width = Math.abs(topLeft.x - bottomRight.x);
			int height = Math.abs(topLeft.y - bottomRight.y);
			rectangle.setBounds(x, y, width, height);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(isToolbarArea) {
				return;
			}
			
			switch(action) {
				case SELECT_CANVAS_REGION, SELECT_IMAGE_REGION -> {
					modifyRectangle(e.getPoint());
					repaint();
				}
				default -> {}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(isToolbarArea) {
				isToolbarArea = false;
				return;
			}
			
			switch(action) {
				case SELECT_CANVAS_REGION, SELECT_IMAGE_REGION -> {
					modifyRectangle(e.getPoint());
					
					dragStart.x = rectangle.x;
					dragStart.y = rectangle.y;
					dragEnd.x = rectangle.x + rectangle.width;
					dragEnd.y = rectangle.y + rectangle.height;
					resizeOption = ResizeOption.NONE;
					
					repaint();
				}
				default -> {}
			}
		}
	};
	
	protected BobRustOverlay(BobRustEditor gui) {
		this.gui = gui;
		
		dialog = new JDialog(null, "BobRust", ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setSize(DEFAULT_DIALOG_SIZE);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setFocusable(false);
		dialog.setFocusableWindowState(true);
		dialog.setContentPane(this);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		try {
			java.util.List<Image> icons = new ArrayList<>();
			for(int i = 0; i < 4; i++) {
				InputStream stream = BobRustOverlay.class.getResourceAsStream("/icons/%s.png".formatted(16 << i));
				icons.add(ImageIO.read(stream));
				stream.close();
			}
			dialog.setIconImage(new BaseMultiResolutionImage(icons.toArray(Image[]::new)));
		} catch(IOException e) {
			LogUtils.error("Failed to load program icons. %s", e);
		}
		
		settingsGui = new BobRustSettingsDialog(gui, dialog);
		monitorPicker = new BobRustMonitorPicker(dialog);
		drawDialog = new BobRustDrawDialog(gui, this, dialog);
		shapeRender = new BobRustShapeRender(SHAPE_CACHE_INTERVAL);
		
		dialog.addMouseListener(mouseAdapter);
		dialog.addMouseMotionListener(mouseAdapter);
		
		this.setOpaque(false);
		this.setBackground(new Color(0, true));
		this.setLayout(null);

		actionPanel = new JRandomPanel(gui);
		actionPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
		actionPanel.setBackground(Color.WHITE);
		actionPanel.setBounds(0, 0, 132, 500);
		add(actionPanel);
		
		topBarPanel = new JPanel();
		topBarPanel.setBounds(150, 5, 10, 10);
		topBarPanel.setBackground(new Color(0x7f000000, true));
		topBarPanel.setLayout(null);
		add(topBarPanel);
		
		generationLabel = new JLabel("No active generation");
		generationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		generationLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		generationLabel.setForeground(Color.lightGray);
		generationLabel.setFont(generationLabel.getFont().deriveFont(18.0f));
		generationLabel.setBounds(0, 0, 380, 25);
		generationLabel.setBackground(Color.blue);
		topBarPanel.add(generationLabel);
		
		generationInfo = new JLabel("");
		generationInfo.setHorizontalAlignment(SwingConstants.CENTER);
		generationInfo.setHorizontalTextPosition(SwingConstants.CENTER);
		generationInfo.setForeground(Color.WHITE);
		generationInfo.setFont(generationInfo.getFont().deriveFont(Font.BOLD, 16.0f));
		generationInfo.setBounds(0, 20, 440, 20);
		generationInfo.setBackground(Color.red);
		topBarPanel.add(generationInfo);
		
		Dimension buttonSize = new Dimension(120, 24);
		
		JLabel lblOptions = new JLabel(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
		lblOptions.setForeground(Color.BLACK);
		actionPanel.add(lblOptions);
		labels.add(lblOptions);
		
		btnSelectMonitor = new JStyledButton(RustUI.getString(Type.ACTION_SELECTMONITOR_BUTTON));
		btnSelectMonitor.setMaximumSize(buttonSize);
		btnSelectMonitor.addActionListener((event) -> {
			GraphicsConfiguration gc = monitorPicker.openDialog();
			if(isFullscreen) {
				dialog.setBounds(gc.getBounds());
			}
			Rectangle bounds = gc.getBounds();
			String idString = gc.getDevice().getIDstring();
			LogUtils.info("Selected Monitor: { id: '%s', x: %d, y: %d, width: %d, height: %d }", idString, bounds.x, bounds.y, bounds.width, bounds.height);
		});
		
		btnMaximize = new JStyledButton(RustUI.getString(Type.ACTION_MAKEFULLSCREEN_ON));
		btnMaximize.setMaximumSize(buttonSize);
		btnMaximize.addActionListener(this::changeFullscreen);
		actionPanel.add(btnMaximize);
		actionPanel.add(btnSelectMonitor);
		
		btnOpenImage = new JStyledButton(RustUI.getString(Type.ACTION_LOADIMAGE_BUTTON));
		btnOpenImage.setMaximumSize(buttonSize);
		btnOpenImage.addActionListener((event) -> openImage());
		actionPanel.add(btnOpenImage);
		
		btnOptions = new JStyledButton(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
		btnOptions.setMaximumSize(buttonSize);
		btnOptions.addActionListener((event) -> {
			settingsGui.openDialog(btnOptions.getLocationOnScreen());
			updateButtons();
		});
		actionPanel.add(btnOptions);
		
		{
			regionsPanel = new JPanel();
			regionsPanel.setVisible(false);
			regionsPanel.setOpaque(false);
			regionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			actionPanel.add(regionsPanel);
			regionsPanel.setLayout(new BoxLayout(regionsPanel, BoxLayout.Y_AXIS));
			
			JLabel lblRegions = new JLabel(RustUI.getString(Type.ACTION_REGIONS_LABEL));
			lblRegions.setBorder(new EmptyBorder(10, 0, 0, 0));
			lblRegions.setForeground(Color.BLACK);
			regionsPanel.add(lblRegions);
			labels.add(lblRegions);
			
			btnHideRegions = new JStyledToggleButton(RustUI.getString(Type.ACTION_SHOWREGIONS_ON));
			btnHideRegions.setMaximumSize(buttonSize);
			btnHideRegions.setSelected(true);
			btnHideRegions.addActionListener((event) -> setHideRegions(btnHideRegions.isSelected()));
			regionsPanel.add(btnHideRegions);
			
			btnSelectCanvasRegion = new JStyledToggleButton(RustUI.getString(Type.ACTION_CANVASREGION_BUTTON));
			btnSelectCanvasRegion.setMaximumSize(buttonSize);
			btnSelectCanvasRegion.setEnabled(false);
			btnSelectCanvasRegion.addActionListener((event) -> {
				if(btnSelectCanvasRegion.isSelected()) {
					startSelectRegion(canvasRegion, OverlayType.SELECT_CANVAS_REGION);
				} else {
					endSelectRegion();
				}
			});
			regionsPanel.add(btnSelectCanvasRegion);
			
			btnSelectImageRegion = new JStyledToggleButton(RustUI.getString(Type.ACTION_IMAGEREGION_BUTTON));
			btnSelectImageRegion.setMaximumSize(buttonSize);
			btnSelectImageRegion.setEnabled(false);
			btnSelectImageRegion.addActionListener((event) -> {
				if(btnSelectImageRegion.isSelected()) {
					startSelectRegion(imageRegion, OverlayType.SELECT_IMAGE_REGION);
				} else {
					endSelectRegion();
				}
			});
			regionsPanel.add(btnSelectImageRegion);
		}
		
		Dimension textDimension = new Dimension(120, 24);
		
		{
			JPanel previewPanel = new JPanel();
			previewPanel.setOpaque(false);
			previewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			actionPanel.add(previewPanel);
			previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
			
			JLabel lblActions = new JLabel(RustUI.getString(Type.ACTION_PREVIEWACTIONS_LABEL));
			lblActions.setForeground(Color.BLACK);
			lblActions.setBorder(new EmptyBorder(10, 0, 0, 0));
			previewPanel.add(lblActions);
			labels.add(lblActions);
			
			btnStartGenerate = new JStyledButton(RustUI.getString(Type.ACTION_STARTGENERATE_BUTTON));
			btnStartGenerate.setMaximumSize(buttonSize);
			btnStartGenerate.setEnabled(false);
			btnStartGenerate.addActionListener((event) -> startGeneration());
			previewPanel.add(btnStartGenerate);
	
			btnPauseGenerate = new JStyledButton(RustUI.getString(Type.ACTION_PAUSEGENERATE_ON));
			btnPauseGenerate.setMaximumSize(buttonSize);
			btnPauseGenerate.setEnabled(false);
			btnPauseGenerate.addActionListener((event) -> setPauseGeneration(gui.borstGenerator.isPaused()));
			previewPanel.add(btnPauseGenerate);
			
			btnResetGenerate = new JStyledButton(RustUI.getString(Type.ACTION_RESETGENERATE_BUTTON));
			btnResetGenerate.setMaximumSize(buttonSize);
			btnResetGenerate.setEnabled(false);
			btnResetGenerate.addActionListener((event) -> resetGeneration());
			previewPanel.add(btnResetGenerate);
			
			JStyledButton btnClose = new JStyledButton(RustUI.getString(Type.ACTION_CLOSE_BUTTON));
			btnClose.setMaximumSize(buttonSize);
			btnClose.addActionListener((event) -> {
				int dialogResult = JOptionPane.showConfirmDialog(dialog,
					RustUI.getString(Type.ACTION_CLOSEDIALOG_MESSAGE),
					RustUI.getString(Type.ACTION_CLOSEDIALOG_TITLE),
					JOptionPane.YES_NO_OPTION
				);
				if(dialogResult == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			});
			previewPanel.add(btnClose);
		}
		
		{
			painterPanel = new JPanel();
			painterPanel.setOpaque(false);
			painterPanel.setVisible(false);
			painterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			actionPanel.add(painterPanel);
			painterPanel.setLayout(new BoxLayout(painterPanel, BoxLayout.Y_AXIS));
			
			JLabel lblPaintImage = new JLabel(RustUI.getString(Type.ACTION_DRAW_LABEL));
			lblPaintImage.setForeground(Color.BLACK);
			lblPaintImage.setBorder(new EmptyBorder(10, 0, 0, 0));
			painterPanel.add(lblPaintImage);
			labels.add(lblPaintImage);
			
			btnDrawImage = new JStyledButton(RustUI.getString(Type.ACTION_DRAWIMAGE_BUTTON));
			btnDrawImage.setMaximumSize(buttonSize);
			btnDrawImage.setEnabled(false);
			btnDrawImage.addActionListener((event) -> {
				drawDialog.openDialog(btnDrawImage.getLocationOnScreen());
				doRenderShapes = false;
				repaint();
				updateEditor();
			});
			painterPanel.add(btnDrawImage);
		}
		
		{
			JPanel helpPanel = new JPanel();
			helpPanel.setOpaque(false);
			helpPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
			helpPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			actionPanel.add(helpPanel);
			helpPanel.setMaximumSize(textDimension);
			helpPanel.setMinimumSize(textDimension);
			helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.X_AXIS));
			
			JLabel lblHelp = new JLabel(RustUI.getString(Type.ACTION_HELP_LABEL));
			lblHelp.setForeground(Color.BLACK);
			lblHelp.setBorder(new EmptyBorder(10, 0, 0, 0));
			helpPanel.add(lblHelp);
			labels.add(lblHelp);
			
			JStyledButton btnGithubIssue = new JStyledButton(RustUI.getString(Type.ACTION_REPORTISSUE_BUTTON));
			btnGithubIssue.setMaximumSize(buttonSize);
			btnGithubIssue.addActionListener((event) -> UrlUtils.openIssueUrl());
			actionPanel.add(btnGithubIssue);
			
			JStyledButton btnDonate = new JStyledButton(RustUI.getString(Type.ACTION_DONATE_BUTTON));
			btnDonate.setMaximumSize(buttonSize);
			btnDonate.addActionListener((event) -> UrlUtils.openDonationUrl());
			actionPanel.add(btnDonate);
			
			JStyledButton btnAbout = new JStyledButton(RustUI.getString(Type.ACTION_ABOUT_BUTTON));
			btnAbout.setMaximumSize(buttonSize);
			btnAbout.addActionListener((event) -> {
				String message = "Created by HardCoded & Sekwah41\n" +
								 "\n" +
								 "HardCoded\n" +
								 "- Design\n" +
								 "- Sorting algorithm\n" +
								 "- Optimized generation\n" +
								 "\n" +
								 "Sekwah41\n" +
								 "- Initial generation";
				
				JOptionPane.showMessageDialog(dialog, message, "About me", JOptionPane.INFORMATION_MESSAGE);
			});
			actionPanel.add(btnAbout);
		}
		
		updateEditor();
	}
	
	private void openImage() {
		File file = gui.openImageFileChooser(dialog);
		if(file != null) {
			try {
				BufferedImage selectedImage = ImageIO.read(file);
				LogUtils.info("Loaded image '%s'", file);
				image = selectedImage;
				updateButtons();
			} catch(IOException e) {
				LogUtils.error("Failed to read image '%s'", file);
			} catch(Throwable t) {
				t.printStackTrace();
			}
			
			repaint();
		}
	}
	
	private void startGeneration() {
		if(!gui.borstGenerator.isRunning()) {
			Rectangle rect = canvasRegion.createIntersection(imageRegion).getBounds();
			
			if(!rect.isEmpty()) {
				Sign signType = gui.getSettingsSign();
				Color bgColor = gui.getSettingsBackgroundCalculated();
				
				BufferedImage scaled;
				scaled = RustImageUtil.getScaledInstance(
					image,
					canvasRegion,
					imageRegion,
					signType.width,
					signType.height,
					bgColor,
					gui.getSettingsScaling()
				);
				
				// Apply the ICC cmyk lut filter.
				scaled = RustImageUtil.applyFilters(scaled);
				
				BorstSettings settings = gui.getBorstSettings();
				settings.Background = bgColor.getRGB();
				settings.DirectImage = scaled;
				updateEditor();
				if(gui.borstGenerator.start()) {
					shapeRender.createCanvas(scaled.getWidth(), scaled.getHeight(), bgColor.getRGB());
					
					action = OverlayType.GENERATE_IMAGE;
					updateButtons();
				}
			}
			
			repaint();
		}
	}
		
	private void resetGeneration() {
		if(gui.borstGenerator.isRunning()) {
			try {
				gui.getBorstSettings().DirectImage = null;
				gui.borstGenerator.stop();
				modelImage = null;
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			shapeRender.reset();
			lastData = null;
			action = OverlayType.NONE;
			updateButtons();
			updateEditor();
			repaint();
		}
	}
	
	public void setHideRegions(boolean enable) {
		if(enable) {
			btnHideRegions.setText(RustUI.getString(Type.ACTION_SHOWREGIONS_ON));
		} else {
			btnHideRegions.setText(RustUI.getString(Type.ACTION_SHOWREGIONS_OFF));
		}
		
		btnHideRegions.setSelected(enable);
		
		updateButtons();
		repaint();
	}
	
	private void setPauseGeneration(boolean enable) {
		if(enable) {
			btnPauseGenerate.setText(RustUI.getString(Type.ACTION_PAUSEGENERATE_ON));
			gui.borstGenerator.resume();
		} else {
			btnPauseGenerate.setText(RustUI.getString(Type.ACTION_PAUSEGENERATE_OFF));
			gui.borstGenerator.pause();
		}
		
		updateButtons();
	}
	
	public void updateEditor() {
		setBorder(isFullscreen ? new LineBorder(gui.getEditorBorderColor(), BORDER_SIZE):null);
		actionPanel.setBackground(gui.getEditorToolbarColor());
		
		for(JLabel label : labels) {
			label.setForeground(gui.getEditorLabelColor());
		}
		
		generationLabel.setLocation((topBarPanel.getWidth() - generationLabel.getWidth()) / 2, generationLabel.getY());
		generationInfo.setLocation((topBarPanel.getWidth() - generationInfo.getWidth()) / 2, generationInfo.getY());
		
		BorstData lastData = this.lastData;
		setEstimatedGenerationLabel(lastData != null ? lastData.getIndex():0, gui.getSettingsMaxShapes());
		
		if(gui.borstGenerator.isPaused()) {
			btnPauseGenerate.setText(RustUI.getString(Type.ACTION_PAUSEGENERATE_OFF));
		} else {
			btnPauseGenerate.setText(RustUI.getString(Type.ACTION_PAUSEGENERATE_ON));
		}
	}
	
	public Rectangle getCanvasArea() {
		return canvasRegion;
	}
	
	public Rectangle getScreenLocation() {
		return monitorPicker.getMonitor().getBounds();
	}
	
	public void setEstimatedGenerationLabel(int index, int maxShapes) {
		generationLabel.setText("%d/%d shapes generated".formatted(index, maxShapes));
		long time = (long)(index * 1.1 * (ESTIMATE_DELAY_OFFSET + 1000.0 / (double)gui.getSettingsClickInterval()));
		generationInfo.setText("Estimated %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setExactGenerationLabel(long time) {
		generationInfo.setText("Time %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setRemainingTime(int index, int maxShapes, long timeLeft) {
		generationLabel.setText("%d/%d shapes drawn".formatted(index, maxShapes));
		generationInfo.setText("Time left %s".formatted(RustTranslator.getTimeMinutesMessage(timeLeft)));
	}
	
	private void updateButtons() {
		boolean defaultAction = action == OverlayType.NONE;
		boolean isPaused = gui.borstGenerator.isRunning() && gui.borstGenerator.isPaused();
		
		// You should only be able to pick monitor the screen is enabled.
		btnSelectMonitor.setEnabled(defaultAction || isPaused);
		regionsPanel.setVisible(isFullscreen);
		painterPanel.setVisible(isFullscreen);
		
		btnHideRegions.setEnabled(defaultAction || isPaused);
		boolean defaultRegion = !btnHideRegions.isSelected() && defaultAction;
		btnSelectCanvasRegion.setEnabled(defaultRegion || action == OverlayType.SELECT_CANVAS_REGION);
		btnSelectImageRegion.setEnabled(defaultRegion && image != null || action == OverlayType.SELECT_IMAGE_REGION);
		
		btnMaximize.setEnabled(true);
		btnOpenImage.setEnabled(defaultAction);
		btnStartGenerate.setEnabled(isFullscreen && defaultAction && image != null && !gui.borstGenerator.isRunning());
		btnPauseGenerate.setEnabled(isFullscreen && gui.borstGenerator.isRunning());
		btnResetGenerate.setEnabled(isPaused);
		btnDrawImage.setEnabled(isPaused);
	}
	
	private void changeFullscreen(ActionEvent event) {
		isFullscreen = !isFullscreen;
		btnMaximize.setText(RustUI.getString(isFullscreen ? Type.ACTION_MAKEFULLSCREEN_OFF:Type.ACTION_MAKEFULLSCREEN_ON));
		actionPanel.setLocation(isFullscreen ? BORDER_SIZE:0, isFullscreen ? BORDER_SIZE:0);
		
		dialog.setVisible(false);
		dialog.dispose();
		
		updateButtons();
		
		dialog.setAlwaysOnTop(isFullscreen);
		if(!isFullscreen) {
			dialog.setBackground(Color.lightGray);
			dialog.setUndecorated(false);
			dialog.setSize(DEFAULT_DIALOG_SIZE);
		} else {
			dialog.setUndecorated(true);
			dialog.setBackground(new Color(0, true));
			GraphicsConfiguration gc = monitorPicker.getMonitor();
			dialog.setBounds(gc.getBounds());
		}
		
		actionPanel.setSize(actionPanel.getWidth(), dialog.getHeight() - BORDER_SIZE * 2);
		topBarPanel.setBounds(BORDER_SIZE + (dialog.getWidth() - 440) / 2, isFullscreen ? BORDER_SIZE:-50, 440, 40);
		updateEditor();
		dialog.setVisible(true);
	}
	
	public BorstData getBorstData() {
		return lastData;
	}
	
	public void onBorstCallback(BorstData data) {
		modelImage = data.getModel().current.image;
		lastData = data;
		
		setEstimatedGenerationLabel(data.getIndex(), gui.getSettingsMaxShapes());
		repaint();
		
		if(data.isDone()) {
			btnPauseGenerate.setText(RustUI.getString(Type.ACTION_PAUSEGENERATE_ON));
		}
	}
	
	private boolean doRenderShapes = false;
	private int renderShapesAmount = 0;
	public void setRenderPreviewShapes(int value) {
		// TODO: Render the preview in a better way!
		doRenderShapes = true;
		renderShapesAmount = value;
	}
	
	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		if(!isFullscreen) return;
		
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(0, true));
		g.fillRect(0, 0, getWidth(), getHeight());
		
//		{
//			g.setColor(gui.getToolbarColor());
//			Stroke old_stroke = g.getStroke();
//			g.setStroke(new BasicStroke(6.0f));
//			g.drawRoundRect(131, 0, getWidth() - 132, getHeight(), 30, 30);
//			g.setStroke(old_stroke);
//		}
		
		if(!btnHideRegions.isSelected()) {
			g.setColor(new Color(0x30000000, true));
			g.fillRect(0, 0, getWidth() - 150, getHeight());
			
			// Draw region outline.
			{
				Stroke old_stroke = g.getStroke();
				g.setStroke(new BasicStroke(5.0f));
				
				BufferedImage img = image;
				if(img != null) {
					if(!gui.borstGenerator.isRunning()) {
						g.drawImage(img, imageRegion.x, imageRegion.y, imageRegion.width, imageRegion.height, null);
					}
					drawRectangle(g, imageRegion, Color.cyan, action == OverlayType.SELECT_IMAGE_REGION);
				}
				
				if(canvasRegion.x != 0 && canvasRegion.y != 0) {
					drawRectangle(g, canvasRegion, Color.yellow, action == OverlayType.SELECT_CANVAS_REGION);
				}
				
				g.setStroke(old_stroke);
			}
			
			if(action == OverlayType.DRAW_IMAGE) {
				Composite old_composite = g.getComposite();
				g.setComposite(AlphaComposite.Clear);
				g.setColor(new Color(0.5f, 0.7f, 0.2f, 0.5f));
				g.fillRect(canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height);
				g.setComposite(old_composite);
			}
			
			// Draw model image.
			{
				BufferedImage image = modelImage;
				if(image != null) {
					g.setColor(new Color(gui.getBorstSettings().Background));
					g.fillRect(canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height);
					g.drawImage(image, canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height, null);
				}
			}
			
			// Draw Custom Shapes.
			if(doRenderShapes) {
				BorstData data = lastData;
				if(data != null) {
					Model model = data.getModel();
					
					if(shapeRender.hasCanvas()) {
						int len = Math.min(model.colors.size(), renderShapesAmount);
						BufferedImage image = shapeRender.getImage(model, len);
						g.drawImage(image, canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height, null);
					}
				}
			}
			

			{
				BobRustPalette palette = drawDialog.getPalette();
				if(palette.hasPalette()) {
					Map<BorstColor, Point> map = palette.getColorMap();
					
					g.setColor(Color.white);
					Point screen = dialog.getLocationOnScreen();
					for(Map.Entry<BorstColor, Point> entry : map.entrySet()) {
						Point point = entry.getValue();
						Point sc = new Point(point.x - screen.x, point.y - screen.y);
						g.drawOval(sc.x - 15, sc.y - 15, 30, 29);
					}
					
					// TODO: Find a better way to draw these than crashing the application. xd
					try {
						for(int i = 0; i < 6; i++) {
							Point point = palette.getAlphaButton(i);
							Point sc = new Point(point.x - screen.x, point.y - screen.y);
							g.drawOval(sc.x - 10, sc.y - 10, 20, 20);
						}
						
						for(int i = 0; i < 6; i++) {
							Point point = palette.getSizeButton(i);
							Point sc = new Point(point.x - screen.x, point.y - screen.y);
							g.drawOval(sc.x - 10, sc.y - 10, 20, 20);
						}
						
						for(int i = 0; i < 4; i++) {
							Point point = palette.getShapeButton(i);
							Point sc = new Point(point.x - screen.x, point.y - screen.y);
							g.drawOval(sc.x - 15, sc.y - 15, 30, 29);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void drawRectangle(Graphics2D g, Rectangle rect, Color light, boolean selected) {
		Stroke old_stroke = g.getStroke();
		
		float weight = selected ? 5:1;
		BasicStroke def_stroke = new BasicStroke(weight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke sel_stroke = new BasicStroke(weight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		Color dark = light.darker();
		
		if(!selected) {
			g.setStroke(def_stroke);
			
			g.setColor(dark);
			g.drawRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
			g.drawRect(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2);
			
			g.setColor(light);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		} else {
			ResizeOption option = resizeOption;
			
			if(option == ResizeOption.ALL) {
				option = ResizeOption.NONE;
			}
			
			Point start = dragStart;
			Point end = dragEnd;

			// Draw unselected lines
			g.setStroke(def_stroke);
			g.setColor(light);
			if(!option.top) g.drawLine(start.x, start.y, end.x, start.y);
			if(!option.right) g.drawLine(end.x, start.y, end.x, end.y);
			if(!option.bottom) g.drawLine(start.x, end.y, end.x, end.y);
			if(!option.left) g.drawLine(start.x, start.y, start.x, end.y);
			
			// Draw selected lines
			g.setStroke(sel_stroke);
			g.setColor(dark);
			if(option.top) g.drawLine(start.x, start.y, end.x, start.y);
			if(option.right) g.drawLine(end.x, start.y, end.x, end.y);
			if(option.bottom) g.drawLine(start.x, end.y, end.x, end.y);
			if(option.left) g.drawLine(start.x, start.y, start.x, end.y);
		}
		
		g.setStroke(old_stroke);
	}
	
	protected synchronized void startSelectRegion(Rectangle region, OverlayType type) {
		if(action != OverlayType.NONE) {
			return;
		}
		
		action = type;
		
		dialog.setBounds(monitorPicker.getMonitor().getBounds());
		if(region != null) {
			dragStart.setLocation(region.x, region.y);
			dragEnd.setLocation(region.x + region.width, region.y + region.height);
		}
		
		updateButtons();
		repaint();
	}
	
	protected void endSelectRegion() {
		action = OverlayType.NONE;
		updateButtons();
		repaint();
	}
	
	private enum OverlayType {
		NONE,
		SELECT_CANVAS_REGION,
		SELECT_IMAGE_REGION,
		DRAW_IMAGE,
		GENERATE_IMAGE
	}
	
	private enum ResizeOption {
		NONE(false, false, false, false),
		ALL(true, true, true, true),
		TOP(true, false, false, false),
		TOP_RIGHT(true, true, false, false),
		RIGHT(false, true, false, false),
		BOTTOM_RIGHT(false, true, true, false),
		BOTTOM(false, false, true, false),
		BOTTOM_LEFT(false, false, true, true),
		LEFT(false, false, false, true),
		TOP_LEFT(true, false, false, true);
		
		public final boolean top;
		public final boolean right;
		public final boolean bottom;
		public final boolean left;
		private ResizeOption(boolean top, boolean right, boolean bottom, boolean left) {
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.left = left;
		}
	}
}
