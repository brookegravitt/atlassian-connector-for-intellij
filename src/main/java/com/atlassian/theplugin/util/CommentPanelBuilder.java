package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.intellij.ui.components.labels.BoldLabel;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

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
				return new Color(239, 224, 255);
			}

			@Override
			public Color getBodyBackground() {
				return new Color(234, 255, 255);
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
				return new Color(255, 224, 224);
			}

			@Override
			public Color getBodyBackground() {
				return new Color(234, 255, 255);
			}
		};
	}

	private static abstract class CommentPanel extends JPanel {
		private static final Component EMPTY_LABEL = new JLabel();
		private ReviewData review;
		private Comment comment;
		private static final CellConstraints AUTHOR_POS = new CellConstraints(2, 2);
		private static final CellConstraints DATE_POS = new CellConstraints(4, 2);
		private static final CellConstraints RANKING_POS = new CellConstraints(6, 2);
		private static final CellConstraints STATE_POS = new CellConstraints(8, 2);
		private static final CellConstraints TOOLBAR_POS = new CellConstraints(10, 2);

		private CommentPanel(ReviewData review, Comment comment) {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));

			this.review = review;
			this.comment = comment;
			setBackground(getBodyBackground());
			CellConstraints cc = new CellConstraints();
			JPanel header = new JPanel(new FormLayout("4dlu, left:pref, 10dlu, left:pref, 10dlu, pref:grow, 10dlu, right:pref, 10dlu, min, 4dlu",
					"2dlu, pref:grow, 2dlu"));
			header.add(getAuthorLabel(), AUTHOR_POS);
			header.add(getDateLabel(), DATE_POS);
			header.add(getRankingLabel(), RANKING_POS);
			header.add(getStateLabel(), STATE_POS);
			header.add(AtlassianToolbar.createToolbar("a place", getToolbarName()), TOOLBAR_POS);
			header.setBackground(getHeaderBackground());


			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu",
					"2dlu, pref:grow, 2dlu"));
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
			SimpleColoredComponent component = new SimpleColoredComponent();
			boolean isFirst = true;
			for(Map.Entry<String, CustomField> elem : comment.getCustomFields().entrySet()) {
				if (!isFirst) {
					component.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				}
				component.append(elem.getKey(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
				component.append(": ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				component.append(elem.getValue().getValue(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			}
			isFirst = false;
			return component;
		}

		protected Component getMessageBody() {
			JTextArea result = new JTextArea();
			result.setText(comment.getMessage());
			result.setLineWrap(true);
			result.setWrapStyleWord(true);
			result.setBackground(getBodyBackground());
			return result;
		}

		public abstract Color getHeaderBackground();
//		{
//			return Color.yellow;
//		}

		public abstract Color getBodyBackground();
//		{
//			return Color.darkGray;
//		}
	}


}
