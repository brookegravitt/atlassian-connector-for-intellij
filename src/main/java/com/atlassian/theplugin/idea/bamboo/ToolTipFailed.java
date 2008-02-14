package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.ContentManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-14
 * Time: 09:45:33
 * To change this template use File | Settings | File Templates.
 */
public class ToolTipFailed extends JPanel implements BambooStatusDisplay {

	private static final Color BACKGROUND_COLOR_FAILED = new Color(255, 214, 214);
	private static final Color BACKGROUND_COLOR_SUCCEED = new Color(214, 255, 214);

	private Project projectComponent;
	JEditorPane content;

	public ToolTipFailed(Project project) {

		projectComponent = project;

		content = new JEditorPane();
		content.setEditable(false);
		content.setContentType("text/html");

		content.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ToolWindow toolWindow = ToolWindowManager.getInstance(projectComponent).getToolWindow(ThePluginProjectComponent.TOOL_WINDOW_NAME);
				ContentManager contentManager = toolWindow.getContentManager();
				toolWindow.activate(null);
				contentManager.setSelectedContent(contentManager.getContent(0));
			}
		});

		content.addHyperlinkListener(new GenericHyperlinkListener());

		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);

	}

	public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {

		content.setText(htmlPage);

		switch (generalBuildStatus) {
			case BUILD_SUCCEED:
				content.setBackground(BACKGROUND_COLOR_SUCCEED);
				break;
			case BUILD_FAILED:
				content.setBackground(BACKGROUND_COLOR_FAILED);
				break;
			default:
				content.setBackground(BACKGROUND_COLOR_FAILED);
				break;
		}

		// fire notification popup
		WindowManager.getInstance().getStatusBar(projectComponent).fireNotificationPopup(this, null);
	}
}
