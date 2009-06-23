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
package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.util.MiscUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

public final class ServerConfigPanelTestUi {
	private ServerConfigPanelTestUi() {
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("ServerConfigPanel test");
		final BambooServerCfg bambooServerCfg = new BambooServerCfg(false, "mybamboo2", new ServerId());
		final Collection<ServerCfg> serverCfgs = MiscUtil.buildArrayList(
				new BambooServerCfg(true, "mybamboo", new ServerId()),
				bambooServerCfg,
				new CrucibleServerCfg("Crucible EAC", new ServerId()),
				new JiraServerCfg("My Jira Server", new ServerId()),
				new JiraServerCfg("Second Jira", new ServerId()),
				new FishEyeServerCfg("FishEye1 Server", new ServerId())
		);

		final Collection<ServerCfg> serverCfgs2 = MiscUtil.buildArrayList(
				new BambooServerCfg(true, "2-mybamboo", new ServerId()),
				bambooServerCfg,
				new CrucibleServerCfg("2-Crucible EAC Very Very long name", new ServerId()),
				new JiraServerCfg("2-My Jira Server", new ServerId()),
				new JiraServerCfg("2-Second Jira", new ServerId())
		);

		ServerConfigPanel configPanel = new ServerConfigPanel(
				null, null, new ProjectConfiguration(serverCfgs), null, false) {

			@Override
			protected JComponent createToolbar() {
				JToolBar toolbar = new JToolBar("My Fake Toolbar", JToolBar.HORIZONTAL);
				final JButton button = new JButton("Test Only");
				button.addActionListener(new ActionListener() {

					public void actionPerformed(final ActionEvent e) {
						setData(serverCfgs2);
					}
				});
				toolbar.add(button);
				final JButton addButton = new JButton("Add");
				addButton.addActionListener(new ActionListener() {

					public void actionPerformed(final ActionEvent e) {
						addServer(ServerType.CRUCIBLE_SERVER);
					}
				});
				toolbar.add(addButton);
				return toolbar;
			}
		};

		frame.getContentPane().add(configPanel, BorderLayout.CENTER);

		//Finish setting up the frame, and show it.
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);

	}


}

