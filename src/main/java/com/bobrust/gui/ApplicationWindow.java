package com.bobrust.gui;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.gui.comp.JRoundPanel;
import com.bobrust.gui.comp.JToolbarButton;
import com.bobrust.gui.dialog.*;
import com.bobrust.settings.Settings;
import com.bobrust.util.ResourceUtil;
import com.bobrust.util.data.RustConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Application window.
 * 
 * Adds a toolbar and shows the current version of the app
 * 
 * @author HardCoded
 */
public class ApplicationWindow extends JDialog {
	private final RustFileDialog fileDialog;
	private final SignPickerDialog signPickerDialog;
	private final SettingsDialog settingsDialog;
	private final DrawDialogNew drawDialog;
	private final RegionSelectionDialog regionSelectionDialog;
	private final BorstGenerator borstGenerator;
	
	public ApplicationWindow() {
		super(null, "BobRust", ModalityType.MODELESS);
		
		setIconImage(RustConstants.DIALOG_ICON);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setFocusable(true);
		setFocusableWindowState(true);
		setSize(0, 520);
		// setUndecorated(true);
		// setBackground(new Color(0, true));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// The vm might close when all dialogs has been disposed but
				// to make sure the tool closes we will call exit here
				System.exit(0);
			}
		});
		
		// Setup member variables
		fileDialog = new RustFileDialog();
		regionSelectionDialog = new RegionSelectionDialog(this);
		signPickerDialog = new SignPickerDialog(this);
		settingsDialog = new SettingsDialog(this);
		drawDialog = new DrawDialogNew(this);
		borstGenerator = null; // TODO - Move the borst generator closer to the code using it
		/*new BorstGenerator.BorstGeneratorBuilder()
			.setCallback(this::onBorstCallback)
			.setSettings(getBorstSettings())
			.create();*/
		
		// Setup overlay UI
		JRoundPanel panel = new JRoundPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorderRadius(0);
		panel.setBackground(new Color(0x1d1b43));
		
		// panel.add(createHeader(), BorderLayout.NORTH);
		panel.add(createToolbar(), BorderLayout.CENTER);
		panel.add(createVersion(), BorderLayout.SOUTH);
		
		// TODO: Canvas area region selector will give us which monitor to draw on
		setContentPane(panel);
	}
	
	private JPanel createHeader() {
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(new Color(0x302d5b));
		
		// WindowDragListener listener = new WindowDragListener(this);
		// headerPanel.addMouseListener(listener);
		// headerPanel.addMouseMotionListener(listener);
		
		Image image = ResourceUtil.loadImageFromResources("/ui/close_button.png");
		Image scaledImage = image.getScaledInstance(12, 12, Image.SCALE_SMOOTH);
		scaledImage = JToolbarButton.changeColor(scaledImage, 0xffffff);
		JLabel closeButton = new JLabel(new ImageIcon(scaledImage));
		closeButton.setBorder(new EmptyBorder(5, 5, 5, 10));
		headerPanel.add(closeButton, BorderLayout.EAST);
		
		return headerPanel;
	}
	
	private JPanel createToolbar() {
		JPanel toolbarPanel = new JPanel();
		// toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.PAGE_AXIS));
		toolbarPanel.setOpaque(false);
		
		int iconSize = 42;
		toolbarPanel.add(createButton("/ui/settings_icon.png", iconSize, 0xe0e0e0, false, e -> {
			settingsDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/upload_image.png", iconSize, 0xe0e0e0, false, e -> {
			fileDialog.open(this, new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()), "test", Settings.EditorImageDirectory.get());
		}));
		toolbarPanel.add(createButton("/ui/canvas_icon.png", iconSize, 0xe0e0e0, false, e -> {
			signPickerDialog.openSignDialog(toolbarPanel.getLocationOnScreen());
			Settings.SettingsSign.set(signPickerDialog.getSelectedSign());
		}));
		toolbarPanel.add(createButton("/ui/crop_canvas.png", iconSize, 0xe0e0e0, false,  e -> {
			regionSelectionDialog.openDialog();
		}));
		toolbarPanel.add(createButton("/ui/image_select_icon.png", iconSize, 0xe0e0e0, true, e -> {}));
		// toolbarPanel.add(createButton("/ui/draw_play_icon.png", iconSize, 0x33ff66, false, e -> {}));
		// toolbarPanel.add(createButton("/ui/draw_paused_icon.png", iconSize, 0xff6633, false, e -> {}));
		toolbarPanel.add(createButton("/ui/draw_icon.png", iconSize, 0xe0e0e0, true, e -> {
			drawDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/sponsor_icon.png", iconSize, 0xdb61a2, false, e -> {}));
		
		return toolbarPanel;
	}
	
	private JComponent createButton(String iconPath, int size, int rgb, boolean disable, ActionListener action) {
		Image icon = ResourceUtil.loadImageFromResources(iconPath);
		icon = icon.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		
		JToolbarButton button = new JToolbarButton(icon, rgb);
		button.setBackground(new Color(0x302d5b));
		button.setEnabled(!disable);
		button.addActionListener(action);
		return button;
	}
	
	private JPanel createVersion() {
		JLabel versionLabel = new JLabel("Version " + RustConstants.VERSION);
		versionLabel.setForeground(new Color(0xcbcbcb));
		
		JPanel versionPanel = new JPanel();
		versionPanel.setOpaque(false);
		versionPanel.add(versionLabel);
		return versionPanel;
	}
	
	/*
	private static class WindowDragListener extends MouseAdapter {
		private final Point dragStart = new Point();
		private final Window window;
		private boolean shouldDrag;
		
		public WindowDragListener(Window window) {
			this.window = window;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			shouldDrag = e.getButton() == MouseEvent.BUTTON1;
			
			Point mouse = e.getLocationOnScreen();
			Point window = this.window.getLocationOnScreen();
			dragStart.setLocation(
				mouse.x - window.x,
				mouse.y - window.y
			);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!shouldDrag) {
				return;
			}
			
			window.setLocation(
				e.getXOnScreen() - dragStart.x,
				e.getYOnScreen() - dragStart.y
			);
		}
	}
	*/
}
