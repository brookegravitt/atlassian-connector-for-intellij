package com.atlassian.theplugin.idea.jira.table;

import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;

import javax.swing.*;
import java.awt.*;

public class JIRAConstantListRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
		JLabel comp = (JLabel) super.getListCellRendererComponent(jList, o, i, b, b1);
		JIRAConstant c = (JIRAConstant) o;
		comp.setText(c.getName());
		comp.setIcon(CachedIconLoader.getIcon(c.getIconUrl()));
		return comp;
	}
}
