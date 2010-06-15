package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilter;
import com.atlassian.connector.commons.jira.beans.JIRASavedFilterBean;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.UUID;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModelTest extends TestCase {
	JIRAFilterListModel listModel;
	private int notifiedModelChanged = 0;
	private int notifiedManualFilterChanged = 0;
	private final JiraServerData jServer = new JiraServerData(
            new JiraServerCfg(true, "DZira serwer", new ServerIdImpl(), true) {
		public ServerType getServerType() {
			return null;
		}

		public JiraServerCfg getClone() {
			return null;
		}
	});
	private int notifiedServerRemoved;
	private int notifiedServerAdded;
	private int notifiedServerNameChanged;

	public void setUp() throws Exception {
		super.setUp();
		listModel = new JIRAFilterListModel(null);
		notifiedModelChanged = 0;
		notifiedManualFilterChanged = 0;
		notifiedServerRemoved = 0;
		notifiedServerAdded = 0;
		notifiedServerNameChanged = 0;
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNotifyListeners() {

		JIRAServerFiltersBean serverFilter1 = new JIRAServerFiltersBean();
		fillInServerFiltersBean(serverFilter1, 2);

		listModel.addModelListener(new JIRAFilterListModelListener() {


			public void modelChanged(final JIRAFilterListModel listModel) {
				notifiedModelChanged++;
			}

			public void manualFilterChanged(final JiraCustomFilter manualFilter, final JiraServerData jiraServer) {
				notifiedManualFilterChanged++;
			}

			public void serverRemoved(final JIRAFilterListModel jiraFilterListModel) {
				notifiedServerRemoved++;
			}

			public void serverAdded(final JIRAFilterListModel jiraFilterListModel) {
				notifiedServerAdded++;
			}

			public void serverNameChanged(final JIRAFilterListModel jiraFilterListModel) {
				notifiedServerNameChanged++;
			}

            public void manualFilterAdded(JIRAFilterListModel jiraFilterListModel, JiraCustomFilter manualFilter, ServerId serverId) {

            }

            public void manualFilterRemoved(JIRAFilterListModel jiraFilterListModel, JiraCustomFilter manualFilter, ServerId serverId) {

            }

        });

		for (int i = 0; i < 10; i++) {
			listModel.fireModelChanged();
			listModel.fireManualFilterChanged(null, null);
			listModel.fireServerRemoved();
			listModel.fireServerAdded();
			listModel.fireServerNameChanged();
		}

		assertEquals(notifiedModelChanged, 10);
		assertEquals(notifiedManualFilterChanged, 10);
		assertEquals(10, notifiedServerRemoved);
		assertEquals(10, notifiedServerAdded);
		assertEquals(10, notifiedServerNameChanged);

		listModel.addManualFilter(jServer, serverFilter1.getManualFilters().iterator().next());
		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());


		for (int i = 0; i < 10; i++) {
			listModel.fireModelChanged();
			listModel.fireManualFilterChanged(null, null);
		}

		assertEquals(20, notifiedModelChanged);
		assertEquals(20, notifiedManualFilterChanged);
	}

	public void testAddSavedManualFilter() {

		JIRAServerFiltersBean serverFilter1 = new JIRAServerFiltersBean();
		final JiraCustomFilter manual = new JiraCustomFilter(UUID.randomUUID().toString(),
                "Custom filter", new ArrayList<JIRAQueryFragment>());


		fillInServerFiltersBean(serverFilter1, 3);

		serverFilter1.getManualFilters().add(manual);

		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());
		listModel.setManualFilters(jServer, serverFilter1.getManualFilters());

		assertEquals(listModel.getSavedFilters(jServer).size(), 3);
	}

	private void fillInServerFiltersBean(JIRAServerFiltersBean bean, int savedFiltersNo) {
		ArrayList<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();
		for (int i = 0; i < savedFiltersNo; i++) {
			JIRASavedFilter filter = new JIRASavedFilterBean("saved filter" + i, i);
			savedFilters.add(filter);
		}
		bean.setSavedFilters(savedFilters);
		JiraCustomFilter manual = new JiraCustomFilter(UUID.randomUUID().toString(),
                "Custom filter", new ArrayList<JIRAQueryFragment>());
		bean.getManualFilters().add(manual);

	}
}
