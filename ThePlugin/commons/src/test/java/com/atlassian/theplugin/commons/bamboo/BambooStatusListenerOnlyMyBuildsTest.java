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

import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createStrictMock;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.HashSet;

/**
 * @author Jacek Jaroczynski
 */
public class BambooStatusListenerOnlyMyBuildsTest extends TestCase {
	private BambooStatusDisplay displayMock;
	private BambooStatusTooltipListener tooltipListener;
	private static final String DEFAULT_PLAN_ID = "DEF";
	private static final String DEFAULT_BUILD_NAME = "Build";
	private static final String DEFAULT_PROJECT_NAME = "Project";
	private static final String DEFAULT_SERVER_URL = "Server URL";
	private static final String DEFAULT_ERROR_MESSAGE = "Error message";
	private static final HashSet<String> COMMITERS = new HashSet<String>(Arrays.asList("jjaroczynski", "unknown"));
	private static final String LOGGED_USER_JJ = "jjaroczynski";
	private static final String LOGGED_USER_US = "user";
	private static final String LOGGED_USER_UN = "unknown";
	private static final String ServerID = UUID.randomUUID().toString();
	private PluginConfigurationBean conf;
	private BambooConfigurationBean bean;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		displayMock = createStrictMock(BambooStatusDisplay.class);

		conf = new PluginConfigurationBean();
		bean = new BambooConfigurationBean();
		conf.setBambooConfigurationData(bean);
		bean.setBambooTooltipOption(BambooTooltipOption.ALL_FAULIRES_AND_FIRST_SUCCESS);
	}


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		displayMock = null;
		tooltipListener = null;
	}


	public void testOnlyMyOn() {

		bean.setOnlyMyBuilds(true);
		tooltipListener = new BambooStatusTooltipListener(displayMock, conf);

		BambooBuild buildOK_JJ = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "1", LOGGED_USER_JJ);
		BambooBuild buildFail_JJ = generateBuildInfo(BuildStatus.BUILD_FAILED, "2", LOGGED_USER_JJ);
		BambooBuild buildFail_US = generateBuildInfo(BuildStatus.BUILD_FAILED, "3", LOGGED_USER_US);
		BambooBuild buildOK_US = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "4", LOGGED_USER_US);
		BambooBuild buildFail_UN = generateBuildInfo(BuildStatus.BUILD_FAILED, "5", LOGGED_USER_UN);
		BambooBuild buildOK_UN = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "6", LOGGED_USER_UN);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), EasyMock.isA(BambooPopupInfo.class));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_JJ));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_JJ));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_UN));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_UN));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_US));
	}

	public void testOnlyMyOff() {

		bean.setOnlyMyBuilds(false);
		tooltipListener = new BambooStatusTooltipListener(displayMock, conf);

		BambooBuild buildOK_JJ = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "1", LOGGED_USER_JJ);
		BambooBuild buildFail_JJ = generateBuildInfo(BuildStatus.BUILD_FAILED, "2", LOGGED_USER_JJ);
		BambooBuild buildFail_US = generateBuildInfo(BuildStatus.BUILD_FAILED, "3", LOGGED_USER_US);
		BambooBuild buildOK_US = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "4", LOGGED_USER_US);
		BambooBuild buildFail_UN = generateBuildInfo(BuildStatus.BUILD_FAILED, "5", LOGGED_USER_UN);
		BambooBuild buildOK_UN = generateBuildInfo(BuildStatus.BUILD_SUCCEED, "6", LOGGED_USER_UN);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), EasyMock.isA(BambooPopupInfo.class));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_JJ));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_JJ));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_UN));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_UN));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US));
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_US));
	}

	public static BambooBuildInfo generateBuildInfo(BuildStatus status, String buildNumber, String loggedUser) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setBuildKey(DEFAULT_PLAN_ID);
		buildInfo.setBuildName(DEFAULT_BUILD_NAME);
		buildInfo.setBuildNumber(buildNumber);
        buildInfo.setProjectName(DEFAULT_PROJECT_NAME);
        buildInfo.setServerUrl(DEFAULT_SERVER_URL);
		buildInfo.setEnabled(true);
		buildInfo.setCommiters(COMMITERS);

		switch (status) {
			case UNKNOWN:
				buildInfo.setBuildState("Unknown");
				buildInfo.setMessage(DEFAULT_ERROR_MESSAGE);
				break;
			case BUILD_SUCCEED:
				buildInfo.setBuildState(BambooBuildInfo.BUILD_SUCCESSFUL);
				buildInfo.setBuildTime(new Date());
				break;
			case BUILD_FAILED:
				buildInfo.setBuildState(BambooBuildInfo.BUILD_FAILED);
				buildInfo.setBuildTime(new Date());
				break;
			case BUILD_DISABLED:
				buildInfo.setBuildState(BambooBuildInfo.BUILD_FAILED);
				buildInfo.setBuildTime(new Date());
				buildInfo.setEnabled(false);
				break;

		}
		buildInfo.setPollingTime(new Date());

		BambooServerCfg server = new BambooServerCfg("name", new ServerId(ServerID));
		server.setUsername(loggedUser);
		buildInfo.setServer(server);

		return buildInfo;
	}

}
