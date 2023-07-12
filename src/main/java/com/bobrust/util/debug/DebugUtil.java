package com.bobrust.util.debug;

import javax.swing.*;
import java.awt.*;

public class DebugUtil {
	private DebugUtil() {
		
	}
	
	@Deprecated
	public static void debugShowImage(Image image, int scale) {
		int width = image.getWidth(null) * scale;
		int height = image.getHeight(null) * scale;
		
		Image scaled = image.getScaledInstance(width, height, Image.SCALE_FAST);
		ImageIcon icon = new ImageIcon(scaled);
		JOptionPane.showMessageDialog(null, new JLabel(icon), "Debug image", JOptionPane.INFORMATION_MESSAGE);
	}
}
