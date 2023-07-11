package com.bobrust.settings.type.parent;

import com.bobrust.util.RustSigns;
import com.bobrust.util.Sign;

import java.awt.*;

public class SettingParser {
	private SettingParser() {
		
	}
	
	public static Color parseColor(String value, Color defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		try {
			return new Color(Integer.parseInt(value, 16) & 0xffffff);
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}
	
	public static String parseString(String value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		// Escaping?
		return value;
	}
	
	public static Integer parseInteger(String value, Integer defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}
	
	public static Boolean parseBoolean(String value, Boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		return Boolean.parseBoolean(value);
	}
	
	public static Sign parseSign(String value, Sign defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		return RustSigns.SIGNS.getOrDefault(value, defaultValue);
	}
	
	public static Dimension parseDimension(String value, Dimension defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		
		String[] parts = value.split(" ");
		if (parts.length != 2) {
			return defaultValue;
		}
		
		try {
			int width = Integer.parseInt(parts[0]);
			int height = Integer.parseInt(parts[1]);
			return new Dimension(width, height);
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}
}
