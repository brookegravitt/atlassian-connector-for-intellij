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
package com.atlassian.theplugin.idea.crucible.tree.node;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

/**
 * @author Jacek Jaroczynski
 */
public class CrucibleReviewServerTreeNode extends CrucibleReviewGroupTreeNode {
	private ServerData crucibleServer;

	public CrucibleReviewServerTreeNode(ProjectCfgManager cfgManager, ServerData server) {
		super(determineName(cfgManager, server), null, null);

		this.crucibleServer = server;
	}

	public ServerData getCrucibleServer() {
		return crucibleServer;
	}

    private static String determineName(ProjectCfgManager projectCfgManager, ServerData server) {
        if (server != null && projectCfgManager != null && projectCfgManager.getServerr(server.getServerId()) != null) {
            ServerData s = projectCfgManager.getServerr(server.getServerId());
            if (s != null) {
                return s.getName();
            }
            return "";
        }
        return server != null ? server.getName() : "";
    }

}
