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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.RequestData;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;

import java.util.Date;

/**
 * Build information retrieved from Bamboo server.
 */
public interface BambooBuild extends RequestData {
	BambooServerCfg getServer();

	String getServerUrl();

    String getProjectName();

    String getProjectKey();

    String getProjectUrl();

	String getBuildUrl();

    String getBuildName();

	String getBuildKey();

	boolean getEnabled();

	String getBuildNumber();

    String getBuildResultUrl();

	BuildStatus getStatus();

	String getMessage();

	int getTestsPassed();

	int getTestsFailed();

	String getBuildReason();

	Date getBuildTime();

	String getBuildRelativeBuildDate();

	boolean isMyBuild();
}
