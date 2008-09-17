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

//import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerNotUsed;

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.CommentRemoved;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentAdded;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentPublished;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentReplyAdded;
import com.atlassian.theplugin.idea.crucible.events.GeneralCommentUpdated;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAdded;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentPublished;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentReplyAdded;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentUpdated;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public final class CrucibleReviewWindow extends JPanel implements DataProvider {
	public static final String TOOL_WINDOW_TITLE = "Crucible Review";
	private static final Key<CrucibleReviewWindow> WINDOW_PROJECT_KEY
			= Key.create(CrucibleReviewWindow.class.getName());
	private Project project;
//	private static final float SPLIT_RATIO = 0.3f;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAReviewActionEventBrokernimation = new ProgressAnimationProvider();
	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
	private ReviewItemTreePanel reviewItemTreePanel;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CrucibleFilteredModelProvider.Filter filter = CrucibleFilteredModelProvider.Filter.FILES_ALL;


	protected String getInitialMessage() {

		return "Waiting for Crucible review info.";
	}

	public static CrucibleReviewWindow getInstance(Project project) {

		CrucibleReviewWindow window = project.getUserData(WINDOW_PROJECT_KEY);

		if (window == null) {
			window = new CrucibleReviewWindow(project);
			project.putUserData(WINDOW_PROJECT_KEY, window);
		}
		return window;
	}

	public ReviewItemTreePanel getReviewItemTreePanel() {
		return reviewItemTreePanel;
	}

//	public CommentTreePanel getReviewComentsPanel() {
//		return reviewComentsPanel;
//	}

	public void showCrucibleReviewWindow(final String crucibleReviewId) {

		ToolWindowManager twm = ToolWindowManager.getInstance(this.project);
		ToolWindow toolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (toolWindow == null) {
			toolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			toolWindow.setIcon(PluginToolWindow.ICON_CRUCIBLE);
		}

		final ContentManager contentManager = toolWindow.getContentManager();
		Content content = (contentManager.getContents().length > 0) ? contentManager.getContents()[0] : null;

		if (content == null) {

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(this, crucibleReviewId, false);
			content.setIcon(PluginToolWindow.ICON_CRUCIBLE);
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			toolWindow.getContentManager().addContent(content);
		}
		content.setDisplayName(crucibleReviewId);

		toolWindow.getContentManager().setSelectedContent(content);
		toolWindow.show(null);
	}

	private CrucibleReviewWindow(Project project) {
		super(new BorderLayout());

		this.project = project;
		setBackground(UIUtil.getTreeTextBackground());
		reviewItemTreePanel = new ReviewItemTreePanel(project, filter);
//		Splitter splitter = new Splitter(false, SPLIT_RATIO);
//		splitter.setShowDividerControls(true);
		reviewItemTreePanel.getProgressAnimation().configure(reviewItemTreePanel, reviewItemTreePanel, BorderLayout.CENTER);
//		splitter.setFirstComponent(reviewItemTreePanel);
//		splitter.setHonorComponentsMinimumSize(true);
//		reviewComentsPanel = new CommentTreePanel(project, filter);
//		splitter.setSecondComponent(reviewComentsPanel);
//		add(splitter, BorderLayout.CENTER);
		add(reviewItemTreePanel, BorderLayout.CENTER);

		ReviewActionEventBroker eventBroker = IdeaHelper.getReviewActionEventBroker(project);
		eventBroker.registerListener(new MyAgent(project));


		progressAnimation.configure(this, reviewItemTreePanel, BorderLayout.CENTER);
	}


	protected JScrollPane setupPane(JEditorPane pane, String initialText) {
		pane.setText(initialText);
		JScrollPane scrollPane = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		return scrollPane;
	}

	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	public CrucibleVersion getCrucibleVersion() {
		return crucibleVersion;
	}


	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.FILE_TREE)) {
			return reviewItemTreePanel.getReviewItemTree();
		} else if (dataId.equals(Constants.CRUCIBLE_BOTTOM_WINDOW)) {
			return this;
		}
//		} else if (dataId.equals(Constants.CRUCIBLE_COMMENT_TREE)) {
//			return reviewComentsPanel.getCommentTree();
//		}
		return null;
	}

	public void switchFilter() {
		filter = filter.getNextState();
//		getReviewComentsPanel().filterTreeNodes(filter);
		getReviewItemTreePanel().filterTreeNodes(filter);
	}

	private final class MyAgent extends CrucibleReviewActionListener {
		private final CrucibleServerFacade facade = CrucibleServerFacadeImpl.getInstance();
		private final ReviewActionEventBroker eventBroker;
		private Project project;

		public MyAgent(final Project project) {
			this.project = project;
			eventBroker = IdeaHelper.getReviewActionEventBroker(this.project);
		}

		@Override
		public void showReview(final ReviewData reviewData) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					showCrucibleReviewWindow(reviewData.getPermId().getId());
				}
			});
		}

		@Override
		public void focusOnLineCommentEvent(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment, final boolean openIfClosed) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Editor editor = CrucibleHelper.getEditorForCrucibleFile(review, file);
					if (editor != null) {
						CrucibleHelper.openFileOnComment(project, review, file, comment);
						return;
					}
					if (openIfClosed) {
						CrucibleHelper.openFileOnComment(project, review, file, comment);
					}
				}
			});
		}

		@Override
		public void showDiff(final CrucibleFileInfo file) {
			CrucibleHelper.showRevisionDiff(project, file);
		}

		@Override
		public void showFile(final ReviewData review, final CrucibleFileInfo file) {
			CrucibleHelper.showVirtualFileWithComments(project, review, file);
		}

		@Override
		public void aboutToAddLineComment(final ReviewData review, final CrucibleFileInfo file, final Editor editor,
				final int start, final int end) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					VersionedCommentBean newComment = new VersionedCommentBean();
					CommentEditForm dialog = new CommentEditForm(project, review, newComment,
							CrucibleHelper.getMetricsForReview(project, review));
					dialog.pack();
					dialog.setModal(true);
					dialog.show();
					if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
						newComment.setCreateDate(new Date());
						newComment.setAuthor(new UserBean(review.getServer().getUsername()));
						newComment.setToStartLine(start);
						newComment.setToEndLine(end);
						eventBroker.trigger(new VersionedCommentAboutToAdd(CrucibleReviewActionListener.ANONYMOUS, review,
								file, newComment));
					}
				}
			});
		}

		@Override
		public void aboutToPublishGeneralComment(final ReviewData review, final GeneralComment comment) {
			try {
				facade.publishComment(review.getServer(), review.getPermId(), comment.getPermId());
				// @todo - dirty hack - probably remote api should return new comment info
				if (comment instanceof GeneralCommentBean) {
					((GeneralCommentBean) comment).setDraft(false);
				}
				eventBroker.trigger(new GeneralCommentPublished(this, review, comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToPublishVersionedComment(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			try {
				facade.publishComment(review.getServer(), review.getPermId(), comment.getPermId());
				// @todo - dirty hack - probably remote api should return new comment info
				if (comment instanceof VersionedCommentBean) {
					((VersionedCommentBean) comment).setDraft(false);
				}
				eventBroker.trigger(new VersionedCommentPublished(this, review, file.getItemInfo(), comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}


		@Override
		public void aboutToAddGeneralComment(final ReviewData review, final GeneralComment newComment) {
			try {
				GeneralComment comment = facade.addGeneralComment(review.getServer(), review.getPermId(),
						newComment);
				eventBroker.trigger(new GeneralCommentAdded(this, review, comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToAddGeneralCommentReply(ReviewData review, GeneralComment parentComment,
				GeneralComment newComment) {
			try {
				GeneralComment comment = facade.addGeneralCommentReply(review.getServer(), review.getPermId(),
						parentComment.getPermId(), newComment);
				eventBroker.trigger(new GeneralCommentReplyAdded(this, review, parentComment, comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToAddVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
			try {
				VersionedComment newComment = facade.addVersionedComment(review.getServer(), review.getPermId(),
						file.getItemInfo().getId(), comment);
				List<VersionedComment> comments;
				comments = file.getItemInfo().getComments();
				if (comments == null) {
					comments = facade.getVersionedComments(review.getServer(), review.getPermId(),
							file.getItemInfo().getId());
					file.getItemInfo().setComments(comments);
				}
				comments.add(newComment);
				eventBroker.trigger(new VersionedCommentAdded(this, review, file.getItemInfo(), newComment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToAddVersionedCommentReply(ReviewData review, CrucibleFileInfo file,
				VersionedComment parentComment, VersionedComment comment) {

			try {
				VersionedComment newComment = facade.addVersionedCommentReply(review.getServer(), review.getPermId(),
						parentComment.getPermId(), comment);
				eventBroker.trigger(
							new VersionedCommentReplyAdded(this, review, file.getItemInfo(), parentComment, newComment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToUpdateVersionedComment(final ReviewData review, final CrucibleFileInfo file,
				final VersionedComment comment) {
			try {
				facade.updateComment(review.getServer(), review.getPermId(), comment);
				eventBroker.trigger(new VersionedCommentUpdated(this, review, file.getItemInfo(), comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToUpdateGeneralComment(final ReviewData review, final GeneralComment comment) {
			try {
				facade.updateComment(review.getServer(), review.getPermId(), comment);
				eventBroker.trigger(new GeneralCommentUpdated(this, review, comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

		@Override
		public void aboutToRemoveComment(final ReviewData review, final Comment comment) {
			try {
				facade.removeComment(review.getServer(), review.getPermId(), comment);
				eventBroker.trigger(new CommentRemoved(this, review, comment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}
	}

}
