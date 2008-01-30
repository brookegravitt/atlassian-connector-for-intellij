package com.atlassian.theplugin.bamboo.api.bamboomock;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.bamboo.BambooPlan;
import junit.framework.Assert;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

public abstract class Util {

	private static final String RESOURCE_BASE = "/mock/bamboo/1_2_4/api/rest/";


	private Util() {
	}

	static void copyResource(OutputStream outputStream, String resource) {
		BufferedInputStream is = new BufferedInputStream(Util.class.getResourceAsStream(RESOURCE_BASE + resource));
		int c;
		try {
			while ((c = is.read()) != -1) {
				outputStream.write(c);
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public static void verifySuccessfulBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertNotNull(build);
		Assert.assertEquals("TP-DEF", build.getBuildKey());
		Assert.assertEquals("140", build.getBuildNumber());
		//todo: sginter: What should go here? bamboo-provided status or the BuildStatus.toString()
		//assertEquals("Successful", build.getStatus());
		Assert.assertSame(BuildStatus.BUILD_SUCCEED, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals(baseUrl, build.getServerUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-140", build.getBuildUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getPlanUrl());
		Assert.assertNull(build.getMessage());
	}

	public static void verifyFailedBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertNotNull(build);
		Assert.assertEquals("TP-DEF", build.getBuildKey());
		Assert.assertEquals("141", build.getBuildNumber());
		//todo: sginter: What should go here? bamboo-provided status or the BuildStatus.toString()
		//assertEquals("Failed", build.getStatus());
		Assert.assertSame(BuildStatus.BUILD_FAILED, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals(baseUrl, build.getServerUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-141", build.getBuildUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getPlanUrl());
		Assert.assertNull(build.getMessage());
	}

	public static void verifyErrorBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
	}

	public static void verifyLoginErrorBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("Login exception: The user does not have sufficient permissions to perform this action.\n", build.getMessage());
	}

	private static final String[][] expectedProjects = {
			{ "PO", "Project One" },
			{ "PT", "Project Two" },
			{ "PEMPTY", "Project Three - Empty" }
	};

	public static void verifyProjectListResult(Collection<BambooProject> projects) {
		Assert.assertEquals(expectedProjects.length, projects.size());

		Iterator<BambooProject> iterator = projects.iterator();
		for (String[] pair : expectedProjects) {
			BambooProject project = iterator.next();
			Assert.assertEquals(pair[0], project.getProjectKey());
			Assert.assertEquals(pair[1], project.getProjectName());
		}
	}


	private static final String[][] expectedPlans = {
			{ "PO-FP", "First Project - First Plan" },
			{ "PO-SECPLAN", "First Project - Second Plan" },
			{ "PO-TP", "First Project - Third Plan" },
			{ "PT-TOP", "Second Project - The Only Plan" }
	};

	public static void verifyPlanListResult(Collection<BambooPlan> plans) {
		assertEquals(expectedPlans.length, plans.size());
		Iterator<BambooPlan> iterator = plans.iterator();
		for (String[] pair : expectedPlans) {
			BambooPlan plan = iterator.next();
			assertEquals(pair[0], plan.getPlanKey());
			assertEquals(pair[1], plan.getPlanName());
		}
	}
}
