package com.bobrust.gui;

import com.bobrust.gui.comp.JToolbarButton;
import com.bobrust.gui.dialog.*;
import com.bobrust.settings.Settings;
import com.bobrust.util.ResourceUtil;
import com.bobrust.util.data.AppConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

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
	
	// Drawing variables
	private GraphicsConfiguration monitor;
	private final Rectangle canvasRect = new Rectangle();
	private final Rectangle imageRect = new Rectangle();
	
	public ApplicationWindow() {
		super(null, "BobRust", ModalityType.MODELESS);
		
		setIconImage(AppConstants.DIALOG_ICON);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setFocusable(true);
		setFocusableWindowState(true);
		setSize(0, 520);
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
		
		// Setup overlay UI
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(new Color(0x1d1b43));
		
		panel.add(createToolbar(), BorderLayout.CENTER);
		panel.add(createVersion(), BorderLayout.SOUTH);
		setContentPane(panel);
	}
	
	private Image testImage;
	private JPanel createToolbar() {
		JPanel toolbarPanel = new JPanel();
		// toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.PAGE_AXIS));
		toolbarPanel.setOpaque(false);
		
		int iconSize = 42;
		toolbarPanel.add(createButton("/ui/settings_icon.png", "Open Settings", iconSize, 0xe0e0e0, false, e -> {
			settingsDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/upload_image.png", "Select Image", iconSize, 0xe0e0e0, false, e -> {
			System.out.println(Settings.EditorImageDirectory.get());
			File image = fileDialog.open(this, new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()), "test", Settings.EditorImageDirectory.get());
			try {
				if (image != null) {
					testImage = ImageIO.read(image);
					Settings.EditorImageDirectory.set(image.getParent().toString());
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}));
		toolbarPanel.add(createButton("/ui/canvas_icon.png", "Sign Type", iconSize, 0xe0e0e0, false, e -> {
			signPickerDialog.openSignDialog(toolbarPanel.getLocationOnScreen());
			Settings.SettingsSign.set(signPickerDialog.getSelectedSign());
		}));
		toolbarPanel.add(createButton("/ui/crop_canvas.png", "Select Canvas Area", iconSize, 0xe0e0e0, false,  e -> {
			var region = regionSelectionDialog.openDialog(true, null, canvasRect);
			System.out.println(region);
			monitor = region.monitor();
			canvasRect.setBounds(region.selection());
			
			// TODO: This should move the image rect
		}));
		toolbarPanel.add(createButton("/ui/image_select_icon.png", "Select Image Area", iconSize, 0xe0e0e0, false, e -> {
			var region = regionSelectionDialog.openDialog(false, testImage, imageRect);
			System.out.println(region);
			imageRect.setBounds(region.selection());
		}));
		// toolbarPanel.add(createButton("/ui/draw_play_icon.png", iconSize, 0x33ff66, false, e -> {}));
		// toolbarPanel.add(createButton("/ui/draw_paused_icon.png", iconSize, 0xff6633, false, e -> {}));
		toolbarPanel.add(createButton("/ui/draw_icon.png", "Draw Image", iconSize, 0xe0e0e0, false, e -> {
			drawDialog.openDialog(monitor, toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/sponsor_icon.png", "Donate", iconSize, 0xdb61a2, false, e -> {}));
		
		return toolbarPanel;
	}
	
	private JComponent createButton(String iconPath, String tooltip, int size, int rgb, boolean disable, ActionListener action) {
		Image icon = ResourceUtil.loadImageFromResources(iconPath);
		icon = icon.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		
		JToolbarButton button = new JToolbarButton(icon, rgb);
		button.setBackground(new Color(0x302d5b));
		button.setToolTipText(tooltip);
		button.setEnabled(!disable);
		button.addActionListener(action);
		return button;
	}
	
	private JPanel createVersion() {
		JLabel versionLabel = new JLabel("Version " + AppConstants.VERSION);
		versionLabel.setForeground(new Color(0xcbcbcb));
		
		JPanel versionPanel = new JPanel();
		versionPanel.setOpaque(false);
		versionPanel.add(versionLabel);
		return versionPanel;
	}
}
