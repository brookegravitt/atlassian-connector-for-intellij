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
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JIRAIssueListModelImplTest extends TestCase {
    private JiraServerData jiraServerData;
	public void setUp() throws Exception {
		super.setUp();
        jiraServerData = new JiraServerData(new Server(){
            public ServerIdImpl getServerId() {
                return null;
            }

            public String getName() {
                return null;
            }

            public String getUrl() {
                return null;
            }

            public boolean isEnabled() {
                return false;
            }

            public boolean isUseDefaultCredentials() {
                return false;
            }

            public String getUsername() {
                return null;
            }

            public String getPassword() {
                return null;
            }

            public ServerType getServerType() {
                return ServerType.JIRA_SERVER;
            }

            public boolean isDontUseBasicAuth() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public UserCfg getBasicHttpUser() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

//    public void testAddOne() {
//	    JIRAIssueListModel model = new JIRAIssueListModelImpl();
//	    model.addIssue(new JIRAIssueBean());
//	    assertEquals(1, model.getIssues().size());
//    }

	public void testAddMany() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		List<JiraIssueAdapter> list = new ArrayList<JiraIssueAdapter>();
		JiraIssueAdapter proto = new JiraIssueAdapter(jiraServerData);
		proto.setKey("A-1");
		list.add(new JiraIssueAdapter(proto));
		proto.setKey("A-2");
		list.add(new JiraIssueAdapter(proto));
		proto.setKey("A-3");
		list.add(new JiraIssueAdapter(proto));
		proto.setKey("A-4");
		list.add(new JiraIssueAdapter(proto));
		model.addIssues(list);
		assertEquals(list.size(), model.getIssues().size());
	}

	public void testClear() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		model.addIssues(Arrays.asList((JiraIssueAdapter) new JiraIssueAdapter(jiraServerData)));
		assertEquals(1, model.getIssues().size());
		model.clear();
		assertEquals(0, model.getIssues().size());
	}

	private boolean listenerCalled = false;
	private boolean listenerCalled2 = false;

	public void testListeners() {
		final JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelListener l = new JIRAIssueListModelListener() {
			public void issueUpdated(final JiraIssueAdapter issue) {

			}

			public void modelChanged(JIRAIssueListModel m) {
				if (model == m) {
					listenerCalled = true;
				}
			}

			public void issuesLoaded(JIRAIssueListModel m, int loadedIssues) {
				if (model == m) {
					listenerCalled2 = true;
				}
			}
		};

		model.addModelListener(l);
		model.fireModelChanged();
		assertTrue(listenerCalled);
		model.fireIssuesLoaded(0);
		assertTrue(listenerCalled2);

		listenerCalled = false;
		listenerCalled2 = false;
		model.removeModelListener(l);
		model.fireModelChanged();
		assertFalse(listenerCalled);
		model.fireIssuesLoaded(0);
		assertFalse(listenerCalled2);
	}

	public void testListenersWithEasyMock() {

		final int numberOfIssues = 4;

		// create and apply mock
		final JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelListener l = EasyMock.createMock(JIRAIssueListModelListener.class);
		model.addModelListener(l);

		// teach mock
		l.modelChanged(model);
		l.issuesLoaded(model, numberOfIssues);

		// use mock
		EasyMock.replay(l);
		model.fireModelChanged();
		model.fireIssuesLoaded(numberOfIssues);

		// check mock
		EasyMock.verify(l);

		// reset mock and start again
		EasyMock.reset(l);
		model.removeModelListener(l);

		// teach mock

		// use mock
		EasyMock.replay(l);
		model.fireModelChanged();
		model.fireIssuesLoaded(numberOfIssues);

		// check mock
		EasyMock.verify(l);
	}

	public void testListenersWithMockito() {

		final int numberOfIssues = 4;

		// create and apply mock
		final JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelListener listener = Mockito.mock(JIRAIssueListModelListener.class);
		model.addModelListener(listener);

		// use mock
		model.fireModelChanged();
		model.fireIssuesLoaded(numberOfIssues);

		// check mock
		Mockito.verify(listener).modelChanged(model);
		Mockito.verify(listener).issuesLoaded(model, numberOfIssues);

		// start again (don't know how to reset mock without creating new one)
		model.removeModelListener(listener);

		// use mock
		model.fireModelChanged();
		model.fireIssuesLoaded(numberOfIssues);

		// check mock (no interactions since last verification)
		Mockito.verifyNoMoreInteractions(listener);
	}
}
