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
package com.atlassian.connector.intellij.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.fisheye.api.model.FisheyePathHistoryItem;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Collection;

/**
 * User: pmaruszak
 */
public interface FishEyeServerFacade extends ProductServerFacade {

	Collection<String> getRepositories(final ServerData server) throws RemoteApiException;

    Collection<FisheyePathHistoryItem> getPathHistory(final ConnectionCfg server, String repo, String path)
            throws RemoteApiException;
}
