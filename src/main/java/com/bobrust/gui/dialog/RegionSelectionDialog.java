package com.bobrust.gui.dialog;

import com.bobrust.util.RustWindowUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog that allows you to select a region
 */
public class RegionSelectionDialog extends JDialog {
	private final JDialog parent;
	private GraphicsConfiguration config;
	
	public RegionSelectionDialog(JDialog parent) {
		super(parent, "", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		
		setUndecorated(true);
		setFocusable(true);
		setAlwaysOnTop(true);
		setBackground(new Color(0x40000000, true));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Set state of region to no-change
				dispose();
				parent.setVisible(true);
				RustWindowUtil.showWarningMessage(parent, "When closing the region dialog you should use 'Enter' or 'Escape' instead of pressing Alt+F4", "How to close the region selector");
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// Correct way to close the region selector
					dispose();
				}
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new LineBorder(Color.red, 10));
		panel.setOpaque(false);
		setContentPane(panel);
		
		JPanel topTextPanel = new JPanel();
		topTextPanel.setBackground(Color.red);
		panel.add(topTextPanel, BorderLayout.NORTH);
		
		// TODO: Move the text a bit further up to make more space for selecting the canvas area
		JLabel topText = new JLabel("Press Enter or Escape to close");
		topText.setHorizontalTextPosition(SwingConstants.CENTER);
		topText.setFont(topText.getFont().deriveFont(20.0f));
		topText.setForeground(Color.white);
		topTextPanel.add(topText);
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
					// Follow the mouse and allow it to change
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
			parent.setVisible(false);
			setVisible(true);
			parent.setVisible(true);
			dispose();
			
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
