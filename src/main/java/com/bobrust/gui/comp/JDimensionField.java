package com.bobrust.gui.comp;

import javax.swing.*;
import java.awt.*;

public class JDimensionField extends JPanel {
	public final JIntegerField widthField;
	public final JIntegerField heightField;
	
	public JDimensionField(Dimension def) {
		setLayout(new BorderLayout());
		widthField = new JIntegerField(def.width, Integer.MIN_VALUE, Integer.MAX_VALUE);
		widthField.setFocusable(true);
		
		heightField = new JIntegerField(def.height, Integer.MIN_VALUE, Integer.MAX_VALUE);
		heightField.setFocusable(true);
		
		add(widthField, BorderLayout.WEST);
		add(heightField, BorderLayout.EAST);
	}
	
	public void setMinimumDimension(Dimension size) {
		widthField.setMinimum(size.width);
		heightField.setMinimum(size.height);
	}
	
	public void setMaximumDimension(Dimension size) {
		widthField.setMaximum(size.width);
		heightField.setMaximum(size.height);
	}
	
	public Dimension getDimensionValue() {
		return new Dimension(widthField.getNumberValue(), heightField.getNumberValue());
	}
	
	public void setValue(Dimension dimension) {
		widthField.setValue(dimension.width);
		heightField.setValue(dimension.height);
	}
	
	@Override
	public void setMaximumSize(Dimension maximumSize) {
		super.setMaximumSize(maximumSize);
		
		Dimension half = new Dimension(maximumSize.width / 2, maximumSize.height);
		widthField.setMinimumSize(half);
		widthField.setMaximumSize(half);
		widthField.setPreferredSize(half);
		
		half = new Dimension(maximumSize.width - half.width, maximumSize.height);
		heightField.setMinimumSize(half);
		heightField.setMaximumSize(half);
		heightField.setPreferredSize(half);
		
		revalidate();
	}
}
