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

package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;

import javax.swing.tree.DefaultMutableTreeNode;

public class ServerNode extends DefaultMutableTreeNode {
	private final ServerCfg server;

	public ServerNode(ServerCfg aServer) {
		this.server = aServer;
	}

    public ServerType getServerType() {
        return server.getServerType(); 
    }

    public ServerCfg getServer() {
		return server;
	}

    /**
     * Used ultimately by {@link javax.swing.tree.DefaultTreeCellRenderer}. so it must be like this
     * Sorry :(
     *
     * @return server name
     */
    @Override
    public String toString() {
		return server.getName();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ServerNode that = (ServerNode) o;

		if (!server.equals(that.server)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return server.hashCode();
	}
}
