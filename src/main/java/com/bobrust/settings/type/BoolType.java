package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;

public class BoolType extends SettingsType<Boolean> {
	public BoolType(boolean defaultValue) {
		super(defaultValue);
	}
	
	@Override
	protected void load(String value) {
		this.value = SettingParser.parseBoolean(value, defaultValue);
	}
	
	@Override
	public void set(Boolean value) {
		this.value = value == null ? defaultValue : value;
		parent.update(this);
	}
	
	@Override
	public String stringValue() {
		return Boolean.toString(get());
	}
}
