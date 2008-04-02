package com.atlassian.theplugin.idea;

import com.intellij.openapi.util.IconLoader;
import javax.swing.*;

public final class Constants {
	private Constants() { }

	public static final int DIALOG_MARGIN = 12;
	public static final int BG_COLOR_R = 238;
	public static final int BG_COLOR_G = 238;
	public static final int BG_COLOR_B = 238;

	// TODO this is wrong - take a look at the description of https://studio.atlassian.com/browse/PL-163
	public static final String HELP_URL_BASE = "http://confluence.atlassian.com/display/IDEPLUGIN";

	public static final Icon HELP_ICON = IconLoader.getIcon("/actions/help.png");
}
