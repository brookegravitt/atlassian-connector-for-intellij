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

import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerNotUsed;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.config.ContentPanel;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.*;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public final class CrucibleReviewWindow extends JPanel implements ContentPanel, DataProvider {
	public static final String TOOL_WINDOW_TITLE = "Crucible Review";
	private static final Key<CrucibleReviewWindow> WINDOW_PROJECT_KEY
			= Key.create(CrucibleReviewWindow.class.getName());
	private Project project;
	private static final float SPLIT_RATIO = 0.3f;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAReviewActionEventBrokernimation = new ProgressAnimationProvider();
	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
	private ReviewItemTreePanel reviewItemTreePanel;
	private CommentTreePanel reviewComentsPanel;
	private ReviewActionEventBroker eventBroker;
	private static final int LEFT_WIDTH = 150;
	private static final int LEFT_HEIGHT = 250;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CrucibleFilteredModelProvider.FILTER filter = CrucibleFilteredModelProvider.FILTER.FILES_ALL;


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

	public CommentTreePanel getReviewComentsPanel() {
		return reviewComentsPanel;
	}

	public void showCrucibleReviewWindow() {

		ToolWindowManager twm = ToolWindowManager.getInstance(this.project);
		ToolWindow toolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (toolWindow == null) {
			toolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			toolWindow.setIcon(PluginToolWindow.ICON_CRUCIBLE);
		}

		Content content = toolWindow.getContentManager().findContent(WINDOW_PROJECT_KEY.toString());

		if (content == null) {

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(this, WINDOW_PROJECT_KEY.toString(), false);
			content.setIcon(PluginToolWindow.ICON_CRUCIBLE);
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			toolWindow.getContentManager().addContent(content);
		}

		toolWindow.getContentManager().setSelectedContent(content);
		toolWindow.show(null);
	}

	private CrucibleReviewWindow(Project project) {
		super(new BorderLayout());

		this.project = project;
		setBackground(UIUtil.getTreeTextBackground());
		reviewItemTreePanel = new ReviewItemTreePanel(project, filter);
		Splitter splitter = new Splitter(false, SPLIT_RATIO);
		splitter.setShowDividerControls(true);
		reviewItemTreePanel.getProgressAnimation().configure(reviewItemTreePanel, reviewItemTreePanel, BorderLayout.CENTER);
		splitter.setFirstComponent(reviewItemTreePanel);
		splitter.setHonorComponentsMinimumSize(true);
		reviewComentsPanel = new CommentTreePanel(project, filter);
		splitter.setSecondComponent(reviewComentsPanel);
		add(splitter, BorderLayout.CENTER);


		eventBroker = IdeaHelper.getReviewActionEventBroker();
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

	protected String wrapBody(String s) {
		return "<html>" + HtmlBambooStatusListenerNotUsed.BODY_WITH_STYLE + s + "</body></html>";

	}

	protected void setStatusMessage(String msg) {
		setStatusMessage(msg, false);
	}

	protected void setStatusMessage(String msg, boolean isError) {
		//editorPane.setBackground(isError ? Color.RED : Color.WHITE);
		//editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}


	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	public CrucibleVersion getCrucibleVersion() {
		return crucibleVersion;
	}


	public void resetState() {
	}

	public boolean isModified() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getTitle() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void getData() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void setData(final PluginConfiguration config) {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.FILE_TREE)) {
			return reviewItemTreePanel.getReviewItemTree();
		} else if (dataId.equals(Constants.CRUCIBLE_BOTTOM_WINDOW)) {
			return this;
		} else if (dataId.equals(Constants.CRUCIBLE_COMMENT_TREE)) {
			return reviewComentsPanel.getCommentTree();
		}
		return null;
	}

	public void switchFilter() {
		filter = filter.getNextState();
		getReviewComentsPanel().filterTreeNodes(filter);
		getReviewItemTreePanel().filterTreeNodes(filter);
	}

	private final class MyAgent extends CrucibleReviewActionListener {
		private final CrucibleServerFacade facade = CrucibleServerFacadeImpl.getInstance();
		private final ReviewActionEventBroker eventBroker = IdeaHelper.getReviewActionEventBroker();
		private Project project;

		public MyAgent(final Project project) {
			super();
			this.project = project;
		}

		@Override
		public void showReview(final ReviewData reviewData) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					showCrucibleReviewWindow();
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
						newComment.setAuthor(new UserBean(review.getServer().getUserName()));
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
				eventBroker.trigger(new VersionedCommentPublished(this, review, file, comment));
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
		public void aboutToAddVersionedComment(ReviewData review, CrucibleFileInfo file,
				VersionedComment comment) {
			try {
				VersionedComment newComment = facade.addVersionedComment(review.getServer(), review.getPermId(),
						file.getPermId(), comment);
				List<VersionedComment> comments;
				try {
					comments = file.getVersionedComments();
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					comments = facade.getVersionedComments(review.getServer(), review.getPermId(),
							file.getPermId());
					((CrucibleFileInfoImpl) file).setVersionedComments(comments);
				}
				comments.add(newComment);
				eventBroker.trigger(new VersionedCommentAdded(this, review, file, newComment));
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
				eventBroker.trigger(new VersionedCommentReplyAdded(this, review, file, parentComment, newComment));
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
				eventBroker.trigger(new VersionedCommentUpdated(this, review, file, comment));
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
