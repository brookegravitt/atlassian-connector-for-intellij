package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

public class JIRAIssueGroupTreeNode extends JIRAAbstractTreeNode {
	private final JIRAIssueListModel model;
	private final String name;
	private final Icon iconOpen;
	private final Icon iconClosed;
	private final Icon disabledIconOpen;
	private final Icon disabledIconClosed;


	public JIRAIssueGroupTreeNode(JIRAIssueListModel model, String name, Icon icon, Icon disabledIcon) {
		this.model = model;
		this.name = name;
		if (icon != null) {
			this.iconOpen = icon;
			this.iconClosed = icon;
		} else {
			this.iconOpen = Icons.DIRECTORY_OPEN_ICON;
			this.iconClosed = Icons.DIRECTORY_CLOSED_ICON;
		}
		if (disabledIcon != null) {
			this.disabledIconOpen = disabledIcon;
			this.disabledIconClosed = disabledIcon;
		} else {
			this.disabledIconOpen = this.iconOpen;
			this.disabledIconClosed = this.iconClosed;
		}
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		JPanel panel = new JPanel(new FormLayout("left:pref, left:pref, pref:grow", "pref:grow"));
		JPanel mainPanel = new JPanel(new FormLayout("pref, pref:grow", "pref"));

		CellConstraints cc = new CellConstraints();
		Color bgColor = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
		Color fgColor = selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground();

		fgColor = c.isEnabled() ? fgColor : UIUtil.getInactiveTextColor();

		panel.setBackground(bgColor);
		SimpleColoredComponent groupComponet = new SimpleColoredComponent();
		if (c.isEnabled()) {
			mainPanel.add(new JLabel(expanded ? iconOpen : iconClosed), cc.xy(1, 1));
		} else {
			mainPanel.add(new JLabel(expanded ? disabledIconOpen : disabledIconClosed), cc.xy(1, 1));
		}
		
		groupComponet.append(name, new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, fgColor));
		panel.add(groupComponet, cc.xy(1, 1));

		groupComponet.append(" (" + getChildCount() + ")", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor));
		panel.add(groupComponet, cc.xy(2, 1));


		mainPanel.add(panel, cc.xy(2, 1));
		return mainPanel;
	}

	public void onSelect() {
		model.setSeletedIssue(null);
	}

	public String toString() {
		return name;
	}
}
