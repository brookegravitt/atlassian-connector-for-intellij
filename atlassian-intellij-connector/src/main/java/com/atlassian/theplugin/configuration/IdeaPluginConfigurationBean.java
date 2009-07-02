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

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.util.HttpConfigurableIdeaImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(name = "atlassian-ide-plugin",
		storages = { @Storage(id = "atlassian-ide-plugin-id", file = "$APP_CONFIG$/atlassian-ide-plugin.app.xml") })
public class IdeaPluginConfigurationBean extends PluginConfigurationBean
		implements PersistentStateComponent<IdeaPluginConfigurationBean>, ApplicationComponent {
	public IdeaPluginConfigurationBean() {
    }

	@NonNls
	@NotNull
	public String getComponentName() {
		return getClass().getCanonicalName();
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	/**
	 * Copying constructor.<p>
	 * Makes a deep copy of provided configuration.
	 * @param cfg configuration to be deep copied.
	 */
	public IdeaPluginConfigurationBean(PluginConfiguration cfg) {
		setConfiguration(cfg);
    }

	public IdeaPluginConfigurationBean getState() {
		return this;
	}

	public void loadState(IdeaPluginConfigurationBean state) {
		setConfiguration(state);
		transientSetHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());
	}

}