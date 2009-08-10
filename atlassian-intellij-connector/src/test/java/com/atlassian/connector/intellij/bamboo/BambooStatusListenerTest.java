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

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.createStrictMock;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import java.util.Arrays;
import junit.framework.TestCase;

public class BambooStatusListenerTest extends TestCase {

	private BambooStatusDisplay displayMock;
	private BambooStatusTooltipListener tooltipListener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		displayMock = createStrictMock(BambooStatusDisplay.class);
		tooltipListener = new BambooStatusTooltipListener(displayMock, null);
	}


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		displayMock = null;
		tooltipListener = null;
	}

	public void testSingleBuildFailed() {
		BambooBuildAdapter buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail), null);

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildSucceed() {
		BambooBuildAdapter build = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(build), null);

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildUnknown() {
		BambooBuildAdapter build = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(build), null);

		EasyMock.verify(displayMock);
	}


	public static class PopupInfoHtmlContains implements IArgumentMatcher {
		private final String expected;

		public PopupInfoHtmlContains(String expected) {
			this.expected = expected;
		}

		public boolean matches(Object actual) {
			if (!(actual instanceof BambooPopupInfo)) {
				return false;
			}
			String actualMessage = ((BambooPopupInfo) actual).toHtml();

			return actualMessage.matches(expected);
		}

		public void appendTo(StringBuffer buffer) {
			buffer.append("xxx (");
			buffer.append(expected);
			buffer.append(")");
		}
	}

	public static BambooPopupInfo findPopupInfo(String pattern) {
		EasyMock.reportMatcher(new PopupInfoHtmlContains(pattern));
		return null;
	}

	public void testSingleBuildOK2Failed() {
		BambooBuildAdapter buildOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);
		BambooBuildAdapter buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), findPopupInfo(".*red.*failed.*"));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail), null);

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildFailed2OK() {
		BambooBuildAdapter buildOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);
		BambooBuildAdapter buildFail = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), findPopupInfo(".*green.*succeed.*"));

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildFail), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildOK), null);

		EasyMock.verify(displayMock);
	}

	public void testSingleBuildSequence() {
		BambooBuildAdapter buildUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildSucceeded = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);
		BambooBuildAdapter buildFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);

		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), findPopupInfo(".*red.*failed.*"));
		EasyMock.replay(displayMock);

		tooltipListener.updateBuildStatuses(Arrays.asList(buildFailed), null);
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFailed), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFailed), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFailed), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), findPopupInfo(".*green.*succeed.*"));
		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		EasyMock.verify(displayMock);
		EasyMock.reset(displayMock);

		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildSucceeded), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildUnknown), null);
		EasyMock.verify(displayMock);
	}

	public void testMultipleOK2Failed() {
		BambooBuildAdapter buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);
		BambooBuildAdapter buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);

		BambooBuildAdapter buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.SUCCESS);
		BambooBuildAdapter buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.FAILURE);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), findPopupInfo(".*red.*failed.*"));
		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondOK), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondFailed), null);
		EasyMock.verify(displayMock);

	}

	public void testMultipleFailed2OK() {
		BambooBuildAdapter buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);
		BambooBuildAdapter buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);

		BambooBuildAdapter buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.SUCCESS);
		BambooBuildAdapter buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.FAILURE);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.SUCCESS), findPopupInfo(".*green.*succeed.*"));
		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondFailed), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondOK), null);
		EasyMock.verify(displayMock);

	}

	public void testMultiple() {
		BambooBuildAdapter buildFirstUnknown = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildFirstFailed = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.FAILURE);
		BambooBuildAdapter buildFirstOK = HtmlBambooStatusListenerTest.generateBuildInfo(BuildStatus.SUCCESS);

		BambooBuildAdapter buildSecondUnknown = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.UNKNOWN);
		BambooBuildAdapter buildSecondOK = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.SUCCESS);
		BambooBuildAdapter buildSecondFailed = HtmlBambooStatusListenerTest.generateBuildInfo2(BuildStatus.FAILURE);

		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), and(findPopupInfo(".*green.*succeed.*"), findPopupInfo(".*red.*failed.*")));
		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondOK), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondFailed), null);
		EasyMock.verify(displayMock);

		EasyMock.reset(displayMock);
		displayMock.updateBambooStatus(EasyMock.eq(BuildStatus.FAILURE), and(findPopupInfo(".*green.*succeed.*"), findPopupInfo(".*red.*failed.*")));
		EasyMock.replay(displayMock);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstUnknown, buildSecondUnknown), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstOK, buildSecondFailed), null);
		tooltipListener.updateBuildStatuses(Arrays.asList(buildFirstFailed, buildSecondOK), null);
		EasyMock.verify(displayMock);

	}

} 
