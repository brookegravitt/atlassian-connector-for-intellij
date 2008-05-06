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
import com.atlassian.theplugin.commons.Server;

/**
 * Represents Bambbo server on servers JTree
 * User: mwent
 * Date: 2008-01-31
 * Time: 09:25:29
 */
public class BambooServerNode extends ServerNode {
	static final long serialVersionUID = -6944317541000292469L;
	
	public BambooServerNode(Server aServer) {
		super(aServer);
	}

    public ServerType getServerType() {
        return ServerType.BAMBOO_SERVER;
    }
}
