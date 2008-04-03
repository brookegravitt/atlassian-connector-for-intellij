package com.atlassian.theplugin.idea;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class Constants {
	private Constants() { }

	public static final int DIALOG_MARGIN = 12;
	public static final int BG_COLOR_R = 238;
	public static final int BG_COLOR_G = 238;
	public static final int BG_COLOR_B = 238;

	public static final String HELP_URL_BASE     = "theplugin.help.url.prefix";
	public static final String HELP_CONFIG_PANEL = "theplugin.config";
	public static final String HELP_BAMBOO       = "theplugin.bamboo";
	public static final String HELP_CRUCIBLE     = "theplugin.crucible";
	public static final String HELP_JIRA         = "theplugin.jira";

	public static final Icon HELP_ICON = IconLoader.getIcon("/actions/help.png");
}
