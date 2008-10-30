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

import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleFilteredModelProvider;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.notification.crucible.CrucibleReviewNotifier;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
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

//		ReviewActionEventBroker eventBroker = IdeaHelper.getReviewActionEventBroker(project);
//		eventBroker.registerListener(new MyAgent(project));


		progressAnimation.configure(this, reviewItemTreePanel, BorderLayout.CENTER);
	}

	public void showCrucibleReviewWindow(final ReviewAdapter crucibleReview) {

		reviewItemTreePanel.startListeningForCredentialChanges(project, crucibleReview);
		crucibleReview.addReviewListener(reviewItemTreePanel);

		CrucibleReviewNotifier notifier =
				IdeaHelper.getProjectComponent(project, ThePluginProjectComponent.class).getCrucibleReviewNotifier();

		if (notifier != null) {
			notifier.registerReviewListener(reviewItemTreePanel);
		}

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

		Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving Crucible Data", false) {
			public void run(final ProgressIndicator indicator) {
				reviewItemTreePanel.showReview(crucibleReview);
			}

			public void onCancel() {
				progressAnimation.stopProgressAnimation();
			}

			public void onSuccess() {
				progressAnimation.stopProgressAnimation();
			}
		};

		ProgressManager.getInstance().run(task);

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
}
