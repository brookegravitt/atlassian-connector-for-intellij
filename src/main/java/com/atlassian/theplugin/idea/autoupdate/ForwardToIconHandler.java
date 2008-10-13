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

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 13, 2008
 * Time: 12:17:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ForwardToIconHandler implements UpdateActionHandler {
	private GeneralConfigurationBean generalConfiguration;

	public ForwardToIconHandler(GeneralConfigurationBean generalConfiguration) {
		this.generalConfiguration = generalConfiguration;
	}

	public void doAction(InfoServer.VersionInfo versionInfo, boolean showConfigPath) throws ThePluginException {
		if (!versionInfo.getVersion().equals(generalConfiguration.getRejectedUpgrade())) {
			ConfirmPluginUpdateHandler handler = ConfirmPluginUpdateHandler.getInstance();
			handler.setNewVersionInfo(versionInfo);
			ApplicationManager.getApplication().invokeLater(handler);
		}
	}
}
