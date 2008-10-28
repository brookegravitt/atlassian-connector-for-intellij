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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.crucible.CommentEditForm;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewListenerImpl;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAboutToAdd;
import com.atlassian.theplugin.idea.crucible.events.VersionedCommentAddedOrEdited;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
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

	private CrucibleReviewWindow(Project project) {
		super(new BorderLayout());

		this.project = project;
		setBackground(UIUtil.getTreeTextBackground());
		reviewItemTreePanel = new ReviewItemTreePanel(project, filter);
		reviewItemTreePanel.getProgressAnimation().configure(reviewItemTreePanel, reviewItemTreePanel, BorderLayout.CENTER);
		add(reviewItemTreePanel, BorderLayout.CENTER);

		ReviewActionEventBroker eventBroker = IdeaHelper.getReviewActionEventBroker(project);
		eventBroker.registerListener(new MyAgent(project));


		progressAnimation.configure(this, reviewItemTreePanel, BorderLayout.CENTER);
	}

	public void showCrucibleReviewWindow(final ReviewAdapter crucibleReview) {

		reviewItemTreePanel.startListeningForCredentialChanges(project, crucibleReview);
		crucibleReview.addReviewListener(reviewItemTreePanel);

		ToolWindowManager twm = ToolWindowManager.getInstance(this.project);
		ToolWindow toolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);		
		if (toolWindow == null) {
			toolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			toolWindow.setIcon(PluginToolWindow.ICON_CRUCIBLE);
		}

		final ContentManager contentManager = toolWindow.getContentManager();
		Content content = (contentManager.getContents().length > 0) ? contentManager.getContents()[0] : null;

		if (content != null) {
			contentManager.removeContent(content, true);
		}

		PeerFactory peerFactory = PeerFactory.getInstance();
		content = peerFactory.getContentFactory().createContent(this, crucibleReview.getPermId().getId(), false);
		content.setIcon(PluginToolWindow.ICON_CRUCIBLE);
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
		toolWindow.getContentManager().addContent(content);

		toolWindow.getContentManager().setSelectedContent(content);
		toolWindow.show(null);

		progressAnimation.startProgressAnimation();

//		reviewItemTreePanel.showReview(crucibleReview);

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
		return null;
	}

	public void switchFilter() {
		filter = filter.getNextState();
		getReviewItemTreePanel().filterTreeNodes(filter);
	}

	public AtlassianTreeWithToolbar getAtlassianTreeWithToolbar() {
		return reviewItemTreePanel.getAtlassianTreeWithToolbar();
	}

	private final class MyAgent extends CrucibleReviewListenerImpl {
		private final CrucibleServerFacade facade = CrucibleServerFacadeImpl.getInstance();
		private final ReviewActionEventBroker eventBroker;
		private Project project;

		public MyAgent(final Project project) {
			this.project = project;
			eventBroker = IdeaHelper.getReviewActionEventBroker(this.project);
		}

		public void commentsDownloaded(ReviewAdapter review) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					progressAnimation.stopProgressAnimation();
				}
			});
		}

		@Override
		public void focusOnLineCommentEvent(final ReviewAdapter review, final CrucibleFileInfo file,
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
			ApplicationManager.getApplication().runReadAction(new Runnable() {
				public void run() {
					CrucibleHelper.showRevisionDiff(project, file);
				}
			});
		}

		@Override
		public void showFile(final ReviewAdapter review, final CrucibleFileInfo file) {
			ApplicationManager.getApplication().runReadAction(new Runnable() {
				public void run() {
					CrucibleHelper.showVirtualFileWithComments(project, review, file);
				}
			});
		}

		private void setCommentAuthor(CrucibleServerCfg server, Comment comment) {
			CommentBean bean = (CommentBean) comment;
			bean.setAuthor(CrucibleUserCacheImpl.getInstance().getUser(server, server.getUsername(), false));

		}

		@Override
		public void aboutToAddLineComment(final ReviewAdapter review, final CrucibleFileInfo file,
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
						setCommentAuthor(review.getServer(), newComment);
						newComment.setToStartLine(start);
						newComment.setToEndLine(end);
						eventBroker.trigger(new VersionedCommentAboutToAdd(CrucibleReviewListenerImpl.ANONYMOUS, review,
								file, newComment));
					}
				}
			});
		}

		@Override
		public void aboutToAddVersionedComment(ReviewAdapter review, CrucibleFileInfo file, VersionedComment comment) {
			try {
				VersionedComment newComment = facade.addVersionedComment(review.getServer(), review.getPermId(),
						file.getPermId(), comment);
				setCommentAuthor(review.getServer(), newComment);
				List<VersionedComment> comments;
				comments = file.getVersionedComments();
				if (comments == null) {
					comments = facade.getVersionedComments(review.getServer(), review.getPermId(),
							file.getPermId());
					file.setVersionedComments(comments);
				} else {
					comments.add(newComment);
				}

//				review.getVersionedComments().add(newComment);

				eventBroker.trigger(new VersionedCommentAddedOrEdited(this, review, file.getPermId(), newComment));
			} catch (RemoteApiException e) {
				IdeaHelper.handleRemoteApiException(project, e);
			} catch (ServerPasswordNotProvidedException e) {
				IdeaHelper.handleMissingPassword(e);
			}
		}

	}

}
