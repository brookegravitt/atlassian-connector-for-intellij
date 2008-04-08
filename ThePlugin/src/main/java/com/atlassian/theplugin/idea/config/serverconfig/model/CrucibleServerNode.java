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

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.ServerType;

/**
 * Represents Crucible Server in servers JTree
 * User: mwent
 * Date: 2008-01-29
 * Time: 09:51:00
 */
public class CrucibleServerNode extends ServerNode {
	static final long serialVersionUID = -5578412486422465295L;
	
	public CrucibleServerNode(Server aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.CRUCIBLE_SERVER;
    }
}
