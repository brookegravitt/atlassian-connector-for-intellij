package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
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
	private CrucibleReviewActionListener listener = new MyCrucibleReviewActionListener();
	private JScrollPane commentScroll;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CommentTree commentTree;
	private AtlassianTreeModel model;

	private static final AtlassianTreeNode ROOT = new AtlassianTreeNode() {
		public TreeCellRenderer getTreeCellRenderer() {
			return new TreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
															  boolean expanded, boolean leaf, int row, boolean hasFocus) {
					return new JLabel("root");
				}
			};
		}
	};

	public CommentTreePanel() {
		super();
		IdeaHelper.getReviewActionEventBroker().registerListener(listener);
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
		GeneralCommentTreeNode commentNode = new GeneralCommentTreeNode(review, generalComment);
		root.addNode(commentNode);
		for (GeneralComment comment : generalComment.getReplies()) {
			addGeneralCommentTree(commentNode, review, comment);
		}
	}

	private void addVersionedCommentTree(AtlassianTreeNode root, ReviewData review, CrucibleFileInfo file, VersionedComment versionedComment) {
		VersionedCommentTreeNode commentNode = new VersionedCommentTreeNode(review, file, versionedComment);
		root.addNode(commentNode);
		for (VersionedComment comment : versionedComment.getReplies()) {
			addVersionedCommentTree(commentNode, review, file, comment);
		}
	}

	private AtlassianTreeModel createTreeModel(ReviewData review) {
		ROOT.removeAllChildren();
		AtlassianTreeModel model = new AtlassianTreeModel(ROOT);

		List<GeneralComment> generalComments;
		try {
			generalComments = review.getGeneralComments();
			for (GeneralComment comment : generalComments) {
				addGeneralCommentTree(ROOT, review, comment);
			}
			for (CrucibleFileInfo file : review.getFiles()) {

				if (file.getVersionedComments().size() > 0) {
					AtlassianTreeNode fileNode = new FileNameNode(file);
					ROOT.addNode(fileNode);
					for (VersionedComment comment : file.getVersionedComments()) {
						addVersionedCommentTree(fileNode, review, file, comment);
					}
				}
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// ignore
		}
		return model;
	}

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {

		@Override
		public void showReview(ReviewData review) {
			commentTree = new CommentTree(createTreeModel(review));
			commentTree.expandAll();
			commentScroll.setViewportView(commentTree);
			commentTree.initializeUI();
			commentTree.revalidate();
			commentTree.repaint();
		}
	}
}
