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
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.intellij.ui.components.labels.BoldLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommentPanelBuilder {
	private static final Color NOT_MINE_HEADER_COLOR = new Color(224, 224, 224);
	private static final Color NOT_MINE_BODY_COLOR = new Color(255, 255, 255);

	private static final Color MINE_HEADER_COLOR = new Color(192, 255, 192);
	private static final Color MINE_BODY_COLOR = new Color(255, 255, 255);

	private CommentPanelBuilder() {
		// this is utility class
	}

	public static JPanel createEditPanelOfGeneralComment(ReviewAdapter review, GeneralComment comment) {
		return createViewPanelOfGeneralComment(review, comment, false); // no editing temporarily
	}

	public static JPanel createViewPanelOfGeneralComment(final ReviewAdapter review, final GeneralComment comment,
														 final boolean isSelected) {
		return new CommentPanel(review, null, comment) {
			@Override
			public Color getHeaderBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUsername())) {
					return getDerivedColor(MINE_HEADER_COLOR, isSelected);
				}
				return getDerivedColor(NOT_MINE_HEADER_COLOR, isSelected);

			}

			@Override
			public Color getBodyBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUsername())) {
					return getDerivedColor(MINE_BODY_COLOR, isSelected);
				}
				return getDerivedColor(NOT_MINE_BODY_COLOR, isSelected);
			}

		};
	}

	public static JPanel createEditPanelOfVersionedComment(ReviewAdapter review, CrucibleFileInfo file,
			VersionedComment comment) {
		return createViewPanelOfVersionedComment(review, file, comment, false);
	}

	public static JPanel createViewPanelOfVersionedComment(final ReviewAdapter review, CrucibleFileInfo file,
			final VersionedComment comment, final boolean isSelected) {
		return new CommentPanel(review, file, comment) {
			@Override
			public Color getHeaderBackground() {
				boolean isLineComment = comment.isFromLineInfo() || comment.isToLineInfo();
				Color c;
				if (comment.getAuthor().getUserName().equals(review.getServer().getUsername())) {
					c = MINE_HEADER_COLOR;
				} else {
					c = NOT_MINE_HEADER_COLOR;
				}
				return getDerivedColor(c, isSelected);
			}

			@Override
			public Color getBodyBackground() {
				if (comment.getAuthor().getUserName().equals(review.getServer().getUsername())) {
					return getDerivedColor(MINE_BODY_COLOR, isSelected);
				}
				return getDerivedColor(NOT_MINE_BODY_COLOR, isSelected);
			}
		};
	}

	private static Color getDerivedColor(Color c, boolean isSelected) {
		return isSelected ? c.darker() : c;
	}

	private abstract static class CommentPanel extends JPanel {
		private Comment comment;
		private ReviewAdapter review;
		private CrucibleFileInfo file;

		private static final CellConstraints AUTHOR_POS = new CellConstraints(2, 2);
		private static final CellConstraints DATE_POS = new CellConstraints(4, 2);
		private static final CellConstraints LINE_POS = new CellConstraints(6, 2);
		private static final CellConstraints RANKING_POS = new CellConstraints(8, 2);
		private static final CellConstraints DRAF_STATE_POS = new CellConstraints(10, 2);
		private static final CellConstraints DEFECT_STATE_POS = new CellConstraints(12, 2);
		private static final CellConstraints TOOLBAR_POS = new CellConstraints(14, 2);
		private static final Color BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);

		private static final float MINIMUM_FONT_SIZE = 3;

		private CommentPanel(ReviewAdapter review, CrucibleFileInfo file, Comment comment) {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));

			this.review = review;
			this.file = file;
			this.comment = comment;

			setBackground(getBodyBackground());
			CellConstraints cc = new CellConstraints();
			JPanel header = new JPanel(
					new FormLayout(
							"4dlu, left:pref, 10dlu, left:pref, 10dlu, left:pref, 10dlu, pref:grow, 10dlu, right:pref, "
							 + "10dlu, right:pref, 10dlu, pref, 4dlu",
							"2dlu, pref:grow, 2dlu"));
			header.add(getAuthorLabel(), AUTHOR_POS);
			header.add(getDateLabel(), DATE_POS);
			header.add(getLineInfoLabel(), LINE_POS);
			if (comment.isDefectRaised()) {
				header.add(getRankingLabel(getHeaderBackground()), RANKING_POS);
			}
			header.add(getStateLabel("DEFECT", comment.isDefectRaised(), Color.RED), DEFECT_STATE_POS);
			header.add(getStateLabel("DRAFT", comment.isDraft(), Color.DARK_GRAY), DRAF_STATE_POS);
			header.add(getToolBar(), TOOLBAR_POS);
			header.setBackground(getHeaderBackground());

			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu", "2dlu, pref:grow, 2dlu"));
			body.add(getMessageBody(), cc.xy(2, 2));
			body.setBackground(getBodyBackground());

			add(header, cc.xy(1, 1));
			add(body, cc.xy(1, 2));
			setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
		}

		private Component getDateLabel() {
			StringBuilder sb = new StringBuilder();
			JLabel label;
			sb.append("[ ");
			sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(comment.getCreateDate()));
			sb.append(" ]");
			label = new JLabel(sb.toString());
			return label;
		}

		protected Component getAuthorLabel() {
			BoldLabel label =
					new BoldLabel("".equals(comment.getAuthor().getDisplayName()) ? comment.getAuthor().getUserName() : comment
							.getAuthor().getDisplayName());
			return label;
		}

		protected Component getLineInfoLabel() {
			if (file != null) {
				VersionedComment vc = (VersionedComment) comment;

				String txt1 = "";
				if (vc.getFromStartLine() > 0 && vc.isFromLineInfo()) {
					int startLine = vc.getFromStartLine();
					int endLine = vc.getFromEndLine();
					if (endLine == 0) {
						endLine = startLine;
					}
					txt1 += "Revision " + file.getOldFileDescriptor().getRevision();
					txt1 += ": ";
					txt1 += endLine != startLine
							? "Lines [" + startLine + " - " + endLine + "]"
							: "Line " + endLine;
				}

				String txt2 = "";
				if (vc.getToStartLine() > 0 && vc.isToLineInfo()) {
					int startLine = vc.getToStartLine();
					int endLine = vc.getToEndLine();
					if (endLine == 0) {
						endLine = startLine;
					}
					txt2 += " Revision " + file.getFileDescriptor().getRevision();
					txt2 += ": ";
					txt2 += endLine != startLine
							? "Lines [" + startLine + " - " + endLine + "]"
							: "Line " + endLine;
				}

				String txt = txt1;
				if (txt1.length() > 0 && txt2.length() > 0) {
					txt += ", ";
				}
				txt += txt2;

				if (!comment.isReply() && (txt.length() == 0)) {
					txt = "General File Comment";
				}
				return new JLabel(txt);
			}
			return new JLabel("");
		}


		protected Component getStateLabel(String text, boolean isInState, Color color) {
			JLabel label = new JLabel("");

			if (isInState) {
				label.setText(text);
				label.setFont(label.getFont().deriveFont(Font.BOLD));
				label.setForeground(color);
			}
			return label;
		}


		protected Component getRankingLabel(Color backgroundColor) {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			panel.setBackground(backgroundColor);

			for (Map.Entry<String, CustomField> elem : comment.getCustomFields().entrySet()) {


				JLabel keyLabel = new JLabel(" " + firstLetterUpperCase(elem.getKey()) + ": ");

				JLabel valueLabel = new JLabel(elem.getValue().getValue());
				valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));

				panel.add(keyLabel);
				panel.add(valueLabel);
			}

			return panel;
		}

		private static final Pattern FIRST_LETTER = Pattern.compile("([a-z])");

		protected String firstLetterUpperCase(String key) {
			StringBuffer sb = new StringBuffer();
			Matcher matcher = FIRST_LETTER.matcher(key);
			if (matcher.find(0)) {
				matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
			}
			key = matcher.appendTail(sb).toString();
			return key;
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
