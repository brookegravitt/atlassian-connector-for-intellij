package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.jira.Html2text;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class JIRAIssueTreeNode extends AbstractTreeNode {
	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";
	private static final int MAX_LINE_LENGTH = 50;

	private final JIRAIssueListModel model;
	private final JIRAIssue issue;

	public JIRAIssueTreeNode(JIRAIssueListModel model, JIRAIssue issue) {
		super(issue.getKey() + ": " + issue.getSummary(), null, null);
		this.model = model;
		this.issue = issue;
	}

	@Override
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
		final String iconTypeUrl = issue.getStatusTypeUrl();
		Icon statusIcon = enabled ? CachedIconLoader.getIcon(iconTypeUrl) : CachedIconLoader.getDisabledIcon(iconTypeUrl);
		JLabel state = new SelectableLabel(selected, enabled, issue.getStatus(), statusIcon,
				SwingConstants.LEADING, ICON_HEIGHT);
		p.add(state, gbc);

		final String iconUrl = issue.getPriorityIconUrl();

		Icon prioIcon = enabled ? CachedIconLoader.getIcon(iconUrl) : CachedIconLoader.getDisabledIcon(iconUrl);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		JLabel prio = new SelectableLabel(selected, enabled, null, prioIcon, SwingConstants.LEADING, ICON_HEIGHT);
		// setting minimum size is necessary as gridbag layout may ignore preffered size if some other lables do not fit!!!
		prio.setMinimumSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
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

		minDimension.setSize(
				Math.max(PluginUtil.getDateWidth(updated, dfo), minDimension.getWidth()), minDimension.getHeight());
		
		updated.setPreferredSize(minDimension);
		updated.setMinimumSize(minDimension);
		updated.setMaximumSize(minDimension);
		updated.setSize(minDimension);
		p.add(updated, gbc);
		JPanel padding = new JPanel();
        gbc.gridx++;
        gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
        padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
        padding.setMinimumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
        padding.setMaximumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
		padding.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
		padding.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
        padding.setOpaque(true);
        p.add(padding, gbc);

		p.setToolTipText(buildTolltip());
		return p;
	}

	private String buildTolltip() {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);

		sb.append("<table width=\"100%\">");
		sb.append("<tr><td colspan=5><b><font color=blue>");
        sb.append(issue.getKey());
        sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");
		String summary = issue.getSummary();
		if (summary.length() > MAX_LINE_LENGTH) {
			summary = summary.substring(0, MAX_LINE_LENGTH) + "...";
		}
		sb.append(summary);
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Description:</b></td><td valign=\"top\">");
		String description = Html2text.translate(issue.getDescription());
		if (description.length() > MAX_LINE_LENGTH) {
			description = description.substring(0, MAX_LINE_LENGTH) + "...";
		}
		sb.append(description);
		sb.append("");
		sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Status:</b></td><td valign=\"top\">");
        sb.append(issue.getStatus());
        sb.append("");
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Reporter:</b></td><td valign=\"top\">");
		sb.append(issue.getReporter());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Assignee:</b></td><td valign=\"top\">");
		sb.append(issue.getAssignee());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Resolution:</b></td><td valign=\"top\">");
		sb.append(issue.getResolution());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Created:</b></td><td valign=\"top\">");
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		try {
			sb.append(ds.format(df.parse(issue.getCreated())));
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Updated:</b></td><td valign=\"top\">");
		try {
			sb.append(ds.format(df.parse(issue.getUpdated())));
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	@Override
	public void onSelect() {
		model.setSeletedIssue(issue);	
	}

	@Override
	public String toString() {
		return name;
	}
}
