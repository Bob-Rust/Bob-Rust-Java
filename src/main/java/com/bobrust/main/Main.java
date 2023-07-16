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
		
		// TODO: Do not draw outside the image region +- the radius of the largest shape
		
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
		
		/*
		Thread aliveThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
					break;
				}
				
				System.out.println("Alive");
			}
		}, "Alive thread");
		aliveThread.setDaemon(true);
		aliveThread.start();
		*/
	}
}
