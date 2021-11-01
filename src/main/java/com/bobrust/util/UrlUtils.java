package com.bobrust.util;

import java.awt.Desktop;
import java.net.URI;

public class UrlUtils {
	private static final String ISSUE_URL = "https://github.com/Bob-Rust/Bob-Rust-Java/issues/new";
	private static final String DONATION_URL = "https://ko-fi.com/hard_coded";
	
	private static boolean openUrl(String url) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(url));
				return true;
			} catch(Exception e) {
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
}
