package com.bobrust.gui.comp;

import javax.swing.*;
import java.awt.*;

public class JRoundPanel extends JPanel {
	private int borderRadius;
	
	public JRoundPanel() {
		// Set transparent
		super.setOpaque(false);
	}
	
	@Override
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;
		g.setColor(getBackground());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);
		
		// Remove everything outside
		// RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);
		// var oldClip = gr.getClip();
		// gr.setClip(shape);
		super.paint(gr);
		// gr.setClip(oldClip);
	}
	
	public void setBorderRadius(int borderRadius) {
		this.borderRadius = borderRadius;
	}
	
	@Override
	public void setOpaque(boolean opaque) {
		super.setOpaque(false);
	}
	
	@Override
	public boolean isOpaque() {
		return false;
	}
}
