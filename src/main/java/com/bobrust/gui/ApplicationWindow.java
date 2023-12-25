package com.bobrust.gui;

import com.bobrust.gui.comp.JToolbarButton;
import com.bobrust.gui.dialog.*;
import com.bobrust.settings.Settings;
import com.bobrust.util.*;
import com.bobrust.util.data.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
	private static final Logger LOGGER = LogManager.getLogger(ApplicationWindow.class);
	
	private final RustFileDialog fileDialog;
	private final SignPickerDialog signPickerDialog;
	private final SettingsDialog settingsDialog;
	private final ScreenDrawDialog screenDrawDialog;
	private final RegionSelectionDialog regionSelectionDialog;
	
	// Drawing variables
	private GraphicsConfiguration monitor;
	private final Rectangle canvasRect = new Rectangle(-1, -1);
	private final Rectangle imageRect = new Rectangle(-1, -1);
	private Image drawImage;
	
	// State Toolbar Buttons
	private JToolbarButton canvasAreaButton;
	private JToolbarButton imageAreaButton;
	private JToolbarButton drawButton;
	
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
		regionSelectionDialog = new RegionSelectionDialog(this, true);
		signPickerDialog = new SignPickerDialog(this);
		settingsDialog = new SettingsDialog(this);
		screenDrawDialog = new ScreenDrawDialog(this);
		
		// Setup overlay UI
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(new Color(0x1d1b43));
		
		panel.add(createToolbar(), BorderLayout.CENTER);
		panel.add(createVersion(), BorderLayout.SOUTH);
		setContentPane(panel);
		
		// If we are running from an IDE then we want to add some debug stuff
		if (AppConstants.IS_IDE && AppConstants.DEBUG_AUTO_IMAGE) {
			canvasAreaButton.setEnabled(true);
			imageAreaButton.setEnabled(true);
			drawButton.setEnabled(true);
			
			monitor = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice()
				.getDefaultConfiguration();
			
			canvasRect.setRect(98, 170, 1348, 671);
			imageRect.setRect(473, 172, 691, 669);
			
			try {
				drawImage = ImageIO.read(new File("src/test/resources/draw-test/draw_0.png"));
			} catch (IOException e) {
				LOGGER.error("Could not find image", e);
			}
		}
	}
	
	private JPanel createToolbar() {
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setOpaque(false);
		
		canvasAreaButton = createButton("/ui/crop_canvas.png", "Select Canvas Area", 0xe0e0e0,  e -> selectCanvasRegion());
		imageAreaButton = createButton("/ui/image_select_icon.png", "Select Image Area", 0xe0e0e0, e -> selectImageRegion());
		
		drawButton = createButton("/ui/draw_icon.png", "Draw Image", 0xe0e0e0, e -> {
			screenDrawDialog.openDialog(monitor, toolbarPanel.getLocationOnScreen());
		});
		
		// Add toolbar items
		toolbarPanel.add(createButton("/ui/settings_icon.png", "Open Settings", 0xe0e0e0, e -> {
			settingsDialog.openDialog(toolbarPanel.getLocationOnScreen());
		}));
		toolbarPanel.add(createButton("/ui/upload_image.png", "Select Image", 0xe0e0e0, e -> importImage()));
		toolbarPanel.add(createButton("/ui/canvas_icon.png", "Sign Type", 0xe0e0e0, e -> {
			signPickerDialog.openSignDialog(toolbarPanel.getLocationOnScreen(), Settings.SettingsSign.get());
			Settings.SettingsSign.set(signPickerDialog.getSelectedSign());
		}));
		toolbarPanel.add(canvasAreaButton);
		toolbarPanel.add(imageAreaButton);
		toolbarPanel.add(drawButton);
		// toolbarPanel.add(createButton("/ui/draw_play_icon.png", "", 0x33ff66, e -> {}));
		// toolbarPanel.add(createButton("/ui/draw_paused_icon.png", "", 0xff6633, e -> {}));
		toolbarPanel.add(createButton("/ui/sponsor_icon.png", "Donate", 0xdb61a2, e -> UrlUtils.openDonationUrl()));
		
		canvasAreaButton.setEnabled(false);
		imageAreaButton.setEnabled(false);
		drawButton.setEnabled(false);
		
		return toolbarPanel;
	}
	
	private void importImage() {
		File image = fileDialog.open(
			this,
			new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()),
			"Open Image",
			Settings.EditorImageDirectory.get()
		);
		
		if (image == null) {
			return;
		}
		
		try {
			BufferedImage bi = ImageIO.read(image);
			if (bi == null) {
				LOGGER.warn("Unsupported image file format '{}'", image.getAbsolutePath());
				RustWindowUtil.showWarningMessage(
					"The file '%s' is not a readable image format, try converting the image to a png before using"
						.formatted(image.getAbsolutePath()),
					"Failed to read image file"
				);
				return;
			}
			drawImage = bi;
			canvasAreaButton.setEnabled(true);
		} catch (IOException e) {
			LOGGER.error("Invalid file format of image", e);
			e.printStackTrace();
		}
	}
	
	private void selectCanvasRegion() {
		var region = regionSelectionDialog.openDialog(null, true, null, canvasRect);
		monitor = region.monitor();
		canvasRect.setBounds(region.selection());
		imageAreaButton.setEnabled(true);
		System.out.println(canvasRect);
	}
	
	private void selectImageRegion() {
		var region = regionSelectionDialog.openDialog(monitor, false, drawImage, imageRect);
		imageRect.setBounds(region.selection());
		drawButton.setEnabled(true);
		System.out.println(imageRect);
	}
	
	private JToolbarButton createButton(String iconPath, String tooltip, int rgb, ActionListener action) {
		int size = 42;
		Image icon = ResourceUtil.loadImageFromResources(iconPath);
		icon = ImageUtil.getSmoothScaledInstance(icon, size, size);
		
		JToolbarButton button = new JToolbarButton(icon, rgb);
		button.setBackground(new Color(0x302d5b));
		button.setToolTipText(tooltip);
		button.addActionListener(action);
		return button;
	}
	
	private JPanel createVersion() {
		JLabel versionLabel = new JLabel("Version " + AppConstants.VERSION);
		versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		versionLabel.setForeground(new Color(0xcbcbcb));
		
		JLabel aboutLabel = new JLabel("About us");
		aboutLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		aboutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		aboutLabel.setForeground(new Color(0xcbcbcb));
		aboutLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JEditorPane pane = new JEditorPane("text/html", "");
				pane.setEditable(false);
				pane.setOpaque(false);
				pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
				pane.setText(
					"""
					Created by HardCoded & Sekwah41<br><br>
					
					HardCoded<br>
					- Design / UX<br>
					- Sorting algorithm<br>
					- Optimized generation<br><br>
					
					Sekwah41<br>
					- Shape generation algorithm<br><br>
					
					Links:<br>
					Github <a href="#blank">https://github.com/Bob-Rust/Bob-Rust-Java/</a>"""
				);
				pane.addHyperlinkListener(e2 -> {
					if (e2.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
						UrlUtils.openGithubUrl();
					}
				});
				
				JOptionPane.showMessageDialog(ApplicationWindow.this,
					pane,
					"About us",
					JOptionPane.INFORMATION_MESSAGE
				);
			}
		});
		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.Y_AXIS));
		versionPanel.setOpaque(false);
		versionPanel.add(versionLabel);
		versionPanel.add(aboutLabel);
		return versionPanel;
	}
	
	public Image getDrawImage() {
		return drawImage;
	}
	
	public Rectangle getCanvasRect() {
		return canvasRect;
	}
	
	public Rectangle getImageRect() {
		return imageRect;
	}
}
