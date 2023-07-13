package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class MonitorPickerDialog extends JDialog {
	private GraphicsConfiguration config;
	
	public MonitorPickerDialog(JDialog parent) {
		super(parent, "", ModalityType.APPLICATION_MODAL);
		setUndecorated(true);
		setAlwaysOnTop(true);
		setBackground(new Color(0x20000000, true));
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0x20000000, true));
		panel.setOpaque(false);
		panel.setBorder(new LineBorder(Color.red, 10));
		setContentPane(panel);
		
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dispose();
			}
		});
	}
	
	private GraphicsConfiguration getGraphicsConfiguration(Point point) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		if (point != null) {
			GraphicsDevice[] array = graphicsEnvironment.getScreenDevices();
			
			for (GraphicsDevice device : array) {
				for (GraphicsConfiguration gc : device.getConfigurations()) {
					if (gc.getBounds().contains(point)) {
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
				while (true) {
					Point mousePoint = MouseInfo.getPointerInfo().getLocation();
					GraphicsConfiguration gc = getGraphicsConfiguration(mousePoint);
					setBounds(gc.getBounds());
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		try {
			// Make it possible to select another monitor
			GraphicsConfiguration gc = getGraphicsConfiguration(getLocation());
			setBounds(gc.getBounds());
			
			// This blocks until the monitor has been selected
			setVisible(true);
			
			// TODO: Use getScreenResolution() to calculate the correct screen size
			// Toolkit.getDefaultToolkit().getScreenResolution();
			return updateConfiguration(getLocation());
		} finally {
			thread.interrupt();
		}
	}
	
	public GraphicsConfiguration updateConfiguration(Point point) {
		GraphicsConfiguration config = getGraphicsConfiguration(point);
		this.config = config;
		return config;
	}
	
	public GraphicsConfiguration getMonitor() {
		if (config == null) {
			// If the config is null we use the default configuration
			config = getGraphicsConfiguration(null);
		}
		
		return config;
	}
}
