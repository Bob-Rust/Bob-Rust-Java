package com.bobrust.gui;

import java.awt.Color;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.settings.RustSettingsImpl;
import com.bobrust.settings.Settings;

public class BobRustEditor extends RustSettingsImpl {
	private BobRustOverlay overlayDialog;
	
	private JFileChooser fileChooser;
	private FileFilter filterImages;
	@SuppressWarnings("unused")
	private FileFilter filterPresets;
	
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
		} catch(Exception e) {
			// Do nothing
		}
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(new File(getEditorImageDirectory()));
		filterImages = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
		filterPresets = new FileNameExtensionFilter("Preset Files", "borst");
		
		overlayDialog = new BobRustOverlay(this);
		overlayDialog.dialog.setVisible(true);
	}
	
	public File openImageFileChooser(JDialog dialog) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(filterImages);
		fileChooser.setCurrentDirectory(new File(getEditorImageDirectory()));
		fileChooser.setFileFilter(filterImages);
		int status = fileChooser.showOpenDialog(dialog);
		
		if(status == JFileChooser.APPROVE_OPTION) {
			setEditorImageDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
			return fileChooser.getSelectedFile();
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
	protected void setProperty(Settings key, Object value) {
		super.setProperty(key, value);
		
		if(overlayDialog != null) {
			overlayDialog.updateEditor();
		}
	}
	
	protected Color getSettingsBackgroundCalculated() {
		Color bgColor = getSettingsBackground();
		return bgColor == null ? getSettingsSign().getAverageColor():bgColor;
	}
}
