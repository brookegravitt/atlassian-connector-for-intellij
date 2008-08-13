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

import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AutoRenewBambooSessionTest extends TestCase {
	private BambooSession testedSession;
	private BambooSession mockDelegate;
	private static final String LOGIN = "login";
	private static final char[] A_PASSWORD = "password".toCharArray();

	public void setUp() throws Exception {
        super.setUp();

		mockDelegate = EasyMock.createStrictMock(BambooSession.class);
		testedSession = new AutoRenewBambooSession("http://dupa");

		Field field = AutoRenewBambooSession.class.getDeclaredField("delegate");
		field.setAccessible(true);
		field.set(testedSession, mockDelegate);

	}

    public void tearDown() throws Exception {
        super.tearDown();
		EasyMock.verify(mockDelegate);
	}

	public void testLogin() throws Exception {
		mockDelegate.login(LOGIN, A_PASSWORD);
		EasyMock.expectLastCall().andThrow(new RemoteApiLoginException(""));
		EasyMock.replay(mockDelegate);

		try {
			testedSession.login(LOGIN, A_PASSWORD);
			fail();
		} catch (RemoteApiLoginException e) {
			//expected
		}

		EasyMock.verify(mockDelegate);
	}

	public void testLogout() throws Exception {
		mockDelegate.login(LOGIN, A_PASSWORD);
		EasyMock.expectLastCall();
		mockDelegate.logout();
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.logout();

		EasyMock.verify(mockDelegate);
	}

	public void testListProjectNames() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listProjectNames();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listProjectNames();
		EasyMock.expectLastCall().andReturn(Arrays.asList(new BambooProject[]{new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}, new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}, new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}}));
		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		List<BambooProject> projects = testedSession.listProjectNames();

		assertNotNull(projects);
		assertEquals(3, projects.size());

		EasyMock.verify(mockDelegate);
	}

	public void testListPlanNames() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listPlanNames();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listPlanNames();
		EasyMock.expectLastCall().andReturn(Arrays.asList(new BambooPlan[]{new BambooPlan() {
			public String getPlanName() {
				return "planName1";
			}
			public String getPlanKey() {
				return "planKey1";
			}
			public boolean isFavourite() {
				return false;
			}
			public boolean isEnabled() {
				return false;
			}
		}, new BambooPlan() {
			public String getPlanName() {
				return "planName2";
			}
			public String getPlanKey() {
				return "planKey2";
			}
			public boolean isFavourite() {
				return false;
			}
			public boolean isEnabled() {
				return false;
			}
		}} ));

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		List<BambooPlan> plans = testedSession.listPlanNames();
		assertNotNull(plans);
		assertEquals(2, plans.size());

		EasyMock.verify(mockDelegate);
	}

	public void testGetLatestBuildForPlan() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getLatestBuildForPlan("planKey");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getLatestBuildForPlan("planKey");
		EasyMock.expectLastCall().andReturn(new BambooBuild() {
			public BambooServerCfg getServer() {
				return null;  
			}

			public String getServerUrl() {
				return null;
			}

			public String getProjectName() {
				return null;
			}

			public String getProjectKey() {
				return null;
			}

			public String getProjectUrl() {
				return null;
			}

			public String getBuildUrl() {
				return null;
			}

			public String getBuildName() {
				return null;
			}

			public String getBuildKey() {
				return null;
			}

			public boolean getEnabled() {
				return false;
			}

			public String getBuildNumber() {
				return null;
			}

			public String getBuildResultUrl() {
				return null;
			}

			public BuildStatus getStatus() {
				return null;
			}

			public String getMessage() {
				return null;
			}

			public int getTestsPassed() {
				return 0;
			}

			public int getTestsFailed() {
				return 0;
			}

			public Date getBuildTime() {
				return null;
			}

			public String getBuildReason() {
				return null;
			}

			public String getBuildRelativeBuildDate() {
				return null;
			}

			public void setPollingTime(Date date) {
			}
			
			public Date getPollingTime() {
				return null;
			}
		});

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		BambooBuild build = testedSession.getLatestBuildForPlan("planKey");
		assertNotNull(build);

		EasyMock.verify(mockDelegate);
	}

	public void testGetFavouriteUserPlans() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getFavouriteUserPlans();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getFavouriteUserPlans();
		EasyMock.expectLastCall().andReturn(Arrays.asList(new String[] {"plan1", "plan2", "plan3"}));

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		List<String> plans = testedSession.getFavouriteUserPlans();
		assertNotNull(plans);
		assertEquals(3, plans.size());

		EasyMock.verify(mockDelegate);

	}

	public void testGetBuildResultDetails() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBuildResultDetails("buildKey", "buildNumber");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBuildResultDetails("buildKey", "buildNumber");
		EasyMock.expectLastCall().andReturn(new BuildDetails() {

			public String getVcsRevisionKey() {
				return null;
			}

			public List<TestDetails> getSuccessfulTestDetails() {
				return null;
			}

			public List<TestDetails> getFailedTestDetails() {
				return null;  
			}

			public List<BambooChangeSet> getCommitInfo() {
				return null;
			}
		});

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		BuildDetails build = testedSession.getBuildResultDetails("buildKey", "buildNumber");
		assertNotNull(build);

		EasyMock.verify(mockDelegate);
	}

	public void testAddLabelToBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addLabelToBuild("buildKey", "buildNumber", "label");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addLabelToBuild("buildKey", "buildNumber", "label");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.addLabelToBuild("buildKey", "buildNumber", "label");

		EasyMock.verify(mockDelegate);
	}

	public void testAddCommentToBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addCommentToBuild("buildKey", "buildNumber", "comment");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addCommentToBuild("buildKey", "buildNumber", "comment");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.addCommentToBuild("buildKey", "buildNumber", "comment");

		EasyMock.verify(mockDelegate);
	}

	public void testExecuteBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.executeBuild("buildKey");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.executeBuild("buildKey");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.executeBuild("buildKey");

		EasyMock.verify(mockDelegate);
	}	

	public void testGetBambooBuildNumber() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBamboBuildNumber();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBamboBuildNumber();
		EasyMock.expectLastCall().andReturn(770);

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.getBamboBuildNumber();

		EasyMock.verify(mockDelegate);
	}


	public void testIsLoggedIn() throws RemoteApiLoginException {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.isLoggedIn();
		EasyMock.expectLastCall().andReturn(true);
		mockDelegate.logout();
		mockDelegate.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		assertTrue(testedSession.isLoggedIn());
		testedSession.logout();
		assertFalse(testedSession.isLoggedIn());

		EasyMock.verify(mockDelegate);
	}

}
