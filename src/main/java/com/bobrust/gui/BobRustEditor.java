package com.bobrust.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bobrust.generator.BorstGenerator;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.BorstGenerator.BorstGeneratorBuilder;
import com.bobrust.generator.BorstSettings;

public class BobRustEditor {
	private static final File CONFIG_FILE = new File(System.getProperty("user.home"), "bobrust.properties");
	private BobRustOverlay overlayDialog;
	
	private JFileChooser fileChooser;
	private FileFilter filterImages;
	@SuppressWarnings("unused")
	private FileFilter filterPresets;
	
	final BorstSettings borstSettings;
	final BorstGenerator borstGenerator;
	
	
	// Editor configurations
	private Color toolbarColor = Color.white;
	private Color borderColor = Color.lightGray;
	private Color labelColor = Color.black;
	private String imagePath = System.getProperty("user.home");
	private String presetPath = System.getProperty("user.home");
	
	// Generator configuration
	private Color settingsBackground = BobRustConstants.CANVAS_AVERAGE;
	private Sign settingsSign;
	
	// Properties
	private final Properties properties;
	private boolean allowSaving;
	
	public BobRustEditor() {
		this.properties = new Properties();
		this.borstSettings = new BorstSettings();
		this.borstGenerator = new BorstGeneratorBuilder()
			.setCallback(this::onBorstCallback)
			.setSettings(borstSettings)
			.create();
		this.loadSettings();
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
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(new File(imagePath));
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
	
	public boolean openIssueUrl() {
		String issueUrl = "https://github.com/Bob-Rust/Bob-Rust-Java/issues/new";
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(issueUrl));
				return true;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public void onBorstCallback(BorstData data) {
		overlayDialog.onBorstCallback(data);
	}
	
	private void loadSettings() {
		if(!CONFIG_FILE.exists()) {
			try {
				CONFIG_FILE.createNewFile();
			} catch(IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try(FileInputStream stream = new FileInputStream(CONFIG_FILE)) {
			properties.load(stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Integer borderColor = getSettingInteger("editor.bordercolor");
		setBorderColor(borderColor == null ? null:new Color(borderColor));
		
		Integer toolbarColor = getSettingInteger("editor.toolbarcolor");
		setToolbarColor(toolbarColor == null ? null:new Color(toolbarColor));
		
		Integer labelColor = getSettingInteger("editor.labelcolor");
		setLabelColor(labelColor == null ? null:new Color(labelColor));
		
		setEditorImageDirectory(properties.getProperty("editor.imagepath"));
		setEditorPresetDirectory(properties.getProperty("editor.presetpath"));
		
		Integer settingsAlpha = getSettingInteger("settings.alpha");
		setSettingsAlpha(settingsAlpha == null ? borstSettings.Alpha:settingsAlpha);
		
		Integer settingsMaxShapes = getSettingInteger("settings.maxshapes");
		setSettingsMaxShapes(settingsMaxShapes == null ? borstSettings.MaxShapes:settingsMaxShapes);
		
		Integer settingsCallbackInterval = getSettingInteger("settings.callbackinterval");
		setSettingsCallbackInterval(settingsCallbackInterval == null ? borstSettings.CallbackInterval:settingsCallbackInterval);
		
		String settingsSigntype = properties.getProperty("settings.signtype");
		setSettingsSign(RustSigns.SIGNS.get(settingsSigntype));
		
		Integer settingsBackground = getSettingInteger("settings.background");
		setSettingsBackground(settingsBackground == null ? null:new Color(settingsBackground));
		
		allowSaving = true;
		saveSettings();
	}
	
	private Integer getSettingInteger(String name) {
		try {
			return Integer.valueOf(properties.getProperty(name));
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	private void saveSettings() {
		if(!allowSaving) return;
		
		try(FileOutputStream stream = new FileOutputStream(CONFIG_FILE)) {
			properties.store(stream, "");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setProperty(String key, Object value) {
		properties.setProperty(key, Objects.toString(value));
		saveSettings();
		
		if(overlayDialog != null) {
			overlayDialog.updateEditor();
		}
	}
	
	protected void setBorderColor(Color color) {
		borderColor = color == null ? Color.lightGray:color;
		setProperty("editor.bordercolor", borderColor.getRGB());
	}
	
	protected void setToolbarColor(Color color) {
		toolbarColor = color == null ? Color.white:color;
		setProperty("editor.toolbarcolor", toolbarColor.getRGB());
	}
	
	protected void setLabelColor(Color color) {
		labelColor = color == null ? Color.black:color;
		setProperty("editor.labelcolor", labelColor.getRGB());
	}
	
	protected void setEditorImageDirectory(String pathname) {
		imagePath = pathname == null ? System.getProperty("user.home"):pathname;
		setProperty("editor.imagepath", imagePath);
	}
	
	protected void setEditorPresetDirectory(String pathname) {
		presetPath = pathname == null ? System.getProperty("user.home"):pathname;
		setProperty("editor.presetpath", presetPath);
	}
	
	protected void setSettingsAlpha(int alpha) {
		borstSettings.Alpha = alpha;
		setProperty("settings.alpha", alpha);
	}
	
	protected void setSettingsMaxShapes(int maxShapes) {
		borstSettings.MaxShapes = maxShapes;
		setProperty("settings.maxshapes", maxShapes);
	}
	
	protected void setSettingsCallbackInterval(int callbackInterval) {
		borstSettings.CallbackInterval = callbackInterval;
		setProperty("settings.callbackinterval", callbackInterval);
	}
	
	protected void setSettingsSign(Sign sign) {
		settingsSign = sign == null ? RustSigns.FIRST:sign;
		setProperty("settings.signtype", settingsSign.name);
	}
	
	protected void setSettingsBackground(Color color) {
		settingsBackground = color;
		setProperty("settings.background", color == null ? null:settingsBackground.getRGB());
	}
	
	protected Color getBorderColor() {
		return Objects.requireNonNull(borderColor);
	}
	
	protected Color getToolbarColor() {
		return Objects.requireNonNull(toolbarColor);
	}
	
	protected Color getLabelColor() {
		return Objects.requireNonNull(labelColor);
	}
	
	protected String getEditorImageDirectory() {
		return Objects.requireNonNull(imagePath);
	}
	
	protected String getEditorPresetDirectory() {
		return Objects.requireNonNull(presetPath);
	}
	
	protected int getSettingsAlpha() {
		return borstSettings.Alpha;
	}
	
	protected int getSettingsMaxShapes() {
		return borstSettings.MaxShapes;
	}
	
	protected int getSettingsCallbackInterval() {
		return borstSettings.CallbackInterval;
	}
	
	protected Color getSettingsBackground() {
		return settingsBackground;
	}
	
	protected Sign getSettingsSign() {
		return Objects.requireNonNull(settingsSign);
	}
	
	protected Color getSettingsBackgroundCalculated() {
		Color bgColor = getSettingsBackground();
		return bgColor == null ? getSettingsSign().getAverageColor():bgColor;
	}
}
