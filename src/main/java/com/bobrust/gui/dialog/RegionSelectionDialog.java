package com.bobrust.gui.dialog;

import com.bobrust.gui.comp.JResizeComponent;
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
	private final JResizeComponent resizeComponent;
	private final Timer selectMonitorTimer;
	
	public RegionSelectionDialog(JDialog parent) {
		super(parent, "", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		
		setUndecorated(true);
		setFocusable(true);
		setAlwaysOnTop(true);
		setBackground(new Color(0x10000000, true));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Set state of region to no-change
				dispose();
				parent.setVisible(true);
				RustWindowUtil.showWarningMessage(parent, "When closing the region dialog you should use 'Escape' or 'Enter' instead of pressing Alt+F4", "How to close the region selector");
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
		
		{
			JPanel test = new JPanel();
			test.setOpaque(false);
			test.setLayout(null);
			panel.add(test, BorderLayout.CENTER);
			
			resizeComponent = new JResizeComponent();
			test.add(resizeComponent);
		}
		
		JPanel topTextPanel = new JPanel();
		topTextPanel.setBackground(Color.red);
		topTextPanel.setPreferredSize(new Dimension(0, 40));
		panel.add(topTextPanel, BorderLayout.NORTH);
		
		// TODO: Make sure this label is always blocking some part of the top of the screen
		//       Make sure the font exists and will look similar on multiple systems!
		JLabel topText = new JLabel("Press Escape or Enter to close");
		topText.setHorizontalTextPosition(SwingConstants.CENTER);
		topText.setFont(topText.getFont().deriveFont(20.0f));
		topText.setForeground(Color.white);
		topTextPanel.add(topText);
		
		selectMonitorTimer = new Timer(100, e -> {
			var pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo == null) {
				return;
			}
			
			Point mousePoint = pointerInfo.getLocation();
			GraphicsConfiguration gc = getGraphicsConfiguration(mousePoint);
			setBounds(gc.getBounds());
			
			// Revalidate resize component
			resizeComponent.revalidateSelection();
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
	
	public Region openDialog(boolean allowChangingMonitor, Image displayedImage, Rectangle rect) {
		if (allowChangingMonitor) {
			selectMonitorTimer.start();
		}
		
		try {
			// Make it possible to select another monitor
			GraphicsConfiguration config = getGraphicsConfiguration(getLocation());
			setBounds(config.getBounds());
			getContentPane().revalidate();
			
			// Update resizeComponent with the specified image
			if (rect.width < 0 || rect.height < 0) {
				Rectangle screenBounds = config.getBounds();
				rect.x = (screenBounds.width - 20 - 300) / 2;
				rect.y = (screenBounds.height - 60 - 300) / 2;
				rect.width = 300;
				rect.height = 300;
			}
			resizeComponent.setImage(displayedImage);
			resizeComponent.setSelectedRectangle(rect);
			
			// This blocks until the monitor has been selected
			parent.setVisible(false);
			setVisible(true);
			parent.setVisible(true);
			dispose();
			
			if (allowChangingMonitor) {
				// Update result value
				config = getGraphicsConfiguration(getLocation());
			}
			
			return new Region(config, resizeComponent.getSelectedRectangle());
		} finally {
			selectMonitorTimer.stop();
		}
	}
	
	public static record Region(GraphicsConfiguration monitor, Rectangle selection) { }
}
