package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;
import com.bobrust.util.RustUtil;

public class IntType extends SettingsType<Integer> {
	private boolean useMinMax;
	private int min;
	private int max;
	
	public IntType(int defaultValue) {
		super(defaultValue);
	}
	
	public IntType(int defaultValue, int min, int max) {
		super(defaultValue);
		
		this.useMinMax = true;
		this.min = min;
		this.max = max;
		
		if (RustUtil.clamp(defaultValue, min, max) != defaultValue) {
			throw new RuntimeException("Invalid default value '" + defaultValue + "' for min max (" + min + " / " + max + ")");
		}
	}
	
	@Override
	protected void load(String value) {
		int parsedValue = SettingParser.parseInteger(value, defaultValue);
		if (useMinMax) {
			this.value = RustUtil.clamp(parsedValue, min, max);
		} else {
			this.value = parsedValue;
		}
	}
	
	@Override
	public void set(Integer value) {
		int parsedValue = value == null ? defaultValue : value;
		if (useMinMax) {
			parsedValue = RustUtil.clamp(parsedValue, min, max);
		}
		
		this.value = parsedValue;
		parent.update(this);
	}
	
	@Override
	public Integer get() {
		Integer result = super.get();
		int parsedValue = result == null ? defaultValue : result;
		if (useMinMax) {
			parsedValue = RustUtil.clamp(parsedValue, min, max);
		}
		
		return parsedValue;
	}
	
	@Override
	public String stringValue() {
		return Integer.toString(get());
	}
	
	public boolean isRange() {
		return useMinMax;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
}
