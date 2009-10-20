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

import com.atlassian.theplugin.idea.config.ProjectConfigurationComponent;
import com.intellij.ide.DataManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class ToolWindowConfigPanel extends JPanel {
	private static final int ROW_COUNT = 5;

	public ToolWindowConfigPanel(final Project project) {
		super(new GridBagLayout());

		JPanel panel = new JPanel(new GridLayout(ROW_COUNT, 1));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		this.add(panel, c);

		panel.add(new Label("No enabled servers for tool window found!", Label.CENTER));
		panel.add(new JLabel(" "));

		HyperlinkLabel projectSettingsLink = new HyperlinkLabel("Configure Plugin Project Settings");
		projectSettingsLink.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {

				Configurable component = project.getComponent(ProjectConfigurationComponent.class);
				ShowSettingsUtil.getInstance().editConfigurable(project, component);
			}
		});

		projectSettingsLink.setIcon(IconLoader.getIcon("/general/ideOptions.png"));

		panel.add(projectSettingsLink);

		HyperlinkLabel globalSettingsLink = new HyperlinkLabel("Configure Plugin IDE Settings");
		globalSettingsLink.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(final HyperlinkEvent e) {

				Configurable component = project.getComponent(ThePluginApplicationComponent.class);
				ShowSettingsUtil.getInstance().editConfigurable(
						IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(ToolWindowConfigPanel.this)),
						IdeaHelper.getAppComponent());
			}
		});

		globalSettingsLink.setIcon(IconLoader.getIcon("/general/ideOptions.png"));

		panel.add(new JLabel(" "));
		panel.add(globalSettingsLink);


	}
}
