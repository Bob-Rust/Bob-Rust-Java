package com.bobrust.gui.dialog;

import com.bobrust.gui.comp.JCoordinateMarker;
import com.bobrust.gui.comp.JResizeComponent;
import com.bobrust.robot.ButtonConfiguration;
import com.bobrust.util.RustWindowUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * A dialog that allows you to select a region
 */
public class RegionSelectionDialog extends JDialog {
	private final JDialog parent;
	private final JResizeComponent resizeComponent;
	private final JCoordinateMarker coordinateMarker;
	private final JLabel topText;
	
	private final Timer selectMonitorTimer;
	private final boolean hideParent;
	
	public RegionSelectionDialog(JDialog parent, boolean hideParent) {
		super(parent, "", ModalityType.APPLICATION_MODAL);
		this.parent = parent;
		this.hideParent = hideParent;
		
		setUndecorated(true);
		setFocusable(true);
		setAlwaysOnTop(true);
		setBackground(new Color(0x10000000, true));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				if (hideParent) {
					parent.setVisible(true);
				}
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

			coordinateMarker = new JCoordinateMarker();
			test.add(coordinateMarker);
		}
		
		JPanel topTextPanel = new JPanel();
		topTextPanel.setBackground(Color.red);
		topTextPanel.setPreferredSize(new Dimension(0, 40));
		panel.add(topTextPanel, BorderLayout.NORTH);

		topText = new JLabel("Press Escape or Enter to close");
		topText.setHorizontalTextPosition(SwingConstants.CENTER);
		topText.setFont(topText.getFont().deriveFont(20.0f));
		topText.setForeground(Color.white);
		topTextPanel.add(topText);

		JButton closeButton = new JButton("(Close)");
		closeButton.setBackground(Color.red.darker());
		closeButton.setForeground(Color.white);
		closeButton.setFocusPainted(false);
		closeButton.setBorderPainted(false);
		closeButton.setPreferredSize(new Dimension(100, 30));
		closeButton.setFont(topText.getFont().deriveFont(20.0f));
		closeButton.addActionListener(e -> dispose());  // Action same as pressing Enter
		topTextPanel.add(closeButton);

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
			coordinateMarker.revalidateSelection();
			coordinateMarker.repaint();
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

	public Region openArrowMarker(GraphicsConfiguration monitor, boolean allowChangingMonitor, String topText, Point position) {
		this.topText.setText(topText);

		if (position == null) {
			GraphicsConfiguration config = monitor != null ? monitor : getGraphicsConfiguration(getLocation());
			int screenWidth = config.getBounds().width;
			int screenHeight = config.getBounds().height;

			int xCoordinate = (int) (screenWidth * 0.8); // 80% from the left
			int yCoordinate = screenHeight / 2;
			position = new Point(xCoordinate, yCoordinate);
		}

		coordinateMarker.setPosition(position);

		if (allowChangingMonitor) {
			selectMonitorTimer.start();
		}

		resizeComponent.setVisible(false);
		coordinateMarker.setVisible(true);

		try {
			// Make it possible to select another monitor
			GraphicsConfiguration config = monitor != null ? monitor : getGraphicsConfiguration(getLocation());
			setBounds(config.getBounds());
			getContentPane().revalidate();

			// This blocks until the monitor has been selected
			if (hideParent) {
				parent.setVisible(false);
			}
			setVisible(true);
			if (hideParent) {
				parent.setVisible(true);
			}
			dispose();

			boolean hasConfigChanged = false;
			if (allowChangingMonitor) {
				// Update result value
				var nextConfig = getGraphicsConfiguration(getLocation());
				hasConfigChanged = !Objects.equals(nextConfig, config);
				config = nextConfig;
			}

			ButtonConfiguration.Coordinate selectedCoordinate = coordinateMarker.getCoordinate();
			Rectangle selectedRectangle = new Rectangle(selectedCoordinate.x(), selectedCoordinate.y(), 1, 1);

			return new Region(config, selectedRectangle, hasConfigChanged);
		} finally {
			selectMonitorTimer.stop();
		}
	}

	public Region openDialog(GraphicsConfiguration monitor, boolean allowChangingMonitor, String topText, Image displayedImage, Rectangle rect) {
		this.topText.setText(topText);

		if (allowChangingMonitor) {
			selectMonitorTimer.start();
		}
		
		try {
			// Make it possible to select another monitor
			GraphicsConfiguration config = monitor != null ? monitor : getGraphicsConfiguration(getLocation());
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
			resizeComponent.setUnfocused();
			resizeComponent.setImage(displayedImage);
			resizeComponent.setSelectedRectangle(rect);
			resizeComponent.setVisible(true);
			coordinateMarker.setVisible(false);
			// This blocks until the monitor has been selected
			if (hideParent) {
				parent.setVisible(false);
			}
			setVisible(true);
			if (hideParent) {
				parent.setVisible(true);
			}
			dispose();
			
			boolean hasConfigChanged = false;
			if (allowChangingMonitor) {
				// Update result value
				var nextConfig = getGraphicsConfiguration(getLocation());
				hasConfigChanged = !Objects.equals(nextConfig, config);
				config = nextConfig;
			}
			
			var nextRect = resizeComponent.getSelectedRectangle();
			return new Region(config, nextRect, !nextRect.equals(rect) || hasConfigChanged);
		} finally {
			selectMonitorTimer.stop();
		}
	}
	
	public static record Region(GraphicsConfiguration monitor, Rectangle selection, boolean hasChanged) { }
}
