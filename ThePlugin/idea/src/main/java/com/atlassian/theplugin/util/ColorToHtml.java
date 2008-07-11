package com.atlassian.theplugin.util;

import java.awt.*;

public final class ColorToHtml {
	private ColorToHtml() {
	}

	public static String getHtmlFromColor(Color c) {
		return String.format("#%1$2X%2$2X%3$2X", c.getRed(), c.getGreen(), c.getBlue());
	}
}
