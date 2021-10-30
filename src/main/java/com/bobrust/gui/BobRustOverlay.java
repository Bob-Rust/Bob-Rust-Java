package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import com.bobrust.gui.comp.JStyledButton;
import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.lang.RustTranslator;
import com.bobrust.logging.LogUtils;
import com.bobrust.robot.BobRustPalette;

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
	private final BobRustSettings settingsGui;
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
	private final JPanel actionPanel;
	
	private final java.util.List<JLabel> labels = new ArrayList<>();
	private final JLabel generationLabel;
	private final JLabel generationInfo;
	private final JPanel topBarPanel;
	private final JPanel regionsPanelTest;
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
				// If the point is in the action panel we should not compute anything
				return;
			}
			
			originPoint = new Point(e.getPoint());
			if(originPoint.x < 137) {
				originPoint.x = 137;
			}
			if(action == OverlayType.SELECT_CANVAS_REGION) rectangle = canvasRegion;
			if(action == OverlayType.SELECT_IMAGE_REGION) rectangle = imageRegion;
			
			switch(action) {
				case SELECT_CANVAS_REGION, SELECT_IMAGE_REGION -> {
					// Keep a copy of the original.
					original.setBounds(rectangle);
					
					Rectangle larger = new Rectangle(rectangle);
					larger.setBounds(
						rectangle.x - RECTANGLE_SELECTION_SIZE,
						rectangle.y - RECTANGLE_SELECTION_SIZE,
						rectangle.x + rectangle.width + RECTANGLE_SELECTION_SIZE * 2,
						rectangle.y + rectangle.height + RECTANGLE_SELECTION_SIZE * 2
					);
					
					Point mouse = e.getPoint();
					
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
					
					modifyRectangle(e.getPoint());
				}
				default -> {
					
				}
			}
		}
		
		public void mouseMoved(MouseEvent e) {
			if(isToolbarArea) {
				return;
			}
			
			if(action == OverlayType.SELECT_CANVAS_REGION) rectangle = canvasRegion;
			if(action == OverlayType.SELECT_IMAGE_REGION) rectangle = imageRegion;
			
			switch(action) {
				case SELECT_CANVAS_REGION, SELECT_IMAGE_REGION -> {
					// Keep a copy of the original.
					original.setBounds(rectangle);
					
					Point mouse = e.getPoint();
					
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
					
					repaint();
				}
				default -> {}
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
		
		settingsGui = new BobRustSettings(gui, dialog);
		monitorPicker = new BobRustMonitorPicker(dialog);
		drawDialog = new BobRustDrawDialog(gui, this, dialog);
		shapeRender = new BobRustShapeRender(SHAPE_CACHE_INTERVAL);
		
		dialog.addMouseListener(mouseAdapter);
		dialog.addMouseMotionListener(mouseAdapter);
		
		this.setOpaque(false);
		this.setBackground(new Color(0, true));
		this.setLayout(null);

		actionPanel = new JPanel();
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
		
		JLabel lblOptions = new JLabel("Options");
		lblOptions.setForeground(Color.BLACK);
		actionPanel.add(lblOptions);
		labels.add(lblOptions);
		
		btnSelectMonitor = new JStyledButton("Select Monitor");
		btnSelectMonitor.setMaximumSize(buttonSize);
		btnSelectMonitor.setFocusable(false);
		btnSelectMonitor.setOpaque(false);
		btnSelectMonitor.addActionListener((event) -> {
			GraphicsConfiguration gc = monitorPicker.openDialog();
			if(isFullscreen) {
				dialog.setBounds(gc.getBounds());
			}
			Rectangle bounds = gc.getBounds();
			String idString = gc.getDevice().getIDstring();
			LogUtils.info("Selected Monitor: { id: '%s', x: %d, y: %d, width: %d, height: %d }", idString, bounds.x, bounds.y, bounds.width, bounds.height);
		});
		
		btnMaximize = new JStyledButton("Make Fullscreen");
		btnMaximize.setOpaque(false);
		btnMaximize.setMaximumSize(new Dimension(120, 24));
		btnMaximize.setFocusable(false);
		btnMaximize.addActionListener(this::changeFullscreen);
		actionPanel.add(btnMaximize);
		actionPanel.add(btnSelectMonitor);
		
		btnOpenImage = new JStyledButton("Load Image");
		btnOpenImage.setMaximumSize(buttonSize);
		btnOpenImage.setFocusable(false);
		btnOpenImage.setOpaque(false);
		btnOpenImage.addActionListener((event) -> {
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
		});
		actionPanel.add(btnOpenImage);
		
		btnOptions = new JStyledButton("Options");
		btnOptions.setOpaque(false);
		btnOptions.setMaximumSize(buttonSize);
		btnOptions.setFocusable(false);
		btnOptions.addActionListener((event) -> {
			settingsGui.openDialog(btnOptions.getLocationOnScreen());
			updateButtons();
		});
		actionPanel.add(btnOptions);
		
		regionsPanelTest = new JPanel();
		regionsPanelTest.setVisible(false);
		regionsPanelTest.setOpaque(false);
		regionsPanelTest.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionPanel.add(regionsPanelTest);
		regionsPanelTest.setLayout(new BoxLayout(regionsPanelTest, BoxLayout.Y_AXIS));
		
		JLabel lblRegions = new JLabel("Regions");
		lblRegions.setBorder(new EmptyBorder(10, 0, 0, 0));
		lblRegions.setForeground(Color.BLACK);
		regionsPanelTest.add(lblRegions);
		labels.add(lblRegions);
		
		btnHideRegions = new JStyledToggleButton("Show Regions");
		btnHideRegions.setOpaque(false);
		btnHideRegions.setMaximumSize(new Dimension(120, 24));
		btnHideRegions.setFocusable(false);
		btnHideRegions.setSelected(true);
		btnHideRegions.addActionListener((event) -> setHideRegions(btnHideRegions.isSelected()));
		regionsPanelTest.add(btnHideRegions);
		
		btnSelectCanvasRegion = new JStyledToggleButton("Canvas Region");
		btnSelectCanvasRegion.setMaximumSize(buttonSize);
		btnSelectCanvasRegion.setFocusable(false);
		btnSelectCanvasRegion.setOpaque(false);
		btnSelectCanvasRegion.setEnabled(false);
		btnSelectCanvasRegion.addActionListener((event) -> {
			if(btnSelectCanvasRegion.isSelected()) {
				startSelectRegion(canvasRegion, OverlayType.SELECT_CANVAS_REGION);
			} else {
				endSelectRegion();
			}
		});
		regionsPanelTest.add(btnSelectCanvasRegion);
		
		btnSelectImageRegion = new JStyledToggleButton("Image Region");
		btnSelectImageRegion.setMaximumSize(buttonSize);
		btnSelectImageRegion.setOpaque(false);
		btnSelectImageRegion.setFocusable(false);
		btnSelectImageRegion.setEnabled(false);
		btnSelectImageRegion.addActionListener((event) -> {
			if(btnSelectImageRegion.isSelected()) {
				startSelectRegion(imageRegion, OverlayType.SELECT_IMAGE_REGION);
			} else {
				endSelectRegion();
			}
		});
		regionsPanelTest.add(btnSelectImageRegion);
		
		JLabel lblActions = new JLabel("Preview Actions");
		lblActions.setForeground(Color.BLACK);
		lblActions.setBorder(new EmptyBorder(10, 0, 0, 0));
		actionPanel.add(lblActions);
		labels.add(lblActions);
		
		btnStartGenerate = new JStyledButton("Start Generate");
		btnStartGenerate.setMaximumSize(buttonSize);
		btnStartGenerate.setEnabled(false);
		btnStartGenerate.setFocusable(false);
		btnStartGenerate.setOpaque(false);
		btnStartGenerate.addActionListener((event) -> {
			if(!gui.borstGenerator.isRunning()) {
				Rectangle rect = canvasRegion.createIntersection(imageRegion).getBounds();
				
				if(!rect.isEmpty()) {
					Sign signType = gui.getSettingsSign();
					Color bgColor = gui.getSettingsBackgroundCalculated();
					
					BufferedImage clip = new BufferedImage(canvasRegion.width, canvasRegion.height, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = clip.createGraphics();
					g.drawImage(image, imageRegion.x - canvasRegion.x, imageRegion.y - canvasRegion.y, imageRegion.width, imageRegion.height, null);
					g.dispose();
					
					BufferedImage scaled = new BufferedImage(signType.width, signType.height, BufferedImage.TYPE_INT_ARGB);
					g = scaled.createGraphics();
					g.setColor(bgColor);
					g.fillRect(0, 0, scaled.getWidth(), scaled.getHeight());
					g.drawImage(clip, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
					g.dispose();
					
					BorstSettings settings = gui.borstSettings;
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
		});
		actionPanel.add(btnStartGenerate);

		btnPauseGenerate = new JStyledButton("Pause Generate");
		btnPauseGenerate.setOpaque(false);
		btnPauseGenerate.setMaximumSize(buttonSize);
		btnPauseGenerate.setFocusable(false);
		btnPauseGenerate.setEnabled(false);
		btnPauseGenerate.addActionListener((event) -> setPauseGeneration(gui.borstGenerator.isPaused()));
		actionPanel.add(btnPauseGenerate);
		
		btnResetGenerate = new JStyledButton("Reset Generate");
		btnResetGenerate.setOpaque(false);
		btnResetGenerate.setMaximumSize(new Dimension(120, 24));
		btnResetGenerate.setFocusable(false);
		btnResetGenerate.setEnabled(false);
		btnResetGenerate.addActionListener((event) -> {
			if(gui.borstGenerator.isRunning()) {
				try {
					gui.borstSettings.DirectImage = null;
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
		});
		actionPanel.add(btnResetGenerate);
		
		JStyledButton btnClose = new JStyledButton("Close");
		btnClose.setOpaque(false);
		btnClose.setMaximumSize(buttonSize);
		btnClose.setFocusable(false);
		btnClose.addActionListener((event) -> {
			int dialogResult = JOptionPane.showConfirmDialog(dialog, "Do you want to close the application?", "Warning", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		});
		actionPanel.add(btnClose);
		
		Dimension textDimension = new Dimension(120, 24);
		
		painterPanel = new JPanel();
		painterPanel.setOpaque(false);
		painterPanel.setVisible(false);
		painterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionPanel.add(painterPanel);
		painterPanel.setLayout(new BoxLayout(painterPanel, BoxLayout.Y_AXIS));
		
		JLabel lblPaintImage = new JLabel("Draw");
		lblPaintImage.setForeground(Color.BLACK);
		lblPaintImage.setBorder(new EmptyBorder(10, 0, 0, 0));
		painterPanel.add(lblPaintImage);
		labels.add(lblPaintImage);
		
		btnDrawImage = new JStyledButton("Draw Image");
		btnDrawImage.setEnabled(false);
		btnDrawImage.setOpaque(false);
		btnDrawImage.setMaximumSize(new Dimension(120, 24));
		btnDrawImage.setFocusable(false);
		btnDrawImage.addActionListener((event) -> {
			drawDialog.openDialog(btnDrawImage.getLocationOnScreen());
			doRenderShapes = false;
			repaint();
			updateEditor();
		});
		painterPanel.add(btnDrawImage);
		
		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel_1.setBorder(new EmptyBorder(1, 1, 1, 1));
		panel_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionPanel.add(panel_1);
		panel_1.setMaximumSize(textDimension);
		panel_1.setMinimumSize(textDimension);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JLabel lblHelp = new JLabel("Help");
		lblHelp.setForeground(Color.BLACK);
		lblHelp.setBorder(new EmptyBorder(10, 0, 0, 0));
		panel_1.add(lblHelp);
		labels.add(lblHelp);
		
		JStyledButton btnGithubIssue = new JStyledButton("Report Issue");
		btnGithubIssue.setOpaque(false);
		btnGithubIssue.setMaximumSize(new Dimension(120, 24));
		btnGithubIssue.setFocusable(false);
		btnGithubIssue.addActionListener((event) -> {
			gui.openIssueUrl();
		});
		actionPanel.add(btnGithubIssue);
		
		JStyledButton btnDonate = new JStyledButton("Donate");
		btnDonate.setOpaque(false);
		btnDonate.setFocusable(false);
		btnDonate.setMaximumSize(new Dimension(120, 24));
		btnDonate.addActionListener((event) -> gui.openDonationUrl());
		actionPanel.add(btnDonate);
		
		JStyledButton btnAbout = new JStyledButton("About");
		btnAbout.setOpaque(false);
		btnAbout.setMaximumSize(new Dimension(120, 24));
		btnAbout.setFocusable(false);
		btnAbout.addActionListener((event) -> {
			String message = """
							 Created by HardCoded & Sekwah41
							 
							 HardCoded
							 - Design
							 - Sorting algorithm
							 - Optimized generation
							 
							 Sekwah41
							 - Initial generation
							 """;
			
			JOptionPane.showMessageDialog(dialog, message, "About me", JOptionPane.INFORMATION_MESSAGE);
		});
		actionPanel.add(btnAbout);
		
		updateEditor();
	}
	
	protected void setHideRegions(boolean enable) {
		if(enable) {
			btnHideRegions.setText("Show Regions");
		} else {
			btnHideRegions.setText("Hide Regions");
		}
		
		btnHideRegions.setSelected(enable);
		
		updateButtons();
		repaint();
	}
	
	private void setPauseGeneration(boolean enable) {
		if(enable) {
			btnPauseGenerate.setText("Pause Generate");
			gui.borstGenerator.resume();
		} else {
			btnPauseGenerate.setText("Resume Generate");
			gui.borstGenerator.pause();
		}
		
		updateButtons();
	}
	
	public void updateEditor() {
		setBorder(isFullscreen ? new LineBorder(gui.getBorderColor(), BORDER_SIZE):null);
		actionPanel.setBackground(gui.getToolbarColor());
		
		for(JLabel label : labels) {
			label.setForeground(gui.getLabelColor());
		}
		
		generationLabel.setLocation((topBarPanel.getWidth() - generationLabel.getWidth()) / 2, generationLabel.getY());
		generationInfo.setLocation((topBarPanel.getWidth() - generationInfo.getWidth()) / 2, generationInfo.getY());
		
		BorstData lastData = this.lastData;
		if(lastData == null) {
			setEstimatedGenerationLabel(0, gui.getSettingsMaxShapes());
		} else {
			setEstimatedGenerationLabel(lastData.getIndex(), gui.getSettingsMaxShapes());
		}
		
		btnPauseGenerate.setText(gui.borstGenerator.isPaused() ? "Resume Generate":"Pause Generate");
	}
	
	public Rectangle getCanvasArea() {
		return canvasRegion;
	}
	
	public Rectangle getScreenLocation() {
		return monitorPicker.getMonitor().getBounds();
	}
	
	protected void setEstimatedGenerationLabel(int index, int maxShapes) {
		generationLabel.setText("%d/%d shapes generated".formatted(index, maxShapes));
		generationInfo.setText("Estimated %s".formatted(RustTranslator.getTimeMinutesMessage((long)(index * 1.1 * (1000.0 / (double)gui.getSettingsClickInterval())))));
	}
	
	protected void setExactGenerationLabel(long time) {
		generationInfo.setText("Time %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setRemainingTime(int index, int maxShapes, long timeLeft) {
		generationLabel.setText("%d/%d shapes drawn".formatted(index, maxShapes));
		generationInfo.setText("Time left %s".formatted(RustTranslator.getTimeMinutesMessage(timeLeft)));
	}
	
	private void updateButtons() {
		boolean defaultAction = action == OverlayType.NONE;
		
		// You should only be able to pick monitor the screen is enabled
		btnSelectMonitor.setEnabled(defaultAction || gui.borstGenerator.isRunning() && gui.borstGenerator.isPaused());
		regionsPanelTest.setVisible(isFullscreen);
		painterPanel.setVisible(isFullscreen);
		
		btnHideRegions.setEnabled(defaultAction || gui.borstGenerator.isPaused() && gui.borstGenerator.isRunning());
		boolean defaultRegion = !btnHideRegions.isSelected() && defaultAction;
		btnSelectCanvasRegion.setEnabled(defaultRegion || action == OverlayType.SELECT_CANVAS_REGION);
		btnSelectImageRegion.setEnabled(defaultRegion && image != null || action == OverlayType.SELECT_IMAGE_REGION);
		
		btnMaximize.setEnabled(true);
		btnOpenImage.setEnabled(defaultAction);
		btnStartGenerate.setEnabled(isFullscreen && defaultAction && image != null && !gui.borstGenerator.isRunning());
		btnPauseGenerate.setEnabled(isFullscreen && gui.borstGenerator.isRunning());
		btnResetGenerate.setEnabled(gui.borstGenerator.isPaused() && gui.borstGenerator.isRunning());
		btnDrawImage.setEnabled(gui.borstGenerator.isPaused() && gui.borstGenerator.isRunning());
	}
	
	private void changeFullscreen(ActionEvent event) {
		isFullscreen = !isFullscreen;
		btnMaximize.setText(isFullscreen ? "Minimize":"Make Fullscreen");
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
	
	protected BorstData getBorstData() {
		return lastData;
	}
	
	public void onBorstCallback(BorstData data) {
		modelImage = data.getModel().current.image;
		lastData = data;
		
		setEstimatedGenerationLabel(data.getIndex(), gui.getSettingsMaxShapes());
		repaint();
		
		if(data.isDone()) {
			setPauseGeneration(false);
		}
	}
	
	private boolean doRenderShapes = false;
	private int renderShapesAmount = 0;
	public void renderShapes(int value) {
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
		
		if(!btnHideRegions.isSelected()) {
			g.setColor(new Color(0x30000000, true));
			g.fillRect(0, 0, getWidth() - 150, getHeight());
			
			// Draw region outline
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
			
			// Draw model image
			{
				BufferedImage image = modelImage;
				if(image != null) {
					g.setColor(new Color(gui.borstSettings.Background));
					g.fillRect(canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height);
					g.drawImage(image, canvasRegion.x, canvasRegion.y, canvasRegion.width, canvasRegion.height, null);
				}
			}
			
			// Draw Custom Shapes
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
			
			try {
				BobRustPalette palette = drawDialog.getPalette();
				Map<BorstColor, Point> map = palette.getColorMap();
				
				g.setColor(Color.white);
				Point screen = dialog.getLocationOnScreen();
				for(Map.Entry<BorstColor, Point> entry : map.entrySet()) {
					Point point = entry.getValue();
					Point sc = new Point(point.x - screen.x, point.y - screen.y);
					g.drawOval(sc.x - 15, sc.y - 15, 30, 29);
				}
				
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
