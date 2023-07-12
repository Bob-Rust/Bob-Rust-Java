package com.bobrust.settings;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstSettings;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.*;

public abstract class RustSettingsImpl {
	private static final Logger LOGGER = LogManager.getLogger(RustSettingsImpl.class);
	private static final File CONFIG_FILE = new File("bobrust.properties");
	
	// Properties
	private boolean allowSaving;
	
	// Borst settings
	private final BorstSettings borstSettings;
	
	public RustSettingsImpl() {
		this.borstSettings = new BorstSettings();
	}
	
	protected void loadSettings() {
		Settings.InternalSettings.init(CONFIG_FILE);
		Settings.InternalSettings.load();
		Settings.InternalSettings.setListener(changed -> {
			saveSettings();
			updateBorstSettings();
			postSetProperty();
		});
		
		updateBorstSettings();
		
		allowSaving = true;
		saveSettings(true);
	}
	
	private void updateBorstSettings() {
		borstSettings.MaxShapes = Settings.SettingsMaxShapes.get();
		borstSettings.Alpha = Settings.SettingsAlpha.get();
		borstSettings.Background = Settings.getSettingsBackgroundCalculated().getRGB();
		borstSettings.CallbackInterval = Settings.EditorCallbackInterval.get();
	}
	
	private void saveSettings() {
		saveSettings(false);
	}
	
	private void saveSettings(boolean giveMessage) {
		if (!allowSaving) return;
		
		try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE)) {
			Settings.InternalSettings.properties.store(stream, "");
		} catch (IOException e) {
			if (giveMessage) {
				RustWindowUtil.showWarningMessage(
					RustUI.getString(Type.WARNING_CONFIGPERMISSION_MESSAGE),
					RustUI.getString(Type.WARNING_CONFIGPERMISSION_TITLE)
				);
			}
			
			LOGGER.error("Error saving config file", e);
			LOGGER.throwing(e);
			e.printStackTrace();
		}
	}
	
	protected abstract void postSetProperty();
	
	public BorstSettings getBorstSettings() {
		return borstSettings;
	}
}
