package com.bobrust.gui;

import java.awt.Color;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.gui.dialog.RustFileDialog;
import com.bobrust.settings.RustSettingsImpl;

public class BobRustEditor extends RustSettingsImpl {
	private BobRustDesktopOverlay overlayDialog;
	
	private RustFileDialog fileChooser;
	private FileNameExtensionFilter filterImages;
	@SuppressWarnings("unused")
	private FileNameExtensionFilter filterPresets;
	
	final BorstGenerator borstGenerator;
	
	public BobRustEditor() {
		this.borstGenerator = new BorstGeneratorBuilder()
			.setCallback(this::onBorstCallback)
			.setSettings(getBorstSettings())
			.create();
		this.loadSettings();
		
		SwingUtilities.invokeLater(this::setup);
	}
	
	private void setup() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignore) {
			// Do nothing
		}
		
		fileChooser = new RustFileDialog();
		filterImages = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
		filterPresets = new FileNameExtensionFilter("Preset Files", "borst");
		
		overlayDialog = new BobRustDesktopOverlay(this);
		overlayDialog.getDialog().setVisible(true);
	}
	
	public File openImageFileChooser(JDialog dialog) {
		File file = fileChooser.open(dialog, filterImages, "Open Image", EditorImageDirectory.get());
		if (file != null) {
			EditorImageDirectory.set(file.getParentFile().getAbsolutePath());
			return file;
		}
		
		return null;
	}
	
	public File openPresetFileChooser(JDialog dialog) {
		throw new UnsupportedOperationException();
	}
	
	public void onBorstCallback(BorstData data) {
		overlayDialog.onBorstCallback(data);
	}
	
	@Override
	protected void postSetProperty() {
		if (overlayDialog != null) {
			overlayDialog.updateEditor();
		}
	}
	
	protected Color getSettingsBackgroundCalculated() {
		Color bgColor = SettingsBackground.get();
		return bgColor == null ? SettingsSign.get().getAverageColor() : bgColor;
	}
}
