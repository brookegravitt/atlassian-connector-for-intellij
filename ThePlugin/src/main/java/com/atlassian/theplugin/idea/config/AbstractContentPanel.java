package com.atlassian.theplugin.idea.config;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-06
 * Time: 09:00:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContentPanel extends JPanel {
	public abstract boolean isEnabled();
	public abstract boolean isModified();
	public abstract String getTitle();
	public abstract void getData();
	public abstract void setData();
}
