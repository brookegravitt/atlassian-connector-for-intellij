package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.SwingUtilities;
import java.util.Collection;
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
    private static final String MARKING_COMMENTS_READ_FAILED = "Marking comment as read failed: ";
    private static final String MARKING_COMMENT_LEAVE_UNREAD_FAILED = "Leaving comments unread failed: ";

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review, CrucibleFileInfo fileInfo,
                                          VersionedComment comment, VersionedComment parent) {
        this(event, review, fileInfo, comment, parent, Mode.SHOW);
    }

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review,
                                          Comment comment, Comment parentComment) {
        this(event, review, null, comment, parentComment, Mode.SHOW);
    }

    public CommentTooltipPanelWithRunners(AnActionEvent event, ReviewAdapter review, CrucibleFileInfo fileInfo,
                                          Comment comment, Comment parent, Mode mode) {
        super(event, review, fileInfo, comment, parent, mode);
    }

    private Comment createReplyBean(Comment parent, String text) {
		final Comment reply = new GeneralComment(getReview().getReview(), parent);
		reply.setMessage(text);
        reply.setAuthor(new User(getReview().getServerData().getUsername()));
        reply.setDefectRaised(false);
        reply.setDefectApproved(false);
        reply.setDeleted(false);
        reply.setDraft(false);
        return reply;
    }

    private Comment createCommentBean(Comment comment) {
        final Comment bean = comment instanceof VersionedComment
                ? new VersionedComment((VersionedComment) comment) : new GeneralComment(comment);
        bean.setAuthor(new User(getReview().getServerData().getUsername()));
        bean.setCreateDate(new Date());

        return bean;
    }

    @Override
	protected void addNewComment(final Comment comment, boolean draft) {
        final Comment newComment = createCommentBean(comment);
        newComment.setDraft(draft);

        runAddCommentTask(newComment, this);
    }

    @Override
	protected void addNewReply(final Comment parentComment, String text, boolean draft) {
        final Comment reply = createReplyBean(parentComment, text);
        reply.setDraft(draft);

        runAddReplyTask(parentComment, reply, this);
    }

    @Override
	protected void updateComment(final Comment cmt, String text) {
        final Comment commentBean = cmt;
        commentBean.setMessage(text);
        runUpdateCommandTask(commentBean, this);
    }

    @Override
	protected void removeComment(final Comment aComment) {
        runRemoveCommentTask(aComment, this);
    }

    @Override
	protected void publishComment(Comment aComment) {
        runPublishCommentTask(aComment, this);
    }

    @Override
	protected void markCommentsRead(final Collection<Comment> comments) {
        runMarkCommentsReadTask(comments, this);
    }

    @Override
	protected void markCommentLeaveUnread(final Comment comment) {
        runMarkCommentLeaveUnreadTask(comment, this);
    }

    private void runMarkCommentsReadTask(final Collection<Comment> comments, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Marking comments as read", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    for (Comment comment : comments) {
                        getReview().markCommentRead(comment);
                    }
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(MARKING_COMMENTS_READ_FAILED + e.getMessage(), true);
                        }
                    });

                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runMarkCommentLeaveUnreadTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Leaving comment unread", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    getReview().markCommentLeaveUnread(comment);
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            panel.setStatusText(MARKING_COMMENT_LEAVE_UNREAD_FAILED + e.getMessage(), true);
                        }
                    });

                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runRemoveCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(),
                "Removing comment", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
					getReview().removeComment(comment);
					panel.setAllButtonsVisible();
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(REMOVING_COMMENT_FAILED + e.getMessage(), true);
                        }
                    });
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runUpdateCommandTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Updating comment", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().editVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().editGeneralComment(comment);
                    }
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(UPDATING_COMMENT_FAILED + e.getMessage(), true);
                            panel.resumeEditing(comment);
                        }
                    });

                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runAddCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Adding new comment", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().addVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().addGeneralComment(comment);
                    }
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage(), true);
                            panel.resumeAdding(comment);                            
                        }
                    });

                }
            }
        };
        ProgressManager.getInstance().run(task);
    }
    
    private void runAddReplyTask(final Comment parent, final Comment reply, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Adding new comment reply", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
					getReview().addReply(parent, reply);
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(ADDING_COMMENT_FAILED + e.getMessage(), true);
                            panel.resumeAdding(reply);
                        }
                    });
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void runPublishCommentTask(final Comment comment, final CommentTooltipPanel panel) {
        Task.Backgroundable task = new Task.Backgroundable(getProject(), "Publishing comment", false) {
            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    if (comment instanceof VersionedComment) {
                        getReview().publishVersionedComment(getFileInfo(), (VersionedComment) comment);
                    } else {
                        getReview().publishGeneralComment(comment);
                    }
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setStatusText(PUBLISHING_COMMENT_FAILED + e.getMessage(), true);
                            panel.setAllButtonsVisible();
                        }
                    });

                }
            }
        };
        ProgressManager.getInstance().run(task);
    }
}
