package com.bobrust.util.data;

import com.bobrust.settings.Settings;
import com.bobrust.util.Sign;

import static com.bobrust.util.data.RustConstants.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RustSigns {
	public static final Map<String, Sign> SIGNS;
	public static final Sign FIRST;
	
	/*
sign.pictureframe.landscape       = [256,128]
sign.pictureframe.portrait        = [128,256]
sign.pictureframe.tall            = [128,512]
sign.pictureframe.xl              = [512,512]
sign.pictureframe.xxl             = [1024,512]

sign.huge.wood                    = [1024,256]
sign.large.wood                   = [512,256]
sign.medium.wood                  = [512,256]
sign.small.wood                   = [256,128]

sign.pole.banner.large            = [256,1024]
sign.post.double                  = [512,512]
sign.post.single                  = [256,128]
sign.post.town                    = [512,256]
sign.post.town.roof               = [512,256]

sign.hanging.banner.large         = [256,1024]
sign.hanging.ornate               = [512,256]
sign.hanging                      = [256,512]

// Neoon signs
sign.neon.125x125                 = [128,128]
sign.neon.125x215.animated        = [256,128]
sign.neon.125x215                 = [256,128]
sign.neon.xl.animated             = [256,256]
sign.neon.xl                      = [256,256]

// Not used
spinner.wheel.deployed            = [512,512] // This does not work in practice. Was 256x256 during test
big_wheel                         = [0,0]
carvable.pumpkin                  = [256,256]
photoframe.landscape              = [320,240]
photoframe.large                  = [320,240]
photoframe.portrait               = [320,384]
	 */
	static {
		SIGNS = new LinkedHashMap<>();
		
		List<Sign> signs = List.of(
			// Custom frame
			new Sign("bobrust.custom", -1, -1, CANVAS_AVERAGE) {
				@Override
				public int getWidth() {
					return Settings.SettingsSignDimension.get().width;
				}
				
				@Override
				public int getHeight() {
					return Settings.SettingsSignDimension.get().height;
				}
			}, // Custom picture frame
			
			// Picture Frames
			new Sign("sign.pictureframe.landscape", 256, 128, CANVAS_AVERAGE), // Landscape Picture Frame
			new Sign("sign.pictureframe.portrait", 128, 256, CANVAS_AVERAGE),  // Portrait Picture Frame
			new Sign("sign.pictureframe.tall", 128, 512, CANVAS_AVERAGE),      // Tall Picture Frame
			new Sign("sign.pictureframe.xl", 512, 512, CANVAS_AVERAGE),        // XL Picture Frame
			new Sign("sign.pictureframe.xxl", 1024, 512, CANVAS_AVERAGE),      // XXL Picture Frame
			
			// Wooden Signs
			new Sign("sign.wooden.small", 256, 128, WOODEN_AVERAGE),   // Small Wooden Sign
			new Sign("sign.wooden.medium", 512, 256, WOODEN_AVERAGE), // Wooden Sign
			new Sign("sign.wooden.large", 512, 256, WOODEN_AVERAGE),  // Large Wooden Sign
			new Sign("sign.wooden.huge", 1024, 256, WOODEN_AVERAGE),   // Huge Wooden Sign

			// Banners
			new Sign("sign.hanging.banner.large", 256, 1024, CANVAS_AVERAGE), // Large Banner Hanging
			new Sign("sign.pole.banner.large", 256, 1024, CANVAS_AVERAGE),    // Large Banner on Pole

			// Hanging Signs
			new Sign("sign.hanging", 256, 512, HANGING_METAL_AVERAGE),        // Two Sided Hanging Sign
			new Sign("sign.hanging.ornate", 512, 256, HANGING_METAL_AVERAGE), // Two Sided Ornate Hanging Sign
			
			// Town Signs
			new Sign("sign.post.single", 256, 128, CANVAS_AVERAGE),       // Single Sign Post
			new Sign("sign.post.double", 512, 512, CANVAS_AVERAGE),       // Double Sign Post
			new Sign("sign.post.town", 512, 256, TOWN_POST_AVERAGE),      // One Sided Town Sign Post
			new Sign("sign.post.town.roof", 512, 256, TOWN_POST_AVERAGE)  // Two Sided Town Sign Post

			// Spinner
			// new Sign("spinner.wheel.deployed", 512, 512) // Spinning Wheel
		);
		
		FIRST = signs.get(1);
		signs.forEach(i -> SIGNS.put(i.getName(), i));
	}
}
