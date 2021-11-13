package com.bobrust.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.dialog.RustFileDialog;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	
	public static void main(String[] args) {
		boolean hasLwjgl = RustFileDialog.hasLwjgl();
		
		// Display system information to help with potential bugs.
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
		LOGGER.info("");
		
		new BobRustEditor();
	}
}
