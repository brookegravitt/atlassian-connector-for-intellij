package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.EditAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.PublishAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.RemoveAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.ReplyAction;
import com.atlassian.theplugin.idea.crucible.CommentDateUtil;
import com.atlassian.theplugin.idea.crucible.LineCommentTooltipPanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.popup.JBPopupFactory;
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

		public void actionPerformed(final AnActionEvent anActionEvent) {

			LineCommentTooltipPanel lctp = new LineCommentTooltipPanel(review, fileInfo, comment) {
				protected void addNewReply(final VersionedComment parent, String text) {
					final VersionedCommentBean reply = new VersionedCommentBean();
					reply.setMessage(text);
					reply.setAuthor(new UserBean(review.getServer().getUsername()));
					reply.setDefectRaised(false);
					reply.setDefectApproved(false);
					reply.setDeleted(false);
					reply.setDraft(false);

					Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
							"Adding new comment reply", false) {
						public void run(ProgressIndicator progressIndicator) {
							try {
								review.addVersionedCommentReply(fileInfo, parent, reply);
							} catch (RemoteApiException e) {
								setStatusText(ADDING_COMMENT_FAILED + e.getMessage());
							} catch (ServerPasswordNotProvidedException e) {
								setStatusText(ADDING_COMMENT_FAILED + e.getMessage());
							}
						}
					};
					ProgressManager.getInstance().run(task);
				}

				protected void updateComment(final VersionedComment cmt, String text) {
					final VersionedCommentBean commentBean = (VersionedCommentBean) cmt;
					commentBean.setMessage(text);
					Task.Backgroundable task = new Task.Backgroundable(IdeaHelper.getCurrentProject(anActionEvent),
							"Updating comment", false) {
						public void run(ProgressIndicator progressIndicator) {
							try {
								review.editVersionedComment(fileInfo, commentBean);
							} catch (RemoteApiException e) {
								setStatusText(UPDATING_COMMENT_FAILED + e.getMessage());
							} catch (ServerPasswordNotProvidedException e) {
								setStatusText(UPDATING_COMMENT_FAILED + e.getMessage());
							}
						}
					};
					ProgressManager.getInstance().run(task);
				}
			};
			JBPopupFactory.getInstance().createComponentPopupBuilder(lctp, lctp)
					.setRequestFocus(true)
					.setCancelOnClickOutside(true)
					.setMovable(true)
					.setTitle("Comment")
					.setResizable(true)
					.setCancelKeyEnabled(true)
					.createPopup().showInBestPositionFor(anActionEvent.getDataContext());
		}
	}

	protected boolean checkIfDraftAndAuthor() {
		if (checkIfUserAnAuthor() && comment.isDraft()) {
			return true;
		}
		return false;
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

