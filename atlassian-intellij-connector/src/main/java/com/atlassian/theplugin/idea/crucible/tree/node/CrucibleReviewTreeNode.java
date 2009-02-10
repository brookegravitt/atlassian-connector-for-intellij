package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.Util;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;

/**
 * User: jgorycki
 * Date: Dec 4, 2008
 * Time: 11:40:51 AM
 */
public class CrucibleReviewTreeNode extends ReviewTreeNode {
	private static final int STATUS_LABEL_WIDTH = 80;
	private static final int AUTHOR_LABEL_WIDTH = 120;

	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";
	private static final int MAX_TOOLTIP_WIDTH = 500;


	private final ReviewAdapter review;
	private static final int MAX_LENGTH = 1000;
	private RendererPanel renderer;
	
	public CrucibleReviewTreeNode(ReviewAdapter review) {
		super(review.getPermId().getId(), null, null);
		this.review = review;
		renderer = new RendererPanel();
	}

	@Override
	public ReviewAdapter getReview() {
		return review;
	}

	@Override
	public String toString() {
		return review.getPermId().getId() + ": " + review.getName();
	}

	private final class RendererPanel extends JPanel {
		private SelectableLabel keyAndSummary;
		private SelectableLabel state;
		private SelectableLabel author;
		private SelectableLabel created;
		private JPanel padding;

		private RendererPanel() {
			super(new FormLayout("fill:min(pref;150px):grow, right:pref", "pref"));
			CellConstraints cc = new CellConstraints();

			setBackground(UIUtil.getTreeTextBackground());

			keyAndSummary =
					new SelectableLabel(true, true, review.getPermId().getId() + ": " + review.getName(), ICON_HEIGHT);
			add(keyAndSummary, cc.xy(1, 1));

			add(createPanelForOtherReviewDetails(), cc.xy(2, 1));

			// now black magic here: 2-pass creation of multiline tooltip, with maximum width of MAX_TOOLTIP_WIDTH
			final JToolTip jToolTip = createToolTip();
			jToolTip.setTipText(buildTolltip(0));
			final int prefWidth = jToolTip.getPreferredSize().width;
			int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
			setToolTipText(buildTolltip(width));
		}

		private JPanel createPanelForOtherReviewDetails() {
			JPanel rest = new JPanel(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			state = new SelectableLabel(true, true, "    " + review.getState().value(), null,
					SwingConstants.LEADING, ICON_HEIGHT);
			setFixedComponentSize(state, STATUS_LABEL_WIDTH, ICON_HEIGHT);
			rest.add(state, gbc);

			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			author = new SelectableLabel(true, true, review.getAuthor().getDisplayName(), null,
					SwingConstants.LEADING, ICON_HEIGHT);
			setFixedComponentSize(author, AUTHOR_LABEL_WIDTH, ICON_HEIGHT);
			rest.add(author, gbc);

			DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String t = dfo.format(review.getCreateDate());
			gbc.gridx++;
			gbc.weightx = 0.0;
			created = new SelectableLabel(true, true, t, null, SwingConstants.LEADING, ICON_HEIGHT);
			created.setHorizontalAlignment(SwingConstants.RIGHT);
			Dimension minDimension = created.getPreferredSize();
			minDimension.setSize(
					Math.max(PluginUtil.getDateWidth(created, dfo), minDimension.getWidth()), minDimension.getHeight());
			setFixedComponentSize(created, minDimension.width, ICON_HEIGHT);
			rest.add(created, gbc);

			padding = new JPanel();
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setMinimumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setMaximumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
			padding.setOpaque(true);
			rest.add(padding, gbc);

			return rest;
		}

		public void setParameters(boolean selected, boolean enabled) {
			keyAndSummary.setSelected(selected);
			keyAndSummary.setEnabled(enabled);

			state.setSelected(selected);
			state.setEnabled(enabled);

			author.setSelected(selected);
			author.setEnabled(enabled);

			created.setSelected(selected);
			created.setEnabled(enabled);

			padding.setBackground(selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground());
			padding.setForeground(selected ? UIUtil.getTreeSelectionForeground() : UIUtil.getTreeTextForeground());
		}
	}

	@Override
	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
		renderer.setParameters(selected, c.isEnabled());
		return renderer;
	}

	private String buildTolltip(int width) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);
		final String widthString = width > 0 ? "width='" + width + "px'" : "";
		sb.append("<table ").append(widthString).append(" align='center' cols='2'>");
		sb.append("<tr><td colspan='2'><b><font color='blue'>");
        sb.append(review.getPermId().getId());
        sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Name:</b></td><td valign=\"top\">");

		String summary = review.getName();
		sb.append(Util.textToMultilineHtml(summary));
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Statement of Objectives:</b></td><td valign=\"top\">");

		String description = review.getDescription();
		if (description.length() > MAX_LENGTH) {
			description = description.substring(0, MAX_LENGTH) + "\n...";
		}
		sb.append(Util.textToMultilineHtml(description));

		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Author:</b></td><td valign=\"top\">");
		sb.append(review.getAuthor().getDisplayName());
		sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Moderator:</b></td><td valign=\"top\">");
        sb.append(review.getModerator().getDisplayName());
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Created:</b></td><td valign=\"top\">");
		sb.append(review.getCreateDate());
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Status:</b></td><td valign=\"top\">");
		sb.append(review.getState().value());
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	@Override
	public void onSelect() {
//		model.setSelectedReview(review); <- selection is stored inside the tree instead of global plugin review model
	}

	private static void setFixedComponentSize(JComponent c, int width, int height) {
		c.setPreferredSize(new Dimension(width, height));
		c.setMinimumSize(new Dimension(width, height));
		c.setMaximumSize(new Dimension(width, height));
	}
}
