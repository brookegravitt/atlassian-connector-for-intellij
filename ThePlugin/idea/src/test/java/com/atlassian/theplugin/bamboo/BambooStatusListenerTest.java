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

package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ServerBean;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class BambooStatusListenerTest extends TestCase {

	private BambooStatusDisplay displayMock;
	private BambooStatusListenerImpl listenerImpl;

	protected void setUp() throws Exception {
		super.setUp();

		displayMock = createStrictMock(BambooStatusDisplay.class);
		listenerImpl = new BambooStatusListenerImpl(displayMock, createBambooTestConfiguration());
	}

	private static PluginConfigurationBean createBambooTestConfiguration() {
		BambooConfigurationBean configuration = new BambooConfigurationBean();

		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		configuration.setServersData(servers);
		PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
		pluginConfig.setBambooConfigurationData(configuration);

		return pluginConfig;
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		displayMock = null;
		listenerImpl = null;
	}

	public void testSingleBuildFailed() {
		BambooBuild buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(buildFail));

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildSucceed() {
		BambooBuild build = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(build));

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildUnknown() {
		BambooBuild build = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(build));

		EasyMock.verify(displayMock);
	}


	public void testSingleBuildOK2Failed() {
		BambooBuild buildOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), find("red.*failed"));

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(buildOK));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFail));

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildFailed2OK() {
		BambooBuild buildOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), find("green.*succeed"));

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(buildFail));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildOK));

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildSequence() {
		BambooBuild buildUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuild buildSucceeded = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);

		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), find("red.*failed"));
		EasyMock.replay(displayMock);

		listenerImpl.updateBuildStatuses(Arrays.asList(buildFailed));
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFailed));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFailed));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFailed));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), find("green.*succeed"));
		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildSucceeded));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildUnknown));
		EasyMock.verify(displayMock);
	}

	public void testMultipleOK2Failed() {
		BambooBuild buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuild buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);
		BambooBuild buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);

		BambooBuild buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuild buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_FAILED);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), find("red.*failed"));
		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondOK));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondFailed));
		EasyMock.verify(displayMock);

	}

	public void testMultipleFailed2OK() {
		BambooBuild buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuild buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);
		BambooBuild buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);

		BambooBuild buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuild buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_FAILED);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_SUCCEED), find("green.*succeed"));
		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondFailed));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondOK));
		EasyMock.verify(displayMock);

	}

	public void testMultiple() {
		BambooBuild buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuild buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_FAILED);
		BambooBuild buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.BUILD_SUCCEED);

		BambooBuild buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuild buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_SUCCEED);
		BambooBuild buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.BUILD_FAILED);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), and(find("green.*succeed"), find("red.*failed")));
		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondOK));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondFailed));
		EasyMock.verify(displayMock);

		EasyMock.reset(displayMock);
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.BUILD_FAILED), and(find("green.*succeed"), find("red.*failed")));
		EasyMock.replay(displayMock);
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondFailed));
		listenerImpl.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondOK));
		EasyMock.verify(displayMock);

	}

}
