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
package com.atlassian.theplugin.cfg;

import com.atlassian.theplugin.commons.cfg.AbstractCfgManager;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

/**
 * User: pmaruszak
 */

/*@State(name = "atlassian-ide-plugin-cfg-manager-idea",
		storages = {@Storage(id = "atlassian-ide-plugin-cfg-manager-id", file = "$WORKSPACE_FILE$")})
public class IdeaCfgManager extends AbstractCfgManager implements PersistentStateComponent<IdeaCfgManager> {*/
	public class IdeaCfgManager extends AbstractCfgManager {
	private UserCfg defaultCredentials;

	public ServerData getServerData(final Server serverCfg) {
		if (serverCfg != null) {
			String userName = serverCfg.getUserName();
			String password = serverCfg.getPassword();

			if (defaultCredentials != null) {
				userName = defaultCredentials.getUserName();
				password = defaultCredentials.getPassword();
			}
			return new ServerData(serverCfg.getName(), serverCfg.getServerId().toString(), userName,
					password, serverCfg.getUrl());
		}

		return null;
	}

	
	public IdeaCfgManager getState() {
		return this;
	}

	public void loadState(final IdeaCfgManager ideaCfgManager) {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	public void copyData(final IdeaCfgManager cfgManager) {
		this.defaultCredentials = cfgManager.defaultCredentials;
	}

	public UserCfg getDefaultCredentials() {
		return defaultCredentials;
	}

	public void setDefaultCredentials(final UserCfg defaultCredentials) {
		this.defaultCredentials = defaultCredentials;
	}

}
