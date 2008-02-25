package com.atlassian.theplugin.idea.config;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-22
 * Time: 13:53:32
 * To change this template use File | Settings | File Templates.
 */
public class GeneralConfigPanel extends AbstractContentPanel {
	private static GeneralConfigPanel instance = null;

	public boolean isEnabled() {
		return false;
	}

	public boolean isModified() {
		return false;
	}

	public String getTitle() {
		return "General";
	}

	public void getData() {

	}

	public void setData() {

	}

	public static GeneralConfigPanel getInstance() {
		if (instance == null) {
			instance = new GeneralConfigPanel();
		}

		return instance;
	}
}
