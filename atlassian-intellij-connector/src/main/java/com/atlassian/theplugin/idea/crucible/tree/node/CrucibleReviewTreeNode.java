package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.tree.ReviewTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringEscapeUtils;

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

	public CrucibleReviewTreeNode(ReviewAdapter review) {
		super(review.getPermId().getId(), null, null);
		this.review = review;
	}

	@Override
	public ReviewAdapter getReview() {
		return review;
	}

	@Override
	public String toString() {
		return review.getPermId().getId() + ": " + review.getName();
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
		gbc.fill = GridBagConstraints.NONE;

		gbc.insets = new Insets(0, 0, 0, 0);
		JLabel key = new SelectableLabel(selected, enabled, review.getPermId().getId() + ": ", ICON_HEIGHT);
		p.add(key, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel summary = new SelectableLabel(selected, enabled, review.getName(), ICON_HEIGHT);
		p.add(summary, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		JLabel state = new SelectableLabel(selected, enabled, review.getState().value(), null,
				SwingConstants.LEADING, ICON_HEIGHT);
		setFixedComponentSize(state, STATUS_LABEL_WIDTH, ICON_HEIGHT);
		p.add(state, gbc);

		gbc.gridx++;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		JLabel author = new SelectableLabel(selected, enabled, review.getAuthor().getDisplayName(), null,
				SwingConstants.LEADING, ICON_HEIGHT);
		setFixedComponentSize(author, AUTHOR_LABEL_WIDTH, ICON_HEIGHT);
		p.add(author, gbc);

		DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String t = dfo.format(review.getCreateDate());
		gbc.gridx++;
        gbc.weightx = 0.0;
		JLabel created = new SelectableLabel(selected, enabled, t, null, SwingConstants.LEADING, ICON_HEIGHT);
		created.setHorizontalAlignment(SwingConstants.RIGHT);
		Dimension minDimension = created.getPreferredSize();
		minDimension.setSize(
				Math.max(PluginUtil.getDateWidth(created, dfo), minDimension.getWidth()), minDimension.getHeight());
		setFixedComponentSize(created, minDimension.width, ICON_HEIGHT);
		p.add(created, gbc);

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

		// now black magic here: 2-pass creation of multiline tooltip, with maximum width of MAX_TOOLTIP_WIDTH  
		final JToolTip jToolTip = p.createToolTip();
		jToolTip.setTipText(buildTolltip(0));
		final int prefWidth = jToolTip.getPreferredSize().width;
		int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
		p.setToolTipText(buildTolltip(width));
		return p;
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

		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");

		String summary = review.getName();
		sb.append(StringEscapeUtils.escapeHtml(summary));
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Statement of Objectives:</b></td><td valign=\"top\">");

		String description = review.getDescription();
		if (description.length() > MAX_LENGTH) {
			description = description.substring(0, MAX_LENGTH) + "\n...";
		}
		sb.append(StringEscapeUtils.escapeHtml(description).replace("\n", "<br/>").replace(" ", "&nbsp;")
				.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));

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
