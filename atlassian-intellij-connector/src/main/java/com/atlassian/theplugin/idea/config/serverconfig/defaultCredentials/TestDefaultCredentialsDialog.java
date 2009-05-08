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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HyperlinkLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.ConnectionWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * User: pmaruszak
 */
public class TestDefaultCredentialsDialog extends DialogWrapper {

	private Set<ServerDataExt> servers = new HashSet<ServerDataExt>();
	private final JPanel rootPanel = new JPanel(new BorderLayout());
	JScrollPane scroll = new JScrollPane();

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
		rootPanel.setPreferredSize(new Dimension(300, 200));
		setModal(true);

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
		final FormLayout layout = new FormLayout("left:pref, left:pref, left:pref, pref:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, rootPanel);

		builder.setColumnSpan(200);

		
		builder.setBorder(BorderFactory.createEtchedBorder());

		for (ServerDataExt server : servers) {
			final ServerDataExt serverFinal = server;
			builder.append(new JLabel(server.serverData.getName()));

			builder.append(new JLabel(server.status.getIcon()));
			if (server.status == ConnectionStatus.FAILED) {
				HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("error details");
				hyperlinkLabel.addMouseListener(new MouseListener() {
					public void mouseClicked(final MouseEvent e) {
						DialogWithDetails.showExceptionDialog(rootPanel, "Connection Details",	serverFinal.errorMessage);							
					}

					public void mousePressed(final MouseEvent e) {
					}

					public void mouseReleased(final MouseEvent e) {
					}

					public void mouseEntered(final MouseEvent e) {
					}

					public void mouseExited(final MouseEvent e) {
					}
				});
				builder.append(hyperlinkLabel);

			} else {
				builder.append("");
			}

			builder.nextLine();
		}

		//builder.getPanel().revalidate();
		//builder.getPanel().repaint();				

	}

	@Nullable
	protected JComponent createCenterPanel() {

		//rootPanel.add(scroll, BorderLayout.CENTER);
		return rootPanel;
	}


	public void testConnection() {
		for (ServerDataExt serverDataExt : servers) {
			TestConnectionThread task = new TestConnectionThread("testing connection",
					new LocalTestConnectionProcessor(serverDataExt), serverDataExt);

			task.start();
		}
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
