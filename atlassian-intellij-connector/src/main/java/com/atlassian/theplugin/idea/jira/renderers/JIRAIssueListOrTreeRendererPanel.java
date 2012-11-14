package com.atlassian.theplugin.idea.jira.renderers;

import com.atlassian.connector.commons.jira.JiraTimeFormatter;
import com.atlassian.theplugin.commons.configuration.JiraConfigurationBean;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.idea.util.Html2text;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: kalamon
 * Date: May 12, 2009
 * Time: 12:57:29 PM
 */
public class JIRAIssueListOrTreeRendererPanel extends JPanel {
	private JPanel padding;
	private SelectableLabel updated;
	private SelectableLabel prio;
	private SelectableLabel state;

	private SelectableLabel keyAndSummary;
	private JLabel iconLabel;
	private JiraIssueAdapter issue;

	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";

	private static final int MAX_TOOLTIP_WIDTH = 400;
	private static final int MAX_DESCRIPTION_LENGTH = 360;

	public JIRAIssueListOrTreeRendererPanel(JiraIssueAdapter issue, @Nullable JiraConfigurationBean configuration) {
		super(new FormLayout("1dlu, pref, 1dlu, fill:min(pref;150px):grow, right:pref", "pref"));
		this.issue = issue;

		CellConstraints cc = new CellConstraints();

		setBackground(UIUtil.getTreeTextBackground());

		iconLabel = new JLabel(CachedIconLoader.getIcon(issue.getTypeIconUrl()));
		add(iconLabel, cc.xy(2, 1));

		keyAndSummary = new SelectableLabel(true, true, issue.getKey() + ": " + issue.getSummary(),
				null, SwingConstants.TRAILING, AbstractTreeNode.ICON_HEIGHT);
		add(keyAndSummary, cc.xy(2 + 2, 1));

		add(createPanelForOtherIssueDetails(), cc.xy(2 + 2 + 1, 1));

		setParameters(true, true);

        if (configuration != null && configuration.isShowIssueTooltips()) {
            setToolTipText(buildTolltip(issue, 0));

            // now black magic here: 2-pass creation of multiline tooltip, with maximum width of MAX_TOOLTIP_WIDTH
            final JToolTip jToolTip = createToolTip();

            try {
                jToolTip.setTipText(buildTolltip(issue, 0));
            } catch (ClassCastException e) {
                // don't know why but sometimes setTipText throws CCE (most probably on IDEA start)
                // we cannot do much about thtat
                PluginUtil.getLogger().warn("ClassCastException when setting tooltip text", e);
            }

            final int prefWidth = jToolTip.getPreferredSize().width;
            int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
            setToolTipText(buildTolltip(issue, width));
        }
	}

	private JPanel createPanelForOtherIssueDetails() {
		JPanel rest = new JPanel();
		rest.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		state = new SelectableLabel(true, true, null, "    " + issue.getStatus(),
				CachedIconLoader.getIcon(issue.getStatusTypeUrl()),
				SwingConstants.LEADING, AbstractTreeNode.ICON_HEIGHT, false, true);
		state.setHorizontalAlignment(SwingConstants.RIGHT);
		state.setMinimumSize(state.getPreferredSize());
		rest.add(state, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		prio = new SelectableLabel(true, true, null, null,
				CachedIconLoader.getIcon(issue.getPriorityIconUrl()),
				SwingConstants.LEADING, AbstractTreeNode.ICON_HEIGHT, false, true);
		// setting minimum size is necessary as gridbag layout may
		// ignore preffered size if some other lables do not fit!!!
		prio.setMinimumSize(new Dimension(AbstractTreeNode.ICON_WIDTH, AbstractTreeNode.ICON_HEIGHT));
		prio.setPreferredSize(new Dimension(AbstractTreeNode.ICON_WIDTH, AbstractTreeNode.ICON_HEIGHT));
		rest.add(prio, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		updated = new SelectableLabel(true, true, null, JiraTimeFormatter.formatShortTimeFromJiraTimeString(issue.getUpdated(), issue.getLocale()),
				null, SwingConstants.LEADING, AbstractTreeNode.ICON_HEIGHT, false, true);
		updated.setHorizontalAlignment(SwingConstants.RIGHT);
		Dimension minDimension = updated.getPreferredSize();

		minDimension.setSize(
				Math.max(PluginUtil.getTimeWidth(updated), minDimension.getWidth()), minDimension.getHeight());

		updated.setPreferredSize(minDimension);
		updated.setMinimumSize(minDimension);
		updated.setMaximumSize(minDimension);
		updated.setSize(minDimension);

		rest.add(updated, gbc);

		padding = new JPanel();
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		padding.setPreferredSize(new Dimension(AbstractTreeNode.RIGHT_PADDING, AbstractTreeNode.ICON_HEIGHT));
		padding.setMinimumSize(new Dimension(AbstractTreeNode.RIGHT_PADDING, AbstractTreeNode.ICON_HEIGHT));
		padding.setMaximumSize(new Dimension(AbstractTreeNode.RIGHT_PADDING, AbstractTreeNode.ICON_HEIGHT));
		padding.setOpaque(true);
		rest.add(padding, gbc);
		return rest;
	}

	public void setParameters(boolean selected, boolean enabled) {

		Icon typeIcon = enabled ? CachedIconLoader.getIcon(issue.getTypeIconUrl())
				: CachedIconLoader.getDisabledIcon(issue.getTypeIconUrl());

		iconLabel.setIcon(typeIcon);
		keyAndSummary.setSelected(selected);
		keyAndSummary.setEnabled(enabled);

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

	private static String buildTolltip(JiraIssueAdapter issue, int width) {
		StringBuilder sb = new StringBuilder("<html>" + BODY_WITH_STYLE);

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
		// issue.getDescription() can return null
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
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
		DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

		try {
			final Date date = df.parse(issue.getCreated());
			sb.append(ds.format(date));
			sb.append(" (").append(DateUtil.getRelativePastDate(date)).append(")");
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Updated:</b></td><td valign=\"top\">");
		try {
			final Date date = df.parse(issue.getUpdated());
			sb.append(ds.format(date));
			sb.append(" (").append(DateUtil.getRelativePastDate(date)).append(")");
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}
}
