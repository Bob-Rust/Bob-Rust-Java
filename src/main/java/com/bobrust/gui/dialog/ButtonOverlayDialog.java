package com.bobrust.gui.dialog;

import javax.swing.*;
import java.awt.*;

public class ButtonOverlayDialog extends JPanel {
	public ButtonOverlayDialog() {
		setLayout(new BorderLayout());
		setBackground(new Color(0x333e48));
		setDoubleBuffered(true);
	}
	
	/**
	 * Set the button configuration object to correctly draw the shapes on the screen
	 * @param object the button configuration object
	 */
	public void setButtonConfiguration(Object object) {
		
	}
}
