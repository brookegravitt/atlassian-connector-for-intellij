package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.*;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerNotUsed;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.crucible.comments.ReviewCommentsPanel;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewDetailsPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.config.ContentPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ide.DataManager;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Copyright (C) 2008 Atlassian
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class CrucibleBottomToolWindowPanel extends JPanel implements ContentPanel {
		private static final Key<CrucibleBottomToolWindowPanel> WINDOW_PROJECT_KEY =  Key.create(CrucibleBottomToolWindowPanel.class.getName());
	private Project project;
	private static final float SPLIT_RATIO = 0.3f;
	private ProjectConfigurationBean projectConfiguration;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private static CrucibleServerFacade serverFacade;
	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
	static CrucibleBottomToolWindowPanel instance;
	private static ReviewItemTreePanel reviewItemTreePanel;
	private static Splitter splitter;
	private ReviewCommentsPanel reviewComentsPanel;
	private static CrucibleReviewActionListener tabManager;
	private static final int LEFT_WIDTH = 150;
	private static final int LEFT_HEIGHT = 250;


	protected String getInitialMessage() {

		return "Waiting for Crucible review info.";
	}

	public ReviewCommentsPanel getReviewComentsPanel() {
		return reviewComentsPanel;
	}



	public static CrucibleBottomToolWindowPanel getInstance(Project project, ProjectConfigurationBean projectConfigurationBean) {

        CrucibleBottomToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

        if (window == null) {
            window = new CrucibleBottomToolWindowPanel(project, projectConfigurationBean);
            project.putUserData(WINDOW_PROJECT_KEY, window);
        }
        return window;
    }

	private CrucibleBottomToolWindowPanel(Project project, ProjectConfigurationBean projectConfigurationBean) {
		super(new BorderLayout());

		this.project = project;
		this.projectConfiguration = projectConfigurationBean;

		serverFacade = CrucibleServerFacadeImpl.getInstance();

		setBackground(UIUtil.getTreeTextBackground());
		splitter = new Splitter();
		reviewItemTreePanel = ReviewItemTreePanel.getInstance(projectConfigurationBean);
		Splitter splitter = new Splitter(false, SPLIT_RATIO);
		splitter.setShowDividerControls(true);
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(UIUtil.getTreeTextBackground());
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setMinimumSize(new Dimension(LEFT_WIDTH, LEFT_HEIGHT));
		leftPanel.add(reviewItemTreePanel);
		reviewItemTreePanel.getProgressAnimation().configure(leftPanel, reviewItemTreePanel, BorderLayout.CENTER);
		splitter.setFirstComponent(leftPanel);
		splitter.setHonorComponentsMinimumSize(true);
		tabManager = new ReviewTabManager(project);
		reviewComentsPanel = ReviewCommentsPanel.getInstance();
		splitter.setSecondComponent(reviewComentsPanel);
		add(splitter, BorderLayout.CENTER);

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

	public ProjectConfigurationBean getProjectConfiguration() {
		return projectConfiguration;
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

	public void setData(PluginConfiguration config) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private static class ReviewTabManager implements CrucibleReviewActionListener {
		private CrucibleServerFacade crucibleServerFacade;
		private static final Logger LOGGER = PluginUtil.getLogger();
		private Project project;

		public ReviewTabManager(Project project) {
			super();
			this.project = project;
			IdeaHelper.getReviewActionEventBroker().registerListener(this);
			crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		}

		public Content findOrCreatePanel(String panelName, JPanel panel, Boolean requestFocus) {
			Content content = null;
			PeerFactory peerFactory = PeerFactory.getInstance();
			ToolWindow tw = IdeaHelper.getBottomIdeaToolWindow(project);
			if (tw != null) {
				ContentManager contentManager = tw.getContentManager();
				content = contentManager.findContent(panelName);
				if (content == null && panel != null) {
					content = peerFactory.getContentFactory().createContent(
							panel, panelName, false);
					content.setIcon(IconLoader.getIcon("/icons/tab_jira.png"));
					content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
					contentManager.addContent(content);
				}
				if (requestFocus) {
					contentManager.setSelectedContent(content);
				}
			}
			return content;
		}


		public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void showReview(ReviewDataInfoAdapter reviewItem) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					ToolWindow tw = IdeaHelper.getBottomIdeaToolWindow(project);
					if (tw != null) {
						ContentManager contentManager = tw.getContentManager();
						for (Content content : contentManager.getContents()) {
							if (content.getComponent() instanceof ReviewDetailsPanel) {
								contentManager.removeContent(content, true);
							}
						}
					}
				}
			});
		}

		public void showReviewedFileItem(final ReviewDataInfoAdapter reviewDataInfoAdapter, final ReviewItem reviewItem) {
			try {

				final Collection<VersionedComment> versionedComments = crucibleServerFacade.getVersionedComments(
						reviewDataInfoAdapter.getServer(), reviewDataInfoAdapter.getPermaId(), reviewItem.getPermId());
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						Project project = IdeaHelper.getCurrentProject();

						Content content = findOrCreatePanel(reviewItem.toString(),
								new ReviewDetailsPanel(reviewDataInfoAdapter, reviewItem, versionedComments), true);
						CrucibleHelper.showVirtualFileWithComments(project, reviewItem, versionedComments);

					}
				});
			} catch (RemoteApiException e) {
				LOGGER.warn(e);
			} catch (ServerPasswordNotProvidedException e) {
				LOGGER.warn(e);
			}
		}

		public void showGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void showGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void showVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, VersionedComment versionedComment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void showVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

	}
}
