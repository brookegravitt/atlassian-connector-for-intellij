/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.bamboo.tree;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.util.Util;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringEscapeUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeNode extends AbstractBuildTreeNode {

	private static final int MAX_TOOLTIP_WIDTH = 500;
	private static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";

	private BambooBuildAdapterIdea build;
	private static final int DATE_LABEL_WIDTH = 85;
	public static final String CODE_HAS_CHANGED = "Code has changed";

	public BuildTreeNode(final BambooBuildAdapterIdea build) {
		super(build.getBuildKey(), null, null);

		this.build = build;
	}

	public BambooBuildAdapterIdea getBuild() {
		return build;
	}

	@Override
	public String toString() {
		return build.getBuildKey();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected, final boolean expanded, final boolean hasFocus) {
		boolean enabled = c.isEnabled();

		JPanel p = new JPanel();
		p.setBackground(UIUtil.getTreeTextBackground());
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;


		// Icon
		gbc.insets = new Insets(0, 0, 0, 0);
		JLabel icon = new SelectableLabel(selected, enabled, " ", build.getBuildIcon(),
				SwingConstants.LEADING, ICON_HEIGHT);
		p.add(icon, gbc);

		// title
		String title = " " + build.getBuildKey() + "-" + build.getBuildNumber();
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel key = new SelectableLabel(selected, enabled, title, ICON_HEIGHT);
		p.add(key, gbc);

		// failed tests
		if (build.getStatus() == BuildStatus.BUILD_FAILED) {
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			JLabel tests = new SelectableLabel(selected, enabled,
					" " + build.getTestsFailed() + "/" + build.getTestsNumber() + " Tests Failed", ICON_HEIGHT);
			p.add(tests, gbc);
		}


		// empty label
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel empty = new SelectableLabel(selected, enabled, "", ICON_HEIGHT);
		p.add(empty, gbc);

		// gap
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel empty1 = new SelectableLabel(selected, enabled, "", null,
				SwingConstants.LEADING, ICON_HEIGHT);
		setFixedComponentSize(empty1, GAP, ICON_HEIGHT);
		p.add(empty1, gbc);

		String commiters = getCommiters();

		// reason
		if (!build.getBuildReason().equals(CODE_HAS_CHANGED) || commiters.length() == 0) {
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_END;
			JLabel reason = new SelectableLabel(selected, enabled, build.getBuildReason(), null,
					SwingConstants.LEADING, ICON_HEIGHT);
//			setFixedComponentSize(reason, REASON_LABEL_WIDTH, ICON_HEIGHT);
			reason.setHorizontalAlignment(SwingConstants.RIGHT);
			p.add(reason, gbc);
		}

		// commiters
		if (commiters.length() > 0 && build.getBuildReason().equals(CODE_HAS_CHANGED)) {

			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_END;
			JLabel commitersList = new SelectableLabel(selected, enabled, "Changes by: " + commiters, null,
					SwingConstants.LEADING, ICON_HEIGHT);
			commitersList.setHorizontalAlignment(SwingConstants.RIGHT);
			p.add(commitersList, gbc);

		}

				// gap
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel empty2 = new SelectableLabel(selected, enabled, "", null,
				SwingConstants.LEADING, ICON_HEIGHT);
		setFixedComponentSize(empty2, GAP + GAP + GAP, ICON_HEIGHT);
		p.add(empty1, gbc);

		// server
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel server = new SelectableLabel(selected, enabled, "(" + build.getServer().getName() + ")", null,
				SwingConstants.LEADING, ICON_HEIGHT);
		server.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(server, gbc);


		// date
		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		JLabel date = new SelectableLabel(selected, enabled, build.getBuildRelativeBuildDate(), null,
				SwingConstants.LEADING, ICON_HEIGHT);
		setFixedComponentSize(date, DATE_LABEL_WIDTH, ICON_HEIGHT);
		date.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(date, gbc);

		// padding
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

		final JToolTip jToolTip = p.createToolTip();
		jToolTip.setTipText(buildTolltip(0));
		final int prefWidth = jToolTip.getPreferredSize().width;
		int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
		p.setToolTipText(buildTolltip(width));

		return p;

	}

	private String getCommiters() {
		String commiters = "";
		for (String commiter : build.getCommiters()) {
			commiters += commiter + ", ";
		}
		return commiters;
//		return "Jacek, Zenek, Rysiek";
	}

	private String buildTolltip(int width) {
		StringBuilder sb = new StringBuilder(
				"<html>"
						+ BODY_WITH_STYLE);
		final String widthString = width > 0 ? "width='" + width + "px'" : "";
		sb.append("<table ").append(widthString).append(" align='center' cols='2'>");
		sb.append("<tr><td colspan='2'><b><font color='blue'>");
		sb.append(build.getBuildKey()).append("-").append(build.getBuildNumber());
		sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Name:</b></td><td valign=\"top\">");

		String name = build.getBuildName();
		sb.append(StringEscapeUtils.escapeHtml(name));
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Project:</b></td><td valign=\"top\">");

		String project = build.getProjectName();
		sb.append(StringEscapeUtils.escapeHtml(project));
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Reason:</b></td><td valign=\"top\">");
		sb.append(build.getBuildReason());
		sb.append("</td></tr>");

		if (getCommiters().length() > 0) {
			sb.append("<tr><td valign=\"top\"><b>Commiters:</b></td><td valign=\"top\">");
			sb.append(getCommiters());
			sb.append("</td></tr>");
		}

		sb.append("<tr><td valign=\"top\"><b>Server:</b></td><td valign=\"top\">");
		sb.append(build.getServerName());
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Build Date:</b></td><td valign=\"top\">");
		String date = build.getBuildRelativeBuildDate();
		sb.append(StringEscapeUtils.escapeHtml(date).replace("\n", Util.HTML_NEW_LINE).replace(" ", "&nbsp;")
				.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));

		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	public void onSelect() {
	}

	private static void setFixedComponentSize(JComponent c, int width, int height) {
		c.setPreferredSize(new Dimension(width, height));
		c.setMinimumSize(new Dimension(width, height));
		c.setMaximumSize(new Dimension(width, height));
	}
}
