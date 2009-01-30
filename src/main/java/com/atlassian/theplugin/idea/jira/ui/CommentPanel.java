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
package com.atlassian.theplugin.idea.jira.ui;

import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


class CommentPanel extends JPanel {
	private JIRAComment comment;

	private static final CellConstraints MORE_LINK_POS = new CellConstraints(4, 1);
	private static final CellConstraints AUTHOR_POS = new CellConstraints(6, 1);
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

	public CommentPanel(IssueCommentTreeNode node, int width) {

		comment = node.getComment();
		boolean isExpanded = node.isExpanded();

		final JLabel authorAndDateLabel =
				new JLabel("<html><b>" + getAuthorLabel(comment) + "</b>, " + getDateLabel(comment));
		final int lastColumnWidth = getLastColumnWidth(width, authorAndDateLabel.getPreferredSize().width);
		setLayout(new FormLayout(
				"4px, max(d;" + MIN_TEXT_WIDTH + "px):grow, "
				+ "2dlu, d, 2dlu, "
				+ "right:" + lastColumnWidth + "px, "
				+ "4px",
				"top:pref:grow"));
		setOpaque(false);
		
		final JTextPane messageBody = createMessageBody(comment);
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
			add(messageBody, cc.xy(2, 1));
			messageBody.setPreferredSize(new Dimension(queryWidth, preferredHeight));
		} else {
			final SimpleColoredComponent jLabel = getSingleLineComponent();
			jLabel.setMinimumSize(new Dimension(0, 0));
			add(jLabel, cc.xy(2, 1));
		}

		add(authorAndDateLabel, AUTHOR_POS);

		validate();
		setSize(new Dimension(width, Integer.MAX_VALUE));
		addNotify();
		doLayout();
		if (moreLabel != null) {
			moreBounds = moreLabel.getBounds();
		}
	}

	@NotNull
	private static String getDateLabel(JIRAComment comment) {
		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
		DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String t;
		try {
			t = dfo.format(df.parse(comment.getCreationDate().getTime().toString()));
		} catch (java.text.ParseException e) {
			t = "Invalid date: " + comment.getCreationDate().getTime().toString();
		}
		return t;
	}

	private static String getAuthorLabel(JIRAComment comment) {
		return comment.getAuthorFullName();
	}

	private SimpleColoredComponent getSingleLineComponent() {
		final SimpleColoredComponent res = new SimpleColoredComponent();
		res.setOpaque(false);
		final String message = StringUtil.getFirstLine(comment.getBody());
		res.append(message + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		return res;
	}

	private static JTextPane createMessageBody(JIRAComment comment) {
		JTextPane pane = new JTextPane();
		pane.setOpaque(false);
		final StyledDocument doc = pane.getStyledDocument();
		addStylesToDocument(doc);
		try {
			doc.insertString(doc.getLength(), comment.getBody() + " ", doc.getStyle("regular"));
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

		Style s = doc.addStyle("line", regular);
		StyleConstants.setBold(s, true);
	}
}