package com.atlassian.theplugin.idea.crucible;

import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.Content;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.peer.PeerFactory;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.PluginToolWindow;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 12, 2008
 * Time: 11:52:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewDetailsToolWindow extends ContentManagerAdapter {
	public static final String TOOL_WINDOW_NAME = "Crucible reviews";
	private ToolWindow ideaToolWindow;
	private Project project;
	private static ReviewDetailsPanel reviewPanel;

	public ReviewDetailsToolWindow(ToolWindowManager toolWindowManager, Project project) {
		this.ideaToolWindow = toolWindowManager.registerToolWindow(
				TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);
		this.project = project;
	}


	/**
	 * Methods opens the ToolWindow and focuses on a particular component.
	 * If component does not exists it is created
	 *
	 * @param project
	 * @param selectedItem
	 */
	public static void focusPanel(Project project, ReviewDataInfoAdapter selectedItem) {
		ToolWindow tw = IdeaHelper.getReviewDetailsWindow(project);
		if (tw != null) {
			tw.activate(null);
			ContentManager contentManager = tw.getContentManager();
			contentManager.setSelectedContent(getContent(project, selectedItem));
		}
	}

	private static Content getContent(Project project, ReviewDataInfoAdapter selectedItem) {
		Content content = null;
		PeerFactory peerFactory = PeerFactory.getInstance();
		ToolWindow tw = IdeaHelper.getReviewDetailsWindow(project);
		if (tw != null) {
			ContentManager contentManager = tw.getContentManager();
			content = contentManager.findContent(ReviewDetailsPanel.PANEL_NAME);
			if (content == null) {
				reviewPanel = new ReviewDetailsPanel();
				content = peerFactory.getContentFactory().createContent(
						reviewPanel, ReviewDetailsPanel.PANEL_NAME, false);
				content.setIcon(IconLoader.getIcon("/icons/tab_jira.png"));
				content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
				contentManager.addContent(content);
			}
			 reviewPanel.setReviewData(selectedItem);
		}
		return content;
	}
}