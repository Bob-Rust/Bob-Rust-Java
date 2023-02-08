package com.bobrust.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RustConstants {
	private static final Logger LOGGER = LogManager.getLogger(RustConstants.class);
	
	// Used by JRandomPanel
	public static final boolean ENABLE_RANDOM_BACKGROUND = false;
	
	// Used by BorstGenerator
	public static final boolean DEBUG_GENERATOR = false;
	
	// Used for image scaling
	public static final int IMAGE_SCALING_NEAREST = 0;
	public static final int IMAGE_SCALING_BILINEAR = 1;
	public static final int IMAGE_SCALING_BICUBIC = 2;
	
	// TODO: Approximate these
	public static final Color CANVAS_AVERAGE = new Color(0x8f887c);
	public static final Color WOODEN_AVERAGE = new Color(0x6d5033);
	public static final Color TOWN_POST_AVERAGE = new Color(0x94624d);
	public static final Color HANGING_METAL_AVERAGE = new Color(0x534c46);
	
	// Used by JStyledButton, JStyledToggleButton
	public static final int BUTTON_BORDER_RADIUS = 20;
	public static final Color BUTTON_DEFAULT_COLOR = new Color(242, 242, 242);
	public static final Color BUTTON_HOVER_COLOR = new Color(229, 243, 255);
	public static final Color BUTTON_DISABLED_COLOR = new Color(192, 192, 192);
	public static final Color BUTTON_SELECTED_COLOR = new Color(204, 232, 255);
	
	public static final BufferedImage COLOR_PALETTE;
	public static final Image DIALOG_ICON;
	public static final String VERSION;
	
	static {
		BufferedImage bi = null;
		try(InputStream stream = RustConstants.class.getResourceAsStream("/mapping/color_palette.png")) {
			bi = ImageIO.read(stream);
		} catch(IOException e) {
			LOGGER.throwing(e);
			e.printStackTrace();
		}
		COLOR_PALETTE = bi;
		
		String version = "error";
		try(InputStream stream = RustConstants.class.getResourceAsStream("/version")) {
			version = new String(stream.readAllBytes());
		} catch(IOException e) {
			LOGGER.throwing(e);
			e.printStackTrace();
		}
		VERSION = version;
		
		Image dialogIcon = null;
		try {
			List<Image> icons = new ArrayList<>();
			for(int i = 0; i < 4; i++) {
				try(InputStream stream = RustConstants.class.getResourceAsStream("/icons/%s.png".formatted(16 << i))) {
					icons.add(ImageIO.read(stream));
				} catch(IOException e) {
					LOGGER.throwing(e);
					e.printStackTrace();
				}
			}
			
			dialogIcon = new BaseMultiResolutionImage(icons.toArray(Image[]::new));
		} catch(Exception e) {
			LOGGER.error("Failed to load program icons. {}", e);
		}
		
		DIALOG_ICON = dialogIcon;
	}
}
