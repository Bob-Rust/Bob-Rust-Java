package com.bobrust.settings.type.parent;

public abstract class SettingsType<T> {
	protected InternalSettings parent;
	protected String id;
	protected T value;
	protected T defaultValue;
	
	protected SettingsType(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	void bind(InternalSettings parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	
	/**
	 * Deserialize value from a string
	 */
	protected abstract void load(String value);
	
	public abstract void set(T value);
	
	public T get() {
		return value == null ? defaultValue : value;
	}
	
	public void setDefault() {
		this.value = defaultValue;
	}
	
	public abstract String stringValue();
	
	public String getId() {
		return id;
	}
}
