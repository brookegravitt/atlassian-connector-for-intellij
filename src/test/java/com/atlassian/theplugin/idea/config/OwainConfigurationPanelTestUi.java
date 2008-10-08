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

import com.atlassian.theplugin.commons.DefaultSwingUiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Project;
import com.atlassian.theplugin.commons.crucible.api.model.ProjectBean;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class OwainConfigurationPanelTestUi {

	private OwainConfigurationPanelTestUi() {
	}

	public static void main(String[] args) throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleServerCfg crucibleServerCfg = new CrucibleServerCfg("Cruc Server 1", new ServerId());
		crucibleServerCfg.setFisheyeInstance(true);
		final CrucibleServerCfg crucibleServerCfg2 = new CrucibleServerCfg("Cruc Server 2", new ServerId());
		final CrucibleServerCfg crucibleServerCfg3 = new CrucibleServerCfg("Cruc Server 3", new ServerId());
		final ProjectConfiguration projectConfiguration = new ProjectConfiguration(MiscUtil.buildArrayList(
				crucibleServerCfg2,
				new BambooServerCfg("Bamboo Server 1", new ServerId()),
				crucibleServerCfg,
				crucibleServerCfg3));
		projectConfiguration.setDefaultCrucibleServerId(crucibleServerCfg2.getServerId());
		projectConfiguration.setDefaultCrucibleProject("PR-5");
		projectConfiguration.setDefaultCrucibleRepo("Connector");

		List<Project> projects1 = MiscUtil.buildArrayList(makeProject("id1", "PR-1", "Crucible Project 1"),
				makeProject("id2", "PR-2", "Crucible Project 2"));

		final List<Project> projects2 = MiscUtil.buildArrayList(makeProject("id5", "PR-5", "Crucible Project 5"));

		final CrucibleServerFacade crucibleServerFacade = EasyMock.createNiceMock(CrucibleServerFacade.class);
		EasyMock.expect(crucibleServerFacade.getProjects(crucibleServerCfg)).andReturn(projects1).anyTimes();
		EasyMock.expect(crucibleServerFacade.getProjects(crucibleServerCfg2)).andAnswer(new IAnswer<List<Project>>() {
			public List<Project> answer() throws Throwable {
				Thread.sleep(2000);
				return projects2;
			}
		}).anyTimes();
		EasyMock.expect(crucibleServerFacade.getProjects(crucibleServerCfg3)).andAnswer(new IAnswer<List<Project>>() {
			public List<Project> answer() throws Throwable {
				Thread.sleep(2000);
				throw new RuntimeException("fake RE");
			}
		}).anyTimes();
		EasyMock.expect(crucibleServerFacade.getRepositories(crucibleServerCfg2)).andAnswer(new IAnswer<List<Repository>>() {
			public List<Repository> answer() throws Throwable {
				Thread.sleep(2000);
				return MiscUtil.buildArrayList(makeRepository("Clover"), makeRepository("Connector"), makeRepository("FishEye"));

			}
		}).anyTimes();
		EasyMock.replay(crucibleServerFacade);

		JPanel panel = new OwainConfigurationPanel(projectConfiguration, crucibleServerFacade,
				new DefaultSwingUiTaskExecutor());

		JFrame frame = new JFrame("OwainConfigurationPanel test");
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

	private static Project makeProject(String id, String key, String name) {
		ProjectBean res = new ProjectBean();
		res.setId(id);
		res.setKey(key);
		res.setName(name);
		return res;
	}

	private static Repository makeRepository(String name) {
		RepositoryBean res = new RepositoryBean();
		res.setName(name);
		return res;
	}


}
