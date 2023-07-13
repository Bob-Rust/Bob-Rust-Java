package com.bobrust.gui;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.gui.dialog.RustFileDialog;
import com.bobrust.settings.Settings;
import com.bobrust.settings.RustSettingsImpl;

// TODO: Remove extends
public class BobRustEditor extends RustSettingsImpl {
	private BobRustDesktopOverlay overlayDialog;
	private ApplicationWindow overlayDialogNew;
	
	private RustFileDialog fileChooser;
	private FileNameExtensionFilter filterImages;
	
	final BorstGenerator borstGenerator;
	
	public BobRustEditor() {
		this.borstGenerator = new BorstGeneratorBuilder()
			.setCallback(data -> {
				var overlay = overlayDialog;
				if (overlay != null) {
					overlay.onBorstCallback(data);
				}
			})
			.setSettings(getBorstSettings())
			.create();
		this.loadSettings();
		
		SwingUtilities.invokeLater(this::setup);
	}
	
	private void setup() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignore) {
			// Do nothing
		}
		
		fileChooser = new RustFileDialog();
		filterImages = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
		
		overlayDialogNew = new ApplicationWindow();
		overlayDialogNew.setVisible(true);
		
		overlayDialog = new BobRustDesktopOverlay(this);
		overlayDialog.getDialog().setVisible(true);
	}
	
	public File openImageFileChooser(JDialog dialog) {
		File file = fileChooser.open(dialog, filterImages, "Open Image", Settings.EditorImageDirectory.get());
		if (file != null) {
			Settings.EditorImageDirectory.set(file.getParentFile().getAbsolutePath());
			return file;
		}
		
		return null;
	}
	
	@Override
	protected void postSetProperty() {
		if (overlayDialog != null) {
			overlayDialog.updateEditor();
		}
	}
}
