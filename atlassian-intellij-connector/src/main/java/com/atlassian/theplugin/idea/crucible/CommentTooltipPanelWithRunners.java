package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * User: kalamon
 * Date: Apr 30, 2009
 * Time: 10:56:10 AM
 */
public class CommentTooltipPanelWithRunners extends CommentTooltipPanel {

    private static final String ADDING_COMMENT_FAILED = "Adding comment failed: ";
    private static final String UPDATING_COMMENT_FAILED = "Updating comment failed: ";
    private static final String REMOVING_COMMENT_FAILED = "Removing comment failed: ";
    private static final String PUBLISHING_COMMENT_FAILED = "Publishing comment failed: ";

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review, CrucibleFileInfo fileInfo,
                                          VersionedComment comment, VersionedComment parent) {
        this(event, review, fileInfo, comment, parent, Mode.SHOW);
    }

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review,
                                          GeneralComment comment, GeneralComment parentComment) {
        this(event, review, null, comment, parentComment, Mode.SHOW);
    }

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review, CrucibleFileInfo fileInfo,
                                          Comment comment, Comment parent, Mode mode) {
        super(review, fileInfo, comment, parent, mode);
        setProject(IdeaHelper.getCurrentProject(event));
    }

    private CommentBean createReplyBean(Comment parent, String text) {
        final CommentBean reply = parent instanceof VersionedComment
                ? new VersionedCommentBean() : new GeneralCommentBean();
        reply.setMessage(text);
        reply.setAuthor(new UserBean(getReview().getServerData().getUserName()));
        reply.setDefectRaised(false);
        reply.setDefectApproved(false);
        reply.setDeleted(false);
        reply.setDraft(false);
        return reply;
    }

    private CommentBean createCommentBean(Comment comment) {
        final CommentBean bean = comment instanceof VersionedComment
                ? new VersionedCommentBean((VersionedComment) comment) : new GeneralCommentBean((GeneralComment) comment);
        bean.setAuthor(new UserBean(getReview().getServerData().getUserName()));
        bean.setCreateDate(new Date());

        return bean;
    }

    protected void addNewComment(final Comment comment, boolean draft) {
        final CommentBean newComment = createCommentBean(comment);
        newComment.setDraft(draft);

        runAddCommentTask(newComment, this);
    }

    protected void addNewReply(final Comment parentComment, String text, boolean draft) {
        final CommentBean reply = createReplyBean(parentComment, text);
        reply.setDraft(draft);

        runAddReplyTask(parentComment, reply, this);
    }

    protected void updateComment(final Comment cmt, String text) {
        final CommentBean commentBean = (CommentBean) cmt;
        commentBean.setMessage(text);
        runUpdateCommandTask(commentBean, this);
    }

    protected void removeComment(final Comment aComment) {
        runRemoveCommentTask(aComment, this);
    }

    protected void publishComment(Comment aComment) {
        runPublishCommentTask(aComment, this);
    }

    private void runRemoveCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(),
                "Removing comment", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().removeVersionedComment((VersionedComment) comment, getFileInfo());
                    } else {
                        getReview().removeGeneralComment((GeneralComment) comment);
                    }
                } catch (Exception e) {
                    panel.setStatusText(REMOVING_COMMENT_FAILED + e.getMessage(), true);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runUpdateCommandTask(final CommentBean comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Updating comment", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().editVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().editGeneralComment((GeneralComment) comment);
                    }
                } catch (Exception e) {
                    panel.setStatusText(UPDATING_COMMENT_FAILED + e.getMessage(), true);
                    panel.resumeEditing(comment);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runAddCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Adding new comment", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().addVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().addGeneralComment((GeneralComment) comment);
                    }
                } catch (Exception e) {
                    panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage(), true);
                    panel.resumeAdding((CommentBean) comment);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }
    
    private void runAddReplyTask(final Comment parent, final CommentBean reply, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Adding new comment reply", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (parent instanceof VersionedComment) {
                        getReview().addVersionedCommentReply(getFileInfo(), (VersionedComment) parent,
                                (VersionedCommentBean) reply);
                    } else {
                        getReview().addGeneralCommentReply((GeneralComment) parent, (GeneralCommentBean) reply);
                    }
                } catch (Exception e) {
                    panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage(), true);
                    panel.resumeAdding(reply);
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runPublishCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Publishing comment", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().publishVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().publishGeneralComment((GeneralComment) comment);
                    }
                } catch (Exception e) {
                    panel.setStatusText(PUBLISHING_COMMENT_FAILED + e.getMessage(), true);
                    panel.setAllButtonsVisible();
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }
}
