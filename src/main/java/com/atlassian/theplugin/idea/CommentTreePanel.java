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
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.SectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
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

	private static final AtlassianTreeNode ROOT = new SectionNode("root", AtlassianClickAction.EMPTY_ACTION);
	private static final String GENERAL_COMMENTS_SECTION = "General comments";

	public CommentTreePanel() {
		super();
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

	private void addGeneralCommentTree(AtlassianTreeNode root, ReviewData review, GeneralComment generalComment) {
		GeneralCommentTreeNode commentNode = new GeneralCommentTreeNode(review, generalComment, AtlassianClickAction.EMPTY_ACTION);
		root.addNode(commentNode);
		for (GeneralComment comment : generalComment.getReplies()) {
			addGeneralCommentTree(commentNode, review, comment);
		}
	}

	private void addVersionedCommentTree(AtlassianTreeNode root, ReviewData review, CrucibleFileInfo file, VersionedComment versionedComment) {
		VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, versionedComment, AtlassianClickAction.EMPTY_ACTION);
		root.addNode(commentNode);
		for (VersionedComment comment : versionedComment.getReplies()) {
			addVersionedCommentTree(commentNode, review, file, comment);
		}
	}

	private AtlassianTreeModel createTreeModel(final ReviewData review) {
		ROOT.removeAllChildren();
		AtlassianTreeModel model = new AtlassianTreeModel(ROOT);

		List<GeneralComment> generalComments;
		try {
			generalComments = review.getGeneralComments();
			AtlassianTreeNode generalNode = new SectionNode(GENERAL_COMMENTS_SECTION, AtlassianClickAction.EMPTY_ACTION);
			ROOT.addNode(generalNode);
			for (GeneralComment comment : generalComments) {
				addGeneralCommentTree(generalNode, review, comment);
			}
			for (CrucibleFileInfo file : review.getFiles()) {
				AtlassianTreeNode fileNode = new FileNameNode(file, AtlassianClickAction.EMPTY_ACTION);
				ROOT.addNode(fileNode);
				for (VersionedComment comment : file.getVersionedComments()) {
					addVersionedCommentTree(fileNode, review, file, comment);
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

		private AtlassianTreeNode locateFileTreeNode
				(ReviewData
						review, CrucibleFileInfo
						file) {
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
}
