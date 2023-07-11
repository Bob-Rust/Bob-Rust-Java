package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;
import com.bobrust.util.Sign;

public class SignType extends SettingsType<Sign> {
	public SignType(Sign defaultValue) {
		super(defaultValue);
	}
	
	@Override
	public void load(String value) {
		this.value = SettingParser.parseSign(value, defaultValue);
	}
	
	@Override
	public void set(Sign value) {
		this.value = value == null ? defaultValue : value;
		parent.update(this);
	}
	
	@Override
	public String stringValue() {
		var local = get();
		if (local == null) {
			return null;
		}
		
		return local.name;
	}
}
