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

package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.api.bamboomock.ErrorResponse;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

public abstract class Util {

	private static final String RESOURCE_BASE = "/mock/crucible/api/rest/";


	private Util() {
	}

	static void copyResource(OutputStream outputStream, String resource) {
		BufferedInputStream is = new BufferedInputStream(com.atlassian.theplugin.crucible.api.rest.cruciblemock.Util.class.getResourceAsStream(RESOURCE_BASE + resource));
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
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-140", build.getBuildResultUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getBuildUrl());
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
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-141", build.getBuildResultUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getBuildUrl());
		Assert.assertNull(build.getMessage());
	}

	public static void verifyErrorBuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
	}

	public static void verifyLoginErrorBuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
	}

	public static void verifyError400BuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertTrue(build.getMessage().startsWith(
				ErrorResponse.getStaticErrorMessage(400, "Bad Request")));
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

	private static final String[][] expectedFavouritePlans = {
			{ "PO-FP", "First Project - First Plan" },
			{ "PT-TOP", "Second Project - The Only Plan" }
	};

	public static void verifyFavouriteListResult(Collection<String> plans) {
		assertEquals(expectedFavouritePlans.length, plans.size());
		Iterator<String> iterator = plans.iterator();
		for (String[] pair : expectedFavouritePlans) {
			String plan = iterator.next();
			assertEquals(pair[0], plan);
		}
	}

	private static final String[][] expectedPlansWithFavourites = {
			{ "PO-FP", "First Project - First Plan", "true" },
			{ "PO-SECPLAN", "First Project - Second Plan", "false" },
			{ "PO-TP", "First Project - Third Plan", "false" },
			{ "PT-TOP", "Second Project - The Only Plan", "true" }
	};

	public static void verifyPlanListWithFavouritesResult(Collection<BambooPlan> plans) {
		assertEquals(expectedPlansWithFavourites.length, plans.size());
		Iterator<BambooPlan> iterator = plans.iterator();
		for (String[] pair : expectedPlansWithFavourites) {
			BambooPlan plan = iterator.next();
			assertEquals(pair[0], plan.getPlanKey());
			assertEquals(pair[1], plan.getPlanName());
			if ("true".equalsIgnoreCase(pair[2])) {
				Assert.assertTrue(plan.isFavourite());
			}
		}
	}

}