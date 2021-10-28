package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class BobRustMonitorPicker {
	private final JDialog dialog;
	
	private GraphicsConfiguration config;
	
	public BobRustMonitorPicker(JDialog parent) {
		dialog = new JDialog(parent, "", ModalityType.APPLICATION_MODAL);
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setBackground(new Color(0x20000000, true));
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0x20000000, true));
		panel.setOpaque(false);
		panel.setBorder(new LineBorder(Color.red, 10));
		dialog.setContentPane(panel);
		
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dialog.dispose();
			}
		});
	}
	
	private GraphicsConfiguration getGraphicsConfiguration(Point point) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		if(point != null) {
			GraphicsDevice[] array = graphicsEnvironment.getScreenDevices();
			
			for(GraphicsDevice device : array) {
				for(GraphicsConfiguration gc : device.getConfigurations()) {
					if(gc.getBounds().contains(point)) {
						return gc;
					}
				}
			}
		}
		
		// Returns the default configuration
		return graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
	}
	
	public GraphicsConfiguration openDialog() {
		Thread thread = new Thread(() -> {
			try {
				while(true) {
					Point mousePoint = MouseInfo.getPointerInfo().getLocation();
					GraphicsConfiguration gc = getGraphicsConfiguration(mousePoint);
					dialog.setBounds(gc.getBounds());
					Thread.sleep(100);
				}
			} catch(InterruptedException e) {
				
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		try {
			// Make it possible to select another monitor
			GraphicsConfiguration gc = getGraphicsConfiguration(dialog.getLocation());
			dialog.setBounds(gc.getBounds());
			dialog.setVisible(true);
			config = getGraphicsConfiguration(dialog.getLocation());
		} finally {
			thread.interrupt();
		}
		
		return config;
	}
	
	public GraphicsConfiguration getMonitor() {
		if(config == null) {
			// If the config is null we use the default configuration
			config = getGraphicsConfiguration(null);
		}
		
		return config;
	}
}
