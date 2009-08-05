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

import com.atlassian.connector.intellij.bamboo.BambooPopupInfo;
import com.atlassian.connector.intellij.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BuildStatusChangedToolTip extends JPanel implements BambooStatusDisplay {

	public static final Color BACKGROUND_COLOR_FAILED = new Color(255, 214, 214);
	public static final Color BACKGROUND_COLOR_SUCCEED = new Color(214, 255, 214);

	private transient Project projectComponent;
	private JEditorPane content;

	public BuildStatusChangedToolTip(Project project, @NotNull final PluginToolWindow pluginToolWindow) { 

		projectComponent = project;

		content = new JEditorPane();
		content.setEditable(false);
		EditorKit kit = new ClasspathHTMLEditorKit();
		// PL-1127 - we need to explicitely set editor kit before setting content type,
		// otherwise if Thread.currentThread().getContextClassLoader() is null (which 
		// it sometimes is for some weird-ass reason), setContentType() throws NPE.
		// For more info, see this for example (or google around): 
		// http://forums.sun.com/thread.jspa?threadID=560696&tstart=-1
		//
		// and yes - the below is proabably a belt-and-suspenders solution,
		// but I am making a blind fix here, so hell if I know which one will work :) 
		content.setEditorKit(kit);
		content.setEditorKitForContentType("text/html", kit);

		content.setContentType("text/html");

		content.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pluginToolWindow.focusPanel(PluginToolWindow.ToolWindowPanels.BUILDS);
			}
		});

		content.addHyperlinkListener(new GenericHyperlinkListener());
		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);

	}

	public void updateBambooStatus(BuildStatus generalBuildStatus, BambooPopupInfo popupInfo) {
		content.setText(popupInfo.toHtml());

		IdeaVersionFacade.OperationStatus status;
		switch (generalBuildStatus) {
			case SUCCESS:
				content.setBackground(BACKGROUND_COLOR_SUCCEED);
				status = IdeaVersionFacade.OperationStatus.INFO;
				break;
			case FAILURE:
			default:
				content.setBackground(BACKGROUND_COLOR_FAILED);
				status = IdeaVersionFacade.OperationStatus.ERROR;
				break;
		}
		content.setCaretPosition(0);
		IdeaVersionFacade.getInstance()
				.fireNofification(projectComponent, new JScrollPane(content), content.getText(), "/icons/bamboo-blue-16.png",
						status, content.getBackground());
	}
}
