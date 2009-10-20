package com.atlassian.theplugin.jira.model;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class FrozenModelTest extends TestCase {
	Collection<FrozenModel> frozenModels;
	int frozenModelCounter = 0;


	public void setUp() throws Exception {
		super.setUp();
		frozenModelCounter = 0;
		frozenModels = new ArrayList<FrozenModel>();
		frozenModels.add(new JIRAFilterListModel());
		frozenModels.add(new JIRAIssueListModelImpl());
		frozenModels.add(new JIRAServerModelIdea());

	}


	public void testFrozenModelJIRAFilterList() {

		for (FrozenModel model : frozenModels) {

			model.addFrozenModelListener(new FrozenModelListener() {

				public void modelFrozen(FrozenModel model, boolean frozen) {
					if (frozen) {
						frozenModelCounter++;
					} else {
						frozenModelCounter--;
					}
				}
			});


			model.setModelFrozen(true);
			assertEquals(frozenModelCounter, 1);

			model.setModelFrozen(true);
			assertEquals(frozenModelCounter, 2);

			model.setModelFrozen(false);
			assertEquals(frozenModelCounter, 1);

			model.setModelFrozen(false);
			assertEquals(frozenModelCounter, 0);
		}


	}
}
