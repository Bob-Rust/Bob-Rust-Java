package com.bobrust.util;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UrlUtils {
	private static final Logger LOGGER = LogManager.getLogger(UrlUtils.class);
	private static final String ISSUE_URL = "https://github.com/Bob-Rust/Bob-Rust-Java/issues/new";
	private static final String DONATION_URL = "https://ko-fi.com/hard_coded";
	
	private static boolean openUrl(String url) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(url));
				return true;
			} catch(Exception e) {
				LOGGER.error("Error opening url '{}', {}", url, e);
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	private static boolean openFileDirectory(File directory) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
			try {
				desktop.open(directory);
				return true;
			} catch(Exception e) {
				LOGGER.error("Error opening directory '{}', {}", directory, e);
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static boolean openIssueUrl() {
		return openUrl(ISSUE_URL);
	}
	
	public static boolean openDonationUrl() {
		return openUrl(DONATION_URL);
	}
	
	public static boolean openDirectory(File path) {
		return openFileDirectory(path);
	}
	
	public static File getJarPath() {
		try {
			return new File(UrlUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch(URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}
