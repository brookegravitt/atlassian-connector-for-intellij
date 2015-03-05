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

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.Connector;
import org.jetbrains.annotations.NotNull;

public class CrucibleConnector implements Connector {
	private final CrucibleServerFacade facade;
	private volatile boolean isFisheye;
	private FishEyeServerFacade fishEyeServerFacade;

	public CrucibleConnector(@NotNull final CrucibleServerFacade facade,
			@NotNull final FishEyeServerFacade fishEyeServerFacade) {
		this.facade = facade;
		this.fishEyeServerFacade = fishEyeServerFacade;
	}

	public synchronized void connect(final ServerData server) throws RemoteApiException {
		isFisheye = false;
		facade.testServerConnection(server);
		try {
			fishEyeServerFacade.testServerConnection(server);
			isFisheye = true;
		} catch (RemoteApiException e) {
			// it's apparently not a FishEye instance
		}
	}

	public boolean isFisheye() {
		return isFisheye;
	}
}









		