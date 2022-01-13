package com.bobrust.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.dialog.RustFileDialog;

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
		LOGGER.info("");
		
		// TODO: Make sure logs and properties can be saved without admin rights.
		// TODO: Do not draw outside the image region +- the radius of the largest shape.
		// TODO: Maybe remove the monitor button and only make it choose the screen you
		//       expanded the window on?
		// ----: Move the Monitor Button into the options.
		// TODO: Remove the show regions button.
		// TODO: Add keybinds like F11 to minimize the tool.
		// TODO: Maybe show the image at the bottom of the screen and add animations.
		// MAYBE: Have it always generate the shape to a maximum of 99999 shapes.
		//        This would remove all buttons and be recomputed when the one of
		//        the following are changed:
		//         * Image region
		//         * Canvas region
		//         * Current image
		//         * Options
		
		new BobRustEditor();
	}
}
