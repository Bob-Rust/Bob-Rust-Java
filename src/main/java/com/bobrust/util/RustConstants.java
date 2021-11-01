package com.bobrust.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.bobrust.gui.BobRustOverlay;

public class RustConstants {
	// TODO: Approximate these.
	public static final Color CANVAS_AVERAGE = new Color(0x8f887c);
	public static final Color WOODEN_AVERAGE = new Color(0x6d5033);
	public static final Color TOWN_POST_AVERAGE = new Color(0x94624d);
	public static final Color HANGING_METAL_AVERAGE = new Color(0x534c46);
	
	public static final BufferedImage COLOR_PALETTE;
	
	static {
		BufferedImage bi = null;
		try(InputStream stream = BobRustOverlay.class.getResourceAsStream("/mapping/color_palette.png")) {
			bi = ImageIO.read(stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		COLOR_PALETTE = bi;
	}
}
