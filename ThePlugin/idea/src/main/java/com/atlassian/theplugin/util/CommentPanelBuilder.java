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

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.labels.BoldLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

//import thirdparty.publicobject.RoundedBorder;

public final class CommentPanelBuilder {
	private static final Color NOT_MINE_GENERAL_COMMENT_HEADER_COLOR = new Color(239, 224, 255);
	private static final Color NOT_MINE_GENERAL_COMMENT_BODY_COLOR = new Color(234, 255, 255);
	private static final Color NOT_MINE_FILE_COMMENT_HEADER_COLOR = new Color(255, 224, 224);
	private static final Color NOT_MINE_FILE_COMMENT_BODY_COLOR = new Color(234, 255, 255);

	private static final Color MINE_GENERAL_COMMENT_HEADER_COLOR = new Color(0xE0, 0xFE, 0xFF);
	private static final Color MINE_GENERAL_COMMENT_BODY_COLOR = new Color(0xEA, 0xFF, 0xFF);
	private static final Color MINE_FILE_COMMENT_HEADER_COLOR = new Color(0xE0, 0xFE, 0xFF);
	private static final Color MINE_FILE_COMMENT_BODY_COLOR = new Color(0xEA, 0xFF, 0xFF);

    private CommentPanelBuilder() {
        // this is utility class
    }

    public static JPanel createEditPanelOfGeneralComment(ReviewData review, GeneralComment comment) {
		return createViewPanelOfGeneralComment(review, comment); // no editing temporarily
	}

	public static JPanel createViewPanelOfGeneralComment(final ReviewData review, final GeneralComment comment) {
		return new CommentPanel(review, comment) {
			@Override
			public Color getHeaderBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUserName())) {
					return MINE_GENERAL_COMMENT_HEADER_COLOR;
				}
				return NOT_MINE_GENERAL_COMMENT_HEADER_COLOR;

			}

			@Override
			public Color getBodyBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUserName())) {
					return MINE_GENERAL_COMMENT_BODY_COLOR;
				}
				return NOT_MINE_GENERAL_COMMENT_BODY_COLOR;
			}

		};
	}

	public static JPanel createEditPanelOfVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		return createViewPanelOfVersionedComment(review, file, comment);
	}

	public static JPanel createViewPanelOfVersionedComment(final ReviewData review, CrucibleFileInfo file,
            final VersionedComment comment) {
		return new CommentPanel(review, comment) {
			@Override
			public Color getHeaderBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUserName())) {
					return MINE_FILE_COMMENT_HEADER_COLOR;
				}
				return NOT_MINE_FILE_COMMENT_HEADER_COLOR;
			}

			@Override
			public Color getBodyBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUserName())) {
					return MINE_FILE_COMMENT_BODY_COLOR;
				}
				return NOT_MINE_FILE_COMMENT_BODY_COLOR;
			}
		};
	}

	private abstract static class CommentPanel extends JPanel {
		private ReviewData review;
		private Comment comment;
		private static final CellConstraints AUTHOR_POS = new CellConstraints(2, 2);
		private static final CellConstraints DATE_POS = new CellConstraints(4, 2);
		private static final CellConstraints RANKING_POS = new CellConstraints(6, 2);
		private static final CellConstraints STATE_POS = new CellConstraints(8, 2);
		private static final CellConstraints TOOLBAR_POS = new CellConstraints(10, 2);
		private static final Color BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);

		private CommentPanel(ReviewData review, Comment comment) {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));

			this.review = review;
			this.comment = comment;
			setBackground(getBodyBackground());
			CellConstraints cc = new CellConstraints();
            JPanel header = new JPanel(
                    new FormLayout("4dlu, left:pref, 10dlu, left:pref, 10dlu, pref:grow, 10dlu, right:pref, 10dlu, pref, 4dlu",
                            "2dlu, pref:grow, 2dlu"));
            header.add(getAuthorLabel(), AUTHOR_POS);
			header.add(getDateLabel(), DATE_POS);
			header.add(getRankingLabel(), RANKING_POS);
			header.add(getStateLabel(), STATE_POS);
			header.add(getToolBar(), TOOLBAR_POS);
			header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
			header.setBackground(getHeaderBackground());
//			header.setBorder(new RoundedBorder(getHeaderBackground(), Color.black, getForeground(), 8, 1));


			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu", "2dlu, pref:grow, 2dlu"));
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

		protected Component getAuthorLabel() {
			return new BoldLabel(review.getAuthor().getDisplayName());
		}

		protected Component getStateLabel() {
			Comment.STATE state = comment.getState();
			StringBuilder sb = new StringBuilder();
			switch (state) {
				case REVIEW:
					break;
				default:
					sb.append("<html><body><span color=\"");
					sb.append(state.getColorString());
					sb.append("\">");
					sb.append(state.toString());
					sb.append("</span></body></html>");
			}
			return new JLabel(sb.toString());
		}

		protected Component getRankingLabel() {
			SimpleColoredComponent component = new SimpleColoredComponent();
			boolean isFirst = true;
			for (Map.Entry<String, CustomField> elem : comment.getCustomFields().entrySet()) {
				if (!isFirst) {
					component.append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				}
				component.append(elem.getKey(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
				component.append(": ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
				component.append(elem.getValue().getValue(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
				isFirst = false;
			}
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

		public abstract Color getBodyBackground();

		public Component getToolBar() {
			return new JLabel();
		}
	}


}
