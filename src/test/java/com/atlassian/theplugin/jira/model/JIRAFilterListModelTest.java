package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.api.JIRASavedFilterBean;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModelTest extends TestCase {
	JIRAFilterListModel listModel;
	private int notifiedModelChanged = 0;
    private int notifiedSavedFilterSelected = 0;
	private int notifiedManualFilterSelected = 0;
	final private JiraServerCfg jServer = new JiraServerCfg("DZira serwer", new ServerId());

	public void setUp() throws Exception {
        super.setUp();
		listModel = new JIRAFilterListModel();
		notifiedModelChanged = 0;
		notifiedSavedFilterSelected = 0;
		notifiedManualFilterSelected = 0;


	}

    public void tearDown() throws Exception {
        super.tearDown();
    }

	public void testNotifyListeners(){
		JIRAServerFiltersBean serverFilter1 = new JIRAServerFiltersBean();
		fillInServerFiltersBean(serverFilter1, 2);

		listModel.addModelListener(new JIRAFilterListModelListener(){

			public void modelChanged(final JIRAFilterListModel listModel) {
				notifiedModelChanged++;
			}

			public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
				notifiedSavedFilterSelected++;
			}

			public void selectedManualFilter(final JiraServerCfg jiraServer, final List<JIRAQueryFragment> manualFilter) {
				notifiedManualFilterSelected++;
			}

			public void modelFrozen(JIRAFilterListModel jiraFilterListModel, boolean frozen) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});

		for (int i=0; i<10; i++) {
			listModel.fireModelChanged();
			listModel.fireManualFilterSelected();
			listModel.fireSavedFilterSelected();
		}

		assertEquals(notifiedModelChanged, 10);
		assertEquals(notifiedSavedFilterSelected, 0);
		assertEquals(notifiedManualFilterSelected, 0);

		listModel.setManualFilter(jServer, serverFilter1.getManualFilter());
		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());
		
		listModel.selectManualFilter(jServer, serverFilter1.getManualFilter());
		listModel.selectSavedFilter(jServer, serverFilter1.getSavedFilters().get(0));
		
		for (int i=0; i<10; i++) {
			listModel.fireModelChanged();
			listModel.fireManualFilterSelected();
			listModel.fireSavedFilterSelected();
		}

		assertEquals(20, notifiedModelChanged);
		assertEquals(11, notifiedSavedFilterSelected);
		assertEquals(11, notifiedManualFilterSelected);
	}

	public void testAddSavedManualFilter(){

		JIRAServerFiltersBean serverFilter1 = new JIRAServerFiltersBean();
		final JIRAManualFilter manual = new JIRAManualFilter("Custom filter", new ArrayList<JIRAQueryFragment>());


		fillInServerFiltersBean(serverFilter1, 3);
		serverFilter1.setManualFilter(manual);

		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());
		listModel.setManualFilter(jServer, serverFilter1.getManualFilter());

		assertEquals(listModel.getSavedFilters(jServer).size(), 3);
		assertEquals(listModel.getManualFilter(jServer), manual);

	}

	public void testSelectedManaualSavedFilter(){

		JIRAServerFiltersBean serverFilter1 = new JIRAServerFiltersBean();
		final JIRAManualFilter manual = new JIRAManualFilter("Custom filter", new ArrayList<JIRAQueryFragment>());
		final JIRAServerFiltersBean finalFilters = serverFilter1;

		listModel.addModelListener(new JIRAFilterListModelListener(){

			public void modelChanged(final JIRAFilterListModel listModel) {
			}

			public void selectedSavedFilter(final JiraServerCfg jiraServer, final JIRASavedFilter savedFilter) {
				assertTrue(finalFilters.getSavedFilters().contains(savedFilter));
				assertTrue(jServer.equals(jiraServer));
			}

			public void selectedManualFilter(final JiraServerCfg jiraServer, final List<JIRAQueryFragment> manualFilter) {
				assertTrue(manual.getQueryFragment().equals(manualFilter));
				assertTrue(jServer.equals(jiraServer));
			}

			public void modelFrozen(JIRAFilterListModel jiraFilterListModel, boolean frozen) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
		fillInServerFiltersBean(serverFilter1, 3);
		listModel.setSavedFilters(jServer, serverFilter1.getSavedFilters());

		assertTrue(listModel.getJiraSelectedManualFilter() == null);

		serverFilter1.setManualFilter(manual);
		listModel.setManualFilter(jServer, manual);
		listModel.selectManualFilter(jServer, manual);
		assertEquals(listModel.getJiraSelectedManualFilter(), manual);

		listModel.selectSavedFilter(jServer, listModel.getSavedFilters(jServer).get(0));

	}

	private void fillInServerFiltersBean(JIRAServerFiltersBean bean,  int savedFiltersNo){
		ArrayList<JIRASavedFilter> savedFilters = new ArrayList<JIRASavedFilter>();
		for (int i=0; i<savedFiltersNo; i++){
			JIRASavedFilter filter = new JIRASavedFilterBean("saved filter" + i, i);
			savedFilters.add(filter);
		}
		bean.setSavedFilters(savedFilters);
		JIRAManualFilter manual = new JIRAManualFilter("Custom filter", new ArrayList<JIRAQueryFragment>());
		bean.setManualFilter(manual);

	}
}
