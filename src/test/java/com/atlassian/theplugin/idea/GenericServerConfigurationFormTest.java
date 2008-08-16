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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.config.serverconfig.GenericServerConfigForm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GenericServerConfigurationFormTest extends TestCase {

	private GenericServerConfigForm genericServerConfigurationForm;



	@Override
    protected void setUp() throws Exception {
		super.setUp();
		genericServerConfigurationForm = new GenericServerConfigForm(null, null);
	}

	public void testGenericSetGetData() throws Exception {
		assertNotNull(genericServerConfigurationForm.getRootComponent());

		ServerCfg inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);
		genericServerConfigurationForm.saveData();

		ServerCfg outServerBean = genericServerConfigurationForm.getServerCfg();

		// form use cloned instance
		assertSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
	}

	@SuppressWarnings({ "RedundantStringConstructorCall" })
	public void testBambooFormIsModified() throws Exception {
		ServerCfg inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);

		ServerCfg outServerBean = createServerBean();

		assertFalse(genericServerConfigurationForm.isModified());

		/* equals vs == */

		outServerBean.setName(new String("name"));
		outServerBean.setPassword(new String("password"));
		outServerBean.setUrl(new String("url"));
		outServerBean.setUsername(new String("userName"));
        assertFalse(genericServerConfigurationForm.isModified());


		GenericServerConfigFormFieldMapper formHelper = new GenericServerConfigFormFieldMapper(genericServerConfigurationForm);

		formHelper.getServerName().setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.getServerName().setText(outServerBean.getName());

		formHelper.getServerUrl().setText(outServerBean.getUrl() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.getServerUrl().setText(outServerBean.getUrl());

		formHelper.getUsername().setText(outServerBean.getUsername() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.getUsername().setText(outServerBean.getUsername());


		formHelper.getPassword().setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.getPassword().setText(outServerBean.getPassword());
	}

	private static ServerCfg createServerBean() {
        ServerCfg tmp = new ServerCfg(true, "name", new ServerId()) {
            @Override
            public ServerType getServerType() {
                return null;
            }

			@Override
			public ServerCfg getClone() {
				throw new UnsupportedOperationException("not yet implemented");
			}
		};
		tmp.setPassword("password");
        tmp.setPasswordStored(true);
        tmp.setUrl("url");
		tmp.setUsername("userName");
		return tmp;
	}

	private static void checkServerBean(ServerCfg outServer) throws ServerPasswordNotProvidedException {

		assertEquals("name", outServer.getName());
		assertEquals("password", outServer.getPassword());
		assertEquals("http://url", outServer.getUrl());
		assertEquals("userName", outServer.getUsername());
	}

	public static Test suite() {
		return new TestSuite(GenericServerConfigurationFormTest.class);
	}


}
