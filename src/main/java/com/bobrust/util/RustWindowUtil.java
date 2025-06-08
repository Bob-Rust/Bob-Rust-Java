package com.bobrust.util;

import com.bobrust.util.data.AppConstants;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class RustWindowUtil {
	static {
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
		frame.setIconImage(AppConstants.DIALOG_ICON);
		frame.setLocationRelativeTo(null);
		return frame;
	}
	
	public static void showWarningMessage(String message, String title) {
		JFrame frame = getDisposableFrame();
		showWarningMessage(frame, message, title);
		frame.dispose();
	}
	
	public static void showWarningMessage(Component component, String message, String title) {
		JOptionPane.showMessageDialog(component, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showWarningMessage(Component component, Component message, String title) {
		JOptionPane.showMessageDialog(component, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static boolean showConfirmDialog(String message, String title) {
		JFrame frame = getDisposableFrame();
		int result = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.OK_CANCEL_OPTION);
		frame.dispose();;
		return result == JOptionPane.OK_OPTION;
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
	
	public static BufferedImage captureScreenshotWithScale(GraphicsConfiguration gc) {
		GraphicsDevice device = gc.getDevice();
		
		try {
			Robot robot = new Robot(device);
			return robot.createScreenCapture(gc.getBounds());
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
