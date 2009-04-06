/*
  Copyright (C) 2008 Atlassian
 
  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class JIRAServerModelImplTest extends TestCase {

	private JIRATestServerFacade facade;

	public void setUp() throws Exception {
		super.setUp();
		facade = new JIRATestServerFacade();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetProjects() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		List<JIRAProject> projects = null;
		try {
			projects = model.getProjects(new JiraServerCfg("test", new ServerId()));
		} catch (JIRAException e) {
			fail();
		}
		// should be 3 projects from the facade + "Any"
		assertEquals(4, projects.size());
	}

	public void testGetProjectsNull() {
		try {
			JIRAServerModelImpl model = new JIRAServerModelImpl();
			List<JIRAProject> projects = model.getProjects(null);
			assertNull(projects);
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testGetServerFromCache() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		facade.counter = 0;
		JiraServerCfg cfg = new JiraServerCfg("test", new ServerId());
		try {
			model.getProjects(cfg);
			model.getProjects(cfg);
			model.getProjects(cfg);
		} catch (JIRAException e) {
			fail();
		}
		assertEquals(1, facade.counter);
	}

	public void testClear() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JiraServerCfg cfg1 = new JiraServerCfg("test1", new ServerId());
		JiraServerCfg cfg2 = new JiraServerCfg("test2", new ServerId());

		try {
			model.setFacade(facade);
			model.getProjects(cfg1);
			model.getProjects(cfg2);
			model.clear(cfg1);
			facade.counter = 0;
			model.getProjects(cfg1);
			assertEquals(1, facade.counter);
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testClearAll() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JiraServerCfg cfg1 = new JiraServerCfg("test1", new ServerId());
		JiraServerCfg cfg2 = new JiraServerCfg("test2", new ServerId());
		try {
			model.setFacade(facade);
			model.getProjects(cfg1);
			model.getProjects(cfg2);
			model.clearAll();

			facade.counter = 0;
			model.getProjects(cfg1);
			model.getProjects(cfg2);
			assertEquals(2, facade.counter);
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testStatuses() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAConstant> statuses = model.getStatuses(new JiraServerCfg("test", new ServerId()));
			// should be 3 from the facade + "Any"
			assertEquals(4, statuses.size());
		} catch (JIRAException e) {
			fail();
		}


	}

	public void testStatusesWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAConstant> statuses = new ArrayList<JIRAConstant>();
		facade.throwException = true;
		model.setFacade(facade);
		try {
			statuses = model.getStatuses(new JiraServerCfg("test", new ServerId()));
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, statuses.size());
		}

	}

	public void testIssueTypesNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAConstant> issueTypes = model.getIssueTypes(new JiraServerCfg("test", new ServerId()), null, true);
			// should be 3 from the facade + "Any"
			assertEquals(4, issueTypes.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testIssueTypesWithProject() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");

		try {
			model.setFacade(facade);
			List<JIRAConstant> issueTypes = model.getIssueTypes(new JiraServerCfg("test", new ServerId()), p, true);
			// should be 3 from the facade + "Any"
			assertEquals(4, issueTypes.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testIssueTypesNoProjectWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			issueTypes = model.getIssueTypes(new JiraServerCfg("test", new ServerId()), null, true);
			fail();
		} catch (JIRAException e) {
			assertEquals(0, issueTypes.size());
			facade.throwException = false;
		}
	}

	public void testIssueTypesWithProjectWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");

		facade.throwException = true;
		model.setFacade(facade);
		try {
			issueTypes = model.getIssueTypes(new JiraServerCfg("test", new ServerId()), p, true);
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, issueTypes.size());
		}
	}

	public void testSavedFilters() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAQueryFragment> filters = model.getSavedFilters(new JiraServerCfg("test", new ServerId()));
			assertEquals(3, filters.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testSavedFiltersWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAQueryFragment> filters = new ArrayList<JIRAQueryFragment>();
		facade.throwException = true;
		model.setFacade(facade);
		try {
			filters = model.getSavedFilters(new JiraServerCfg("test", new ServerId()));
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, filters.size());
		}
	}

	public void testPriorities() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAConstant> priorities = model.getPriorities(new JiraServerCfg("test", new ServerId()));
			// should be 3 from the facade + "Any"
			assertEquals(4, priorities.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testPrioritiesWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAConstant> priorities = new ArrayList<JIRAConstant>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			priorities = model.getPriorities(new JiraServerCfg("test", new ServerId()));
			fail();
		} catch (JIRAException e) {

			facade.throwException = false;
			assertEquals(0, priorities.size());
		}
	}

	public void testComponents() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(new JiraServerCfg("test", new ServerId()), p);
		} catch (JIRAException e) {
		}
		// should be 3 from the facade + "Any" + "no component"
		assertEquals(5, components.size());
	}

	public void testComponentsWithNullProject() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(new JiraServerCfg("test", new ServerId()), null);
		} catch (JIRAException e) {
		}
		assertEquals(0, components.size());
	}

	public void testComponentsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		facade.throwException = true;
		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(new JiraServerCfg("test", new ServerId()), p);
		} catch (JIRAException e) {

		}
		facade.throwException = false;
		assertEquals(0, components.size());
	}

	public void testVersions() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model.getVersions(new JiraServerCfg("test", new ServerId()), p);
			// should be 3 from the facade + "Any" + "No" + "Unreleased" + "Released"
			assertEquals(7, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testVersionsNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model.getVersions(new JiraServerCfg("test", new ServerId()), null);
			assertEquals(0, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testVersionsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		facade.throwException = true;
		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model.getVersions(new JiraServerCfg("test", new ServerId()), p);
			facade.throwException = false;
			assertEquals(0, versions.size());
		} catch (JIRAException e) {

		}
	}

	public void testFixForVersions() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		try {
			List<JIRAFixForVersionBean> versions = model.getFixForVersions(new JiraServerCfg("test", new ServerId()), p);
			// should be 3 from the facade + "Any" + "No" + "Unreleased" + "Released"
			assertEquals(7, versions.size());
		} catch (JIRAException e) {

		}
	}

	public void testFixForVersionsNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAFixForVersionBean> versions = model.getFixForVersions(new JiraServerCfg("test", new ServerId()), null);
			assertEquals(0, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testResolutions() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();

		model.setFacade(facade);
		try {
			List<JIRAResolutionBean> resolutions = model.getResolutions(new JiraServerCfg("test", new ServerId()), true);
			// should be 3 from the facade + "Any" + "Unresolved"
			assertEquals(5, resolutions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testResolutionsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImpl();
		List<JIRAResolutionBean> resolutions = new ArrayList<JIRAResolutionBean>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			resolutions = model.getResolutions(new JiraServerCfg("test", new ServerId()), true);
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, resolutions.size());

		}
	}

	private class JIRATestServerFacade implements JIRAServerFacade {
		public int counter = 0;
		public boolean throwException = false;

		public List<JIRAIssue> getIssues(JiraServerCfg server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size) throws JIRAException {
			return null;
		}

		public List<JIRAIssue> getSavedFilterIssues(JiraServerCfg server, List<JIRAQueryFragment> query, String sort,
				String sortOrder, int start, int size) throws JIRAException {
			return null;
		}

		public List<JIRAProject> getProjects(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAProject> projects = new ArrayList<JIRAProject>();
			projects.add(new JIRAProjectBean());
			projects.add(new JIRAProjectBean());
			projects.add(new JIRAProjectBean());
			++counter;
			return projects;
		}

		public List<JIRAConstant> getIssueTypes(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getStatuses(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAStatusBean(1, "test", null));
			list.add(new JIRAStatusBean(2, "test", null));
			list.add(new JIRAStatusBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getIssueTypesForProject(JiraServerCfg server, String project) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerCfg server, String project)
				throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAQueryFragment> getSavedFilters(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
			list.add(new JIRASavedFilterBean("test", 1));
			list.add(new JIRASavedFilterBean("test", 1));
			list.add(new JIRASavedFilterBean("test", 1));
			return list;
		}

		public List<JIRAComponentBean> getComponents(JiraServerCfg server, String projectKey) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAComponentBean> list = new ArrayList<JIRAComponentBean>();
			list.add(new JIRAComponentBean(1, "test"));
			list.add(new JIRAComponentBean(2, "test"));
			list.add(new JIRAComponentBean(3, "test"));
			return list;
		}

		public List<JIRAVersionBean> getVersions(JiraServerCfg server, String projectKey) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAVersionBean> list = new ArrayList<JIRAVersionBean>();
			list.add(new JIRAVersionBean(1, "test"));
			list.add(new JIRAVersionBean(2, "test"));
			list.add(new JIRAVersionBean(3, "test"));
			list.get(0).setReleased(true);
			list.get(1).setReleased(true);
			list.get(2).setReleased(false);
			return list;
		}

		public List<JIRAConstant> getPriorities(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAPriorityBean(1, "test", null));
			list.add(new JIRAPriorityBean(2, "test", null));
			list.add(new JIRAPriorityBean(3, "test", null));
			return list;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerCfg server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAResolutionBean> list = new ArrayList<JIRAResolutionBean>();
			list.add(new JIRAResolutionBean(1, "test"));
			list.add(new JIRAResolutionBean(2, "test"));
			list.add(new JIRAResolutionBean(3, "test"));
			return list;
		}

		public List<JIRAAction> getAvailableActions(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void progressWorkflowAction(final JiraServerCfg server, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {
		}

		public void addComment(JiraServerCfg server, String issueKey, String comment) throws JIRAException {
		}

		public JIRAIssue createIssue(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueUpdate(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueDetails(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void logWork(JiraServerCfg server, JIRAIssue issue, String timeSpent, Calendar startDate,
				String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(JiraServerCfg server, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(JiraServerCfg server, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		}

		public void testServerConnection(final ServerCfg serverCfg) throws RemoteApiException {
		}

		public ServerType getServerType() {
			return null;
		}

		public JIRAIssue getIssue(JiraServerCfg server, String key) throws JIRAException {
			return null;
		}
	}
}
