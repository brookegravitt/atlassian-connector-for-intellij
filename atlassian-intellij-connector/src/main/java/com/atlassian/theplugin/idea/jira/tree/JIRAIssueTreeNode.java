package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class JIRAIssueTreeNode extends AbstractTreeNode {
	private final JIRAIssueListModel model;
	private final JIRAIssue issue;

	public JIRAIssueTreeNode(JIRAIssueListModel model, JIRAIssue issue) {
		super(issue.getKey() + ": " + issue.getSummary(), null, null);
		this.model = model;
		this.issue = issue;
	}

	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		boolean enabled = c.isEnabled();

		JPanel p = new JPanel();
		p.setBackground(UIUtil.getTreeTextBackground());
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, GAP);
		gbc.fill = GridBagConstraints.NONE;
		Icon typeIcon = enabled ? CachedIconLoader.getIcon(issue.getTypeIconUrl())
				: CachedIconLoader.getDisabledIcon(issue.getTypeIconUrl());
		JLabel typeLabel = new JLabel(typeIcon, SwingConstants.LEADING);
		typeLabel.setOpaque(true);
		typeLabel.setBackground(UIUtil.getTreeTextBackground());
		p.add(typeLabel, gbc);

		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridx++;
		JLabel key = new SelectableLabel(selected, enabled, issue.getKey() + ": ", ICON_HEIGHT);
		p.add(key, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel summary = new SelectableLabel(selected, enabled, issue.getSummary(), ICON_HEIGHT);
		p.add(summary, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		Icon statusIcon = enabled ? CachedIconLoader.getIcon(issue.getStatusTypeUrl())
				: CachedIconLoader.getDisabledIcon(issue.getStatusTypeUrl());
		JLabel state = new SelectableLabel(selected, enabled, issue.getStatus(), statusIcon,
				SwingConstants.LEADING, ICON_HEIGHT);
		p.add(state, gbc);

        Icon prioIcon = enabled ? CachedIconLoader.getIcon(issue.getPriorityIconUrl())
				: CachedIconLoader.getDisabledIcon(issue.getPriorityIconUrl());

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		JLabel prio = new SelectableLabel(selected, enabled, "", prioIcon, SwingConstants.LEADING, ICON_HEIGHT);
		prio.setPreferredSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
		p.add(prio, gbc);


		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
		DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);		
		String t;
		try {
			t = dfo.format(df.parse(issue.getUpdated()));
		} catch (java.text.ParseException e) {
			t = "Invalid";
		}
		gbc.gridx++;
        gbc.weightx = 0.0;		
		JLabel updated = new SelectableLabel(selected, enabled, t, null, SwingConstants.LEADING, ICON_HEIGHT);
		updated.setHorizontalAlignment(SwingConstants.RIGHT);
		Dimension minDimension = updated.getPreferredSize();
		minDimension.setSize(Math.max(FIXED_DATE_COLUMN_WIDTH, minDimension.getWidth()), minDimension.getHeight());
		
		updated.setPreferredSize(minDimension);
		updated.setMinimumSize(minDimension);
		updated.setMaximumSize(minDimension);
		
		p.add(updated, gbc);		
		JPanel padding = new JPanel();
        gbc.gridx++;
        gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
        padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
        padding.setMinimumSize(new Dimension(RIGHT_PADDING, 1));
        padding.setMaximumSize(new Dimension(RIGHT_PADDING, 1));
		padding.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		padding.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
        padding.setOpaque(true);
        p.add(padding, gbc);

        return p;
	}

	public void onSelect() {
		model.setSeletedIssue(issue);	
	}

	public String toString() {
		return name;
	}
}
