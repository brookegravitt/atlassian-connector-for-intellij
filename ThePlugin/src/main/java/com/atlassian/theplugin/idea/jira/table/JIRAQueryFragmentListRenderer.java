package com.atlassian.theplugin.idea.jira.table;

import com.atlassian.theplugin.jira.api.JIRAQueryFragment;

import javax.swing.*;
import java.awt.*;

public class JIRAQueryFragmentListRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
		JLabel comp = (JLabel) super.getListCellRendererComponent(jList, o, i, b, b1);
		JIRAQueryFragment c = (JIRAQueryFragment) o;
		comp.setText(c.getName());
		return comp;
	}
}