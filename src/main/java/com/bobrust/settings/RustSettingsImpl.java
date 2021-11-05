package com.bobrust.settings;

import java.awt.Color;
import java.io.*;
import java.util.Objects;
import java.util.Properties;

import com.bobrust.generator.BorstSettings;
import com.bobrust.util.RustConstants;
import com.bobrust.util.RustSigns;
import com.bobrust.util.Sign;

public abstract class RustSettingsImpl implements RustSettings {
	private static final File CONFIG_FILE = new File(System.getProperty("user.home"), "bobrust.properties");
	
	// Editor configurations
	private Color toolbarColor;
	private Color borderColor;
	private Color labelColor;
	private String imagePath;
	private String presetPath;
	
	// Generator configuration
	private Color settingsBackground = RustConstants.CANVAS_AVERAGE;
	private Sign settingsSign;
	private int clickInterval;
	private int autosaveInterval;
	private int scalingType;
	
	// Properties
	private final Properties properties;
	private boolean allowSaving;
	
	// Borst settings
	private BorstSettings borstSettings;
	
	public RustSettingsImpl() {
		this.properties = new Properties();
		this.borstSettings = new BorstSettings();
	}
	
	protected void loadSettings() {
		if(!CONFIG_FILE.exists()) {
			try {
				CONFIG_FILE.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		try(FileInputStream stream = new FileInputStream(CONFIG_FILE)) {
			properties.load(stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Integer borderColor = getSettingInteger(Settings.BORDER_COLOR);
		setBorderColor(borderColor == null ? null:new Color(borderColor));
		
		Integer toolbarColor = getSettingInteger(Settings.TOOLBAR_COLOR);
		setToolbarColor(toolbarColor == null ? null:new Color(toolbarColor));
		
		Integer labelColor = getSettingInteger(Settings.LABEL_COLOR);
		setLabelColor(labelColor == null ? null:new Color(labelColor));
		
		setEditorImageDirectory(getSettingsProperty(Settings.IMAGE_PATH));
		setEditorPresetDirectory(getSettingsProperty(Settings.PRESET_PATH));
		
		Integer settingsAlpha = getSettingInteger(Settings.SETTINGS_ALPHA);
		setSettingsAlpha(settingsAlpha == null ? 2:settingsAlpha);
		
		Integer settingsScaling = getSettingInteger(Settings.SETTINGS_SCALING);
		setSettingsScaling(settingsScaling == null ? RustConstants.IMAGE_SCALING_NEAREST:settingsScaling);
		
		Integer settingsMaxShapes = getSettingInteger(Settings.SETTINGS_MAX_SHAPES);
		setSettingsMaxShapes(settingsMaxShapes == null ? 4000:settingsMaxShapes);
		
		Integer settingsCallbackInterval = getSettingInteger(Settings.SETTINGS_CALLBACK_INTERVAL);
		setSettingsCallbackInterval(settingsCallbackInterval == null ? 100:settingsCallbackInterval);
		
		String settingsSigntype = getSettingsProperty(Settings.SETTINGS_SIGN_TYPE);
		setSettingsSign(RustSigns.SIGNS.get(settingsSigntype));
		
		Integer settingsBackground = getSettingInteger(Settings.SETTINGS_BACKGROUND);
		setSettingsBackground(settingsBackground == null ? null:new Color(settingsBackground));
		
		Integer clickInterval = getSettingInteger(Settings.SETTINGS_CLICK_INTERVAL);
		setSettingsClickInterval(clickInterval == null ? 30:clickInterval);
		
		Integer autosaveInterval = getSettingInteger(Settings.SETTINGS_AUTOSAVE_INTERVAL);
		setSettingsAutosaveInterval(autosaveInterval == null ? 1000:autosaveInterval);
		
		allowSaving = true;
		saveSettings();
	}
	
	private Integer getSettingInteger(Settings key) {
		try {
			return Integer.valueOf(properties.getProperty(key.id));
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	private String getSettingsProperty(Settings key) {
		return properties.getProperty(key.id);
	}
	
	private void saveSettings() {
		if(!allowSaving) return;
		
		try(FileOutputStream stream = new FileOutputStream(CONFIG_FILE)) {
			properties.store(stream, "");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void setProperty(Settings key, Object value) {
		properties.setProperty(key.id, Objects.toString(value));
		saveSettings();
	}
	
	public BorstSettings getBorstSettings() {
		return borstSettings;
	}
	
	@Override
	public void setBorderColor(Color color) {
		borderColor = color == null ? new Color(0xff3333):color;
		setProperty(Settings.BORDER_COLOR, borderColor.getRGB());
	}

	@Override
	public void setToolbarColor(Color color) {
		toolbarColor = color == null ? new Color(0x4f4033):color;
		setProperty(Settings.TOOLBAR_COLOR, toolbarColor.getRGB());
	}

	@Override
	public void setLabelColor(Color color) {
		labelColor = color == null ? Color.white:color;
		setProperty(Settings.LABEL_COLOR, labelColor.getRGB());
	}

	@Override
	public void setEditorImageDirectory(String pathname) {
		imagePath = pathname == null ? System.getProperty("user.home"):pathname;
		setProperty(Settings.IMAGE_PATH, imagePath);
	}

	@Override
	public void setEditorPresetDirectory(String pathname) {
		presetPath = pathname == null ? System.getProperty("user.home"):pathname;
		setProperty(Settings.PRESET_PATH, presetPath);
	}

	@Override
	public void setSettingsAlpha(int alpha) {
		borstSettings.Alpha = alpha;
		setProperty(Settings.SETTINGS_ALPHA, alpha);
	}
	
	@Override
	public void setSettingsScaling(int index) {
		scalingType = index;
		setProperty(Settings.SETTINGS_SCALING, index);
	}

	@Override
	public void setSettingsMaxShapes(int maxShapes) {
		borstSettings.MaxShapes = maxShapes;
		setProperty(Settings.SETTINGS_MAX_SHAPES, maxShapes);
	}

	@Override
	public void setSettingsCallbackInterval(int callbackInterval) {
		borstSettings.CallbackInterval = callbackInterval;
		setProperty(Settings.SETTINGS_CALLBACK_INTERVAL, callbackInterval);
	}

	@Override
	public void setSettingsSign(Sign sign) {
		settingsSign = sign == null ? RustSigns.FIRST:sign;
		setProperty(Settings.SETTINGS_SIGN_TYPE, settingsSign.name);
	}

	@Override
	public void setSettingsBackground(Color color) {
		settingsBackground = color;
		setProperty(Settings.SETTINGS_BACKGROUND, color == null ? null:color.getRGB());
	}

	@Override
	public void setSettingsClickInterval(int interval) {
		clickInterval = interval;
		setProperty(Settings.SETTINGS_CLICK_INTERVAL, clickInterval);
	}

	@Override
	public void setSettingsAutosaveInterval(int interval) {
		autosaveInterval = interval;
		setProperty(Settings.SETTINGS_AUTOSAVE_INTERVAL, autosaveInterval);
	}

	@Override
	public Color getBorderColor() {
		return Objects.requireNonNull(borderColor);
	}

	@Override
	public Color getToolbarColor() {
		return Objects.requireNonNull(toolbarColor);
	}

	@Override
	public Color getLabelColor() {
		return Objects.requireNonNull(labelColor);
	}

	@Override
	public String getEditorImageDirectory() {
		return Objects.requireNonNull(imagePath);
	}

	@Override
	public String getEditorPresetDirectory() {
		return Objects.requireNonNull(presetPath);
	}

	@Override
	public int getSettingsAlpha() {
		return borstSettings.Alpha;
	}
	
	@Override
	public int getSettingsScaling() {
		return scalingType;
	}
	
	@Override
	public int getSettingsMaxShapes() {
		return borstSettings.MaxShapes;
	}

	@Override
	public int getSettingsCallbackInterval() {
		return borstSettings.CallbackInterval;
	}

	@Override
	public int getSettingsAutosaveInterval() {
		return autosaveInterval;
	}

	@Override
	public Color getSettingsBackground() {
		return settingsBackground;
	}

	@Override
	public Sign getSettingsSign() {
		return Objects.requireNonNull(settingsSign);
	}
	
	@Override
	public int getSettingsClickInterval() {
		return clickInterval;
	}
}
