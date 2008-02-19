package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BuildStatusChangedToolTip extends JPanel implements BambooStatusDisplay {

	public static final Color BACKGROUND_COLOR_FAILED = new Color(255, 214, 214);
	public static final Color BACKGROUND_COLOR_SUCCEED = new Color(214, 255, 214);

	private transient Project projectComponent;
	private JEditorPane content;

	public BuildStatusChangedToolTip(Project project) {

		projectComponent = project;

		content = new JEditorPane();
		content.setEditable(false);
		content.setContentType("text/html");
		content.setEditorKit(new ClasspathHTMLEditorKit());

		content.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
                IdeaHelper.focusPanel(IdeaHelper.TOOLWINDOW_PANEL_BAMBOO);
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
			default:
				content.setBackground(BACKGROUND_COLOR_FAILED);
				break;
		}

		// fire notification popup
		content.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(content);
		WindowManager.getInstance().getStatusBar(projectComponent).fireNotificationPopup(scrollPane, null);
	}
}
