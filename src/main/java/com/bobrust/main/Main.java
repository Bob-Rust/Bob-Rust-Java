package com.bobrust.main;

import com.bobrust.gui.ApplicationWindow;
import com.bobrust.settings.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.dialog.RustFileDialog;
import com.bobrust.util.data.AppConstants;

import javax.swing.*;
import java.io.File;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	
	public static void main(String[] args) {
		boolean hasLwjgl = RustFileDialog.hasLwjgl();
		
		// Display system information to help with potential bugs
		LOGGER.info("System information:");
		LOGGER.info("  System      : {}, ({}), ({})",
			System.getProperty("os.name"),
			System.getProperty("os.version"),
			System.getProperty("os.arch")
		);
		LOGGER.info("  JavaName    : {}", System.getProperty("java.vm.name"));
		LOGGER.info("  JavaVersion : {}", System.getProperty("java.version"));
		LOGGER.info("  Threads     : {}", Runtime.getRuntime().availableProcessors());
		LOGGER.info("  Lwjgl       : {}", hasLwjgl);
		LOGGER.info("  AppVersion  : {}", AppConstants.VERSION);
		LOGGER.info("");
		
		// TODO: Do not draw outside the image region +- the radius of the largest shape.
		// TODO: Maybe remove the monitor button and only make it choose the screen you
		//       expanded the window on?
		// ----: Move the Monitor Button into the options.
		// TODO: Add keybinds like F11 to minimize the tool.
		// TODO: Maybe show the image at the bottom of the screen and add animations.
		// MAYBE: Have it always generate the shape to a maximum of 99999 shapes.
		//        This would remove all buttons and be recomputed when the one of
		//        the following are changed:
		//         * Image region
		//         * Canvas region
		//         * Current image
		//         * Options
		
		// Setup settings
		File CONFIG_FILE = new File("bobrust.properties");
		Settings.InternalSettings.init(CONFIG_FILE);
		Settings.InternalSettings.load();
		Settings.InternalSettings.setListener(changed -> {
			Settings.InternalSettings.save(CONFIG_FILE, false);
		});
		
		// Start application
		SwingUtilities.invokeLater(() -> {
			// Setup look and feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				LOGGER.error("Failed to set system look and feel", e);
			}
			
			// Make application window visible
			new ApplicationWindow().setVisible(true);
		});
	}
}
