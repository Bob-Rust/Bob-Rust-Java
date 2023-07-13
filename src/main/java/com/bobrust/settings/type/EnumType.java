package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;
import java.util.Objects;

public class EnumType<T extends Enum<T>> extends SettingsType<T> {
	public EnumType(T defaultValue) {
		super(Objects.requireNonNull(defaultValue));
	}
	
	@Override
	protected void load(String value) {
		this.value = SettingParser.parseEnum(value, defaultValue);
	}
	
	@Override
	public void set(T value) {
		this.value = value == null ? defaultValue : value;
		parent.update(this);
	}
	
	public void setAbstract(Object value) {
		if (value == null) {
			this.value = defaultValue;
			parent.update(this);
		} else if (defaultValue.getDeclaringClass().isAssignableFrom(value.getClass())) {
			this.value = defaultValue.getDeclaringClass().cast(value);
			parent.update(this);
		}
	}
	
	@Override
	public String stringValue() {
		var local = get();
		return local.toString();
	}
	
	public int getIndex() {
		var local = get();
		T[] array = getEnumValues();
		for (int i = 0; i < array.length; i++) {
			if (array[i] == local) {
				return i;
			}
		}
		
		// Unreachable
		return 0;
	}
	
	public T[] getEnumValues() {
		return defaultValue.getDeclaringClass().getEnumConstants();
	}
}
