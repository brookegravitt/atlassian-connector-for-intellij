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

import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.config.serverconfig.BambooServerConfigForm;
import com.atlassian.theplugin.util.PluginUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

public class BambooServerConfigurationFormTest extends TestCase {

	private BambooServerConfigForm bambooPluginConfigurationForm;
	private ProjectCfgManagerImpl projectCfgManager = new LocalProjectCfgManager();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bambooPluginConfigurationForm = new BambooServerConfigForm(null, new UserCfg(),
				BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()));
	}

	public void testBambooSetGetData() throws Exception {
		assertNotNull(bambooPluginConfigurationForm.getRootComponent());

		BambooServerCfg inServerBean = createServerBean();

		bambooPluginConfigurationForm.setData(inServerBean);
		saveData();

		BambooServerCfg outServerBean = inServerBean;

		// form use cloned instance
		checkServerBean(outServerBean);
		assertEquals(0, outServerBean.getSubscribedPlans().size());

		inServerBean.getSubscribedPlans().add(new SubscribedPlan("Plan-1"));

		bambooPluginConfigurationForm.setData(inServerBean);
		outServerBean = bambooPluginConfigurationForm.getBambooServerCfg();
		checkServerBean(outServerBean);
		//@todo enable again		
		//assertEquals(1, outServerBean.getSubscribedPlansData().size());
		//assertEquals("Plan-1", outServerBean.getSubscribedPlansData().get(0).getPlanId());

		/*  */
		inServerBean.getSubscribedPlans().add(new SubscribedPlan("Plan-2"));

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getBambooServerCfg();
		checkServerBean(outServerBean);
		//assertEquals(2, outServerBean.getSubscribedPlansData().size());
		//checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2" });
		/*  */
		inServerBean.getSubscribedPlans().add(new SubscribedPlan("Plan-3"));

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getBambooServerCfg();
		checkServerBean(outServerBean);

		//assertEquals(3, outServerBean.getSubscribedPlansData().size());
		//checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2", "Plan-3" });

		/*  */
		inServerBean.clearSubscribedPlans();

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getBambooServerCfg();

		checkServerBean(outServerBean);

		assertEquals(0, outServerBean.getSubscribedPlans().size());

	}

	private void saveData() {
		bambooPluginConfigurationForm.finalizeData();
		bambooPluginConfigurationForm.saveData();
	}


	private static BambooServerCfg createServerBean() {

		BambooServerCfg outServer = new BambooServerCfg(true, "name", new ServerId());
		outServer.setPassword("password");
		outServer.setPasswordStored(true);
		outServer.setUrl("url");
		outServer.setUsername("userName");


		return outServer;
	}

	private static void checkServerBean(BambooServerCfg outServer) throws ServerPasswordNotProvidedException {

		assertEquals("name", outServer.getName());
		assertEquals("password", outServer.getPassword());
		assertEquals("http://url", outServer.getUrl());
		assertEquals("userName", outServer.getUserName());
	}

	public static Test suite() {
		return new TestSuite(BambooServerConfigurationFormTest.class);
	}


	public void testBambooFormFieldSetting() throws Exception {
		// TODO this call should be removed when HttpClientFactory is not singleton anymore
		ConfigurationFactory.setConfiguration(new IdeaPluginConfigurationBean());

		bambooPluginConfigurationForm.setData(new BambooServerCfg(false, "", new ServerId()));

		BambooServerCfg outServer = bambooPluginConfigurationForm.getBambooServerCfg();
		assertEquals("", outServer.getName());
		assertEquals("", outServer.getUrl());
		assertEquals("", outServer.getUserName());
		assertEquals("", outServer.getPassword());
		assertEquals(0, outServer.getSubscribedPlans().size());

		GenericServerConfigFormFieldMapper helper = new GenericServerConfigFormFieldMapper(
				bambooPluginConfigurationForm.getGenericServerConfigForm());

		helper.getServerName().setText("name");
		helper.getPassword().setText("password");
		helper.getServerUrl().setText("url");
		helper.getUsername().setText("userName");
		saveData();

		outServer = bambooPluginConfigurationForm.getBambooServerCfg();
		checkServerBean(outServer);
	}


}

class LocalProjectCfgManager extends ProjectCfgManagerImpl {

	public LocalProjectCfgManager() {
		super(null);
	}

	@NotNull
	@Override
	public ServerData getServerData(@NotNull final Server serverCfg) {
		return new ServerData(serverCfg.getName(), serverCfg.getServerId().toString(), serverCfg.getUserName(),
				serverCfg.getPassword(), serverCfg.getUrl());
	}
}