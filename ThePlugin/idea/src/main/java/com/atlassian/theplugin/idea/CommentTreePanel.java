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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.SectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Date;

import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 1:26:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentTreePanel extends JPanel {
	private CrucibleReviewActionListener crucibleAgent = new MyCrucibleReviewActionListener();
	private JScrollPane commentScroll;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CommentTree commentTree = new CommentTree();

	private static final AtlassianTreeNode ROOT = new SectionNode("root", AtlassianClickAction.EMPTY_ACTION);
	private static final String GENERAL_COMMENTS_SECTION = "General comments";
	private Project project;

	public CommentTreePanel(Project project) {
		super();
		this.project = project;
		IdeaHelper.getReviewActionEventBroker().registerListener(crucibleAgent);
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		progressAnimation.configure(this, commentScroll, BorderLayout.CENTER);
		commentScroll = new JScrollPane();
		add(commentScroll, BorderLayout.CENTER);
	}

	private void addGeneralCommentTree(AtlassianTreeNode root, final ReviewData review, GeneralComment generalComment, int depth) {
		AtlassianClickAction clickAction;
		if (depth == 0) {
			clickAction = new AtlassianClickAction() {
				public void execute(AtlassianTreeNode node, int noOfClicks) {
					GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
					switch (noOfClicks) {
						case 2:
							EditCommentDialog dialog = new EditCommentDialog();
							dialog.pack();
							dialog.setModal(true);
							dialog.show();
							if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
								GeneralComment parentComment = vnode.getComment();
								GeneralCommentBean newComment = new GeneralCommentBean();
								newComment.setCreateDate(new Date());
								newComment.setDefectRaised(dialog.isDraft());
								newComment.setMessage(dialog.getMessage());
								newComment.setUser(new UserBean(review.getServer().getUserName()));
								newComment.setReply(true);
								newComment.setDefectApproved(false);
								newComment.setDeleted(false);
								IdeaHelper.getReviewActionEventBroker().trigger(
										new GeneralCommentReplyAboutToAdd(crucibleAgent,
												review, parentComment, newComment));
							}
							break;
						default:

					}
				}
			};
		} else {
			clickAction = AtlassianClickAction.EMPTY_ACTION;
		}
		GeneralCommentTreeNode commentNode = new GeneralCommentTreeNode(review, generalComment, clickAction);
		root.addNode(commentNode);
		for (GeneralComment comment : generalComment.getReplies()) {
			addGeneralCommentTree(commentNode, review, comment, depth + 1);
		}
	}

	private void addVersionedCommentTree(AtlassianTreeNode root, final ReviewData review,
										 final CrucibleFileInfo file, VersionedComment versionedComment,
										 int depth) {

		AtlassianClickAction clickAction;
		if (depth == 0) {
			clickAction = new AtlassianClickAction() {
				public void execute(AtlassianTreeNode node, int noOfClicks) {
					VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
					switch (noOfClicks) {
						case 2:
							EditCommentDialog dialog = new EditCommentDialog();
							dialog.pack();
							dialog.setModal(true);
							dialog.show();
							if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
								VersionedComment parentComment = vnode.getComment();
								VersionedCommentBean newComment = new VersionedCommentBean();
								newComment.setFromLineInfo(parentComment.isFromLineInfo());
								newComment.setFromStartLine(parentComment.getFromStartLine());
								newComment.setFromEndLine(parentComment.getFromEndLine());
								newComment.setToLineInfo(parentComment.isToLineInfo());
								newComment.setToStartLine(parentComment.getToStartLine());
								newComment.setToEndLine(parentComment.getToEndLine());
								newComment.setCreateDate(new Date());
								newComment.setReviewItemId(review.getPermId());
								newComment.setDefectRaised(dialog.isDraft());
								newComment.setMessage(dialog.getMessage());
								newComment.setUser(new UserBean(review.getServer().getUserName()));
								newComment.setReply(true);
								newComment.setDefectApproved(false);
								newComment.setDeleted(false);
								IdeaHelper.getReviewActionEventBroker().trigger(
										new VersionedCommentReplyAboutToAdd(crucibleAgent,
												review, file, parentComment, newComment));
							}
							break;
						default:

					}
				}
			};
		} else {
			clickAction = AtlassianClickAction.EMPTY_ACTION;
		}

		VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, versionedComment,
				clickAction);
		root.addNode(commentNode);
		for (VersionedComment comment : versionedComment.getReplies()) {
			addVersionedCommentTree(commentNode, review, file, comment, depth + 1);
		}
	}

	private AtlassianTreeModel createTreeModel(final ReviewData review) {
		ROOT.removeAllChildren();
		AtlassianTreeModel model = new AtlassianTreeModel(ROOT);

		List<GeneralComment> generalComments;
		try {
			generalComments = review.getGeneralComments();
			AtlassianTreeNode generalNode = new SectionNode(GENERAL_COMMENTS_SECTION, createGeneralCommentAction(review));
			ROOT.addNode(generalNode);
			for (GeneralComment comment : generalComments) {
				addGeneralCommentTree(generalNode, review, comment, 0);
			}
			for (CrucibleFileInfo file : review.getFiles()) {
				AtlassianTreeNode fileNode = new FileNameNode(file, AtlassianClickAction.EMPTY_ACTION);
				ROOT.addNode(fileNode);
				for (VersionedComment comment : file.getVersionedComments()) {
					addVersionedCommentTree(fileNode, review, file, comment, 0);
				}
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// ignore
		}
		return model;
	}

	private AtlassianClickAction createGeneralCommentAction(final ReviewData review) {
		return new AtlassianClickAction() {
				public void execute(AtlassianTreeNode node, int noOfClicks) {
					switch (noOfClicks) {
						case 2:
							EditCommentDialog dialog = new EditCommentDialog();
							dialog.pack();
							dialog.setModal(true);
							dialog.show();
							if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
								GeneralCommentBean newComment = new GeneralCommentBean();
								newComment.setCreateDate(new Date());
								newComment.setDefectRaised(dialog.isDraft());
								newComment.setMessage(dialog.getMessage());
								newComment.setUser(new UserBean(review.getServer().getUserName()));
								newComment.setReply(true);
								newComment.setDefectApproved(false);
								newComment.setDeleted(false);
								IdeaHelper.getReviewActionEventBroker().trigger(
										new GeneralCommentAboutToAdd(crucibleAgent,
												review, newComment));
							}
							break;
						default:

					}
				}
			};
	}

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {

		@Override
		public void showReview(final ReviewData review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					commentTree.setVisible(false);
					commentTree = new CommentTree(createTreeModel(review));
					commentTree.expandAll();
					commentScroll.setViewportView(commentTree);
					commentTree.initializeUI();
					commentTree.setVisible(true);
					commentTree.setEnabled(true);
					commentTree.revalidate();
					commentTree.repaint();
				}
			});
		}

		@Override
		public void focusOnGeneralComments(final ReviewData review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = locateGeneralTreeNode(review);
					commentTree.focusOnNode(node);
				}
			});
		}

		@Override
		public void createdVersionedCommentReply(final ReviewData review, final CrucibleFileInfo file, final VersionedComment parentComment, final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = locateVersionedCommentNode(review, file, parentComment);
					if (node != null) {
						node.addNode(new VersionedCommentTreeNode(review, file, comment, AtlassianClickAction.EMPTY_ACTION));
					}
				}


			});
		}

		private AtlassianTreeNode locateVersionedCommentNode(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
			for (int i = 0; i < commentTree.getRowCount(); i++) {
				TreePath path = commentTree.getPathForRow(i);

				AtlassianTreeNode elem = (AtlassianTreeNode) path.getLastPathComponent();
				if (elem instanceof VersionedCommentTreeNode) {
					VersionedCommentTreeNode node = (VersionedCommentTreeNode) elem;
					if (node.getComment().equals(comment)) {
						return node;
					}
				}
			}
			return null;  //To change body of created methods use File | Settings | File Templates.

		}

		private AtlassianTreeNode locateGeneralTreeNode(ReviewData review) {
			for (int i = 0; i < commentTree.getRowCount(); i++) {
				TreePath path = commentTree.getPathForRow(i);

				AtlassianTreeNode elem = (AtlassianTreeNode) path.getLastPathComponent();
				if (elem instanceof SectionNode) {
					SectionNode node = (SectionNode) elem;
					if (node.getSectionName().equals(GENERAL_COMMENTS_SECTION)) {
						return node;
					}
				}
			}
			return null;  //To change body of created methods use File | Settings | File Templates.
		}

		@Override
		public void focusOnFileComments(final ReviewData review, final CrucibleFileInfo file) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode node = locateFileTreeNode(review, file);
					commentTree.focusOnNode(node);
				}
			});
		}

		private AtlassianTreeNode locateFileTreeNode(ReviewData review, CrucibleFileInfo file) {
			for (int i = 0; i < commentTree.getRowCount(); i++) {
				TreePath path = commentTree.getPathForRow(i);

				AtlassianTreeNode elem = (AtlassianTreeNode) path.getLastPathComponent();
				if (elem instanceof FileNameNode) {
					FileNameNode node = (FileNameNode) elem;
					if (node.getFile().equals(file)) {
						return node;
					}
				}
			}
			return null;  //To change body of created methods use File | Settings | File Templates.
		}
	}

	private class EditCommentDialog extends DialogWrapper {
		private final JTextArea area = new JTextArea(10, 40);
		private Action draftAction = new AbstractAction() {
			{
			  putValue(Action.NAME, "Save as Draft");
			}
			public void actionPerformed(ActionEvent e) {
			  if (myPerformAction) return;
			  try {
				myPerformAction = true;
				doOKAction();
			  }
			  finally {
				myPerformAction = false;
			  }
			}
		};
		private Action myOkAction = new AbstractAction() {
			{
			  putValue(Action.NAME, "Post");
			}


			public void actionPerformed(ActionEvent e) {
				getOKAction().actionPerformed(e);
			}

		};

		Checkbox defect = new Checkbox("Defect");
		private boolean isDraft = false;

		public EditCommentDialog() {
			super(CommentTreePanel.this.project, false);
			init();
		}

		@Nullable
		protected JComponent createCenterPanel() {
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel("Your reply:"), BorderLayout.NORTH);
			panel.add(area, BorderLayout.CENTER);
			return panel;
//			JPanel panel = new JPanel(new FormLayout(
//					"4dlu, left:pref, 4dlu, pref, 4dlu, pref:grow, 4dlu",
//					"4dlu, pref:grow, 4dlu, pref, 4dlu"));
//			CellConstraints cc = new CellConstraints();
//			area.setPreferredSize(new Dimension(400, 200));
//			panel.add(area, cc.xyw(2, 1, 5));
//			panel.add(defect, cc.xy(2, 2));
//			panel.add(builder.getPanel(), cc.xy(4, 2));
//			return panel;
		}

		

		@Override
		@Nullable
		protected JComponent createSouthPanel() {
			JPanel panel = new JPanel(new FormLayout(
					"4dlu, left:pref, 4dlu, pref, 4dlu, pref:grow, 4dlu",
					"4dlu, pref:grow, 4dlu"));
			ButtonBarBuilder builder = new ButtonBarBuilder();

			JButton cancel = new JButton("Cancel");
			cancel.setAction(getCancelAction());
			JButton post = new JButton("Post");
			post.setAction(myOkAction);
			JButton draft = new JButton("Save as Draft");
			draft.setAction(getDraftAction());
			builder.addGriddedButtons(new JButton[]{cancel, draft, post});

			CellConstraints cc = new CellConstraints();
			panel.add(defect, cc.xy(2, 2));
			panel.add(builder.getPanel(), cc.xy(4, 2));
			return panel;
		}

		@Override
		protected Action[] createActions() {
			return new Action[0];
		}

		public Action getDraftAction() {
			return draftAction;
		}

		public boolean getIsDefect() {
			return defect.getState();
		}

		public String getMessage() {
			return area.getText();
		}

		public void setIsDraft(boolean draft) {
			isDraft = draft;
		}

		public boolean isDraft() {
			return isDraft;
		}
	}
}
