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
import com.atlassian.theplugin.idea.ui.tree.comment.CommentTreeNode;
import com.atlassian.theplugin.idea.ui.IconPaths;
import com.atlassian.theplugin.idea.util.IdeaIconProvider;
import com.atlassian.theplugin.util.ui.IconProvider;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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
import java.util.Map;

public class ReviewCommentRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

	/**
	 * Useful for injecting your own IconProvider. Facilitates testing outside IDEA framework
	 * @param iconProvider provider used for retrieving icons
	 */
	public ReviewCommentRenderer(final IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	/**
	 * uses default IDEA-specific icon provider. Not testable outside IDEA
	 */
	public ReviewCommentRenderer() {
		this.iconProvider = new IdeaIconProvider();
	}

	private final IconProvider iconProvider;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, final boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean aHasFocus) {
		if (value instanceof CommentTreeNode) {
			final CommentTreeNode node = (CommentTreeNode) value;
			// @todo wseliga inject here IdeaIconProvider
			return new CommentPanel(node.getComment(), getAvailableWidth(node, tree), row,
					iconProvider, node.isExpanded(), isSelected);
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
	private Comment comment;

	private static final CellConstraints MORE_LINK_POS = new CellConstraints(3, 2);
	private static final CellConstraints DEFECT_ICON_POS = new CellConstraints(5, 2);
	private static final CellConstraints AUTHOR_POS = new CellConstraints(7, 2);
	private static final int OTHER_COLUMNS_WIDTH = 34;
	private Rectangle moreBounds;
	private static final int MIN_TEXT_WIDTH = 200;

	private static int getPreferredHeight(JComponent component, int preferredWidth) {
		try {
			component.addNotify();
			component.doLayout();
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

	private static int oneLineHeight = getFDSFDS();

	private static int getFDSFDS() {
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

	private static int getLastColumnWidth(int totalWidth, int preferredWidth) {
		if (totalWidth > preferredWidth + MIN_TEXT_WIDTH + OTHER_COLUMNS_WIDTH) {
			return preferredWidth;
		} else if (totalWidth > MIN_TEXT_WIDTH + OTHER_COLUMNS_WIDTH) {
			return totalWidth - MIN_TEXT_WIDTH - OTHER_COLUMNS_WIDTH;
		} else {
			return 0;
		}
	}

	@SuppressWarnings({"UnusedDeclaration"})
	public CommentPanel(Comment comment, int width, final int row, IconProvider iconProvider,
			boolean isExpanded, final boolean isSelected) {
		final JLabel reviewerAndAuthorLabel = new JLabel(getAuthorLabel(comment) + ", " + getDateLabel(comment));
		final int lastColumnWidth = getLastColumnWidth(width, reviewerAndAuthorLabel.getPreferredSize().width);
		setLayout(new FormLayout("max(d;" + MIN_TEXT_WIDTH + "px):grow, 2dlu, d, 5px, 16px, 5px, right:"
				+ lastColumnWidth + "px" + ", 4px", "4px, top:pref:grow, 2dlu"));
		this.comment = comment;
		if (isSelected) {
			setOpaque(true);
			setBackground(UIUtil.getTreeSelectionBackground());
		} else {
			setOpaque(false);
		}
		final JTextPane messageBody = createMessageBody(comment, isSelected);
		int queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - lastColumnWidth, MIN_TEXT_WIDTH);
		int preferredHeight = getPreferredHeight(messageBody, queryWidth);
		CellConstraints cc = new CellConstraints();
		final JLabel moreLabel;
		if (preferredHeight > oneLineHeight) {
			moreLabel = new JLabel("<html><a href='#'>" + (isExpanded ? "less" : "more") + "</a>");
			add(moreLabel, MORE_LINK_POS);
			queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - moreLabel.getPreferredSize().width - lastColumnWidth,
					MIN_TEXT_WIDTH);
			preferredHeight = getPreferredHeight(messageBody, queryWidth);
		} else {
			moreLabel = null;
		}

		if (preferredHeight < oneLineHeight || isExpanded) {
			add(messageBody, cc.xy(1, 2));
			messageBody.setPreferredSize(new Dimension(queryWidth, preferredHeight));
		} else {
			final SimpleColoredComponent jLabel = getSingleLineComponent(comment, isSelected);
			jLabel.setMinimumSize(new Dimension(0, 0));
			add(jLabel, cc.xy(1, 2));
		}


		if (comment.isDefectRaised()) {
			Icon myicon = iconProvider.getIcon(IconPaths.REVIEW_COMMENT_DEFECT_PATH);
			JLabel icon = new JLabel(myicon);
			add(icon, DEFECT_ICON_POS);
		}

		add(reviewerAndAuthorLabel, AUTHOR_POS);

		validate();
		setSize(new Dimension(width, Integer.MAX_VALUE));
		addNotify();
		doLayout();
		if (moreLabel != null) {
			moreBounds = moreLabel.getBounds();
		}
	}


	@NotNull
	private static String getDateLabel(Comment comment) {
		StringBuilder sb = new StringBuilder();
		sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(comment.getCreateDate()));
		return sb.toString();
	}

	private static String getAuthorLabel(Comment comment) {
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


	private SimpleColoredComponent getSingleLineComponent(Comment vc, boolean isSelected) {
		final SimpleColoredComponent res = new SimpleColoredComponent();
		res.setOpaque(false);
		final String lineInfoLabel = getLineInfoLabel(vc);
		if (lineInfoLabel.length() > 0) {
			res.append("(" + lineInfoLabel + ") ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
		}
		final String message = StringUtil.getFirstLine(comment.getMessage());
		res.append(message + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		res.append(" " + getRankingString(vc), isSelected
				? SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES);
		if (comment.isDraft()) {
			StringBuilder drafInfo = new StringBuilder();
			drafInfo.append(" ");
			drafInfo.append("Draft");
			res.append(drafInfo.toString(), isSelected
					? SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES);
		}
		return res;
	}

	private static JTextPane createMessageBody(Comment vc, boolean isSelected) {
		JTextPane pane = new JTextPane();
		pane.setOpaque(false);
		final StyledDocument doc = pane.getStyledDocument();
		addStylesToDocument(doc);
		try {
			final String lineInfoLabel = getLineInfoLabel(vc);
			if (lineInfoLabel.length() > 0) {
				doc.insertString(doc.getLength(), "(" + lineInfoLabel + ") ", doc.getStyle("line"));
			}
			doc.insertString(doc.getLength(), vc.getMessage() + " ", doc.getStyle("regular"));
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
		return pane;
	}

	private static void addStylesToDocument(StyledDocument doc) {
		//Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("defect", regular);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);

		s = doc.addStyle("defect-selected", regular);

		s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

		s = doc.addStyle("draft", regular);
//		StyleConstants.setBold(s, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);

		s = doc.addStyle("draft-selected", regular);

		s.addAttribute(StyleConstants.ColorConstants.Foreground, UIUtil.getTreeSelectionForeground());

		s = doc.addStyle("line", regular);
		StyleConstants.setBold(s, true);
	}
}

