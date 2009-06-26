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

import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import com.intellij.openapi.project.Project;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public final class BambooFilterListTestUi {
	private static final BambooServerCfg B1 = new BambooServerCfg("Bamboo Server1", new ServerIdImpl());
	private static final BambooServerCfg B2 = new BambooServerCfg("Bamboo Server Two", new ServerIdImpl());
	private static final BambooServerCfg B3 = new BambooServerCfg("Bamboo Server Three", new ServerIdImpl());
	private static ProjectCfgManagerImpl projectCfgManager = new LocalProjectCfgManager();

	private BambooFilterListTestUi() {
	}

	public static void main(String[] args) {
//		final ProjectId projectId1 = new ProjectId("projectId");
		final Project mock = EasyMock.createNiceMock(Project.class);
//		EasyMock.expect(mock.getName()).andReturn("My-test-Project");
		EasyMock.expect(mock.getPresentableUrl()).andReturn("projectId").anyTimes();
		EasyMock.replay(mock);
//		final CfgManagerImpl cfgManager = new CfgManagerImpl();
//		final ProjectCfgManagerImpl cfgManager = new ProjectCfgManagerImpl(null, new CfgManagerImpl(), null);
		projectCfgManager.addServer(B1);
		projectCfgManager.addServer(B2);
		projectCfgManager.addServer(B3);
		final BuildListModelImpl model = new BuildListModelImpl(null, null);
		model.setBuilds(getBuilds());
		SwingAppRunner.run(new JPanel(new BorderLayout()) {
			{
				final BambooFilterList bambooFilterList = new BambooFilterList(projectCfgManager, model);
				add(bambooFilterList, BorderLayout.CENTER);
				final JButton update = new JButton("Update");
				update.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final java.util.List<BambooBuildAdapterIdea> ideas = MiscUtil.buildArrayList(getBuilds());
						ideas.add(createBambooBuild("B7", "PR5", "Project Five", getState(), B3));
						model.setBuilds(ideas);
						bambooFilterList.update();
					}
				});
				JPanel toolbar = new JPanel();
				toolbar.add(update);
				final JButton serversButton = new JButton("Servers");
				serversButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						bambooFilterList.setBambooFilterType(BambooFilterType.SERVER);
					}
				});
				toolbar.add(serversButton);
				final JButton statesButton = new JButton("States");
				statesButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						bambooFilterList.setBambooFilterType(BambooFilterType.STATE);
					}
				});
				toolbar.add(statesButton);
				final JButton prjButton = new JButton("Projects");
				prjButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						bambooFilterList.setBambooFilterType(BambooFilterType.PROJECT);
					}
				});
				toolbar.add(prjButton);
				add(toolbar, BorderLayout.NORTH);
			}
		});


	}

	private static BuildStatus getState() {
		boolean state = new Random().nextBoolean();
		return state ? BuildStatus.SUCCESS : BuildStatus.FAILURE;
	}

	public static java.util.List<BambooBuildAdapterIdea> getBuilds() {
		return Arrays.asList(createBambooBuild("B1", "PR1", "Project One", getState(), B1),
				createBambooBuild("B2", "PR2", "Project Two", getState(), B2),
				createBambooBuild("B3", "PR3", "Project Three", getState(), B3),
				createBambooBuild("B4", "PR1", "Project One", getState(), B2),
				createBambooBuild("B5", "PR3", "Project Three", getState(), B3),
				createBambooBuild("B6", "PR1", "Project One", BuildStatus.UNKNOWN,
						new Random().nextBoolean() ? B1 : B2), createBambooBuild("B7", "PR4", "Project Four", getState(), B2));
	}

	private static BambooBuildAdapterIdea createBambooBuild(String buildKey, String projectKey, String name, BuildStatus state,
			BambooServerCfg serverCfg) {
		final BambooBuildInfo buildInfo = new BambooBuildInfo.Builder(buildKey, null,
				projectCfgManager.getServerData(serverCfg), name, 123, state)
				.startTime(new Date())
				.pollingTime(new Date())
				.completionTime(new Date())
				.relativeBuildDate("55 seconds ago")
				.build();
		return new BambooBuildAdapterIdea(buildInfo);
	}


}


class LocalProjectCfgManager extends ProjectCfgManagerImpl {

	public LocalProjectCfgManager() {
		super(null);
	}

	@NotNull
	@Override
	public ServerData getServerData(@NotNull final com.atlassian.theplugin.commons.cfg.Server serverCfg) {
		return new ServerData(serverCfg.getName(), serverCfg.getServerId(), serverCfg.getUserName(),
				serverCfg.getPassword(), serverCfg.getUrl());
	}
}