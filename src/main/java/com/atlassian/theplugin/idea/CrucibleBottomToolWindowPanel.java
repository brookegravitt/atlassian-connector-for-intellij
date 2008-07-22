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

import com.atlassian.theplugin.commons.crucible.*;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerNotUsed;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.config.ContentPanel;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

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


public final class CrucibleBottomToolWindowPanel extends JPanel implements ContentPanel {
	private static final Key<CrucibleBottomToolWindowPanel> WINDOW_PROJECT_KEY
            = Key.create(CrucibleBottomToolWindowPanel.class.getName());
	private Project project;
	private static final float SPLIT_RATIO = 0.3f;
	private ProjectConfigurationBean projectConfiguration;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
	private static ReviewItemTreePanel reviewItemTreePanel;
	private CommentTreePanel reviewComentsPanel;
	private static CrucibleReviewActionListener tabManager;
	private static final int LEFT_WIDTH = 150;
	private static final int LEFT_HEIGHT = 250;


	protected String getInitialMessage() {

		return "Waiting for Crucible review info.";
	}

	public static CrucibleBottomToolWindowPanel getInstance(Project project,
            ProjectConfigurationBean projectConfigurationBean) {

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


		setBackground(UIUtil.getTreeTextBackground());
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
		reviewComentsPanel = new CommentTreePanel();
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

}
