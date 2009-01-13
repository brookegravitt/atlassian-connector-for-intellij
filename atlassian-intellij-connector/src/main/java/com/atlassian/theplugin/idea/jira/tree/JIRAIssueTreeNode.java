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

	private static final int MAX_TOOLTIP_WIDTH = 400;
	private static final int MAX_DESCRIPTION_LENGTH = 360;
	
	private final JIRAIssueListModel model;
	private final JIRAIssue issue;

	public JIRAIssueTreeNode(JIRAIssueListModel model, JIRAIssue issue) {
		super(issue.getKey() + ": " + issue.getSummary(), null, null);
		this.model = model;
		this.issue = issue;
		renderer = new RendererPanel();
	}

	private final class RendererPanel extends JPanel {
		private JPanel padding;
		private SelectableLabel updated;
		private SelectableLabel prio;
		private SelectableLabel state;
		private SelectableLabel summary;

		private SelectableLabel key;
		private JLabel typeLabel;

		private RendererPanel() {
			super(new GridBagLayout());

			setBackground(UIUtil.getTreeTextBackground());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0.0;
			gbc.insets = new Insets(0, 0, 0, GAP);
			gbc.fill = GridBagConstraints.NONE;
			typeLabel = new JLabel(null, CachedIconLoader.getIcon(issue.getTypeIconUrl()), SwingConstants.LEADING);
			typeLabel.setOpaque(true);
			typeLabel.setBackground(UIUtil.getTreeTextBackground());
			add(typeLabel, gbc);

			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.gridx++;
			key = new SelectableLabel(true, true, issue.getKey() + ": ", ICON_HEIGHT, false, true);
			add(key, gbc);

			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			summary = new SelectableLabel(true, true, issue.getSummary(), ICON_HEIGHT, false, true);
			add(summary, gbc);

			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			state = new SelectableLabel(true, true, issue.getStatus(),
					CachedIconLoader.getIcon(issue.getStatusTypeUrl()),
					SwingConstants.LEADING, ICON_HEIGHT, false, true);
			add(state, gbc);

			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			prio = new SelectableLabel(true, true, null,
					CachedIconLoader.getIcon(issue.getPriorityIconUrl()),
					SwingConstants.LEADING, ICON_HEIGHT, false, true);
			// setting minimum size is necessary as gridbag layout may
			// ignore preffered size if some other lables do not fit!!!
			prio.setMinimumSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
			prio.setPreferredSize(new Dimension(ICON_WIDTH, ICON_HEIGHT));
			add(prio, gbc);

			gbc.gridx++;
			gbc.weightx = 0.0;
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
			DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String t;
			try {
				t = dfo.format(df.parse(issue.getUpdated()));
			} catch (java.text.ParseException e) {
				t = "Invalid";
			}
			updated = new SelectableLabel(true, true, t, null, SwingConstants.LEADING, ICON_HEIGHT, false, true);
			updated.setHorizontalAlignment(SwingConstants.RIGHT);
			Dimension minDimension = updated.getPreferredSize();

			minDimension.setSize(
					Math.max(PluginUtil.getDateWidth(updated, dfo), minDimension.getWidth()), minDimension.getHeight());

			updated.setPreferredSize(minDimension);
			updated.setMinimumSize(minDimension);
			updated.setMaximumSize(minDimension);
			updated.setSize(minDimension);

			add(updated, gbc);

			padding = new JPanel();
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setMinimumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setMaximumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setOpaque(true);
			add(padding, gbc);

			setParameters(true, true);
			
			setToolTipText(buildTolltip(issue, 0));

			// now black magic here: 2-pass creation of multiline tooltip, with maximum width of MAX_TOOLTIP_WIDTH
			final JToolTip jToolTip = createToolTip();
			jToolTip.setTipText(buildTolltip(issue, 0));
			final int prefWidth = jToolTip.getPreferredSize().width;
			int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
			setToolTipText(buildTolltip(issue, width));
		}

		public void setParameters(boolean selected, boolean enabled) {

			Icon typeIcon = enabled ? CachedIconLoader.getIcon(issue.getTypeIconUrl())
					: CachedIconLoader.getDisabledIcon(issue.getTypeIconUrl());

			typeLabel.setIcon(typeIcon);
			
			key.setSelected(selected);
			key.setEnabled(enabled);

			summary.setSelected(selected);
			summary.setEnabled(enabled);

			final String iconTypeUrl = issue.getStatusTypeUrl();
			Icon statusIcon = enabled
					? CachedIconLoader.getIcon(iconTypeUrl) : CachedIconLoader.getDisabledIcon(iconTypeUrl);
			state.setIcon(statusIcon);
			state.setSelected(selected);
			state.setEnabled(enabled);

			final String iconUrl = issue.getPriorityIconUrl();
			Icon prioIcon = enabled ? CachedIconLoader.getIcon(iconUrl) : CachedIconLoader.getDisabledIcon(iconUrl);
			prio.setIcon(prioIcon);
			prio.setSelected(selected);
			prio.setEnabled(enabled);

			updated.setSelected(selected);
			updated.setEnabled(enabled);

			padding.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
			padding.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
		}
	}

	private RendererPanel renderer;

	@Override
	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		renderer.setParameters(selected, c.isEnabled());
		return renderer;
	}

	private static String buildTolltip(JIRAIssue issue, int width) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);

		final String widthString = width > 0 ? "width='" + width + "px'" : "";

		sb.append("<table ").append(widthString).append(">");
		sb.append("<tr><td colspan=5><b><font color=blue>");
        sb.append(issue.getKey());
        sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");
		String summary = issue.getSummary();
//		if (summary.length() > MAX_LINE_LENGTH) {
//			summary = summary.substring(0, MAX_LINE_LENGTH) + "...";
//		}
		sb.append(summary);
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Description:</b></td><td valign=\"top\">");
		String description = Html2text.translate(issue.getDescription());
		if (description.length() > MAX_DESCRIPTION_LENGTH) {
			description = description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
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
