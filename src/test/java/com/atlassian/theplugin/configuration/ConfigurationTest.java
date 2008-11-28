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

package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.configuration.CheckNowButtonOption;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.util.PluginUtil;
import junit.framework.TestCase;
import static junitx.framework.Assert.assertNotEquals;

public class ConfigurationTest extends TestCase {
	private PluginConfigurationBean baseConf;
	private PluginConfigurationBean newConf;

	@Override
	protected void setUp() throws Exception {
		baseConf = new PluginConfigurationBean();
		ConfigurationFactory.setConfiguration(baseConf);

		newConf = new PluginConfigurationBean(baseConf);
	}

    public void testCopyConstructor() throws ServerPasswordNotProvidedException {
       // now let's test cloning a configuration
        assertEquals(baseConf, newConf);
    }

	public void testProjectSettings() {
        assertEquals("Atlassian IntelliJ Connector", PluginUtil.getInstance().getName());
    }

	public void testGeneralConfigEquals() {

		GeneralConfigurationBean generalConf = baseConf.getGeneralConfigurationData();

		assertEquals(baseConf, newConf);
		generalConf.setAnonymousFeedbackEnabled(
				generalConf.getAnonymousFeedbackEnabled() == null || !generalConf.getAnonymousFeedbackEnabled());
		assertNotEquals(baseConf, newConf);

		newConf.setGeneralConfigurationData(new GeneralConfigurationBean(generalConf));
		assertEquals(baseConf, newConf);
		generalConf.setAutoUpdateEnabled(!generalConf.isAutoUpdateEnabled());
		assertNotEquals(baseConf, newConf);
		
		newConf.setGeneralConfigurationData(new GeneralConfigurationBean(generalConf));
		assertEquals(baseConf, newConf);
		generalConf.setCheckNowButtonOption(CheckNowButtonOption.STABLE_AND_SNAPSHOT);
		assertNotEquals(baseConf, newConf);

		newConf.setGeneralConfigurationData(new GeneralConfigurationBean(generalConf));
		assertEquals(baseConf, newConf);
		generalConf.setCheckUnstableVersionsEnabled(!generalConf.isCheckUnstableVersionsEnabled());
		assertNotEquals(baseConf, newConf);

		newConf.setGeneralConfigurationData(new GeneralConfigurationBean(generalConf));
		assertEquals(baseConf, newConf);
		generalConf.setUseIdeaProxySettings(!generalConf.getUseIdeaProxySettings());
		assertNotEquals(baseConf, newConf);

	}
}
