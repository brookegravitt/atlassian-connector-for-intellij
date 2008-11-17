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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.LoginDataProvided;
import org.jetbrains.annotations.NotNull;

public class CrucibleConnector implements Connector {
	private CrucibleServerFacade facade;
	private FishEyeServerFacade fishEyeServerFacade;
	private boolean isFisheye;

	public CrucibleConnector(@NotNull final CrucibleServerFacade facade,
			@NotNull final FishEyeServerFacade fishEyeServerFacade) {
		this.facade = facade;
		this.fishEyeServerFacade = fishEyeServerFacade;
	}

	public void connect(LoginDataProvided loginDataProvided) throws ThePluginException {
		isFisheye = false;
		try {
			facade.testServerConnection(loginDataProvided.getServerUrl(), loginDataProvided.getUserName(),
					loginDataProvided.getPassword());
			try {
				fishEyeServerFacade.testServerConnection(loginDataProvided.getServerUrl(), loginDataProvided.getUserName(),
						loginDataProvided.getPassword());
				isFisheye = true;
			} catch (RemoteApiException e) {
				// it's apparently not a FishEye instance
			}

		} catch (RemoteApiException e) {
			throw new ThePluginException(e.getMessage(), e);
		}
	}

	public boolean isFisheye() {
		return isFisheye;
	}
}
