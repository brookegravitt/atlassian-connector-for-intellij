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

import java.util.Collection;

/**
 * Bamboo global configuration.
 *
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:46:29 AM
 */
public interface ProductServerConfiguration {
    Collection<Server> getServers();
	Collection<Server> getEnabledServers();
	Server getServer(Server aServer);
    void storeServer(Server server);
	void setServers(Collection<Server> servers);
	void removeServer(Server server);

	boolean isAnyServerEnabled();
	boolean isAnyServer();
}
