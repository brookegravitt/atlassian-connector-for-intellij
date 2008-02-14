package com.atlassian.theplugin.bamboo;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.find;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-14
 * Time: 10:50:36
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusListenerTest extends TestCase {

	BambooStatusDisplay displayMock;
	BambooStatusListenerImpl listenerImpl;

	protected void setUp() throws Exception {
		super.setUp();

		displayMock = createStrictMock(BambooStatusDisplay.class);
		listenerImpl = new BambooStatusListenerImpl(displayMock);
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
