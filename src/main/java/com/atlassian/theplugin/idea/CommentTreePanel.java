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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

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

	private static final AtlassianTreeNode ROOT = new FileNode("/", AtlassianClickAction.EMPTY_ACTION);
	private Project project;

	public CommentTreePanel(Project project) {
		this.project = project;
		IdeaHelper.getReviewActionEventBroker().registerListener(crucibleAgent);
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		progressAnimation.configure(this, commentScroll, BorderLayout.CENTER);
		commentScroll = new JScrollPane();
		add(AtlassianToolbar.createToolbar("comment", "ThePlugin.Crucible.Comment.ToolBar"), BorderLayout.NORTH);
		add(commentScroll, BorderLayout.CENTER);
	}

	private void addGeneralCommentTree(AtlassianTreeNode root, final ReviewData review,
									   GeneralComment generalComment, int depth) {
		GeneralCommentTreeNode commentNode
                = new GeneralCommentTreeNode(review, generalComment, AtlassianClickAction.EMPTY_ACTION);
		root.addNode(commentNode);
		for (GeneralComment comment : generalComment.getReplies()) {
			addGeneralCommentTree(commentNode, review, comment, depth + 1);
		}
	}

	private void addVersionedCommentTree(AtlassianTreeNode root, final ReviewData review,
            final CrucibleFileInfo file, VersionedComment versionedComment, int depth) {
		VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, versionedComment,
				AtlassianClickAction.EMPTY_ACTION);
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
			AtlassianTreeNode generalNode = new GeneralSectionNode(review, AtlassianClickAction.EMPTY_ACTION);
			ROOT.addNode(generalNode);
			for (GeneralComment comment : generalComments) {
				addGeneralCommentTree(generalNode, review, comment, 0);
			}
			for (CrucibleFileInfo file : review.getFiles()) {
				AtlassianTreeNode fileNode = new FileNameNode(review, file, AtlassianClickAction.EMPTY_ACTION);
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
					TreeModel model = commentTree.getModel();
					AtlassianTreeNode root = (AtlassianTreeNode) model.getRoot();
					AtlassianTreeNode node = locateNode(root, new NodeSearchAlgorithm() {
						@Override
                        public boolean check(AtlassianTreeNode node1) {
							return node1 instanceof GeneralSectionNode;
						}
					});
					commentTree.focusOnNode(node);
				}
			});
		}

		@Override
		public void createdGeneralComment(final ReviewData review, final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					AtlassianTreeNode newCommentNode
                            = new GeneralCommentTreeNode(review, comment, AtlassianClickAction.EMPTY_ACTION);
					addNewNode(new NodeSearchAlgorithm() {
						@Override
                        public boolean check(AtlassianTreeNode node) {
							return node instanceof GeneralSectionNode;
						}
					}, newCommentNode);
				}
			}
			);
		}

		@Override
		public void createdGeneralCommentReply(final ReviewData review, final GeneralComment parentComment,
                final GeneralComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					addNewNode(new NodeSearchAlgorithm() {
						@Override
                        public boolean check(AtlassianTreeNode node) {
							if (node instanceof GeneralCommentTreeNode) {
								GeneralCommentTreeNode vnode = (GeneralCommentTreeNode) node;
								if (vnode.getReview().equals(review) && vnode.getComment().equals(parentComment)) {
									return true;
								}
							}
							return false;
						}
					}, new GeneralCommentTreeNode(review, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			}
			);
		}

		@Override
		public void createdVersionedComment(final ReviewData review, final CrucibleFileInfo file,
                final VersionedComment comment) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					addNewNode(new NodeSearchAlgorithm() {
						@Override
                        public boolean check(AtlassianTreeNode node) {
							if (node instanceof FileNameNode) {
								FileNameNode vnode = (FileNameNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getFile().equals(file)) {
									return true;
								}
							}
							return false;
						}
					}, new VersionedCommentTreeNode(review, file, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			});
		}

		@Override
		public void createdVersionedCommentReply(final ReviewData review, final CrucibleFileInfo file,
												 final VersionedComment parentComment, final VersionedComment comment) {

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					addNewNode(new NodeSearchAlgorithm() {
						@Override
                        public boolean check(AtlassianTreeNode node) {
							if (node instanceof VersionedCommentTreeNode) {
								VersionedCommentTreeNode vnode = (VersionedCommentTreeNode) node;
								if (vnode.getReview().equals(review)
										&& vnode.getFile().equals(file)
										&& vnode.getComment().equals(parentComment)) {
									return true;
								}
							}
							return false;
						}
					}, new VersionedCommentTreeNode(review, file, comment, AtlassianClickAction.EMPTY_ACTION));
				}
			});
		}

		private void addNewNode(NodeSearchAlgorithm parentLocator, AtlassianTreeNode newCommentNode) {
			TreeModel model1 = commentTree.getModel();
			AtlassianTreeNode root = (AtlassianTreeNode) model1.getRoot();
			AtlassianTreeNode parentNode = locateNode(root, parentLocator);
			if (parentNode != null) {
				AtlassianTreeModel model = (AtlassianTreeModel) commentTree.getModel();
				model.insertNodeInto(newCommentNode, parentNode, parentNode.getChildCount());
				commentTree.expandPath(new TreePath(parentNode.getPath()));
				commentTree.focusOnNode(newCommentNode);
			}
		}


		private AtlassianTreeNode locateNode(AtlassianTreeNode startingNode, NodeSearchAlgorithm alg) {
			if (alg.check(startingNode)) {
				return startingNode;
			}
			for (int i = 0; i < startingNode.getChildCount(); i++) {
				AtlassianTreeNode result = locateNode((AtlassianTreeNode) startingNode.getChildAt(i), alg);
				if (result != null) {
					return result;
				}
			}
			return null;
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

		private abstract class NodeSearchAlgorithm {
			public abstract boolean check(AtlassianTreeNode node);
		}
	}
}
