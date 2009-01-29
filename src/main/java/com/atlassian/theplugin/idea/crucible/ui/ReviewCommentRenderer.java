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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, final boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean aHasFocus) {
		if (value instanceof VersionedCommentTreeNode) {
			final VersionedCommentTreeNode node = (VersionedCommentTreeNode) value;

			return new CommentPanel(node.getFile(), node.getComment(), getAvailableWidth(node, tree), row,
					new SimpleIconProvider(), node.isExpanded(), isSelected);
		} else if (value instanceof GeneralCommentTreeNode) {
			final GeneralCommentTreeNode node = (GeneralCommentTreeNode) value;
			// @todo wseliga inject here IdeaIconProvider
			return new CommentPanel(null, node.getComment(), getAvailableWidth(node, tree), row,
					new SimpleIconProvider(), node.isExpanded(), isSelected);
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

interface IconProvider {
	@Nullable
	Icon getIcon(@NotNull String path);
}

class SimpleIconProvider implements IconProvider {
	@Nullable
	public Icon getIcon(@NotNull final String path) {
		return createImageIcon(path, "");
	}

	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}

class CommentPanel extends JPanel {
	private Comment comment;
	private CrucibleFileInfo file;

	private static final CellConstraints MORE_LINK_POS = new CellConstraints(3, 2);
	private static final CellConstraints DEFECT_ICON_POS = new CellConstraints(5, 2);
	private static final CellConstraints AUTHOR_POS = new CellConstraints(7, 2);
	private static final int OTHER_COLUMNS_WIDTH = 34;
	private Rectangle moreBounds;
	private static final int MIN_TEXT_WIDTH = 100;
	private static final int ONE_LINE_HEIGHT = 25;

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

	@SuppressWarnings({"UnusedDeclaration"})
	public CommentPanel(@Nullable CrucibleFileInfo file, Comment comment, int width, final int row, IconProvider iconProvider,
			boolean isExpanded, final boolean isSelected) {
		super(new FormLayout("max(d;" + MIN_TEXT_WIDTH + "px):grow, 2dlu, d, 5px, 16px, 5px, right:"
				+ LAST_COLUMN_WIDTH + "px" + ", 4px", "4px, top:pref:grow, 2dlu"));
		this.file = file;
		this.comment = comment;
		if (isSelected) {
			setOpaque(true);
			setBackground(UIUtil.getTreeSelectionBackground());
		} else {
			setOpaque(false);
		}

		JTextPane messageBody = createMessageBody();

		int queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - LAST_COLUMN_WIDTH, MIN_TEXT_WIDTH);
		int preferredHeight = getPreferredHeight(messageBody, queryWidth);
		CellConstraints cc = new CellConstraints();
		final JLabel moreLabel;
		if (preferredHeight > ONE_LINE_HEIGHT) {
			moreLabel = new JLabel("<html><a href='#'>" + (isExpanded ? "less" : "more") + "</a>");
			add(moreLabel, MORE_LINK_POS);
			queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - moreLabel.getPreferredSize().width - LAST_COLUMN_WIDTH,
					MIN_TEXT_WIDTH);
			preferredHeight = getPreferredHeight(messageBody, queryWidth);
		} else {
			moreLabel = null;
		}

		if (preferredHeight < ONE_LINE_HEIGHT || isExpanded) {
//			queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - moreLabel.getPreferredSize().width - LAST_COLUMN_WIDTH, 100);
//			preferredHeight = getPreferredHeight(messageBody, queryWidth);
			add(messageBody, cc.xy(1, 2));
			messageBody.setPreferredSize(new Dimension(queryWidth, preferredHeight));
		} else {
			final SimpleColoredComponent jLabel = getSingleLineComponent();
			jLabel.setMinimumSize(new Dimension(0, 0));
			add(jLabel, cc.xy(1, 2));
		}


		if (comment.isDefectRaised()) {
			Icon myicon = iconProvider.getIcon("/icons/icn_plan_failed.gif");
			JLabel icon = new JLabel(myicon);
			add(icon, DEFECT_ICON_POS);
		}

		JLabel reviewer = new JLabel(getAuthorLabel() + " , " + getDateLabel());
		add(reviewer, AUTHOR_POS);

		validate();
		setSize(new Dimension(width, Integer.MAX_VALUE));
		addNotify();
		doLayout();
		if (moreLabel != null) {
			moreBounds = moreLabel.getBounds();
		}
	}


	@NotNull
	private String getDateLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(comment.getCreateDate()));
		return sb.toString();
	}

	protected String getAuthorLabel() {
		return "".equals(comment.getAuthor().getDisplayName()) ? comment.getAuthor().getUserName()
				: comment.getAuthor().getDisplayName();
	}

	protected String getLineInfoLabel() {
		if (file != null) {
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
			if (comment.isReply()) {
				return "Reply";
			}

			if (!comment.isReply()) {
				return "General File";
			}
		}
		return "General Comment";
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


	protected String getRankingString() {
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

	private SimpleColoredComponent getSingleLineComponent() {
		final SimpleColoredComponent res = new SimpleColoredComponent();
		res.setOpaque(false);
		final String lineInfoLabel = getLineInfoLabel();
		if (lineInfoLabel.length() > 0) {
			res.append("(" + lineInfoLabel + ") ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
		}
		res.append(comment.getMessage() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		res.append(" " + getRankingString(), SimpleTextAttributes.GRAY_ATTRIBUTES);
		if (comment.isDraft()) {
			StringBuilder drafInfo = new StringBuilder();
			drafInfo.append(" ");
			drafInfo.append("Draft");
			res.append(drafInfo.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES);
		}
		return res;
	}

	protected JTextPane createMessageBody() {

		JTextPane pane = new JTextPane();
		pane.setOpaque(false);

		final StyledDocument doc = pane.getStyledDocument();
		addStylesToDocument(doc);
		try {
			final String lineInfoLabel = getLineInfoLabel();
			if (lineInfoLabel.length() > 0) {
				doc.insertString(doc.getLength(), "(" + lineInfoLabel + ") ", doc.getStyle("line"));
			}
			doc.insertString(doc.getLength(), comment.getMessage() + " ", doc.getStyle("regular"));
			doc.insertString(doc.getLength(), " " + getRankingString(), doc.getStyle("defect"));
			if (comment.isDraft()) {
				StringBuilder drafInfo = new StringBuilder();
				if (doc.getLength() > 0) {
					drafInfo.append(" ");
				}
				drafInfo.append("Draft");
				doc.insertString(doc.getLength(), drafInfo.toString(), doc.getStyle("draft"));
			}
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
		return pane;
	}

	private void addStylesToDocument(StyledDocument doc) {
		//Initialize some styles.
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("defect", regular);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);

		s = doc.addStyle("draft", regular);
		StyleConstants.setBold(s, true);
		s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.BLACK);

		s = doc.addStyle("line", regular);
		StyleConstants.setBold(s, true);
	}
}

