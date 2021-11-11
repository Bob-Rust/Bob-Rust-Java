package com.bobrust.gui.comp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

import com.bobrust.util.RustConstants;

@SuppressWarnings("serial")
public class JStyledButton extends JButton {
	private Color defaultColor = RustConstants.BUTTON_DEFAULT_COLOR;
	private Color hoverColor = RustConstants.BUTTON_HOVER_COLOR;
	private Color selectedColor = RustConstants.BUTTON_SELECTED_COLOR;
	private Color disabledColor = RustConstants.BUTTON_DISABLED_COLOR;
	
	public JStyledButton() {
		this("");
	}
	
	public JStyledButton(String text) {
		super(text);
		setContentAreaFilled(false);
		setFocusable(false);
		setOpaque(false);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				setBackground(selectedColor);
				repaint(0);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				repaint();
			}
		});
	}
	
	@Override
	public void repaint() {
		if(!isEnabled()) {
			setBackground(disabledColor);
		} else if(getMousePosition() != null) {
			setBackground(hoverColor);
		} else {
			setBackground(defaultColor);
		}
		
		super.repaint();
	}
	
	public void setDefaultColor(Color color) {
		defaultColor = color;
	}
	
	public void setSelectedColor(Color color) {
		selectedColor = color;
	}
	
	public void setHoverColor(Color color) {
		hoverColor = color;
	}
	
	public void setDisabledColor(Color color) {
		disabledColor = color;
	}
	
	@Override
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getBackground());
		
		// TODO: Cache this value and only update when the component is resized.
		RoundRectangle2D shape = new RoundRectangle2D.Float(
			1, 2, getWidth() - 2, getHeight() - 4,
			RustConstants.BUTTON_BORDER_RADIUS,
			RustConstants.BUTTON_BORDER_RADIUS
		);
		
		g.fill(shape);
		super.paint(gr);
	}
}
