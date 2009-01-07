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
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeNode extends AbstractBuildTreeNode {

	private BambooBuildAdapterIdea build;

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



			gbc.insets = new Insets(0, 0, 0, 0);
			JLabel icon = new SelectableLabel(selected, enabled, " ", build.getBuildIcon(),
					SwingConstants.LEADING, ICON_HEIGHT);
			p.add(icon, gbc);

			String title = " " + build.getBuildKey() + "-" + build.getBuildNumber();

		if (build.getStatus() == BuildStatus.BUILD_FAILED) {
			title += " " + build.getTestsFailed() + "/" + build.getTestsNumber() + " Tests Failed";
		}

			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			JLabel key = new SelectableLabel(selected, enabled, title, ICON_HEIGHT);
			p.add(key, gbc);

//		if (build.getStatus() == BuildStatus.BUILD_FAILED) {
//			gbc.gridx++;
//			gbc.weightx = 1.0;
//			gbc.fill = GridBagConstraints.HORIZONTAL;
//			JLabel failedTests = new SelectableLabel(selected, enabled,
//					, null, SwingConstants.LEFT,
//					ICON_HEIGHT);
//			p.add(failedTests, gbc);
//		}

//			gbc.gridx++;
//			gbc.weightx = 0.0;
//			gbc.fill = GridBagConstraints.NONE;
//			JLabel state = new SelectableLabel(selected, enabled, review.getState().value(), null,
//					SwingConstants.LEADING, ICON_HEIGHT);
//			setFixedComponentSize(state, STATUS_LABEL_WIDTH, ICON_HEIGHT);
//			p.add(state, gbc);
//
//			gbc.gridx++;
//			gbc.weightx = 0.0;
//			gbc.insets = new Insets(0, 0, 0, 0);
//			JLabel author = new SelectableLabel(selected, enabled, review.getAuthor().getDisplayName(), null,
//					SwingConstants.LEADING, ICON_HEIGHT);
//			setFixedComponentSize(author, AUTHOR_LABEL_WIDTH, ICON_HEIGHT);
//			p.add(author, gbc);
//
//			DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//			String t = dfo.format(review.getCreateDate());
//			gbc.gridx++;
//			gbc.weightx = 0.0;
//			JLabel created = new SelectableLabel(selected, enabled, t, null, SwingConstants.LEADING, ICON_HEIGHT);
//			created.setHorizontalAlignment(SwingConstants.RIGHT);
//			Dimension minDimension = created.getPreferredSize();
//			minDimension.setSize(
//					Math.max(PluginUtil.getDateWidth(created, dfo), minDimension.getWidth()), minDimension.getHeight());
//			setFixedComponentSize(created, minDimension.width, ICON_HEIGHT);
//			p.add(created, gbc);
//
//			JPanel padding = new JPanel();
//			gbc.gridx++;
//			gbc.weightx = 0.0;
//			gbc.fill = GridBagConstraints.NONE;
//			padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
//			padding.setMinimumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
//			padding.setMaximumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
//			padding.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
//			padding.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
//			padding.setOpaque(true);
//			p.add(padding, gbc);
//
//		final JToolTip jToolTip = p.createToolTip();
//		jToolTip.setTipText(buildTolltip(0));
//		final int prefWidth = jToolTip.getPreferredSize().width;
//		int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
//		p.setToolTipText(buildTolltip(width));

		return p;

	}

//	private String buildTolltip(int width) {
//		StringBuilder sb = new StringBuilder(
//				"<html>"
//						+ BODY_WITH_STYLE);
//		final String widthString = width > 0 ? "width='" + width + "px'" : "";
//		sb.append("<table ").append(widthString).append(" align='center' cols='2'>");
//		sb.append("<tr><td colspan='2'><b><font color='blue'>");
//		sb.append(review.getPermId().getId());
//		sb.append("</font></b>");
//
//		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");
//
//		String summary = review.getName();
//		sb.append(StringEscapeUtils.escapeHtml(summary));
//		sb.append("</td></tr>");
//
//		sb.append("<tr><td valign=\"top\"><b>Statement of Objectives:</b></td><td valign=\"top\">");
//
//		String description = review.getDescription();
//		if (description.length() > MAX_LENGTH) {
//			description = description.substring(0, MAX_LENGTH) + "\n...";
//		}
//		sb.append(StringEscapeUtils.escapeHtml(description).replace("\n", "<br/>").replace(" ", "&nbsp;")
//				.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));
//
//		sb.append("</td></tr>");
//
//		sb.append("<tr><td valign=\"top\"><b>Author:</b></td><td valign=\"top\">");
//		sb.append(review.getAuthor().getDisplayName());
//		sb.append("</td></tr>");
//
//		sb.append("<tr><td valign=\"top\"><b>Moderator:</b></td><td valign=\"top\">");
//		sb.append(review.getModerator().getDisplayName());
//		sb.append("</td></tr>");
//
//		sb.append("<tr><td valign=\"top\"><b>Created:</b></td><td valign=\"top\">");
//		sb.append(review.getCreateDate());
//		sb.append("</td></tr>");
//
//		sb.append("<tr><td valign=\"top\"><b>Status:</b></td><td valign=\"top\">");
//		sb.append(review.getState().value());
//		sb.append("</td></tr>");
//
//		sb.append("</table>");
//		sb.append("</body></html>");
//		return sb.toString();
//	}

	public void onSelect() {
	}
}
