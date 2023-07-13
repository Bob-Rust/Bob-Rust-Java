package com.bobrust.gui.dialog;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * <pre>
 *   This file dialog has been created in order to fix some common issue with
 *   trying to use the system file viewer.
 *   
 *   (org.eclipse.swt): Does not work because there is no way to
 *   connect an AWT/Swing application with SWT.
 *   
 *   (javafx.stage.FileChooser): Does not work because there is no way
 *   to correctly fix modality. If you make the fullscreen dialog
 *   AlwaysOnTop the FileChooser will be placed beneath it.
 *   
 *   (java.awt.FileDialog): Does not work because there is no way to make
 *   the window AlwaysOnTop. There is also crashes between systems.
 *   
 *   (LWJGL3 Tinyfd): Would be hard to link all dll files but should be
 *   tested.
 * </pre>
 * @author HardCoded
 */
public class RustFileDialog {
	private static final Logger LOGGER = LogManager.getLogger(RustFileDialog.class);
	private static final boolean HAS_LWJGL;
	
	static {
		boolean hasLwjgl;
		try {
			Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
			hasLwjgl = true;
		} catch (Throwable ignore) {
			// If we caught an exception that means that we did not load the library
			hasLwjgl = false;
		}
		HAS_LWJGL = hasLwjgl;
	}
	
	/**
	 * Returns if LWJGL has been successfully loaded.
	 */
	public static boolean hasLwjgl() {
		return HAS_LWJGL;
	}

	private JFileChooser fileChooser;
	
	public RustFileDialog() {
		
	}
	
	public File open(JDialog parent, FileNameExtensionFilter fileFilter, String title, String directory) {
		try {
			if (HAS_LWJGL) {
				// If we successfully loaded LWJGL
				try (MemoryStack stack = MemoryStack.stackPush()) {
					PointerBuffer filters = stack.mallocPointer(fileFilter.getExtensions().length);
					for (String filter : fileFilter.getExtensions()) {
						filters.put(stack.UTF8("*." + filter));
					}
					filters.flip();
					
					String result = TinyFileDialogs.tinyfd_openFileDialog(title, directory + File.separatorChar, filters, fileFilter.getDescription(), false);
					
					if (result != null) {
						return new File(result).getAbsoluteFile();
					}
				}
				
				return null;
			}
		} catch (Throwable t) {
			// We where not supposed to crash here but we still did.
			// This is a fallback catch to make sure we always have
			// at least one file dialog displayed
			LOGGER.error("Failed to create Tinyfd open dialog", t);
			t.printStackTrace();
		}
		
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
		}
		
		fileChooser.setDialogTitle(title);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(new File(directory));
		
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilter);
		int status = fileChooser.showOpenDialog(parent);
		if (status == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		
		return null;
	}
}
