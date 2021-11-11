package com.bobrust.settings;

import java.awt.Color;
import java.io.*;
import java.util.Objects;
import java.util.Properties;

import com.bobrust.generator.BorstSettings;
import com.bobrust.util.*;

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
		
		setEditorBorderColor(getSettingsColor(Settings.BORDER_COLOR));
		setEditorToolbarColor(getSettingsColor(Settings.TOOLBAR_COLOR));
		setEditorLabelColor(getSettingsColor(Settings.LABEL_COLOR));
		setEditorImageDirectory(getSettingsProperty(Settings.IMAGE_PATH));
		setEditorPresetDirectory(getSettingsProperty(Settings.PRESET_PATH));
		setEditorCallbackInterval(getSettingInteger(Settings.SETTINGS_CALLBACK_INTERVAL));
		
		setSettingsAlpha(getSettingInteger(Settings.SETTINGS_ALPHA));
		setSettingsScaling(getSettingInteger(Settings.SETTINGS_SCALING));
		setSettingsMaxShapes(getSettingInteger(Settings.SETTINGS_MAX_SHAPES));
		setSettingsSign(RustSigns.SIGNS.get(getSettingsProperty(Settings.SETTINGS_SIGN_TYPE)));
		setSettingsBackground(getSettingsColor(Settings.SETTINGS_BACKGROUND));
		setSettingsClickInterval(getSettingInteger(Settings.SETTINGS_CLICK_INTERVAL));
		setSettingsAutosaveInterval(getSettingInteger(Settings.SETTINGS_AUTOSAVE_INTERVAL));
		
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
	
	private Color getSettingsColor(Settings key) {
		Integer rgb = getSettingInteger(key);
		return rgb == null ? null:new Color(rgb);
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
	public void setEditorBorderColor(Color color) {
		borderColor = color == null ? new Color(0xff3333):color;
		setProperty(Settings.BORDER_COLOR, borderColor.getRGB());
	}

	@Override
	public void setEditorToolbarColor(Color color) {
		toolbarColor = color == null ? new Color(0x4f4033):color;
		setProperty(Settings.TOOLBAR_COLOR, toolbarColor.getRGB());
	}

	@Override
	public void setEditorLabelColor(Color color) {
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
	public void setEditorCallbackInterval(Integer interval) {
		interval = interval == null ? 100:RustUtil.clamp(interval, 1, 99999);
		borstSettings.CallbackInterval = interval;
		setProperty(Settings.SETTINGS_CALLBACK_INTERVAL, interval);
	}

	@Override
	public void setSettingsAlpha(Integer alpha) {
		alpha = alpha == null ? 2:RustUtil.clamp(alpha, 0, 5);
		borstSettings.Alpha = alpha;
		setProperty(Settings.SETTINGS_ALPHA, alpha);
	}
	
	@Override
	public void setSettingsScaling(Integer index) {
		index = index == null ? RustConstants.IMAGE_SCALING_NEAREST:RustUtil.clamp(index, 0, 2);
		scalingType = index;
		setProperty(Settings.SETTINGS_SCALING, index);
	}

	@Override
	public void setSettingsMaxShapes(Integer maxShapes) {
		maxShapes = maxShapes == null ? 4000:maxShapes;
		borstSettings.MaxShapes = maxShapes;
		setProperty(Settings.SETTINGS_MAX_SHAPES, maxShapes);
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
	public void setSettingsClickInterval(Integer interval) {
		interval = interval == null ? 30:RustUtil.clamp(interval, 1, 99999);
		clickInterval = interval;
		setProperty(Settings.SETTINGS_CLICK_INTERVAL, interval);
	}

	@Override
	public void setSettingsAutosaveInterval(Integer interval) {
		interval = interval == null ? 1000:RustUtil.clamp(interval, 1, 99999);
		autosaveInterval = interval;
		setProperty(Settings.SETTINGS_AUTOSAVE_INTERVAL, interval);
	}

	@Override
	public Color getEditorBorderColor() {
		return Objects.requireNonNull(borderColor);
	}

	@Override
	public Color getEditorToolbarColor() {
		return Objects.requireNonNull(toolbarColor);
	}

	@Override
	public Color getEditorLabelColor() {
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
	public int getEditorCallbackInterval() {
		return borstSettings.CallbackInterval;
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
