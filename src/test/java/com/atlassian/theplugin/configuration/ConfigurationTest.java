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

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.util.PluginUtil;
import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
    @Override
	protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
    }

    public void testConfiguration() throws ServerPasswordNotProvidedException {
       // now let's test cloning a configuration
        PluginConfigurationBean newConfig = new PluginConfigurationBean(ConfigurationFactory.getConfiguration());
        assertEquals(ConfigurationFactory.getConfiguration(), newConfig);
    }



	public void testProjectSettings() {
        assertEquals("Atlassian", PluginUtil.getInstance().getName());
    }
}
