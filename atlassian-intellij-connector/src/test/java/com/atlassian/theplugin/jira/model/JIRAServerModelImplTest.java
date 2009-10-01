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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAAttachment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAFixForVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAIssueTypeBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProjectBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASavedFilterBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAStatusBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAUserBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModelImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JIRAServerModelImplTest extends TestCase {

	private JIRATestServerFacade2 facade;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		facade = new JIRATestServerFacade2();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetProjects() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		List<JIRAProject> projects = null;
		try {
			projects = model.getProjects(createServerData("test"));
		} catch (JIRAException e) {
			fail();
		}
		// should be 3 projects from the facade + "Any"
		assertEquals(4, projects.size());
	}

	private JiraServerData createServerData(final String serverName) {
		return new JiraServerData(new JiraServerCfg(true, serverName, new ServerIdImpl(), true) {
			@Override
			public ServerType getServerType() {
				return null;
			}

			@Override
			public JiraServerCfg getClone() {
				return null;
			}
		}, "", "", true);
	}

	public void testGetProjectsNull() {
		try {
			JIRAServerModelImpl model = new JIRAServerModelImplLocal();
			List<JIRAProject> projects = model.getProjects(null);
			assertNull(projects);
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testGetServerFromCache() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		facade.counter = 0;
		JiraServerData cfg = createServerData("test");
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
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JiraServerData cfg1 = createServerData("test1");
		JiraServerData cfg2 = createServerData("test2");

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
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JiraServerData cfg1 = createServerData("test1");
		JiraServerData cfg2 = createServerData("test2");
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
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAConstant> statuses = model.getStatuses(createServerData("test"));
			// should be 3 from the facade + "Any"
			assertEquals(4, statuses.size());
		} catch (JIRAException e) {
			fail();
		}


	}

	public void testStatusesWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAConstant> statuses = new ArrayList<JIRAConstant>();
		facade.throwException = true;
		model.setFacade(facade);
		try {
			statuses = model.getStatuses(createServerData("test"));
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, statuses.size());
		}

	}

	public void testIssueTypesNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAConstant> issueTypes = model
					.getIssueTypes(createServerData("test"), null, true);
			// should be 3 from the facade + "Any"
			assertEquals(4, issueTypes.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testIssueTypesWithProject() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");

		try {
			model.setFacade(facade);
			List<JIRAConstant> issueTypes = model
					.getIssueTypes(createServerData("test"), p, true);
			// should be 3 from the facade + "Any"
			assertEquals(4, issueTypes.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testIssueTypesNoProjectWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			issueTypes = model.getIssueTypes(createServerData("test"), null, true);
			fail();
		} catch (JIRAException e) {
			assertEquals(0, issueTypes.size());
			facade.throwException = false;
		}
	}

	public void testIssueTypesWithProjectWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");

		facade.throwException = true;
		model.setFacade(facade);
		try {
			issueTypes = model.getIssueTypes(createServerData("test"), p, true);
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, issueTypes.size());
		}
	}

	public void testSavedFilters() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAQueryFragment> filters = model.getSavedFilters(createServerData("test"));
			assertEquals(3, filters.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testSavedFiltersWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAQueryFragment> filters = new ArrayList<JIRAQueryFragment>();
		facade.throwException = true;
		model.setFacade(facade);
		try {
			filters = model.getSavedFilters(createServerData("test"));
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, filters.size());
		}
	}

	public void testPriorities() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAPriorityBean> priorities =
					model.getPriorities(createServerData("test"), true);
			// should be 3 from the facade + "Any"
			assertEquals(4, priorities.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testPrioritiesWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAPriorityBean> priorities = new ArrayList<JIRAPriorityBean>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			priorities = model.getPriorities(createServerData("test"), true);
			fail();
		} catch (JIRAException e) {

			facade.throwException = false;
			assertEquals(0, priorities.size());
		}
	}

	public void testComponents() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(createServerData("test"), p, true);
		} catch (JIRAException e) {
		}
		// should be 3 from the facade + "Any" + "no component"
		assertEquals(5, components.size());
	}

	public void testComponentsWithNullProject() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(createServerData("test"), null, true);
		} catch (JIRAException e) {
		}
		assertEquals(0, components.size());
	}

	public void testComponentsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		facade.throwException = true;
		model.setFacade(facade);
		List<JIRAComponentBean> components = Collections.emptyList();
		try {
			components = model.getComponents(createServerData("test"), p, true);
		} catch (JIRAException e) {

		}
		facade.throwException = false;
		assertEquals(0, components.size());
	}

	public void testVersions() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model
					.getVersions(createServerData("test"), p, true);
			// should be 3 from the facade + "Any" + "No" + "Unreleased" + "Released"
			assertEquals(7, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testVersionsNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model
					.getVersions(createServerData("test"), null, true);
			assertEquals(0, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testVersionsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		facade.throwException = true;
		model.setFacade(facade);
		try {
			List<JIRAVersionBean> versions = model
					.getVersions(createServerData("test"), p, true);
			facade.throwException = false;
			assertEquals(0, versions.size());
		} catch (JIRAException e) {

		}
	}

	public void testFixForVersions() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		JIRAProjectBean p = new JIRAProjectBean(1, "test");
		p.setKey("TEST");
		model.setFacade(facade);
		try {
			List<JIRAFixForVersionBean> versions = model.getFixForVersions(createServerData("test"), p, true);
			// should be 3 from the facade + "Any" + "No" + "Unreleased" + "Released"
			assertEquals(7, versions.size());
		} catch (JIRAException e) {

		}
	}

	public void testFixForVersionsNoProject() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAFixForVersionBean> versions = model
					.getFixForVersions(createServerData("test"), null, true);
			assertEquals(0, versions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testResolutions() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();

		model.setFacade(facade);
		try {
			List<JIRAResolutionBean> resolutions = model.getResolutions(createServerData("test"), true);
			// should be 3 from the facade + "Any" + "Unresolved"
			assertEquals(5, resolutions.size());
		} catch (JIRAException e) {
			fail();
		}
	}

	public void testResolutionsWithException() {
		JIRAServerModelImpl model = new JIRAServerModelImplLocal();
		List<JIRAResolutionBean> resolutions = new ArrayList<JIRAResolutionBean>();

		facade.throwException = true;
		model.setFacade(facade);
		try {
			resolutions = model.getResolutions(createServerData("test"), true);
			fail();
		} catch (JIRAException e) {
			facade.throwException = false;
			assertEquals(0, resolutions.size());

		}
	}

	private class JIRATestServerFacade2 implements JiraServerFacade {
		public int counter = 0;
		public boolean throwException = false;

		public List<JiraIssueAdapter> getIssues(JiraServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size) throws JIRAException {
			return null;
		}

		public List<JiraIssueAdapter> getSavedFilterIssues(JiraServerData server, List<JIRAQueryFragment> query, String sort,
				String sortOrder, int start, int size) throws JIRAException {
			return null;
		}

		public List<JIRAProject> getProjects(JiraServerData server) throws JIRAException {
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

		public List<JIRAConstant> getIssueTypes(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getStatuses(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAStatusBean(1, "test", null));
			list.add(new JIRAStatusBean(2, "test", null));
			list.add(new JIRAStatusBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getIssueTypesForProject(JiraServerData server, String project) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAConstant> list = new ArrayList<JIRAConstant>();
			list.add(new JIRAIssueTypeBean(1, "test", null));
			list.add(new JIRAIssueTypeBean(2, "test", null));
			list.add(new JIRAIssueTypeBean(3, "test", null));
			return list;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData server, String project)
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

		public List<JIRAQueryFragment> getSavedFilters(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
			list.add(new JIRASavedFilterBean("test", 1));
			list.add(new JIRASavedFilterBean("test", 1));
			list.add(new JIRASavedFilterBean("test", 1));
			return list;
		}

		public List<JIRAComponentBean> getComponents(JiraServerData server, String projectKey) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAComponentBean> list = new ArrayList<JIRAComponentBean>();
			list.add(new JIRAComponentBean(1, "test"));
			list.add(new JIRAComponentBean(2, "test"));
			list.add(new JIRAComponentBean(3, "test"));
			return list;
		}

		public List<JIRAVersionBean> getVersions(JiraServerData server, String projectKey) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAVersionBean> list = new ArrayList<JIRAVersionBean>();
			list.add(new JIRAVersionBean(1, "test", false));
			list.add(new JIRAVersionBean(2, "test", false));
			list.add(new JIRAVersionBean(3, "test", false));
			list.get(0).setReleased(true);
			list.get(1).setReleased(true);
			list.get(2).setReleased(false);
			return list;
		}

		public List<JIRAPriorityBean> getPriorities(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAPriorityBean> list = new ArrayList<JIRAPriorityBean>();
			list.add(new JIRAPriorityBean(1, 0, "test", null));
			list.add(new JIRAPriorityBean(2, 1, "test", null));
			list.add(new JIRAPriorityBean(3, 2, "test", null));
			return list;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerData server) throws JIRAException {
			if (throwException) {
				throw new JIRAException("test");
			}
			List<JIRAResolutionBean> list = new ArrayList<JIRAResolutionBean>();
			list.add(new JIRAResolutionBean(1, "test"));
			list.add(new JIRAResolutionBean(2, "test"));
			list.add(new JIRAResolutionBean(3, "test"));
			return list;
		}

		public List<JIRAAction> getAvailableActions(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(JiraServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(JiraServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void progressWorkflowAction(final JiraServerData server, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {
		}

		public void addComment(JiraServerData server, String issueKey, String comment) throws JIRAException {
		}

		public JiraIssueAdapter createIssue(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueUpdate(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueDetails(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

        public Collection<JIRAAttachment> getIssueAttachements(JiraServerData server, JIRAIssue issue)
                throws JIRAException {
            return null;
        }

        public void logWork(JiraServerData server, JIRAIssue issue, String timeSpent, Calendar startDate,
				String comment, boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(JiraServerData server, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(JiraServerData server, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void testServerConnection(final JiraServerData server) throws RemoteApiException {
		}

        public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
        }

        public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {
            
        }

        public ServerType getServerType() {
			return null;
		}

		public JiraIssueAdapter getIssue(JiraServerData server, String key) throws JIRAException {
			return null;
		}
	}

	private class JIRAServerModelImplLocal extends JIRAServerModelImpl {
	    public JIRAServerModelImplLocal() {
	        super(null);
	    }
	
	    @Override
		public boolean isFrozen() {
	        return false;
	    }
	}
}

