package com.bobrust.settings;

import java.awt.Color;
import java.io.*;
import java.util.Objects;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstSettings;
import com.bobrust.util.*;

public abstract class RustSettingsImpl implements RustSettings {
	private static final Logger LOGGER = LogManager.getLogger(RustSettingsImpl.class);
	private static final File CONFIG_FILE = new File("bobrust.properties");
	
	// Editor configurations
	private Color toolbarColor;
	private Color borderColor;
	private Color labelColor;
	private String imagePath;
	private String presetPath;
	
	// Generator configuration
	private Color settingsBackground = RustConstants.WOODEN_AVERAGE;
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
				LOGGER.error("Error creating config file: {}", e);
				LOGGER.throwing(e);
				e.printStackTrace();
			}
		}
		
		try(FileInputStream stream = new FileInputStream(CONFIG_FILE)) {
			properties.load(stream);
		} catch(IOException e) {
			LOGGER.error("Error reading config file: {}", e);
			LOGGER.throwing(e);
			e.printStackTrace();
		}
		
		setEditorBorderColor(getSettingsColor(Settings.BORDER_COLOR));
		setEditorToolbarColor(getSettingsColor(Settings.TOOLBAR_COLOR));
		setEditorLabelColor(getSettingsColor(Settings.LABEL_COLOR));
		setEditorImageDirectory(getSettingsProperty(Settings.IMAGE_PATH));
		setEditorPresetDirectory(getSettingsProperty(Settings.PRESET_PATH));
		setEditorCallbackInterval(getSettingInteger(Settings.CALLBACK_INTERVAL));
		
		setSettingsAlpha(getSettingInteger(Settings.SETTINGS_ALPHA));
		setSettingsScaling(getSettingInteger(Settings.SETTINGS_SCALING));
		setSettingsMaxShapes(getSettingInteger(Settings.SETTINGS_MAX_SHAPES));
		setSettingsSign(RustSigns.SIGNS.get(getSettingsProperty(Settings.SETTINGS_SIGN_TYPE)));
		setSettingsBackground(getSettingsColor(Settings.SETTINGS_BACKGROUND));
		setSettingsClickInterval(getSettingInteger(Settings.SETTINGS_CLICK_INTERVAL));
		setSettingsAutosaveInterval(getSettingInteger(Settings.SETTINGS_AUTOSAVE_INTERVAL));
		
		allowSaving = true;
		saveSettings(true);
	}
	
	private Integer getSettingInteger(Settings key) {
		try {
			return Integer.valueOf(properties.getProperty(key.id));
		} catch(NumberFormatException ignored) {
			return null;
		}
	}
	
	private Color getSettingsColor(Settings key) {
		String string = getSettingsProperty(key);
		if(string == null) {
			return null;
		}
		
		try {
			return new Color(Integer.parseInt(string, 16) & 0xffffff);
		} catch(NumberFormatException ignored) {
			return null;
		}
	}
	
	private String getSettingsProperty(Settings key) {
		return properties.getProperty(key.id);
	}
	
	private void saveSettings() {
		saveSettings(false);
	}
	
	private void saveSettings(boolean giveMessage) {
		if(!allowSaving) return;
		
		try(FileOutputStream stream = new FileOutputStream(CONFIG_FILE)) {
			properties.store(stream, "");
		} catch(IOException e) {
			if (giveMessage) {
				JOptionPane.showMessageDialog(
					null,
					"This tool does not have the permission to update the config file\n" +
					"Try run the application as an Administrator if you want to fix this",
					"Importaint",
					JOptionPane.WARNING_MESSAGE
				);
			}
			
			LOGGER.error("Error saving config file: {}", e);
			LOGGER.throwing(e);
			e.printStackTrace();
		}
	}
	
	private void setColorProperty(Settings key, Color color) {
		setProperty(key, color == null ? null:"%06x".formatted(color.getRGB() & 0xffffff));
	}
	
	private void setProperty(Settings key, Object value) {
		properties.setProperty(key.id, Objects.toString(value));
		saveSettings();
		postSetProperty();
	}
	
	protected abstract void postSetProperty();
	
	public BorstSettings getBorstSettings() {
		return borstSettings;
	}
	
	@Override
	public void setEditorBorderColor(Color color) {
		borderColor = color == null ? new Color(0xff3333):color;
		setColorProperty(Settings.BORDER_COLOR, borderColor);
	}

	@Override
	public void setEditorToolbarColor(Color color) {
		toolbarColor = color == null ? new Color(0x333333):color;
		setColorProperty(Settings.TOOLBAR_COLOR, toolbarColor);
	}

	@Override
	public void setEditorLabelColor(Color color) {
		labelColor = color == null ? Color.white:color;
		setColorProperty(Settings.LABEL_COLOR, labelColor);
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
		interval = interval == null ? 20:RustUtil.clamp(interval, 1, 99999);
		borstSettings.CallbackInterval = interval;
		setProperty(Settings.CALLBACK_INTERVAL, interval);
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
		maxShapes = maxShapes == null ? 99999:RustUtil.clamp(maxShapes, 0, 99999);
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
		setColorProperty(Settings.SETTINGS_BACKGROUND, color);
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
