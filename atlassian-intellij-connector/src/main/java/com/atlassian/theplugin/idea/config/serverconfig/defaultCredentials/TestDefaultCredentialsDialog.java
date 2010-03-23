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
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.TestConnectionProcessor;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HyperlinkLabel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: pmaruszak
 */
public class TestDefaultCredentialsDialog extends DialogWrapper {
	private static final int ALL_COLUMNS = 5;
	private Set<ServerDataExt> servers = new HashSet<ServerDataExt>();
	private final JPanel rootPanel = new JPanel(new BorderLayout());
	private final Collection<Thread> threads = new ArrayList<Thread>();
	//	ProgressMonitor progressMonitor;
	private AtomicInteger progress = new AtomicInteger(0);

	JScrollPane scroll = new JScrollPane();
	private Project project;

	public TestDefaultCredentialsDialog(Project project, final ProjectConfiguration projectConfiguration,
			final UserCfg defaultCredentials) {
		super(project, false);
		this.project = project;
		addServerTypeServers(new ArrayList<ServerCfg>(projectConfiguration.getAllBambooServers()),
				ServerType.BAMBOO_SERVER, defaultCredentials);

		addServerTypeServers(new ArrayList<ServerCfg>(projectConfiguration.getAllCrucibleServers()),
				ServerType.CRUCIBLE_SERVER, defaultCredentials);

		addServerTypeServers(new ArrayList<ServerCfg>(projectConfiguration.getAllFisheyeServers()),
				ServerType.FISHEYE_SERVER, defaultCredentials);

		addServerTypeServers(new ArrayList<ServerCfg>(projectConfiguration.getAllJIRAServers()),
				ServerType.JIRA_SERVER, defaultCredentials);


		setTitle("Testing default credentials");
		setModal(true);

		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());

		buildServerContent();
		init();
	}

	@Override
	protected Action[] createActions() {
		return new Action[]{getCancelAction()};
	}

	private void addServerTypeServers(final Collection<ServerCfg> serversToAdd, final ServerType serverType,
			final UserCfg defaultCredentials) {
		for (ServerCfg server : serversToAdd) {
			if (server.isEnabled() && server.isUseDefaultCredentials()) {
                ServerData.Builder builder = new ServerData.Builder(server);
                builder.defaultUser(defaultCredentials);
				this.servers.add(new ServerDataExt(builder.build(), serverType));
			}
		}
	}

	private synchronized void buildServerContent() {
		rootPanel.removeAll();
		rootPanel.add(new JLabel("Testing default credentials for enabled servers"), BorderLayout.NORTH);

		String rowsSpecs = "3dlu, pref, 3dlu, " + StringUtils.repeat("pref,", servers.size());

		final FormLayout layout = new FormLayout("pref, 4dlu, pref, 4dlu, pref:grow",
				rowsSpecs.substring(0, rowsSpecs.length() - 1));


		int row = 4;
		final CellConstraints cc = new CellConstraints();
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.addSeparator("Servers", cc.xyw(1, 2, ALL_COLUMNS));

		for (ServerDataExt server : servers) {
//            if (server.getServerType())
			builder.add(
					new JLabel(server.getServerData().getName() + " (" + server.getServerType().getShortName() + ")"),
					cc.xy(1, row));
			builder.add(new JLabel(server.getStatus().getIcon()), cc.xy(3, row));

			if (server.getStatus() == ConnectionStatus.FAILED) {
				HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("error details");
				hyperlinkLabel.addMouseListener(getMouseListener(server));
				builder.add(hyperlinkLabel, cc.xy(5, row));
			}
			row++;
		}
		rootPanel.add(builder.getPanel(), BorderLayout.CENTER);
		changeCancelActionName();
	}

	private MouseListener getMouseListener(final ServerDataExt serverFinal) {
		return new MouseListener() {
			public void mouseClicked(final MouseEvent e) {
				DialogWithDetails.showExceptionDialog(rootPanel, "Connection Details", serverFinal.getErrorMessage());
			}

			public void mousePressed(final MouseEvent e) {
			}

			public void mouseReleased(final MouseEvent e) {
			}

			public void mouseEntered(final MouseEvent e) {
			}

			public void mouseExited(final MouseEvent e) {
			}
		};
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return rootPanel;
	}


	public void doCancelAction() {
		for (Thread thread : threads) {
			thread.interrupt();
		}
		super.doCancelAction();
	}

	public void testConnection() {

		if (servers == null || servers.size() == 0) {
			Messages.showInfoMessage(project, "No server uses default credentials", PluginUtil.PRODUCT_NAME);
			return;
		}

		for (ServerDataExt serverDataExt : servers) {
			TestConnectionThread thread = new TestConnectionThread("testing connection",
					new LocalTestConnectionProcessor(serverDataExt), serverDataExt);

			threads.add(thread);
			thread.start();
		}

		super.show();
		for (Thread thread : threads) {
			if (thread.isInterrupted()) {
				thread.interrupt();
			}
		}
	}

	@Override
	public void show() {

	}

	private void changeCancelActionName() {
		if (isShowing() && progress.get() == servers.size()) {
			setCancelButtonText("Close");

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
			refresh();
		}

		public void onError(final String errorMessage, Throwable exception, String helpUrl) {
			serverDataExt.setErrorMessage(errorMessage);
			serverDataExt.setStatus(ConnectionStatus.FAILED);
			refresh();
		}

		private void refresh() {
			progress.addAndGet(1);
			buildServerContent();
			rootPanel.revalidate();
			rootPanel.repaint();
		}


	}
}
