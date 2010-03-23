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
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GenericServerConfigurationFormTest extends TestCase {

	private GenericServerConfigForm genericServerConfigurationForm;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		genericServerConfigurationForm = new GenericServerConfigForm(null, null, null);
	}

	public void testGenericSetGetData() throws Exception {
		assertNotNull(genericServerConfigurationForm.getRootComponent());

		ServerCfg inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);
		genericServerConfigurationForm.finalizeData();
		genericServerConfigurationForm.saveData();

		ServerCfg outServerBean = genericServerConfigurationForm.getServerCfg();

		// form use cloned instance
		assertSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
	}

	private static ServerCfg createServerBean() {
		ServerCfg tmp = new ServerCfg(true, "name", new ServerIdImpl()) {
			@Override
			public ServerType getServerType() {
				return null;
			}

            public boolean isDontUseBasicAuth() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public UserCfg getBasicHttpUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
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
