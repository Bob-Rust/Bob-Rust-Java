package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;

import java.awt.*;

public class ColorType extends SettingsType<Color> {
	public ColorType(Color defaultValue) {
		super(defaultValue);
	}
	
	@Override
	protected void load(String value) {
		this.value = SettingParser.parseColor(value, defaultValue);
	}
	
	@Override
	public void set(Color value) {
		this.value = value == null ? defaultValue : value;
		parent.update(this);
	}
	
	@Override
	public String stringValue() {
		var local = get();
		if (local == null) {
			return null;
		}
		
		return Integer.toString(local.getRGB() & 0xffffff, 16);
	}
}
