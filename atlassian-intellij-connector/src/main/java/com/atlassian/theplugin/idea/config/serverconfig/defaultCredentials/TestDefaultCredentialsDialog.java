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
package com.atlassian.theplugin.idea.config.serverconfig.defaultCredentials;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.HyperlinkLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: pmaruszak
 */
public class TestDefaultCredentialsDialog extends DialogWrapper {

	private Set<ServerDataExt> servers = new HashSet<ServerDataExt>();
	private final JPanel rootPanel = new JPanel(new BorderLayout());
	private final Collection<Thread> threads = new ArrayList<Thread>();


	public TestDefaultCredentialsDialog(Project project) {
		super(project, false);
		for (ServerType serverType : ServerType.values()) {
			for (ServerData serverData :
					IdeaHelper.getProjectCfgManager(project)
							.getAllEnabledServersWithDefaultCredentials(serverType)) {
				servers.add(new ServerDataExt(serverData, serverType));
			}
		}

		setTitle("Testing default credentials");
		setModal(true);

		JScrollPane scroll = new JScrollPane();

		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		buildServerContent();
		init();
	}

	private synchronized void buildServerContent() {
		rootPanel.removeAll();
		rootPanel.add(new JLabel("Testing default credentials for enabled servers"), BorderLayout.NORTH);

		String rowsSpecs = StringUtils.repeat("pref,", servers.size());

		JPanel contentPanel = new JPanel(new FormLayout("pref, pref, pref:grow",
				rowsSpecs.substring(0, rowsSpecs.length() - 1)));

		contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		rootPanel.add(contentPanel, BorderLayout.CENTER);
		int row = 0;
		final CellConstraints cc = new CellConstraints();

		for (ServerDataExt server : servers) {
			final ServerDataExt serverFinal = server;
			row++;

			contentPanel.add(new JLabel(server.getServerData().getName()), cc.xy(1, row));
			contentPanel.add(new JLabel(server.getStatus().getIcon()), cc.xy(2, row));
			if (server.getStatus() == ConnectionStatus.FAILED) {
				HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("error details");
				hyperlinkLabel.addMouseListener(new MouseAdapter() {

					public void mouseClicked(final MouseEvent e) {
						DialogWithDetails.showExceptionDialog(rootPanel, "Connection Details", 
                                serverFinal.getErrorMessage());
					}
				});

				//
				contentPanel.add(hyperlinkLabel, cc.xy(3, row));

			} else {

				//
				contentPanel.add(new JLabel(""), cc.xy(3, row));
			}
		}

		contentPanel.setPreferredSize(new Dimension(600, 200));
		contentPanel.setMinimumSize(new Dimension(600, 200));
		contentPanel.setMaximumSize(new Dimension(600, 200));

	}

	@Nullable
	protected JComponent createCenterPanel() {

		//rootPanel.add(scroll, BorderLayout.CENTER);
		return rootPanel;
	}


	public void doCancelAction() {
		for (Thread thread : threads) {
			thread.interrupt();
		}

		super.doCancelAction();
	}

	public void testConnection() {
		for (ServerDataExt serverDataExt : servers) {
			TestConnectionThread thread = new TestConnectionThread("testing connection",
					new LocalTestConnectionProcessor(serverDataExt), serverDataExt);

			threads.add(thread);
			thread.start();
		}

		show();
	}

	public void setServerDataExt(ServerDataExt server) {
		servers.add(server);

	}


	private class LocalTestConnectionProcessor implements TestConnectionProcessor {
		final private ServerDataExt serverDataExt;

		private LocalTestConnectionProcessor(final ServerDataExt serverDataExt) {
			this.serverDataExt = serverDataExt;
		}

		public void setConnectionResult(final ConnectionWrapper.ConnectionState result) {
		}

		public void onSuccess() {
			serverDataExt.setStatus(ConnectionStatus.PASSED);
			buildServerContent();

			rootPanel.revalidate();
			rootPanel.repaint();
		}

		public void onError(final String errorMessage) {
			serverDataExt.setErrorMessage(errorMessage);
			serverDataExt.setStatus(ConnectionStatus.FAILED);
			buildServerContent();

			rootPanel.revalidate();
			rootPanel.repaint();
		}
	}
}
