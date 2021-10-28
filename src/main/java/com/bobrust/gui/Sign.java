package com.bobrust.gui;

public class Sign {
	public final String name;
	public final int width;
	public final int height;
	
	public Sign(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Sign)) return false;
		return hashCode() == obj.hashCode();
	}
	
	@Override
	public String toString() {
		return "Sign { name: '%s', width: %d, height: %d }".formatted(name, width, height);
	}
}
