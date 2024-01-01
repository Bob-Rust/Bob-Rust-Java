package com.bobrust.gui.comp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class JIntegerField extends JTextField {
	private int minimumValue;
	private int maximumValue;
	
	// Last valid value when focus lost or when enter was pressed
	private int lastValidValue;
	private boolean hasBeeped;
	
	public JIntegerField(int value, int min, int max) {
		this.maximumValue = max;
		this.minimumValue = min;
		this.lastValidValue = value;
		
		setText("" + value);
		setForeground(Color.black);
		
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				processInput(getText(), false);
			}
		});
		
		addActionListener(e -> processInput(getText(), true));
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processInput(getText(), true);
			}
		});
	}
	
	private void processInput(String text, boolean enter) {
		boolean invalid;
		int value = 0;
		try {
			value = Integer.parseInt(text);
			invalid = value < minimumValue || value > maximumValue;
		} catch (NumberFormatException ignore) {
			invalid = true;
		}
		
		setForeground(invalid
			? Color.red
			: Color.black);
		
		if (invalid) {
			if (!text.isEmpty() && !hasBeeped) {
				Toolkit.getDefaultToolkit().beep();
				hasBeeped = true;
			}
		} else {
			hasBeeped = false;
		}
		
		if (enter) {
			if (!invalid) {
				lastValidValue = value;
			}
			
			setText("" + lastValidValue);
			setForeground(Color.black);
			hasBeeped = false;
		}
	}
	
	public void setMinimum(int value) {
		minimumValue = value;
	}
	
	public void setMaximum(int value) {
		maximumValue = value;
	}
	
	public int getMinimumValue() {
		return minimumValue;
	}
	
	public int getMaximumValue() {
		return maximumValue;
	}
	
	public void setValue(int value) {
		this.lastValidValue = Math.max(minimumValue, Math.min(maximumValue, value));
	}
	
	public int getNumberValue() {
		return lastValidValue;
	}
}
