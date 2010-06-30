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
package com.atlassian.connector.intellij.bamboo;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.BambooTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.easymock.EasyMock.createStrictMock;

/**
 * @author Jacek Jaroczynski
 */
public class BambooStatusListenerOnlyMyBuildsTest extends TestCase {
	private BambooStatusDisplay displayMock;

	private BambooStatusTooltipListener tooltipListener;

	private static final String DEFAULT_PLAN_KEY = "PL-DEF";

	private static final String DEFAULT_BUILD_NAME = "Build";

	private static final String DEFAULT_PROJECT_NAME = "Project";

	private static final String DEFAULT_ERROR_MESSAGE = "Error message";

	private static final HashSet<String> COMMITERS = new HashSet<String>(Arrays.asList("jjaroczynski", "unknown"));

	private static final String LOGGED_USER_JJ = "jjaroczynski";

	private static final String LOGGED_USER_US = "user";

	private static final String LOGGED_USER_UN = "unknown";

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

		BambooBuildAdapter buildOK_JJ = generateBuildInfo(BuildStatus.SUCCESS, 1, LOGGED_USER_JJ);
		BambooBuildAdapter buildFail_JJ = generateBuildInfo(BuildStatus.FAILURE, 2, LOGGED_USER_JJ);
		BambooBuildAdapter buildFail_US = generateBuildInfo(BuildStatus.FAILURE, 3, LOGGED_USER_US);
		BambooBuildAdapter buildOK_US = generateBuildInfo(BuildStatus.SUCCESS, 4, LOGGED_USER_US);
		BambooBuildAdapter buildFail_UN = generateBuildInfo(BuildStatus.FAILURE, 5, LOGGED_USER_UN);
		BambooBuildAdapter buildOK_UN = generateBuildInfo(BuildStatus.SUCCESS, 6, LOGGED_USER_UN);
        
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), EasyMock.isA(BambooPopupInfo.class));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_JJ), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_JJ), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_UN), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_UN), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_US), null);
	}

	public void testOnlyMyOff() {

		bean.setOnlyMyBuilds(false);
		tooltipListener = new BambooStatusTooltipListener(displayMock, conf);

		BambooBuildAdapter buildOK_JJ = generateBuildInfo(BuildStatus.SUCCESS, 1, LOGGED_USER_JJ);
		BambooBuildAdapter buildFail_JJ = generateBuildInfo(BuildStatus.FAILURE, 2, LOGGED_USER_JJ);
		BambooBuildAdapter buildFail_US = generateBuildInfo(BuildStatus.FAILURE, 3, LOGGED_USER_US);
		BambooBuildAdapter buildOK_US = generateBuildInfo(BuildStatus.SUCCESS, 4, LOGGED_USER_US);
		BambooBuildAdapter buildFail_UN = generateBuildInfo(BuildStatus.FAILURE, 5, LOGGED_USER_UN);
		BambooBuildAdapter buildOK_UN = generateBuildInfo(BuildStatus.SUCCESS, 6, LOGGED_USER_UN);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), EasyMock.isA(BambooPopupInfo.class));
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), EasyMock.isA(BambooPopupInfo.class));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_JJ), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_JJ), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_UN), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_UN), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail_US), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK_US), null);
	}

	public static BambooBuildAdapter generateBuildInfo(BuildStatus status, int buildNumber, String loggedUser) {
		BambooBuildInfo.Builder builder =
				new BambooBuildInfo.Builder(DEFAULT_PLAN_KEY, DEFAULT_BUILD_NAME,
						new ConnectionCfg("name", "", loggedUser, ""),
				DEFAULT_PROJECT_NAME, buildNumber, status).enabled(true).commiters(COMMITERS);

		switch (status) {
		case UNKNOWN:
			builder.errorMessage(DEFAULT_ERROR_MESSAGE);
			break;
		case SUCCESS:
			builder.startTime(new Date());
			break;
		case FAILURE:
			builder.startTime(new Date());
			break;
		}

		return new BambooBuildAdapter(builder.build(), new BambooServerData(new BambooServerCfg(true, "mybamboo",
				new ServerIdImpl()), new UserCfg("", "")));
	}

}
