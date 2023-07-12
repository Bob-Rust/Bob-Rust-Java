package com.bobrust.settings.type;

import com.bobrust.settings.type.parent.SettingParser;
import com.bobrust.settings.type.parent.SettingsType;
import com.bobrust.util.RustUtil;

import java.awt.*;
import java.util.Objects;

public class SizeType extends SettingsType<Dimension> {
	private boolean useMinMax;
	private Dimension min;
	private Dimension max;
	
	public SizeType(Dimension defaultValue) {
		super(defaultValue);
	}
	
	public SizeType(Dimension defaultValue, Dimension min, Dimension max) {
		super(defaultValue);
		
		this.useMinMax = true;
		this.min = Objects.requireNonNull(min);
		this.max = Objects.requireNonNull(max);
		
		if (!Objects.equals(clampDimension(defaultValue, min, max), defaultValue)) {
			throw new RuntimeException("Invalid default value '" + defaultValue + "' for min max (" + min + " / " + max + ")");
		}
	}
	
	private Dimension clampDimension(Dimension value, Dimension min, Dimension max) {
		if (value == null) {
			return null;
		}
		
		Dimension result = new Dimension(value);
		result.width = RustUtil.clamp(result.width, min.width, max.width);
		result.height = RustUtil.clamp(result.height, min.height, max.height);
		return result;
	}
	
	@Override
	protected void load(String value) {
		Dimension parsedValue = SettingParser.parseDimension(value, defaultValue);
		if (useMinMax) {
			this.value = clampDimension(parsedValue, min, max);
		} else {
			this.value = parsedValue;
		}
	}
	
	@Override
	public void set(Dimension value) {
		Dimension parsedValue = value == null ? defaultValue : value;
		if (useMinMax) {
			parsedValue = clampDimension(parsedValue, min, max);
		}
		
		this.value = parsedValue;
		parent.update(this);
	}
	
	@Override
	public Dimension get() {
		Dimension result = super.get();
		Dimension parsedValue = result == null ? defaultValue : result;
		if (useMinMax) {
			parsedValue = clampDimension(parsedValue, min, max);
		}
		
		return parsedValue;
	}
	
	@Override
	public String stringValue() {
		var local = get();
		if (local == null) {
			return null;
		}
		
		return local.width + " " + local.height;
	}
	
	public boolean isRange() {
		return useMinMax;
	}
	
	public Dimension getMin() {
		return min;
	}
	
	public Dimension getMax() {
		return max;
	}
}
