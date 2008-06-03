package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 2, 2008
 * Time: 2:24:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleFiltersBean {
	private List<CustomFilterData> manualFilter = new ArrayList<CustomFilterData>();
	private CustomFilterData savedFilter = null;
	private boolean savedFilterUsed = false;

	public CrucibleFiltersBean() {
	}

	public List<CustomFilterData> getManualFilter() {
		return manualFilter;
	}

	public void setManualFilter(List<CustomFilterData> manualFilter) {
		this.manualFilter = manualFilter;
	}

	public CustomFilterData getSavedFilter() {
		return savedFilter;
	}

	public void setSavedFilter(CustomFilterData savedFilter) {
		this.savedFilter = savedFilter;
	}

	public boolean getSavedFilterUsed() {
		return savedFilterUsed;
	}

	public void setSavedFilterUsed(boolean savedFilterUsed) {
		this.savedFilterUsed = savedFilterUsed;
	}

	public void safeAddCrucibleFilter(CustomFilterData filter){

		if (manualFilter.contains(filter)){
			manualFilter.remove(filter);
		}

		if (filter!=null) {
			manualFilter.add(filter);
		}
	}
}
