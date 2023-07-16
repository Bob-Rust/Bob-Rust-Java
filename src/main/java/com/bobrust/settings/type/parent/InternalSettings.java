package com.bobrust.settings.type.parent;

import com.bobrust.lang.RustUI;
import com.bobrust.settings.Settings;
import com.bobrust.util.RustWindowUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public class InternalSettings {
	private static final Logger LOGGER = LogManager.getLogger(InternalSettings.class);
	private final Map<String, SettingsType<?>> settings;
	private final Map<String, GuiElement> settingsMapping;
	private final Properties properties;
	private Consumer<SettingsType<?>> listener;
	
	public InternalSettings() {
		settingsMapping = new LinkedHashMap<>();
		settings = new LinkedHashMap<>();
		properties = new Properties();
		
		Field[] fields = Settings.class.getDeclaredFields();
		for (Field field : fields) {
			var type = field.getType();
			var name = field.getName();
			
			if (SettingsType.class.isAssignableFrom(type)) {
				SettingsType<?> element;
				try {
					element = (SettingsType<?>) field.get(null);
				} catch (Exception e) {
					throw new RuntimeException("Failed to initialize settings class", e);
				}
				
				GuiElement annotation = field.getAnnotation(GuiElement.class);
				if (annotation != null) {
					settingsMapping.put(name, annotation);
				}
				
				element.bind(this, name);
				settings.put(name, element);
			}
		}
	}
	
	/**
	 * Call this to initialize this class
	 */
	public void init(File configFile) {
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				LOGGER.error("Error creating config file", e);
			}
		}
		
		try (FileInputStream stream = new FileInputStream(configFile)) {
			properties.load(stream);
		} catch (IOException e) {
			LOGGER.error("Error reading config file", e);
		}
	}
	
	/**
	 * Save data to a config file
	 */
	public void save(File configFile, boolean allowWarningMessage) {
		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			properties.store(stream, "");
		} catch (IOException e) {
			if (allowWarningMessage) {
				RustWindowUtil.showWarningMessage(
					RustUI.getString(RustUI.Type.WARNING_CONFIGPERMISSION_MESSAGE),
					RustUI.getString(RustUI.Type.WARNING_CONFIGPERMISSION_TITLE)
				);
			}
			
			LOGGER.error("Error saving config file", e);
		}
	}
	
	public void setListener(Consumer<SettingsType<?>> listener) {
		this.listener = listener;
	}
	
	public void load() {
		for (SettingsType<?> setting : settings.values()) {
			String value = properties.getProperty(setting.id);
			setting.load(value);
		}
	}
	
	public void update(SettingsType<?> changed) {
		// Update property map
		String updatedValue = changed.stringValue();
		if (updatedValue == null
		// 	|| Objects.equals(changed.value, changed.defaultValue)
		) {
			properties.remove(changed.id);
		} else {
			properties.setProperty(changed.id, changed.stringValue());
		}
		
		// Send update to listener
		var localListener = listener;
		if (localListener != null) {
			localListener.accept(changed);
		}
	}
	
	public void reset() {
		properties.clear();
		
		// Update values
		load();
		
		// Send update to listener
		var localListener = listener;
		if (localListener != null) {
			localListener.accept(null);
		}
	}
	
	public Map<String, GuiElement> getSettings() {
		return settingsMapping;
	}
	
	public SettingsType<?> getSetting(String key) {
		return settings.get(key);
	}
}
