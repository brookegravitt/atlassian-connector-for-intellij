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
package com.atlassian.theplugin.idea.config;

import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.DefaultSwingUiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProjectDefaultsConfigurationPanelTestUi {
	private ProjectDefaultsConfigurationPanelTestUi() {
	}

	public static void main(String[] args) throws RemoteApiException, ServerPasswordNotProvidedException, JIRAException {
		final CrucibleServerCfg crucibleServerCfg = new CrucibleServerCfg("Cruc Server 1", new ServerIdImpl());
		crucibleServerCfg.setFisheyeInstance(true);
		final CrucibleServerCfg crucibleServerCfg2 = new CrucibleServerCfg("Cruc Server 2", new ServerIdImpl());
		final CrucibleServerCfg crucibleServerCfg3 = new CrucibleServerCfg("Cruc Server 3", new ServerIdImpl());
		final FishEyeServerCfg fishEyeServerCfg0 = new FishEyeServerCfg("FishEye Server 0", new ServerIdImpl());
		final FishEyeServerCfg fishEyeServerCfg1 = new FishEyeServerCfg("FishEye Server 1", new ServerIdImpl());
		final JiraServerCfg jiraServerCfg1 = new JiraServerCfg("Jira Server 1", new ServerIdImpl(), true);
		final JiraServerCfg jiraServerCfg2 = new JiraServerCfg("Jira Server 2", new ServerIdImpl(), true);
		final ProjectConfiguration projectConfiguration = new ProjectConfiguration(MiscUtil.buildArrayList(
				crucibleServerCfg2,
				new BambooServerCfg("Bamboo Server 1", new ServerIdImpl()),
				crucibleServerCfg,
				crucibleServerCfg3, fishEyeServerCfg0, fishEyeServerCfg1,
				jiraServerCfg1, jiraServerCfg2));

		projectConfiguration.setDefaultCrucibleServerId(crucibleServerCfg2.getServerId());
		projectConfiguration.setDefaultCrucibleProject("PR-5");
		projectConfiguration.setDefaultCrucibleRepo("Connector");

		projectConfiguration.setDefaultFishEyeServerId(fishEyeServerCfg1.getServerId());
		projectConfiguration.setDefaultFishEyeRepo("studio");
		projectConfiguration.setFishEyeProjectPath("trunk/thePlugin");

		List<CrucibleProject> projects1 = MiscUtil.buildArrayList(makeCrucibleProject("id1", "PR-1", "Crucible Project 1"),
				makeCrucibleProject("id2", "PR-2", "Crucible Project 2"));
		List<String> repos0 = MiscUtil.buildArrayList("studio00", "studio", "studio01");

		final List<CrucibleProject> projects2 = MiscUtil
				.buildArrayList(makeCrucibleProject("id5", "PR-5", "Crucible Project 5"),
						makeCrucibleProject("id7", "LPR-5", "Crucible The Last Project"));

		int id = 1;
		List<JIRAProject> jiraProjects1 = MiscUtil.buildArrayList(makeJiraProject(id++, "AB", "Jira Project 1"),
				makeJiraProject(id++, "PL", "Jira Project 2"));

		final List<JIRAProject> jiraProjects2 = MiscUtil.buildArrayList(makeJiraProject(id++, "CD", "Jira Project 3"),
				makeJiraProject(id, "EF", "Jira Project 4"));

		final CrucibleServerFacade crucibleServerFacade = EasyMock.createNiceMock(CrucibleServerFacade.class);
		EasyMock.expect(crucibleServerFacade.getProjects(getServerData(crucibleServerCfg)))
				.andReturn(projects1).anyTimes();

		EasyMock.expect(crucibleServerFacade.getProjects(getServerData(crucibleServerCfg2)))
				.andAnswer(new IAnswer<List<CrucibleProject>>() {

					public List<CrucibleProject> answer() throws Throwable {
						Thread.sleep(2000);
						return projects2;
					}
				}).anyTimes();
		EasyMock.expect(crucibleServerFacade.getProjects(getServerData(crucibleServerCfg3)))
				.andAnswer(new IAnswer<List<CrucibleProject>>() {
					public List<CrucibleProject> answer() throws Throwable {
						Thread.sleep(2000);
						throw new RuntimeException("fake RE");
					}
				}).anyTimes();
		EasyMock.expect(crucibleServerFacade.getRepositories(getServerData(crucibleServerCfg3)))
				.andAnswer(new IAnswer<List<Repository>>() {
					public List<Repository> answer() throws Throwable {
						Thread.sleep(1000);
						return MiscUtil.buildArrayList(makeRepository("R1"), makeRepository("R2"));
					}
				}).anyTimes();
		EasyMock.expect(crucibleServerFacade.getRepositories(getServerData(crucibleServerCfg2)))
				.andAnswer(new IAnswer<List<Repository>>() {
					public List<Repository> answer() throws Throwable {
						Thread.sleep(2000);
						return MiscUtil
								.buildArrayList(makeRepository("Clover"), makeRepository("Connector"),
										makeRepository("FishEye"));

					}
				}).anyTimes();

		EasyMock.replay(crucibleServerFacade);

		final FishEyeServerFacade fishEyeServerFacade = EasyMock.createNiceMock(FishEyeServerFacade.class);
		EasyMock.expect(fishEyeServerFacade.getRepositories(getServerData(fishEyeServerCfg0)))
				.andReturn(repos0).anyTimes();
		EasyMock.expect(fishEyeServerFacade.getRepositories(getServerData(fishEyeServerCfg1)))
				.andAnswer(new IAnswer<Collection<String>>() {

					public Collection<String> answer() throws Throwable {
						Thread.sleep(7000);
						return MiscUtil.buildArrayList("studioA", "studioB", "StudioC");
					}
				}).anyTimes();

		EasyMock.replay(fishEyeServerFacade);

		final JiraServerFacade jiraServerFacade = EasyMock.createNiceMock(JiraServerFacade.class);
		final BambooServerFacade bambooServerFacade = EasyMock.createNiceMock(BambooServerFacade.class);

		EasyMock.expect(jiraServerFacade.getProjects(getServerData(jiraServerCfg1))).andReturn(jiraProjects1)
				.anyTimes();

		EasyMock.expect(jiraServerFacade.getProjects(getServerData(jiraServerCfg2)))
				.andAnswer(new IAnswer<List<JIRAProject>>() {

					public List<JIRAProject> answer() throws Throwable {
						Thread.sleep(2000);
						return jiraProjects2;
					}
				}).anyTimes();

		EasyMock.replay(jiraServerFacade);

		JPanel panel = new ProjectDefaultsConfigurationPanel(null, projectConfiguration, crucibleServerFacade,
				fishEyeServerFacade, bambooServerFacade, jiraServerFacade, new DefaultSwingUiTaskExecutor(),
				new UserCfg());

		JFrame frame = new JFrame("ProjectDefaultsConfigurationPanel test");
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(panel);

		//Finish setting up the frame, and show it.
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Crucible server: " + projectConfiguration.getDefaultCrucibleServerId());
				System.out.println("Crucible project: " + projectConfiguration.getDefaultCrucibleProject());
				System.out.println("Crucible repo: " + projectConfiguration.getDefaultCrucibleRepo());
				System.out.println("FishEye server: " + projectConfiguration.getDefaultFishEyeServerId());
				System.out.println("FishEye repo: " + projectConfiguration.getDefaultFishEyeRepo());
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);

	}

	private static CrucibleProject makeCrucibleProject(String id, String key, String name) {
        Collection<String> usersNames = new ArrayList<String>();
          usersNames.add("Ala");
          usersNames.add("Zosia");

		return new CrucibleProject(id, key, name, usersNames);
	}

	private static Repository makeRepository(String name) {
		return new Repository(name, "unknown", false);
	}

	private static JIRAProject makeJiraProject(long id, String key, String name) {
		JIRAProjectBean res = new JIRAProjectBean();
		res.setId(id);
		res.setKey(key);
		res.setName(name);
		return res;
	}


	private static ServerData getServerData(@NotNull final com.atlassian.theplugin.commons.cfg.Server serverCfg) {
		return new ServerData(serverCfg, serverCfg.getUsername(), serverCfg.getPassword());
	}

    private static JiraServerData getServerData(@NotNull final JiraServerCfg serverCfg) {
        return new JiraServerData(serverCfg, serverCfg.getUsername(), serverCfg.getPassword(), true);
    }

}