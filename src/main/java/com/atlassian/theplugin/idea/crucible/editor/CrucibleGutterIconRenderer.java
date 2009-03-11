package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.EditAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.PublishAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.RemoveAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.ReplyAction;
import com.atlassian.theplugin.idea.crucible.CommentDateUtil;
import com.atlassian.theplugin.idea.crucible.LineCommentTooltipPanel;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CrucibleGutterIconRenderer extends GutterIconRenderer {
	private static final Icon CRUCIBLE_ICON = IconLoader.getIcon("/icons/tab_crucible.png");

	private final ReviewAdapter review;
	private final CrucibleFileInfo fileInfo;
	private final VersionedComment comment;

	public CrucibleGutterIconRenderer(ReviewAdapter review, CrucibleFileInfo fileInfo, VersionedComment comment) {
		this.review = review;
		this.fileInfo = fileInfo;
		this.comment = comment;
	}

	@NotNull
	public Icon getIcon() {
		return CRUCIBLE_ICON;
	}

	@Override
	public String getTooltipText() {
		StringBuilder s = new StringBuilder();
		s.append("<html><b>")
				.append(comment.getAuthor().getDisplayName())
				.append("</b> said <i>on ")
				.append(CommentDateUtil.getDateText(comment.getCreateDate()))
				.append("</i>:<br>")
				.append(comment.getMessage().replace("\n", "<br>"));
		for (VersionedComment versionedComment : comment.getReplies()) {
			s.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;<b>")
					.append(versionedComment.getAuthor().getDisplayName())
					.append("</b> replied <i> on ")
					.append(CommentDateUtil.getDateText(versionedComment.getCreateDate()))
					.append("</i>:<br>&nbsp;&nbsp;&nbsp;&nbsp;")
					.append(versionedComment.getMessage().replace("\n", "<br>"));
		}
		s.append("</html>");
		return s.toString();
	}

	@Override
	public ActionGroup getPopupMenuActions() {
		final ActionManager actionManager = ActionManager.getInstance();
		DefaultActionGroup defaultactiongroup = new DefaultActionGroup();

		if (checkIfAuthorized()) {
			ReplyAction reply = (ReplyAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Reply");
			reply.setReview(review);
			reply.setFile(fileInfo);
			reply.setComment(comment);
			defaultactiongroup.add(reply);
		}

		if (checkIfUserAnAuthor()) {
			EditAction edit = (EditAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Edit");
			edit.setReview(review);
			edit.setFile(fileInfo);
			edit.setComment(comment);
			defaultactiongroup.add(edit);
		}

		if (checkIfUserAnAuthor()) {
			RemoveAction removeAction = (RemoveAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Remove");
			removeAction.setReview(review);
			removeAction.setFile(fileInfo);
			removeAction.setComment(comment);
			defaultactiongroup.add(removeAction);
		}

		if (checkIfDraftAndAuthor()) {
			PublishAction publishAction = (PublishAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Publish");
			publishAction.setReview(review);
			publishAction.setFile(fileInfo);
			publishAction.setComment(comment);
			defaultactiongroup.add(publishAction);
		}
		return defaultactiongroup;
	}

	@Override
	public AnAction getClickAction() {
		return new ClickAction();
	}

	private class ClickAction extends AnAction {
		private static final String ADDING_COMMENT_FAILED = "Adding comment failed: ";
		private static final String UPDATING_COMMENT_FAILED = "Updating comment failed: ";
		private static final String REMOVING_COMMENT_FAILED = "Removing comment failed: ";
		private static final String PUBLISHING_COMMENT_FAILED = "Publishing comment failed: ";

		public void actionPerformed(final AnActionEvent anActionEvent) {

			LineCommentTooltipPanel lctp = new LineCommentTooltipPanel(review, fileInfo, comment) {
				protected void addNewReply(final VersionedComment parent, String text, boolean draft) {
					final VersionedCommentBean reply = createReplyBean(text);
					reply.setDraft(draft);
					runAddReplyTask(parent, reply, anActionEvent, this);
				}

				protected void updateComment(final VersionedComment cmt, String text) {
					final VersionedCommentBean commentBean = (VersionedCommentBean) cmt;
					commentBean.setMessage(text);
					runUpdateCommandTask(commentBean, anActionEvent, this);
				}

				protected void removeComment(final VersionedComment aComment) {
					runRemoveCommentTask(aComment, anActionEvent, this);
				}

				protected void publishComment(VersionedComment aComment) {
					runPublishCommentTask(aComment, anActionEvent, this);
				}
			};
			LineCommentTooltipPanel.showCommentTooltipPopup(anActionEvent, lctp);
		}

		private void runRemoveCommentTask(final VersionedComment aComment, final AnActionEvent anActionEvent,
				final LineCommentTooltipPanel panel) {
			Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
					"Removing comment", false) {
				public void run(@NotNull ProgressIndicator progressIndicator) {
					try {
						review.removeVersionedComment(aComment, fileInfo);
					} catch (RemoteApiException e) {
						panel.setStatusText(REMOVING_COMMENT_FAILED + e.getMessage());
					} catch (ServerPasswordNotProvidedException e) {
						panel.setStatusText(REMOVING_COMMENT_FAILED + e.getMessage());
					} catch (ValueNotYetInitialized e) {
						panel.setStatusText(REMOVING_COMMENT_FAILED + e.getMessage());
					}
				}
			};
			ProgressManager.getInstance().run(task);
		}

		private void runUpdateCommandTask(final VersionedCommentBean commentBean, final AnActionEvent anActionEvent,
				final LineCommentTooltipPanel panel) {
			Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
					"Updating comment", false) {
				public void run(@NotNull ProgressIndicator progressIndicator) {
					try {
						review.editVersionedComment(fileInfo, commentBean);
					} catch (RemoteApiException e) {
						panel.setStatusText(UPDATING_COMMENT_FAILED + e.getMessage());
					} catch (ServerPasswordNotProvidedException e) {
						panel.setStatusText(UPDATING_COMMENT_FAILED + e.getMessage());
					}
				}
			};
			ProgressManager.getInstance().run(task);
		}

		private void runAddReplyTask(final VersionedComment parent, final VersionedCommentBean reply,
				final AnActionEvent anActionEvent, final LineCommentTooltipPanel panel) {
			Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
					"Adding new comment reply", false) {
				public void run(@NotNull ProgressIndicator progressIndicator) {
					try {
						review.addVersionedCommentReply(fileInfo, parent, reply);
					} catch (RemoteApiException e) {
						panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage());
					} catch (ServerPasswordNotProvidedException e) {
						panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage());
					}
				}
			};
			ProgressManager.getInstance().run(task);
		}

		private void runPublishCommentTask(final VersionedComment aComment, AnActionEvent anActionEvent,
				final LineCommentTooltipPanel panel) {
			Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
					"Publishing comment", false) {
				public void run(@NotNull ProgressIndicator progressIndicator) {
					try {
						review.publisVersionedComment(fileInfo, aComment);
					} catch (RemoteApiException e) {
						panel.setStatusText(PUBLISHING_COMMENT_FAILED + e.getMessage());
					} catch (ServerPasswordNotProvidedException e) {
						panel.setStatusText(PUBLISHING_COMMENT_FAILED + e.getMessage());
					}
				}
			};
			ProgressManager.getInstance().run(task);
		}
	}

	private VersionedCommentBean createReplyBean(String text) {
		final VersionedCommentBean reply = new VersionedCommentBean();
		reply.setMessage(text);
		reply.setAuthor(new UserBean(review.getServer().getUsername()));
		reply.setDefectRaised(false);
		reply.setDefectApproved(false);
		reply.setDeleted(false);
		reply.setDraft(false);
		return reply;
	}

	protected boolean checkIfDraftAndAuthor() {
		return checkIfUserAnAuthor() && comment.isDraft();
	}

	protected boolean checkIfUserAnAuthor() {
		return review.getServer().getUsername().equals(comment.getAuthor().getUserName());
	}

	protected boolean checkIfAuthorized() {
		if (review == null) {
			return false;
		}
		try {
			if (!review.getActions().contains(CrucibleAction.COMMENT)) {
				return false;
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			return false;
		}
		return true;
	}

}

