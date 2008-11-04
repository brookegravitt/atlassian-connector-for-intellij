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

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.idea.PluginToolWindow;

public final class Util {
    private Util() {
    }

    public static ServerType toolWindowPanelsToServerType(PluginToolWindow.ToolWindowPanels panel) throws ThePluginException {
		switch (panel) {
			case BAMBOO:
				return ServerType.BAMBOO_SERVER;
			case CRUCIBLE:			
				return ServerType.CRUCIBLE_SERVER;
			case JIRA:
				return ServerType.JIRA_SERVER;
			case ISSUES:
				return ServerType.JIRA_SERVER;				
			default:
				throw new ThePluginException("Unrecognized tool window type");
		}
	}
}
