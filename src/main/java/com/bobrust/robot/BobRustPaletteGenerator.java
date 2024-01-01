package com.bobrust.robot;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.bobrust.robot.ButtonConfiguration.*;

public class BobRustPaletteGenerator {
	private BobRustPaletteGenerator() {
		
	}
	
	public static ButtonConfiguration createAutomaticConfiguration(int version, BufferedImage screenshot) {
		// Only supported version
		return BobRustPaletteGenerator.createAutomaticV3(screenshot);
	}
	
	private static double transformV3(int x, int source, int target, int height) {
		double multiply = Math.min(source * height / (1080.0 * target), 1);
		return (x / (double) source) * target * multiply;
	}
	
	private static Coordinate v3_remapRelative(int relativeX, int relativeY, double width, double height, Rectangle target) {
		double deltaX = relativeX / width;
		double deltaY = relativeY / height;
		
		return Coordinate.from(
			deltaX * target.getWidth() + target.getX(),
			deltaY * target.getHeight() + target.getY()
		);
	}
	
	public static ButtonConfiguration createAutomaticV3(BufferedImage screenshot) {
		int screen_width = screenshot.getWidth();
		int screen_height = screenshot.getHeight();
		
		ButtonConfiguration config = new ButtonConfiguration();
		
		// Top bar buttons
		int topPadding = (int) transformV3(24, 1920, screen_width, screen_height);
		int topStep    = (int) transformV3(96, 1920, screen_width, screen_height);
		int topSize    = topStep - topPadding;
		
		config.clearCanvas     = Coordinate.from(topPadding + topStep * 0 + topSize / 2.0, topPadding + topSize / 2.0);
		config.saveToDesktop   = Coordinate.from(topPadding + topStep * 1 + topSize / 2.0, topPadding + topSize / 2.0);
		config.saveImage       = Coordinate.from(topPadding + topStep * 2 + topSize / 2.0, topPadding + topSize / 2.0);
		config.clearRotation   = Coordinate.from(topPadding + topStep * 5 + topSize / 2.0, topPadding + topSize / 2.0);
		
		double rightWidth = transformV3(375, 1920, screen_width, screen_height);
		int rightStartX = (int) (screen_width - rightWidth);
		
		int padding = (int) (rightWidth * 0.0316711590296);
		int rightButtonWidth = (int) (rightWidth - padding * 4);
		
		final double rightButtonRatio = 104 / 648.0;
		int rightButtonHeight = (int) (rightButtonRatio * rightButtonWidth);
		
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
		int offsetY = (availableHeight - colorHeight) / 2;
		
		// Right Tool buttons
		Rectangle toolBox = new Rectangle(
			rightStartX + padding,
			padding + offsetY,
			rightBoxWidth,
			toolsHeight
		);
		Rectangle brushBox = new Rectangle(
			rightStartX + padding,
			padding * 2 + toolsHeight + offsetY,
			rightBoxWidth,
			brushHeight
		);
		Rectangle colorBox = new Rectangle(
			rightStartX + padding * 2,
			padding * 4 + toolsHeight + brushHeight + offsetY + boxHeaderHeight,
			rightBoxWidth - padding * 2,
			colorHeight - boxHeaderHeight - padding * 2
		);
		
		config.tool_paintBrush = v3_remapRelative(128, 69, 351, 122, toolBox);
		config.brush_circle    = v3_remapRelative(132, 69, 351, 249, brushBox);
		config.brush_square    = v3_remapRelative(180, 69, 351, 249, brushBox);
		config.size_1          = v3_remapRelative(130, 138, 351, 249, brushBox);
		config.size_32         = v3_remapRelative(277, 138, 351, 249, brushBox);
		config.opacity_0       = v3_remapRelative(130, 223, 351, 249, brushBox);
		config.opacity_1       = v3_remapRelative(277, 223, 351, 249, brushBox);
		config.color_topLeft   = Coordinate.from(colorBox.x + colorBox.width / 8.0, colorBox.y + colorBox.height / 32.0);
		config.color_botRight  = Coordinate.from(colorBox.x + colorBox.width - colorBox.width / 8.0, colorBox.y + colorBox.height - colorBox.height / 32.0);
		
		// Preview box
		int previewSize = (int) transformV3(150, 1920, screen_width, screen_height);
		int previewStartX = rightStartX - padding * 2 - previewSize;
		int previewStartY = screen_height - padding * 2 - previewSize;
		config.colorPreview = Coordinate.from(previewStartX + previewSize / 2.0, previewStartY + previewSize / 2.0);
		config.focus        = config.colorPreview;
		
		return config;
	}
	
	public static ButtonConfiguration old_createAutomaticV3(BufferedImage screenshot) {
		int screen_width = screenshot.getWidth();
		int screen_height = screenshot.getHeight();
		
		// Rust keep the height aspect ratio this means we can figure out almost exact point's on the screen
		
		// Delete button starts ( 24, 24), size (72, 72) on a (1080 height)
		// Next   button starts (120, 24), size (72, 72) offset (96, 0)
		// Next   button starts (216, 24), size (72, 72) offset (96, 0)
		
		ButtonConfiguration config = new ButtonConfiguration();
		
		final double btnOffset = 96;
		config.clearCanvas     = Coordinate.fromSide(24 + 36 + btnOffset * 0, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.saveToDesktop   = Coordinate.fromSide(24 + 36 + btnOffset * 1, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.saveImage       = Coordinate.fromSide(24 + 36 + btnOffset * 2, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		config.clearRotation   = Coordinate.fromSide(24 + 36 + btnOffset * 5, 24 + 36, Coordinate.SIDE_TOP_LEFT, 1920, 1080, screen_width, screen_height);
		
		// The right toolbar is size (374, 1080) on (1080 height)
		config.tool_paintBrush = Coordinate.fromSide(374 - 140, 84 , Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.brush_circle    = Coordinate.fromSide(374 - 144, 214, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.brush_square    = Coordinate.fromSide(374 - 192, 214, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.size_1          = Coordinate.fromSide(374 - 140, 283, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.size_32         = Coordinate.fromSide(374 - 289, 283, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.opacity_0       = Coordinate.fromSide(374 - 140, 368, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.opacity_1       = Coordinate.fromSide(374 - 289, 368, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.color_topLeft   = Coordinate.fromSide(374 - 62 , 481, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		config.color_botRight  = Coordinate.fromSide(374 - 308, 889, Coordinate.SIDE_TOP_RIGHT, 1920, 1080, screen_width, screen_height);
		
		config.focus           = Coordinate.fromSide(474, 102, Coordinate.SIDE_BOT_RIGHT, 1920, 1080, screen_width, screen_height);
		config.colorPreview    = config.focus;
		
		// Right side  188x599 . 3.18617
		// Right side  234x749 . 3.20000
		// Right side  351x904 . 2.575
		return config;
	}
	
}
