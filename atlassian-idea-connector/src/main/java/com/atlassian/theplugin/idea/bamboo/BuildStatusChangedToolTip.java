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

package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.PluginToolWindow;
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
                PluginToolWindow.focusPanel(projectComponent, PluginToolWindow.ToolWindowPanels.BAMBOO);
			}
		});

		content.addHyperlinkListener(new GenericHyperlinkListener());
		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);

	}

	public void updateBambooStatus(BuildStatus generalBuildStatus, BambooPopupInfo popupInfo) {
		content.setText(popupInfo.toHtml());

		switch (generalBuildStatus) {
			case BUILD_SUCCEED:
				content.setBackground(BACKGROUND_COLOR_SUCCEED);
				break;
			case BUILD_FAILED:
			default:
				content.setBackground(BACKGROUND_COLOR_FAILED);
				break;
		}

		// fire crucible popup
		content.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(content);
		WindowManager.getInstance().getStatusBar(projectComponent).fireNotificationPopup(scrollPane, null);
	}
}
