package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class JIRATreeNode extends JIRAAbstractTreeNode {
	private final JIRAIssue issue;

	public JIRATreeNode(JIRAIssue issue) {
		this.issue = issue;
	}

	private class SelectableLabel extends JLabel {
		private SelectableLabel(boolean selected, String text) {
			this(selected, text, null, SwingConstants.LEADING);
		}

		private SelectableLabel(boolean selected, String text, Icon icon, int alignment) {
			super(text, icon, SwingConstants.LEADING);
			setHorizontalTextPosition(alignment);
			setOpaque(true);
			setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
			setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
		}
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		JPanel p = new JPanel();
		p.setBackground(UIUtil.getTreeTextBackground());
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, 6);
		gbc.fill = GridBagConstraints.NONE;
		JLabel icon = new JLabel(CachedIconLoader.getIcon(issue.getTypeIconUrl()), SwingConstants.LEADING);
		icon.setOpaque(true);
		icon.setBackground(UIUtil.getTreeTextBackground());
		p.add(icon, gbc);

		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridx++;
		JLabel key = new SelectableLabel(selected, issue.getKey() + ": ");
		p.add(key, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel summary = new SelectableLabel(selected, issue.getSummary());
		p.add(summary, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel state = new SelectableLabel(selected, issue.getStatus(),
				CachedIconLoader.getIcon(issue.getStatusTypeUrl()), SwingConstants.LEADING);
		p.add(state, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, 6);
		JLabel prio = new SelectableLabel(selected, "",
				CachedIconLoader.getIcon(issue.getPriorityIconUrl()), SwingConstants.LEADING);
		p.add(prio, gbc);

		return p;
	}

	public void onSelect() {
		
	}

	public String toString() {
		return issue.getKey() + " " + issue.getSummary();
	}
}
