package com.atlassian.theplugin.util;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.intellij.ui.components.labels.BoldLabel;
import com.intellij.openapi.roots.ui.componentsList.components.RoundBorder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 3:33:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentPanelBuilder {
	public static JPanel createEditPanelOfGeneralComment(ReviewData review, GeneralComment comment) {
		return createViewPanelOfGeneralComment(review, comment); // no editing temporarily
	}

	public static JPanel createViewPanelOfGeneralComment(final ReviewData review, final GeneralComment comment) {
		return new CommentPanel(review, comment) {
			@Override
			public Color getHeaderBackground() {
				return Color.cyan;	//To change body of overridden methods use File | Settings | File Templates.
			}

			@Override
			public Color getBodyBackground() {
				return Color.white;	//To change body of overridden methods use File | Settings | File Templates.
			}
		};
	}

	public static JPanel createEditPanelOfVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		return createViewPanelOfVersionedComment(review, file, comment);
	}

	public static JPanel createViewPanelOfVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		 return new CommentPanel(review, comment) {
			@Override
			public Color getHeaderBackground() {
				return Color.lightGray;	//To change body of overridden methods use File | Settings | File Templates.
			}

			@Override
			public Color getBodyBackground() {
				return Color.white;	//To change body of overridden methods use File | Settings | File Templates.
			}
		};
	}

	private static class CommentPanel extends JPanel {
		private static final Component EMPTY_LABEL = new JLabel();
		private ReviewData review;
		private Comment comment;

		private CommentPanel(ReviewData review, Comment comment) {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));
			this.review = review;
			this.comment = comment;
			setBackground(getBodyBackground());
			CellConstraints cc = new CellConstraints();
			JPanel header = new JPanel(new FormLayout("4dlu, left:pref, 10dlu, left:pref, 10dlu, pref:grow, 10dlu, right:pref, 10dlu, min, 4dlu",
					"2dlu, pref, 2dlu"));
			header.add(getAuthorLabel(), cc.xy(2, 2));
			header.add(getDateLabel(), cc.xy(4, 2));
			header.add(getRankingLabel(), cc.xy(6, 2));
			header.add(getStateLabel(), cc.xy(8, 2));
			header.add(AtlassianToolbar.createToolbar("a place", getToolbarName()), cc.xy(10, 2));
			header.setBackground(getHeaderBackground());


			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu",
					"2dlu, pref, 2dlu"));
			body.add(getMessageBody(), cc.xy(2, 2));
			body.setBackground(getBodyBackground());

			add(header, cc.xy(1, 1));
			add(body, cc.xy(1, 2));
		}

		private Component getDateLabel() {
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			sb.append(comment.getCreateDate().toString());
			sb.append(" ]");
			return new JLabel(sb.toString());
		}

		protected String getToolbarName() {
			return "";
		}

		protected Component getAuthorLabel() {
			return new BoldLabel(review.getAuthor().getDisplayName());
		}

		protected Component getStateLabel() {
			Comment.STATE state = comment.getState();
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body><span color=\"");
			sb.append(state.getColorString());
			sb.append("\">");
			sb.append(state.toString());
			sb.append("</span></body></html>");
			return new JLabel(sb.toString());
		}

		protected Component getRankingLabel() {
			return EMPTY_LABEL;
		}

		protected Component getMessageBody() {
			JTextPane result = new JTextPane();
			result.setText(comment.getMessage());
			result.setBackground(getBodyBackground());
			return result;
		}

		public Color getHeaderBackground() {
			return Color.yellow;
		}

		public Color getBodyBackground() {
			return Color.darkGray;
		}
	}


}
