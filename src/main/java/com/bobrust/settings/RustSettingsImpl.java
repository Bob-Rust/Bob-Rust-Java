package com.bobrust.settings;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstSettings;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.*;

public abstract class RustSettingsImpl implements RustSettings {
	private static final Logger LOGGER = LogManager.getLogger(RustSettingsImpl.class);
	private static final File CONFIG_FILE = new File("bobrust.properties");
	
	// Properties
	private boolean allowSaving;
	
	// Borst settings
	private BorstSettings borstSettings;
	
	public RustSettingsImpl() {
		this.borstSettings = new BorstSettings();
	}
	
	protected void loadSettings() {
		InternalSettings.init(CONFIG_FILE);
		InternalSettings.load();
		InternalSettings.setListener(changed -> {
			saveSettings();
			postSetProperty();
		});
		
		allowSaving = true;
		saveSettings(true);
	}
	
	private void saveSettings() {
		saveSettings(false);
	}
	
	private void saveSettings(boolean giveMessage) {
		if (!allowSaving) return;
		
		try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE)) {
			InternalSettings.properties.store(stream, "");
		} catch (IOException e) {
			if (giveMessage) {
				RustWindowUtil.showWarningMessage(
					RustUI.getString(Type.WARNING_CONFIGPERMISSION_MESSAGE),
					RustUI.getString(Type.WARNING_CONFIGPERMISSION_TITLE)
				);
			}
			
			LOGGER.error("Error saving config file: {}", e);
			LOGGER.throwing(e);
			e.printStackTrace();
		}
	}
	
	protected abstract void postSetProperty();
	
	public BorstSettings getBorstSettings() {
		return borstSettings;
	}
}
