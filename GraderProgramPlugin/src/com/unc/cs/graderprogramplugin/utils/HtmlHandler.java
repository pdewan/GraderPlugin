package com.unc.cs.graderprogramplugin.utils;

public class HtmlHandler {
	public static String getRefreshTarget(String[] lines) {
		for(String line : lines) {
			line = line.trim();
			if (line.startsWith("<meta http-equiv")) {
				line = line.replaceAll("&amp;", "&");
				String[] parts = line.split("\\s*\"\\s*");
				for(String part : parts) {
					if (part.contains("url=")) {
						for(String seg : part.split("\\s*;\\s*")) {
							if(seg.contains("url=")) {
								return seg.split("=", 2)[1];
							}
						}
					}
				}
			}
		}
		return "";
	}
}
