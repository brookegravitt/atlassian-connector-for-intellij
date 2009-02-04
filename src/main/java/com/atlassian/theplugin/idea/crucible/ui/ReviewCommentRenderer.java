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

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.ui.IconPaths;
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.util.IdeaIconProvider;
import com.atlassian.theplugin.util.ui.IconProvider;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ReviewCommentRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

	/**
	 * Useful for injecting your own IconProvider. Facilitates testing outside IDEA framework
	 *
	 * @param iconProvider provider used for retrieving icons
	 */
	public ReviewCommentRenderer(final IconProvider iconProvider) {
		commentPanel = new CommentPanel(iconProvider);
	}

	/**
	 * uses default IDEA-specific icon provider. Not testable outside IDEA
	 */
	public ReviewCommentRenderer() {
		this(new IdeaIconProvider());
	}

	private final CommentPanel commentPanel;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, final boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean aHasFocus) {
		if (value instanceof CommentTreeNode) {
			final CommentTreeNode node = (CommentTreeNode) value;
			commentPanel.update(node.getComment(), getAvailableWidth(node, tree),
					node.isExpanded(), isSelected, tree.getFont());
			return commentPanel;
		} else {
			return super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, aHasFocus);
		}
	}

	private int getAvailableWidth(DefaultMutableTreeNode obj, JTree jtree) {
		int i1 = jtree.getInsets().left + jtree.getInsets().right + getNesting(jtree) * obj.getLevel();
		return jtree.getVisibleRect().width - i1 - 2;
	}

	private int getNesting(JTree jtree) {
		TreeUI treeui = jtree.getUI();
		if (treeui instanceof BasicTreeUI) {
			BasicTreeUI basictreeui = (BasicTreeUI) treeui;
			return basictreeui.getLeftChildIndent() + basictreeui.getRightChildIndent();
		} else {
			return (Integer) UIUtil.getTreeLeftChildIndent() + (Integer) UIUtil.getTreeRightChildIndent();
		}
	}

}

class CommentPanel extends JPanel {
	private Rectangle moreBounds;
	private static final int MIN_TEXT_WIDTH = 200;
	private final JLabel reviewerAndAuthorLabel;
	private JTextPane messageBody;
	private SimpleColoredComponent singleLineLabel;
	private JLabel defectIconLabel;

	private static int getPreferredHeight(JComponent component, int preferredWidth) {
		try {
			component.addNotify();
			component.doLayout();
			component.setPreferredSize(null);
			component.setSize(preferredWidth, Integer.MAX_VALUE);
			// kalamon: after adding 2 pixels it seems to look less like total shit (but still crappy)
			return component.getPreferredSize().height + 2;
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

	public void update(@NotNull Comment comment, int width, boolean isExpanded, boolean isSelected, Font newFont) {
		final boolean fontChanged = !newFont.equals(font);
		if (fontChanged) {
			font = newFont;
			reviewerAndAuthorLabel.setFont(font);
			messageBody.setFont(font);
			moreLabel.setFont(font);
			singleLineLabel.setFont(font);
		}
		final int verticalMargin = 2;
		final int horizontalMargin = 5;
		defectIconLabel.setVisible(comment.isDefectRaised());
		final int defIconPrefWidth = defectIconLabel.getPreferredSize().width;
		int defectIconWidth = defectIconLabel.isVisible() ? defIconPrefWidth + horizontalMargin : 0;
		reviewerAndAuthorLabel.setText(getAuthorText(comment) + ", " + getDateText(comment));
		reviewerAndAuthorLabel.setForeground(getTextColor(isSelected));
		final int otherColumnsWidth = defectIconWidth + 2 * horizontalMargin;
		final int lastColumnWidth = getLastColumnWidth(width, reviewerAndAuthorLabel.getPreferredSize().width,
				otherColumnsWidth);
//		setLayout(new FormLayout("max(d;" + MIN_TEXT_WIDTH + "px):grow, 2dlu, d, 5px, 16px, 5px, right:"
//				+ lastColumnWidth + "px" + ", 4px", "4px, top:pref:grow, 2dlu"));
		if (isSelected) {
			setOpaque(true);
			setBackground(UIUtil.getTreeSelectionBackground());
		} else {
			setOpaque(false);
		}
		updateMessageBody(comment, isSelected);
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
			updateSingleLineComponent(comment, isSelected);
			singleLineLabel.setFont(font);
			messageBody.setVisible(false);
			singleLineLabel.setVisible(true);
			preferredHeight = singleLineLabel.getPreferredSize().height;
		}


		int moreWidth = moreLabel.isVisible() ? moreLabel.getPreferredSize().width + horizontalMargin : 0;

		int placeForMessage = width - horizontalMargin - lastColumnWidth - defectIconWidth - moreWidth - horizontalMargin;
		if (placeForMessage < MIN_TEXT_WIDTH) {
			placeForMessage = MIN_TEXT_WIDTH;
		}
		if (messageBody.isVisible()) {
			messageBody.setBounds(0, verticalMargin, placeForMessage, preferredHeight);
		} else {
			singleLineLabel.setBounds(0, verticalMargin, placeForMessage, preferredHeight);
		}

		int lastx = placeForMessage + horizontalMargin;

		final Dimension moreLabelPrefSize;
		if (moreLabel.isVisible()) {
			moreLabelPrefSize = moreLabel.getPreferredSize();
			moreLabel.setBounds(lastx, verticalMargin, moreLabelPrefSize.width, moreLabelPrefSize.height);
			lastx += moreLabel.getWidth() + horizontalMargin;
			moreBounds = moreLabel.getBounds();
		} else {
			moreLabelPrefSize = new Dimension(0, 0);
			moreBounds = null;
		}

		final int defectIconLabelPrefHeight;
		if (defectIconLabel.isVisible()) {
			defectIconLabelPrefHeight = defectIconLabel.getPreferredSize().height;
			defectIconLabel.setBounds(lastx, verticalMargin, defIconPrefWidth, defectIconLabelPrefHeight);
			lastx += defectIconLabel.getWidth() + horizontalMargin;
		} else {
			defectIconLabelPrefHeight = 0;
		}

		reviewerAndAuthorLabel
				.setBounds(lastx, verticalMargin, lastColumnWidth, reviewerAndAuthorLabel.getPreferredSize().height);

		int maxPreferredHeight = Collections
				.max(Arrays.<Integer>asList(reviewerAndAuthorLabel.getPreferredSize().height, preferredHeight,
						moreLabelPrefSize.height, defectIconLabelPrefHeight));

//		reviewerAndAuthorLabel.setBounds(width - lastColumnWidth - 4, verticalMargin,
//				lastColumnWidth, reviewerAndAuthorLabel.getPreferredSize().height);
//		defectIconLabel.setBounds(reviewerAndAuthorLabel.getX() - 5 - 16, verticalMargin, 16, 16);


		validate();
		setPreferredSize(new Dimension(width + 100, maxPreferredHeight + verticalMargin * 2));
		setSize(new Dimension(width + 100, Integer.MAX_VALUE));
		addNotify();
		doLayout();
	}

	private Color getTextColor(final boolean isSelected) {
		return isSelected ? UIUtil.getTreeSelectionForeground() : UIUtil.getLabelTextForeground();
	}

//	private int getXPos(int x) {
//		return (x > MIN_TEXT_WIDTH) ? x : Math.max(0, x )
//	}

	public CommentPanel(IconProvider iconProvider) {
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
				? comment.getAuthor().getUserName()
				: comment.getAuthor().getDisplayName();
	}

	private static String getLineInfoLabel(@NotNull Comment comment) {
		if (comment.isReply()) {
			return "Reply";
		}
		if (comment instanceof VersionedComment) {
			VersionedComment vc = (VersionedComment) comment;
			if (vc.getToStartLine() > 0 && vc.isToLineInfo()) {
				int startLine = vc.getToStartLine();
				int endLine = vc.getToEndLine();
				if (endLine == 0) {
					endLine = startLine;
				}
				String txt2 = "";
				txt2 += endLine != startLine ? startLine + " - " + endLine : endLine;
				return txt2;
			} else {
				if (vc.getFromStartLine() > 0 && vc.isFromLineInfo()) {
					int startLine = vc.getFromStartLine();
					int endLine = vc.getFromEndLine();
					if (endLine == 0) {
						endLine = startLine;
					}
					String txt2 = "";
					txt2 += endLine != startLine ? startLine + " - " + endLine : endLine;
					return txt2;
				}
			}
			return "General File";
		}
		return "General Comment";
	}


	private static String getRankingString(Comment comment) {
		StringBuilder sb = new StringBuilder();
		if (comment.getCustomFields().size() > 0) {
			sb.append("(");
		}
		for (Map.Entry<String, CustomField> elem : comment.getCustomFields().entrySet()) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append(elem.getKey()).append(": ");
			sb.append(elem.getValue().getValue());
		}

		if (comment.getCustomFields().size() > 0) {
			sb.append(")");
		}

		return sb.toString();
	}


	private void updateSingleLineComponent(@NotNull Comment comment, boolean isSelected) {
		singleLineLabel.clear();
		final String lineInfoLabel = getLineInfoLabel(comment);
		if (lineInfoLabel.length() > 0) {
			singleLineLabel.append("(" + lineInfoLabel + ") ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD,
					isSelected ? UIUtil.getTreeSelectionForeground() : null));
		}
		final String message = StringUtil.getFirstLine(comment.getMessage());

		singleLineLabel.append(message + " ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN,
				isSelected ? UIUtil.getTreeSelectionForeground() : null));
		singleLineLabel.append(" " + getRankingString(comment), new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC,
				isSelected ? UIUtil.getTreeSelectionForeground() : Color.GRAY));

		if (comment.isDraft()) {
			StringBuilder drafInfo = new StringBuilder();
			drafInfo.append(" ");
			drafInfo.append("Draft");
			singleLineLabel.append(drafInfo.toString(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC,
					isSelected ? UIUtil.getTreeSelectionForeground() : Color.GRAY));

		}
	}

	private void updateMessageBody(Comment vc, boolean isSelected) {
		final StyledDocument doc = messageBody.getStyledDocument();
		try {
			doc.remove(0, doc.getLength());
			final String lineInfoLabel = getLineInfoLabel(vc);
			if (lineInfoLabel.length() > 0) {
				doc.insertString(doc.getLength(), "(" + lineInfoLabel + ") ",
						doc.getStyle(isSelected ? "line-selected" : "line"));
			}
			doc.insertString(doc.getLength(), vc.getMessage() + " ",
					doc.getStyle(isSelected ? "regular-selected" : "regular"));
			doc.insertString(doc.getLength(), " " + getRankingString(vc),
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

	private static void addStylesToDocument(StyledDocument doc) {
		//Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("regular-selected", regular);

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

