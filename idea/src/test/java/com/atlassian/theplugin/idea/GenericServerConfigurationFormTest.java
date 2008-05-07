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

import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.config.serverconfig.GenericServerConfigForm;
import com.atlassian.theplugin.commons.Server;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Feb 20, 2008
 * Time: 2:22:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericServerConfigurationFormTest extends TestCase {

	private GenericServerConfigForm genericServerConfigurationForm;



	protected void setUp() throws Exception {
		super.setUp();
		genericServerConfigurationForm = new GenericServerConfigForm(null);
	}

	public void testGenericSetGetData() throws Exception {
		assertNotNull(genericServerConfigurationForm.getRootComponent());

		ServerBean inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);

		Server outServerBean = genericServerConfigurationForm.getData();

		// form use cloned instance
		assertNotSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
	}

	@SuppressWarnings({ "RedundantStringConstructorCall" })
	public void testBambooFormIsModified() throws Exception {
		ServerBean inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);

		ServerBean outServerBean = createServerBean();

		assertFalse(genericServerConfigurationForm.isModified());


		/* equals vs == */

		outServerBean.setName(new String("name"));
		outServerBean.transientSetPasswordString(new String("password"), true);
		outServerBean.setUrlString(new String("url"));
		outServerBean.setUserName(new String("userName"));


		PluginConfigurationFormHelper formHelper = new PluginConfigurationFormHelper(genericServerConfigurationForm);

		formHelper.serverName.setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.serverName.setText(outServerBean.getName());

		formHelper.serverUrl.setText(outServerBean.getUrlString() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.serverUrl.setText(outServerBean.getUrlString());

		formHelper.username.setText(outServerBean.getUserName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.username.setText(outServerBean.getUserName());


		formHelper.password.setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.password.setText(outServerBean.transientGetPasswordString());
	}

	public void testBambooFormFieldSetting() throws Exception {
		genericServerConfigurationForm.setData(new ServerBean());

		Server outServer = genericServerConfigurationForm.getData();
		assertEquals("", outServer.getName());
		assertEquals("", outServer.getUrlString());
		assertEquals("", outServer.getUserName());
		assertEquals("", outServer.transientGetPasswordString());
		assertEquals(0, outServer.transientGetSubscribedPlans().size());

		PluginConfigurationFormHelper helper = new PluginConfigurationFormHelper(genericServerConfigurationForm);

		helper.serverName.setText("name");
		helper.password.setText("password");
		helper.serverUrl.setText("url");
		helper.username.setText("userName");

		outServer = genericServerConfigurationForm.getData();
		checkServerBean(outServer);
	}


	private static ServerBean createServerBean() {

		ServerBean outServer = new ServerBean();
		outServer.setName("name");
		outServer.transientSetPasswordString("password", true);
		outServer.setUrlString("url");
		outServer.setUserName("userName");


		return outServer;
	}

	private static void checkServerBean(Server outServer) throws ServerPasswordNotProvidedException {

		assertEquals("name", outServer.getName());
		assertEquals("password", outServer.transientGetPasswordString());
		assertEquals("http://url", outServer.getUrlString());
		assertEquals("userName", outServer.getUserName());
	}

	public static Test suite() {
		return new TestSuite(GenericServerConfigurationFormTest.class);
	}



	@SuppressWarnings("all")
	private class PluginConfigurationFormHelper extends PrivateFieldMapper {
		public JTextField serverName;
		public JTextField serverUrl;
		public JTextField username;
		public JPasswordField password;
		public JButton testConnection;
		public JCheckBox chkPasswordRemember;
		public JCheckBox cbEnabled;

		public PluginConfigurationFormHelper(GenericServerConfigForm pluginConfigurationForm) throws Exception {
			super(pluginConfigurationForm);
		}
	}
}
