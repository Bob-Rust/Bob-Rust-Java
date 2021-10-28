package com.bobrust.gui;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.generator.BorstSettings;

public class BobRust {
	private BobRustOverlay dialog;
	
	private JFileChooser fileChooser;
	private FileFilter filterImages;
	@SuppressWarnings("unused")
	private FileFilter filterPresets;
	
	final BorstSettings borstSettings;
	final BorstGenerator borstGenerator;
	
	
	public BobRust() {
		this.borstSettings = new BorstSettings();
		this.borstGenerator = new BorstGeneratorBuilder()
			.setCallback(this::onBorstCallback)
			.setSettings(borstSettings)
			.create();
				
		SwingUtilities.invokeLater(this::setup);
	}
	
	/**
	 * This method should be called on the swing thread.
	 * @wbp.parser.entryPoint
	 */
	private void setup() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			// Do nothing
		}
		
		dialog = new BobRustOverlay(this);
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		
		filterImages = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
		filterPresets = new FileNameExtensionFilter("Preset Files", "borst");
		
		dialog.dialog.setVisible(true);
	}

	public File openImageFileChooser(JDialog dialog) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(filterImages);
		fileChooser.setFileFilter(filterImages);
		int status = fileChooser.showOpenDialog(dialog);
		
		if(status == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		
		return null;
	}
	
	public void onBorstCallback(BorstData data) {
		dialog.onBorstCallback(data);
	}
}
