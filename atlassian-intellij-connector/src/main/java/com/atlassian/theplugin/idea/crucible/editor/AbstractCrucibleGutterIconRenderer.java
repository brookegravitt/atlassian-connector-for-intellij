package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.crucible.CommentDateUtil;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 12:49:04 PM
 */
public abstract class AbstractCrucibleGutterIconRenderer extends GutterIconRenderer {
    private final Editor editor;
    private final ReviewAdapter review;
	private final CrucibleFileInfo fileInfo;
	private final VersionedComment comment;

	public AbstractCrucibleGutterIconRenderer(Editor editor, ReviewAdapter review,
                                      CrucibleFileInfo fileInfo, VersionedComment comment) {
        this.editor = editor;
        this.review = review;
		this.fileInfo = fileInfo;
		this.comment = comment;
	}

	static private String getPadding(int indentation) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < indentation; i++) {
			s.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		return s.toString();
	}

	static private String getReplyText(Comment comment, int indentation) {
		StringBuilder s = new StringBuilder();
		for (Comment versionedComment : comment.getReplies()) {
			s.append("<br>" + getPadding(indentation) + "<b>")
					.append(versionedComment.getAuthor().getDisplayName())
					.append("</b> replied <i> on ")
					.append(CommentDateUtil.getDateText(versionedComment.getCreateDate()))
					.append("</i>:<br>" + getPadding(indentation));
            renderCommentBody(s, versionedComment, indentation);

			s.append(getReplyText(versionedComment, indentation + 1));
		}
		return s.toString();
	}

    @Override
	public String getTooltipText() {
		StringBuilder s = new StringBuilder();
		s.append("<html><b>")
				.append(comment.getAuthor().getDisplayName())
				.append("</b> said <i>on ")
				.append(CommentDateUtil.getDateText(comment.getCreateDate()))
				.append("</i>:<br>");
		renderCommentBody(s, comment, 1);

		s.append(getReplyText(comment, 1));
        
		s.append("</html>");
		return s.toString();
	}

    static private void renderCommentBody(StringBuilder s, Comment cmt, int indentation) {
        boolean boldify =
                cmt.getReadState() == Comment.ReadState.UNREAD
                || cmt.getReadState() == Comment.ReadState.LEAVE_UNREAD;
        if (boldify) {
            s.append("<b>");
        }
        s.append(cmt.getMessage().replace("\n", "<br>" + getPadding(indentation)));
        if (boldify) {
            s.append("</b>");
        }
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

	@Override
	public boolean isNavigateAction() {
		return true;
	}

	private class ClickAction extends AnAction {
		@Override
		public void actionPerformed(final AnActionEvent e) {
			CommentTooltipPanel lctp = new CommentTooltipPanelWithRunners(e, review, fileInfo, comment, null);
            e.getPresentation().putClientProperty(CommentTooltipPanel.JBPOPUP_PARENT_COMPONENT, editor.getComponent());

            CommentTooltipPanel.showCommentTooltipPopup(e, lctp, lctp);
        }
	}

	protected boolean checkIfDraftAndAuthor() {
		return checkIfUserAnAuthor() && comment.isDraft();
	}

	protected boolean checkIfUserAnAuthor() {
		return review.getServerData().getUsername().equals(comment.getAuthor().getUsername());
	}

	protected boolean checkIfAuthorized() {
		if (review == null) {
			return false;
		}
		if (!review.getActions().contains(CrucibleAction.COMMENT)) {
			return false;
		}
		return true;
	}
}
