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

package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.ServerType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-28
 * Time: 12:09:24
 * To change this template use File | Settings | File Templates.
 */
public class AddBambooServerAction extends AnAction {	
	public void actionPerformed(AnActionEvent event) {
        ConfigPanel.getInstance().addServer(ServerType.BAMBOO_SERVER);		
	}
}
