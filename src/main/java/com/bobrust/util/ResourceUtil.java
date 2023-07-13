package com.bobrust.util;

import com.bobrust.util.data.RustConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtil {
	private static final Logger LOGGER = LogManager.getLogger(RustConstants.class);
	
	private ResourceUtil() {
		
	}
	
	private static InputStream getStream(String path) {
		if (path == null) {
			throw new NullPointerException("Cannot load the resource 'null'");
		}
		
		InputStream stream = ResourceUtil.class.getResourceAsStream(path);
		if (stream == null) {
			throw new NullPointerException("Cannot load the resource '" + path + "'");
		}
		
		return stream;
	}
	
	public static BufferedImage loadImageFromResources(String path) {
		return loadImageFromResources(path, LOGGER);
	}
	
	public static BufferedImage loadImageFromResources(String path, Logger logger) {
		try (InputStream stream = getStream(path)) {
			return ImageIO.read(stream);
		} catch (IOException e) {
			if (logger != null) {
				logger.throwing(e);
			}
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public static String readTextFromResources(String path, Logger logger) {
		try (InputStream stream = getStream(path)) {
			return new String(stream.readAllBytes());
		} catch (IOException e) {
			if (logger != null) {
				logger.throwing(e);
			}
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public static Image loadMultiIconImageFromResources(String path, List<String> names, Logger logger) {
		List<Image> icons = new ArrayList<>();
		for (String name : names) {
			try (InputStream stream = getStream(path + name)) {
				icons.add(ImageIO.read(stream));
			} catch (IOException e) {
				if (logger != null) {
					logger.throwing(e);
				}
				e.printStackTrace();
			}
		}
		
		return new BaseMultiResolutionImage(icons.toArray(Image[]::new));
	}
}
