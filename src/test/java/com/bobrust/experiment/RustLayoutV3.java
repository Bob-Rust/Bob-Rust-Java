package com.bobrust.experiment;

import javax.swing.*;
import java.awt.*;

public class RustLayoutV3 extends JPanel {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Rust Layout V3");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new RustLayoutV3());
		frame.setSize(1037 + 16, 441 + 39);
		frame.setVisible(true);
	}
	
	public static final Color background = new Color(0x2b2924);
	
	public static final Color topRedButton = new Color(0xb03726);
	public static final Color topGreenButton = new Color(0x718a43);
	
	public static final Color rightBar = new Color(0x1c221c);
	public static final Color rightBarBox = new Color(0x16160d);
	public static final Color rightBarBoxHeader = new Color(0x0d0d0d);
	
	public RustLayoutV3() {
		setOpaque(true);
		setBackground(background);
	}
	
	private double transform(int x, int source, int target, int height) {
		double multiply = Math.min(source * height / (1080.0 * target), 1);
		return (x / (double) source) * target * multiply;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int screen_width = getWidth();
		int screen_height = getHeight();
		
		// Ideal (375x1080) smallest possible side otherwise we have deforms
		g.setColor(rightBar);
		double rightWidth = transform(375, 1920, screen_width, screen_height);
		
		int rightStartX = (int) (screen_width - rightWidth);
		g.fillRect(rightStartX, 0, (int) rightWidth, screen_height);
		
		int padding = (int) (rightWidth * 0.0316711590296);
		int rightButtonWidth = (int) (rightWidth - padding * 4);
		
		final double rightButtonRatio = 104 / 648.0;
		int rightButtonHeight = (int) (rightButtonRatio * rightButtonWidth);
		
		g.setColor(topRedButton);
		g.fillRect(rightStartX + padding * 2, screen_height - padding * 2 - rightButtonHeight, rightButtonWidth, rightButtonHeight);
		g.setColor(topGreenButton);
		g.fillRect(rightStartX + padding * 2, screen_height - padding * 3 - rightButtonHeight * 2, rightButtonWidth, rightButtonHeight);
		
		final double rightToolsRatio = 122 / 351.0;
		final double rightBrushRatio = 249 / 351.0;
		final double rightColorMaxRatio = 389 / 188.0;
		final double rightBoxHeaderRatio = 51 / 351.0;
		
		int rightBoxWidth = (int) rightWidth - padding * 2;
		
		int toolsHeight = (int) (rightToolsRatio * rightBoxWidth);
		int brushHeight = (int) (rightBrushRatio * rightBoxWidth);
		int boxHeaderHeight = (int) (rightBoxHeaderRatio * rightBoxWidth);
		
		int availableHeight = screen_height - toolsHeight - brushHeight - rightButtonHeight * 2 - padding * 8;
		int colorHeight = Math.min(availableHeight, (int) (rightColorMaxRatio * rightBoxWidth));
		int boxHeights = padding * 2 + toolsHeight + brushHeight + colorHeight;
		int offsetY = (availableHeight - colorHeight) / 2;
		
		g.setColor(rightBarBox);
		g.fillRect(rightStartX + padding, padding + offsetY, rightBoxWidth, toolsHeight);
		g.fillRect(rightStartX + padding, padding * 2 + toolsHeight + offsetY, rightBoxWidth, brushHeight);
		g.fillRect(rightStartX + padding, padding * 3 + toolsHeight + brushHeight + offsetY, rightBoxWidth, colorHeight);
		
		g.setColor(rightBarBoxHeader);
		g.fillRect(rightStartX + padding, padding + offsetY, rightBoxWidth, boxHeaderHeight);
		g.fillRect(rightStartX + padding, padding * 2 + toolsHeight + offsetY, rightBoxWidth, boxHeaderHeight);
		g.fillRect(rightStartX + padding, padding * 3 + toolsHeight + brushHeight + offsetY, rightBoxWidth, boxHeaderHeight);
		
		g.setColor(Color.black);
		g.drawRect(rightStartX + padding, padding + offsetY, rightBoxWidth, boxHeights);
		
		g.setColor(Color.darkGray);
		g.drawRect(
			rightStartX + padding * 2,
			padding * 4 + toolsHeight + brushHeight + offsetY + boxHeaderHeight,
			rightBoxWidth - padding * 2,
			colorHeight - boxHeaderHeight - padding * 2
		);
		
		int topPadding = (int) transform(24, 1920, screen_width, screen_height);
		int topStep    = (int) transform(96, 1920, screen_width, screen_height);
		int topSize    = topStep - topPadding;
		
		for (int i = 0; i < 8; i++) {
			g.setColor(i == 0 ? topRedButton : topGreenButton);
			g.fillRect(topPadding + topStep * i, topPadding, topSize, topSize);
		}
		
		int previewSize = (int) transform(150, 1920, screen_width, screen_height);
		int previewStartX = rightStartX - padding * 2 - previewSize;
		int previewStartY = screen_height - padding * 2 - previewSize;
		g.setColor(Color.darkGray);
		g.fillRect(previewStartX, previewStartY, previewSize, previewSize);
	}
}
