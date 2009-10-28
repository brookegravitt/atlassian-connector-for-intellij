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
package com.atlassian.theplugin.idea.crucible.ui;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.ui.IconPaths;
import com.atlassian.theplugin.util.ui.IconProvider;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class ReviewCommentPanel extends JPanel {
	private Rectangle moreBounds;
	private static final int MIN_TEXT_WIDTH = 200;
	private final JLabel reviewerAndAuthorLabel;
	private JTextPane messageBody;
	private SimpleColoredComponent singleLineLabel;
	private JLabel defectIconLabel;
	private static final int EXTRA_MARGIN = 100;
	private static final int VERTICAL_MARGIN = 1;
	private static final int HORIZONTAL_MARGIN = 5;

	private static int getPreferredHeight(JComponent component, int preferredWidth) {
		try {
			component.addNotify();
			component.doLayout();
			component.setPreferredSize(null);
			component.setSize(preferredWidth, Integer.MAX_VALUE);
			return component.getPreferredSize().height;
		} finally {
			component.removeNotify();
		}
	}

	static final int LAST_COLUMN_WIDTH = 250;

	public Rectangle getMoreBounds() {
		return moreBounds;
	}

	private static int oneLineHeight = getMaximumTextLineHeight();

	private static int getMaximumTextLineHeight() {
		JTextPane pane = new JTextPane();
		final StyledDocument doc = pane.getStyledDocument();
		addStylesToDocument(doc);
		try {
			doc.insertString(doc.getLength(), "Ng", doc.getStyle("regular"));
		} catch (BadLocationException e) {
			// impossible (theoretically)
			throw new RuntimeException(e);
		}
		//CHECKSTYLE:MAGIC:OFF
		return getPreferredHeight(pane, Integer.MAX_VALUE) * 3 / 2;
		//CHECKSTYLE:MAGIC:ON
	}

	private static int getLastColumnWidth(int totalWidth, int preferredWidth, final int otherColumnsWidth) {
		if (totalWidth > preferredWidth + MIN_TEXT_WIDTH + otherColumnsWidth) {
			return preferredWidth;
		} else if (totalWidth > MIN_TEXT_WIDTH + otherColumnsWidth) {
			return totalWidth - MIN_TEXT_WIDTH - otherColumnsWidth;
		} else {
			return 0;
		}
	}

	private final JLabel moreLabel;
	private Font font;

	public void update(@NotNull ReviewAdapter review, @NotNull Comment comment, int width, boolean isExpanded,
			boolean isSelected, Font newFont) {
		final boolean fontChanged = !newFont.equals(font);
		if (fontChanged) {
			font = newFont;
			messageBody.setFont(font);
			moreLabel.setFont(font);
			singleLineLabel.setFont(font);
		}
		defectIconLabel.setVisible(comment.isDefectRaised());
		final int defIconPrefWidth = defectIconLabel.getPreferredSize().width;
		int defectIconWidth = defectIconLabel.isVisible() ? defIconPrefWidth + HORIZONTAL_MARGIN : 0;
        reviewerAndAuthorLabel.setFont(isCommentUnread(comment) ? font.deriveFont(Font.BOLD) : font);
//        String boldifopen = isCommentUnread(comment) ? "<html><b>" : "<html><u>";
//        String boldifclose = isCommentUnread(comment) ? "</b>" : "</u>";
//		reviewerAndAuthorLabel.setText(boldifopen + getAuthorText(comment) + ", " + getDateText(comment) + boldifclose);
        reviewerAndAuthorLabel.setText(getAuthorText(comment) + ", " + getDateText(comment));
		reviewerAndAuthorLabel.setForeground(getTextColor(isSelected));
		final int otherColumnsWidth = defectIconWidth + 2 * HORIZONTAL_MARGIN;
		final int lastColumnWidth = getLastColumnWidth(width, reviewerAndAuthorLabel.getPreferredSize().width,
				otherColumnsWidth);
		if (isSelected) {
			setOpaque(true);
			setBackground(UIUtil.getTreeSelectionBackground());
		} else {
			setOpaque(false);
		}
		updateMessageBody(review, comment, isSelected);
		int queryWidth = Math.max(width - otherColumnsWidth - lastColumnWidth, MIN_TEXT_WIDTH);
		int preferredHeight = getPreferredHeight(messageBody, queryWidth);
		if (preferredHeight > oneLineHeight) {
			moreLabel.setText("<html><a href='#'>" + (isExpanded ? "less" : "more") + "</a>");
			moreLabel.setVisible(true);
			queryWidth = Math
					.max(width - otherColumnsWidth - moreLabel.getPreferredSize().width - lastColumnWidth, MIN_TEXT_WIDTH);
			preferredHeight = getPreferredHeight(messageBody, queryWidth);
		} else {
			moreLabel.setVisible(false);
			moreBounds = null;
		}

		if (preferredHeight < oneLineHeight || isExpanded) {
			messageBody.setVisible(true);
			messageBody.setPreferredSize(new Dimension(queryWidth, preferredHeight));
			singleLineLabel.setVisible(false);
		} else {
			updateSingleLineComponent(review, comment, isSelected);
			singleLineLabel.setFont(font);
			messageBody.setVisible(false);
			singleLineLabel.setVisible(true);
			preferredHeight = singleLineLabel.getPreferredSize().height;
		}


		int moreWidth = moreLabel.isVisible() ? moreLabel.getPreferredSize().width + HORIZONTAL_MARGIN : 0;

		int placeForMessage = width - HORIZONTAL_MARGIN - lastColumnWidth - defectIconWidth - moreWidth - HORIZONTAL_MARGIN;
		if (placeForMessage < MIN_TEXT_WIDTH) {
			placeForMessage = MIN_TEXT_WIDTH;
		}
		if (messageBody.isVisible()) {
			messageBody.setBounds(0, VERTICAL_MARGIN, placeForMessage, preferredHeight);
		} else {
			singleLineLabel.setBounds(0, VERTICAL_MARGIN, placeForMessage, preferredHeight);
		}

		int lastx = placeForMessage + HORIZONTAL_MARGIN;

		final Dimension moreLabelPrefSize;
		if (moreLabel.isVisible()) {
			moreLabelPrefSize = moreLabel.getPreferredSize();
			moreLabel.setBounds(lastx, VERTICAL_MARGIN, moreLabelPrefSize.width, moreLabelPrefSize.height);
			lastx += moreLabel.getWidth() + HORIZONTAL_MARGIN;
			moreBounds = moreLabel.getBounds();
		} else {
			moreLabelPrefSize = new Dimension(0, 0);
			moreBounds = null;
		}

		final int defectIconLabelPrefHeight;
		if (defectIconLabel.isVisible()) {
			defectIconLabelPrefHeight = defectIconLabel.getPreferredSize().height;
			defectIconLabel.setBounds(lastx, VERTICAL_MARGIN, defIconPrefWidth, defectIconLabelPrefHeight);
			lastx += defectIconLabel.getWidth() + HORIZONTAL_MARGIN;
		} else {
			defectIconLabelPrefHeight = 0;
		}

		reviewerAndAuthorLabel
				.setBounds(lastx, VERTICAL_MARGIN, lastColumnWidth, reviewerAndAuthorLabel.getPreferredSize().height);

		int maxPreferredHeight = Collections
				.max(Arrays.<Integer>asList(reviewerAndAuthorLabel.getPreferredSize().height, preferredHeight,
						moreLabelPrefSize.height, defectIconLabelPrefHeight));

		validate();
		setPreferredSize(new Dimension(width + EXTRA_MARGIN, maxPreferredHeight + VERTICAL_MARGIN * 2));
		setSize(new Dimension(width + EXTRA_MARGIN, Integer.MAX_VALUE));
		addNotify();
		doLayout();
	}

	private Color getTextColor(final boolean isSelected) {
		return isSelected ? UIUtil.getTreeSelectionForeground() : UIUtil.getLabelTextForeground();
	}

	public ReviewCommentPanel(IconProvider iconProvider) {
		super(null);
		reviewerAndAuthorLabel = new JLabel();
		messageBody = new JTextPane();
		messageBody.setOpaque(false);
		messageBody.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		final StyledDocument doc = messageBody.getStyledDocument();
		addStylesToDocument(doc);
		moreLabel = new JLabel();
		add(moreLabel);
		add(messageBody);
		singleLineLabel = new SimpleColoredComponent();
		singleLineLabel.setMinimumSize(new Dimension(0, 0));
		singleLineLabel.setOpaque(false);
		singleLineLabel.setIpad(new Insets(0, 0, 0, 0));
		add(singleLineLabel);
		final Icon defectIcon = iconProvider.getIcon(IconPaths.REVIEW_COMMENT_DEFECT_PATH);
		defectIconLabel = new JLabel(defectIcon);
		add(defectIconLabel);
		add(reviewerAndAuthorLabel);
	}


	@NotNull
	private static String getDateText(Comment comment) {
		StringBuilder sb = new StringBuilder();
		sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(comment.getCreateDate()));
		return sb.toString();
	}

	private static String getAuthorText(Comment comment) {
		return "".equals(comment.getAuthor().getDisplayName())
				? comment.getAuthor().getUsername()
				: comment.getAuthor().getDisplayName();
	}

	private static String getLineInfoLabel(@NotNull Comment comment) {
		if (comment.isReply()) {
			return "Reply";
		}
		if (comment instanceof VersionedComment) {
			VersionedComment vc = (VersionedComment) comment;
            Map<String, IntRanges> ranges = vc.getLineRanges();
            if (ranges != null && ranges.size() > 0) {
                String txt = createLineRangesText(ranges);
                if (txt != null) {
                    return txt;
                }
            } else {
                if (vc.getToStartLine() > 0 && vc.isToLineInfo()) {
                    return createLineRangesText(vc.getToStartLine(), vc.getToEndLine());
                } else {
                    if (vc.getFromStartLine() > 0 && vc.isFromLineInfo()) {
                        return createLineRangesText(vc.getFromStartLine(), vc.getFromEndLine());
                    }
                }
            }
			return "General File";
		}
		return "General Comment";
	}

    private static String createLineRangesText(Map<String, IntRanges> ranges) {
        Iterator<String> iterator = ranges.keySet().iterator();
        String revision = null;
        while(iterator.hasNext()) {
            revision = iterator.next();
        }
        if (revision != null) {
            IntRanges intRanges = ranges.get(revision);
            return createLineRangesText(intRanges.getTotalMin(), intRanges.getTotalMax());
        }
        return null;
    }

    private static String createLineRangesText(int startLine, int endLine) {
        if (endLine == 0) {
            endLine = startLine;
        }
        String txt2 = "";
        txt2 += endLine != startLine ? startLine + " - " + endLine : endLine;
        return txt2;
    }


    public static String getRankingString(ReviewAdapter review, Comment comment) {
		StringBuilder sb = new StringBuilder();

		if (comment.isDefectRaised() && !comment.getCustomFields().isEmpty()) {

			if (comment.getCustomFields().size() > 0) {
				sb.append("(");
			}

			for (Map.Entry<String, CustomField> elem : comment.getCustomFields().entrySet()) {
				String label = elem.getKey();
				List<CustomFieldDef> metrics = review.getMetricDefinitions();
				if (metrics != null) {
					for (CustomFieldDef metric : metrics) {
						if (metric.getName().equals(elem.getKey())) {
							label = metric.getLabel();
							break;
						}
					}
				}
				if (sb.length() > 1) {
					sb.append(", ");
				}
				sb.append(label).append(": ");
				sb.append(elem.getValue().getValue());
			}

			if (comment.getCustomFields().size() > 0) {
				sb.append(")");
			}
		}

		return sb.toString();
	}


	private void updateSingleLineComponent(@NotNull ReviewAdapter review, @NotNull Comment comment, boolean isSelected) {
		singleLineLabel.clear();
		final String lineInfoLabel = getLineInfoLabel(comment);
		if (lineInfoLabel.length() > 0) {
			singleLineLabel.append("(" + lineInfoLabel + ") ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD,
					isSelected ? UIUtil.getTreeSelectionForeground() : null));
		}
		final String message = StringUtil.getFirstLine(comment.getMessage());

		singleLineLabel.append(message + " ", new SimpleTextAttributes(
                comment.getReadState() == Comment.ReadState.UNREAD || comment.getReadState() == Comment.ReadState.LEAVE_UNREAD
                        ? SimpleTextAttributes.STYLE_BOLD : SimpleTextAttributes.STYLE_PLAIN,
				isSelected ? UIUtil.getTreeSelectionForeground() : null));
		singleLineLabel
				.append(" " + getRankingString(review, comment), new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC,
						isSelected ? UIUtil.getTreeSelectionForeground() : Color.GRAY));

		if (comment.isDraft()) {
			StringBuilder drafInfo = new StringBuilder();
			drafInfo.append(" ");
			drafInfo.append("Draft");
			singleLineLabel.append(drafInfo.toString(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC,
					isSelected ? UIUtil.getTreeSelectionForeground() : Color.GRAY));

		}
	}

	private void updateMessageBody(ReviewAdapter review, Comment vc, boolean isSelected) {
		final StyledDocument doc = messageBody.getStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			final String lineInfoLabel = getLineInfoLabel(vc);
			if (lineInfoLabel.length() > 0) {
				doc.insertString(doc.getLength(), "(" + lineInfoLabel + ") ",
						doc.getStyle(isSelected ? "line-selected" : "line"));
			}

            boolean unread = isCommentUnread(vc);

			doc.insertString(doc.getLength(), vc.getMessage() + " ",
					doc.getStyle(isSelected
                            ? (unread ? "unread-selected" : "regular-selected")
                            : (unread ? "unread" : "regular")));
			doc.insertString(doc.getLength(), " " + getRankingString(review, vc),
					doc.getStyle(isSelected ? "defect-selected" : "defect"));
			if (vc.isDraft()) {
				StringBuilder drafInfo = new StringBuilder();
				if (doc.getLength() > 0) {
					drafInfo.append(" ");
				}
				drafInfo.append("Draft");
				doc.insertString(doc.getLength(), drafInfo.toString(),
						doc.getStyle(isSelected ? "draft-selected" : "draft"));
			}
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

    private boolean isCommentUnread(Comment vc) {
        return (vc.getReadState() == Comment.ReadState.LEAVE_UNREAD)
                || (vc.getReadState() == Comment.ReadState.UNREAD);
    }

    private static void addStylesToDocument(StyledDocument doc) {
		//Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("regular-selected", regular);
        s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

        s = doc.addStyle("unread", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("unread-selected", regular);
        StyleConstants.setBold(s, true);
        s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

		s = doc.addStyle("defect", regular);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);
		s.addAttribute(StyleConstants.ColorConstants.Italic, true);

		s = doc.addStyle("defect-selected", regular);
		s.addAttribute(StyleConstants.ColorConstants.Italic, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

		s = doc.addStyle("draft", regular);
		s.addAttribute(StyleConstants.ColorConstants.Italic, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);

		s = doc.addStyle("draft-selected", regular);
		s.addAttribute(StyleConstants.ColorConstants.Italic, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

		s = doc.addStyle("line", regular);
		StyleConstants.setBold(s, true);

		s = doc.addStyle("line-selected", regular);
		StyleConstants.setBold(s, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());
	}
}
