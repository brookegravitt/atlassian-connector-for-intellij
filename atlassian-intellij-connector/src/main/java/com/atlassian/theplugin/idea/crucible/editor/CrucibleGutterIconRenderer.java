package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CommentDateUtil;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
		for (Comment versionedComment : comment.getReplies()) {
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

/*	@Override
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
	}*/

	@Override
	public AnAction getClickAction() {
		return new ClickAction();
	}

	public boolean isNavigateAction() {
		return true;
	}

	private class ClickAction extends AnAction {
		public void actionPerformed(final AnActionEvent e) {
			CommentTooltipPanel lctp = new CommentTooltipPanelWithRunners(e, review, fileInfo, comment, null);
            CommentTooltipPanel.showCommentTooltipPopup(e, lctp);
        }
	}

	protected boolean checkIfDraftAndAuthor() {
		return checkIfUserAnAuthor() && comment.isDraft();
	}

	protected boolean checkIfUserAnAuthor() {
		return review.getServerData().getUserName().equals(comment.getAuthor().getUserName());
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

