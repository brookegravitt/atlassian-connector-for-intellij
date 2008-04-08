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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.util.InfoServer;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 11:30:45
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmPluginUpdateHandler implements Runnable {
	private static ConfirmPluginUpdateHandler instance;
	private InfoServer.VersionInfo versionInfo;
	private PluginUpdateIcon display;

	public void run() {
		if (display != null) {
			display.triggerUpdateAvailableAction(versionInfo);
		}
	}	

	public static synchronized ConfirmPluginUpdateHandler getInstance() {
		if (instance == null) {
			instance = new ConfirmPluginUpdateHandler();
		}
		return instance;
	}

	public void setNewVersionInfo(InfoServer.VersionInfo newVersionInfo) {
		this.versionInfo = newVersionInfo;
	}

	public void setDisplay(PluginUpdateIcon display) {
		this.display = display;
	}
}
