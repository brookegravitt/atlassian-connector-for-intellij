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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomField;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewerBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class ReviewCommentRenderer  extends DefaultTreeCellRenderer implements TreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, final boolean isSelected, boolean expanded,
			boolean leaf, int row, boolean aHasFocus) {
		if (value instanceof VersionedCommentTreeNode) {
			final VersionedCommentTreeNode node = (VersionedCommentTreeNode) value;

			return new CommentPanel(node.getFile(), node.getComment(), getAvailableWidth(node, tree), row,
					new SimpleIconProvider(), node.isExpanded(), isSelected);
		} else if (value instanceof GeneralCommentTreeNode) {
			final GeneralCommentTreeNode node = (GeneralCommentTreeNode) value;
			return new CommentPanel(null, node.getComment(), getAvailableWidth(node, tree), row, new SimpleIconProvider(),
					node.isExpanded(), isSelected);
		} else {
			return super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, aHasFocus);
		}


	}

	private int getAvailableWidth(DefaultMutableTreeNode obj, JTree jtree)
	{
		int i1 = jtree.getInsets().left + jtree.getInsets().right + getNesting(jtree) * obj.getLevel();
		return jtree.getVisibleRect().width - i1 - 2;
	}

	private int getNesting(JTree jtree)
	{
		TreeUI treeui = jtree.getUI();
		if(treeui instanceof BasicTreeUI)
		{
			BasicTreeUI basictreeui = (BasicTreeUI)treeui;
			return basictreeui.getLeftChildIndent() + basictreeui.getRightChildIndent();
		} else
		{
			return (Integer) UIUtil.getTreeLeftChildIndent() + (Integer) UIUtil.getTreeRightChildIndent();
		}
	}


	public static void main(String[] args) throws ValueNotYetInitialized {
		ReviewCommentRenderer renderer = new ReviewCommentRenderer();
		CrucibleServerCfg cruc = new CrucibleServerCfg("my crucible server", new ServerId());
		ReviewBean review = new ReviewBean("myreviewbean");
		ReviewAdapter reviewAdapter = new ReviewAdapter(review, cruc);
		VersionedVirtualFile vvf1 = new VersionedVirtualFile("mypath", "1.342");
		VersionedVirtualFile vvf2 = new VersionedVirtualFile("mypath", "1.567");
		CrucibleFileInfo crucibleFileInfo = new CrucibleFileInfoImpl(vvf1, vvf2, new PermIdBean("mypermid"));

		final VersionedCommentBean versionedCommentBean = new VersionedCommentBean();
		versionedCommentBean.setMessage("my beautiful message");
		final ReviewerBean author = new ReviewerBean();
		author.setUserName("wseliga");
		author.setDisplayName("Wojciech Seliga");
		versionedCommentBean.setAuthor(author);

		final VersionedCommentBean versionedCommentBean2 = new VersionedCommentBean();
		versionedCommentBean2.setMessage("my very very very beautiful but annoyingly very very long long long long long comment."
				+ "Let us check if it wraps correctly \nWe have also another line here\n\nThere is also an empty line above");
		versionedCommentBean2.setToEndLine(31);
		final ReviewerBean author2 = new ReviewerBean();
		author2.setUserName("mwent");
		author2.setDisplayName("Marek Went Long Lastname");
		versionedCommentBean2.setAuthor(author2);
		final CustomFieldBean customFieldBean = new CustomFieldBean();
		customFieldBean.setValue("Major");
		versionedCommentBean2.getCustomFields().put("Rank", customFieldBean);
		final CustomFieldBean customFieldBean2 = new CustomFieldBean();
		customFieldBean2.setValue("Missing");
		versionedCommentBean2.getCustomFields().put("Classification", customFieldBean2);
		versionedCommentBean2.setToStartLine(171);
		versionedCommentBean2.setToEndLine(0);
		versionedCommentBean2.setToLineInfo(true);
		versionedCommentBean2.setDraft(true);

		final VersionedCommentBean versionedCommentBean3 = new VersionedCommentBean();
		versionedCommentBean3.setMessage("Another comment. Let us see if it wraps corrent.\nAnd if empty lines work fine\n"
				+ "This statement sucks:\n\tif (false) {\n\t\t...\n\t}");
		versionedCommentBean3.setToStartLine(21);
		versionedCommentBean3.setToEndLine(131);
		versionedCommentBean3.setToLineInfo(true);
		final ReviewerBean author3 = new ReviewerBean();
		author3.setUserName("ewong");
		author3.setDisplayName("Edwin Wong");
		versionedCommentBean3.setAuthor(author3);
		versionedCommentBean3.setDefectRaised(true);


		crucibleFileInfo.addComment(versionedCommentBean);
		crucibleFileInfo.addComment(versionedCommentBean2);
		review.setFiles(Collections.singleton(crucibleFileInfo));
		final VersionedCommentTreeNode n1 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean,
				null);
		final VersionedCommentTreeNode n2 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean2,
				null);
		final VersionedCommentTreeNode n3 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean3,
				null);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		GeneralCommentBean generalComment = new GeneralCommentBean();
		generalComment.setAuthor(author3);
		generalComment.setMessage("This is general comment for this review. Should be quite lengthy");
		GeneralCommentTreeNode generalCommentTreeNode = new GeneralCommentTreeNode(reviewAdapter, generalComment, null);
		root.add(generalCommentTreeNode);
		root.add(n1);
		root.add(n2);
		n2.setExpanded(true);
		DefaultMutableTreeNode nestedChild = new DefaultMutableTreeNode();
		DefaultMutableTreeNode nestedChild2 = new DefaultMutableTreeNode();
		nestedChild.add(nestedChild2);
		nestedChild2.add(n3);
		root.add(nestedChild);

		final JTree jtree = new JTree(root);
		jtree.setCellRenderer(renderer);
		jtree.addMouseMotionListener(new MouseAdapter() {

			private Cursor cursor;
			@Override
			public void mouseMoved(final MouseEvent e) {
				final int row = jtree.getRowForLocation(e.getX(), e.getY());
				final Rectangle bounds = jtree.getRowBounds(row);
				if (bounds != null && e.getX() > bounds.x + bounds.width - CommentPanel.LAST_COLUMN_WIDTH - 65
						&& e.getX() < bounds.x + bounds.width - CommentPanel.LAST_COLUMN_WIDTH - 30
						&& e.getY() > bounds.y && e.getY() < bounds.y + 15) {
					if (cursor == null) {
						cursor = jtree.getCursor();
					}
					jtree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					if (cursor != null) {
						jtree.setCursor(cursor);
					}
				}
			}
		});

		TreeUISetup buildTreeUiSetup = new TreeUISetup(renderer);
		final JScrollPane parentScrollPane = new JScrollPane(jtree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		buildTreeUiSetup.initializeUI(jtree, parentScrollPane);

//		final JComponent component = renderer.getTreeCellRendererComponent(null, null, false, true, true, 0, false);
		SwingAppRunner.run(parentScrollPane);
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

	protected ImageIcon createImageIcon(String path,
											   String description) {
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

	private static final CellConstraints DEFECT_ICON_POS = new CellConstraints(4, 2);
	private static final int OTHER_COLUMNS_WIDTH = 34;

	private static int getPreferredHeight(JComponent component, int preferredWidth) {
		try {
			component.addNotify();
			component.doLayout();
//			component.getToolkit().cre
			component.setSize(preferredWidth, Integer.MAX_VALUE);
			return component.getPreferredSize().height;
		} finally {
			component.removeNotify();
		}
	}

	static final int LAST_COLUMN_WIDTH = 250;

	private boolean isFullyDisplayed;

	public CommentPanel(@Nullable CrucibleFileInfo file, Comment comment, int width, final int row, IconProvider iconProvider,
			boolean isExpanded, final boolean isSelected) {
//		super(new FormLayout("d:grow, 4dlu, 10dlu, 4dlu, right:d, 2dlu", "2dlu, top:pref:grow, 2dlu"));
		super(new FormLayout("d:grow, d, 5px, 16px, 5px, right:"
				+ LAST_COLUMN_WIDTH + "px" + ", 4px", "4px, top:pref:grow, 2dlu"));
		if (isSelected) {
			setOpaque(true);
			setBackground(UIUtil.getTreeSelectionBackground());
		} else {
			setOpaque(false);
		}


		this.file = file;
		this.comment = comment;

		JTextPane messageBody = createMessageBody();

		int queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - LAST_COLUMN_WIDTH, 100);
		int preferredHeight = getPreferredHeight(messageBody, queryWidth);
		CellConstraints cc = new CellConstraints();
		if (preferredHeight > 20) {
			final JLabel moreLabel = new JLabel("<html><a href='#'>" + (isExpanded ? "less" : "more") + "</a>");
			add(moreLabel, cc.xy(2, 2));
			isFullyDisplayed = false;
			queryWidth = Math.max(width - OTHER_COLUMNS_WIDTH - moreLabel.getPreferredSize().width - LAST_COLUMN_WIDTH, 100);
			preferredHeight = getPreferredHeight(messageBody, queryWidth);
		} else {
			isFullyDisplayed = true;
		}

		messageBody.setPreferredSize(new Dimension(queryWidth, preferredHeight));
//		System.out.println(messageBody.getPreferredSize());

		add(messageBody, cc.xy(1, 2));

		if (comment.isDefectRaised()) {
			Icon myicon = iconProvider.getIcon("/icons/icn_plan_failed.gif");
			JLabel icon = new JLabel(myicon);
			add(icon, DEFECT_ICON_POS);
		}


		JLabel reviewer = new JLabel(getAuthorLabel() + " , " + getDateLabel());
//		reviewer.setMinimumSize(new Dimension(0, 0));
		add(reviewer, cc.xy(6, 2));

		validate();
//		int preferredHeight = getPreferredHeight(this, width);
//		setSize(new Dimension(width, preferredHeight));
//		setPreferredSize(new Dimension(width, preferredHeight));
//		System.out.println(getPreferredSize());
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
//				txt2 += " Revision " + file.getFileDescriptor().getRevision();
//				txt2 += ": ";
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

	protected JTextPane createMessageBody() {

		JLabel result = new JLabel();
		result.setText("<html>" + comment.getMessage());
//		result.setLineWrap(true);
//		result.setWrapStyleWord(true);
//		result.setBackground(getBodyBackground());
//		return result;
//
//		JTextArea result = new JTextArea();
//		result.setText(comment.getMessage());
//		result.setLineWrap(true);
//		result.setWrapStyleWord(true);
//		result.setBackground(getBodyBackground());
//		return result;
		JTextPane pane = new JTextPane();
		pane.setOpaque(false);
//		pane.setBackground(Color.PINK);
		pane.setBorder(BorderFactory.createEmptyBorder());


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
				Style def = StyleContext.getDefaultStyleContext().
								getStyle(StyleContext.DEFAULT_STYLE);

				Style regular = doc.addStyle("regular", def);
				StyleConstants.setFontFamily(def, "SansSerif");


				Style s = doc.addStyle("defect", regular);
//				StyleConstants.setItalic(s, true);
				s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.GRAY);

				s = doc.addStyle("draft", regular);
				StyleConstants.setBold(s, true);
				s.addAttribute(StyleConstants.ColorConstants.Foreground, Color.BLACK);

				s = doc.addStyle("line", regular);
				StyleConstants.setBold(s, true);
//
//				s = doc.addStyle("icon", regular);
//				StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
//				ImageIcon pigIcon = createImageIcon("images/Pig.gif",
//													"a cute pig");
//				if (pigIcon != null) {
//					StyleConstants.setIcon(s, pigIcon);
//				}
//
//				s = doc.addStyle("button", regular);
//				StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
//				ImageIcon soundIcon = createImageIcon("images/sound.gif",
//													  "sound icon");
//				JButton button = new JButton();
//				if (soundIcon != null) {
//					button.setIcon(soundIcon);
//				} else {
//					button.setText("BEEP");
//				}
//				button.setCursor(Cursor.getDefaultCursor());
//				button.setMargin(new Insets(0,0,0,0));
//				button.setActionCommand(buttonString);
//				button.addActionListener(this);
//				StyleConstants.setComponent(s, button);
	}
}

