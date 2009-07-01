package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.api.JIRASavedFilterBean;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModelTest extends TestCase {
	JIRAFilterListModel listModel;
	private int notifiedModelChanged = 0;
	private int notifiedManualFilterChanged = 0;
	private final ServerData jServer = new ServerData(new ServerCfg(true, "DZira serwer", "", new ServerIdImpl()) {
		public ServerType getServerType() {
			return null;
		}

		public ServerCfg getClone() {
			return null;
		}
	}, "", "");
	private int notifiedServerRemoved;
	private int notifiedServerAdded;
	private int notifiedServerNameChanged;

	public void setUp() throws Exception {
		super.setUp();
		listModel = new JIRAFilterListModel();
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

			public void manualFilterChanged(final JIRAManualFilter manualFilter, final ServerData jiraServer) {
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

		listModel.setManualFilter(jServer, serverFilter1.getManualFilter());
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
		final JIRAManualFilter manual = new JIRAManualFilter("Custom filter", new ArrayList<JIRAQueryFragment>());


		fillInServerFiltersBean(serverFilter1, 3);
		serverFilter1.setManualFilter(manual);

		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());
		listModel.setManualFilter(jServer, serverFilter1.getManualFilter());

		assertEquals(listModel.getSavedFilters(jServer).size(), 3);
		assertEquals(listModel.getManualFilter(jServer), manual);

	}

	private void fillInServerFiltersBean(JIRAServerFiltersBean bean, int savedFiltersNo) {
		ArrayList<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();
		for (int i = 0; i < savedFiltersNo; i++) {
			JIRASavedFilter filter = new JIRASavedFilterBean("saved filter" + i, i);
			savedFilters.add(filter);
		}
		bean.setSavedFilters(savedFilters);
		JIRAManualFilter manual = new JIRAManualFilter("Custom filter", new ArrayList<JIRAQueryFragment>());
		bean.setManualFilter(manual);

	}
}
