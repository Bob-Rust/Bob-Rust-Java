package com.bobrust.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

public class RustWindowUtil {
	private static boolean init;
	
	// TODO: Make sure the UIManager is initialized
	public static void loadUI() {
		if (init) {
			return;
		}
		
		// Make sure we are initialized
		init = true;
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignore) {
			// Do nothing
		}
	}
	
	private static JFrame getDisposableFrame() {
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.setIconImage(RustConstants.DIALOG_ICON);
		frame.setLocationRelativeTo(null);
		return frame;
	}
	
	public static void showWarningMessage(String message, String title) {
		JFrame frame = getDisposableFrame();
		showWarningMessage(frame, message, title);
		frame.dispose();
	}
	
	public static void showWarningMessage(Component component, String message, String title) {
		RustWindowUtil.loadUI();
		JOptionPane.showMessageDialog(component, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static BufferedImage captureScreenshot(GraphicsConfiguration gc) {
		GraphicsDevice device = gc.getDevice();
		int screenWidth = device.getDisplayMode().getWidth();
		int screenHeight = device.getDisplayMode().getHeight();
		
		try {
			Robot robot = new Robot(device);
			
			return (BufferedImage) robot.createMultiResolutionScreenCapture(gc.getBounds())
				.getResolutionVariant(screenWidth, screenHeight);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
