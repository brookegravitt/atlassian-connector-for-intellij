package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentReplyAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToAdd;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralSectionNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 25, 2008
 * Time: 5:27:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddAction extends AnAction {
	@Override
	public void update(AnActionEvent e) {
		com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node = getSelectedNode(e);
		boolean enabled = node != null;
		if (node instanceof VersionedCommentTreeNode) {
			enabled = !((VersionedCommentTreeNode) node).getComment().isReply();
		}
		e.getPresentation().setEnabled(enabled);
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project currentProject = e.getData(DataKeys.PROJECT);
		com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node = getSelectedNode(e);
		if (node != null && currentProject != null) {
			addComment(currentProject, node);
		}

	}

	private void addComment(Project project, AtlassianTreeNode treeNode) {
		if (treeNode instanceof GeneralCommentTreeNode) {
			GeneralCommentTreeNode node = (GeneralCommentTreeNode) treeNode;
			addReplyToGeneralComment(project, node.getReview(), node.getComment());
		} else if (treeNode instanceof GeneralSectionNode) {
			GeneralSectionNode node = (GeneralSectionNode) treeNode;
			addGeneralComment(project, node.getReview());
		} else if (treeNode instanceof FileNameNode) {
			FileNameNode node = (FileNameNode) treeNode;
			addCommentToFile(project, node.getReview(), node.getFile());
		} else if (treeNode instanceof VersionedCommentTreeNode) {
			VersionedCommentTreeNode node = (VersionedCommentTreeNode) treeNode;
			addReplyToVersionedComment(project, node.getReview(), node.getFile(), node.getComment());
		}
	}

	private void addCommentToFile(Project project, ReviewData review, CrucibleFileInfo file) {
		List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
		VersionedCommentBean newComment = new VersionedCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) newComment, metrics);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setUser(new UserBean(review.getServer().getUserName()));
			IdeaHelper.getReviewActionEventBroker().trigger(
					new VersionedCommentAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, file, newComment));
		}

	}

	private void addReplyToVersionedComment(Project project, ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
		VersionedCommentBean newComment = new VersionedCommentBean();
		newComment.setReply(true);
		CommentEditForm dialog = new CommentEditForm(project, review, (CommentBean) newComment, metrics);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			VersionedComment parentComment = comment;
			newComment.setFromLineInfo(parentComment.isFromLineInfo());
			newComment.setFromStartLine(parentComment.getFromStartLine());
			newComment.setFromEndLine(parentComment.getFromEndLine());
			newComment.setToLineInfo(parentComment.isToLineInfo());
			newComment.setToStartLine(parentComment.getToStartLine());
			newComment.setToEndLine(parentComment.getToEndLine());
			newComment.setCreateDate(new Date());
			newComment.setReviewItemId(review.getPermId());
			newComment.setUser(new UserBean(review.getServer().getUserName()));
			IdeaHelper.getReviewActionEventBroker().trigger(
					new VersionedCommentReplyAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, file, parentComment, newComment));
		}
	}

	private void addGeneralComment(Project project, ReviewData review) {
		java.util.List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
		GeneralCommentBean newComment = new GeneralCommentBean();
		CommentEditForm dialog = new CommentEditForm(project, review, newComment, metrics);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setUser(new UserBean(review.getServer().getUserName()));
			IdeaHelper.getReviewActionEventBroker().trigger(
					new GeneralCommentAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, newComment));
		}
	}

	private void addReplyToGeneralComment(Project project, ReviewData review, GeneralComment comment) {
		java.util.List<CustomFieldDef> metrics = new ArrayList<CustomFieldDef>();
		try {
			metrics = CrucibleServerFacadeImpl.getInstance().getMetrics(review.getServer(), review.getMetricsVersion());
		} catch (RemoteApiException e) {
			IdeaHelper.handleRemoteApiException(project, e);
		} catch (ServerPasswordNotProvidedException e) {
			IdeaHelper.handleMissingPassword(e);
		}
		GeneralComment parentComment = comment;
		GeneralCommentBean newComment = new GeneralCommentBean();
		newComment.setReply(true);
		CommentEditForm dialog = new CommentEditForm(project, review, newComment, metrics);
		dialog.pack();
		dialog.setModal(true);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			newComment.setCreateDate(new Date());
			newComment.setUser(new UserBean(review.getServer().getUserName()));
			IdeaHelper.getReviewActionEventBroker().trigger(
					new GeneralCommentReplyAboutToAdd(CrucibleReviewActionListener.ANONYMOUS,
							review, parentComment, newComment));
		}
	}

	@Nullable
	private TreePath getSelectedTreePath(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		Component component = DataKeys.CONTEXT_COMPONENT.getData(dataContext);
		if (!(component instanceof JTree)) {
			return null;
		}
		final JTree theTree = (JTree) component;
		return theTree.getSelectionPath();
	}

	@Nullable
	protected AtlassianTreeNode getSelectedNode(AnActionEvent e) {
		TreePath treepath = getSelectedTreePath(e);
		if (treepath == null) {
			return null;
		}
		return getSelectedNode(treepath);
	}

	private AtlassianTreeNode getSelectedNode(TreePath path) {
		Object o = path.getLastPathComponent();
		if (o instanceof AtlassianTreeNode) {
			return (AtlassianTreeNode) o;
		}
		return null;
	}


}
