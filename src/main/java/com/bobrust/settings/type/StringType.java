package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;

public class StringType extends SettingsType<String> {
	public StringType(String defaultValue) {
		super(defaultValue);
	}
	
	@Override
	protected void load(String value) {
		this.value = SettingParser.parseString(value, defaultValue);
	}
	
	@Override
	public void set(String value) {
		this.value = value == null ? defaultValue : value;
		parent.update(this);
	}
	
	@Override
	public String stringValue() {
		return get();
	}
}
