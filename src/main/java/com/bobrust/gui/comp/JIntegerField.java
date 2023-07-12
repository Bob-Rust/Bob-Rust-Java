package com.bobrust.gui.comp;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

public class JIntegerField extends JFormattedTextField {
	private final NumberFormatter formatter;
	
	public JIntegerField(int value) {
		NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
		format.setGroupingUsed(false);
		
		// TODO: Make so that if someone types a value that would be greater
		//       than the maximum it converts the value to the max.
		formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(99999);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		formatter.setOverwriteMode(false);
		formatter.setFormat(format);
		
		setFormatterFactory(new AbstractFormatterFactory() {
			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				return formatter;
			}
		});
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterAction");
		getActionMap().put("enterAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JIntegerField.this.fireActionPerformed();
			}
		});
		
		Object backspaceKey = getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
		Action backspaceAction = getActionMap().get(backspaceKey);
		
		getActionMap().put(backspaceKey, new AbstractAction() {
			public boolean isDelegate() {
				return getText().length() != 1;
			}
			
			@Override
			public boolean accept(Object sender) {
				if (isDelegate()) {
					return backspaceAction.accept(sender);
				} else {
					return super.accept(sender);
				}
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isDelegate()) {
					backspaceAction.actionPerformed(e);
				} else {
					JIntegerField.this.selectAll();
				}
			}
		});
		
		setText(Integer.toString(value));
	}
	
	public void setMinimum(int value) {
		formatter.setMinimum(value);
	}
	
	public void setMaximum(int value) {
		formatter.setMaximum(value);
	}
	
	public int getNumberValue() throws NumberFormatException {
		return Integer.parseInt(this.getText());
	}
}
