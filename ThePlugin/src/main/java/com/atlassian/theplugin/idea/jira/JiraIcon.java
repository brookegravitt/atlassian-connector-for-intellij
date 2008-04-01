package com.atlassian.theplugin.idea.jira;

import javax.swing.*;

public class JiraIcon {
	private String text;
	private String iconUrl;

	public JiraIcon(String text, String iconUrl) {
		this.text = text;
		this.iconUrl = iconUrl;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Icon getIcon() {
		return CachedIconLoader.getIcon(iconUrl);
	}
}
