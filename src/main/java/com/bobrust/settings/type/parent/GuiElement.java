package com.bobrust.settings.type.parent;

import com.bobrust.lang.RustUI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiElement {
	enum Tab {
		Generator,
		Editor,
		Debugging
	}
	
	enum Type {
		/**
		 * This element will use the default editor
		 */
		Default,
		
		/**
		 * This element will use a custom editor
		 */
		Custom,
		
		/**
		 * Use a combo-box variation of this setting
		 */
		Combo
	}
	
	/**
	 * Which tab the setting should be placed in
	 */
	Tab tab();
	
	/**
	 * Which editor should be used when modifying this setting
	 */
	Type type() default Type.Default;
	
	RustUI.Type label();
	RustUI.Type tooltip();
	RustUI.Type button() default RustUI.Type.NONE;
}
