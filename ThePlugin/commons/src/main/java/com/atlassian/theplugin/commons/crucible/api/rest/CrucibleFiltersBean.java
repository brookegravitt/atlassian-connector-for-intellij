package com.atlassian.theplugin.commons.crucible.api.rest;

import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;

import java.util.Arrays;
import java.util.HashMap;

public class CrucibleFiltersBean {
    private Boolean[] predefinedFilters = new Boolean[PredefinedFilter.values().length];
    private HashMap<String, CustomFilterData> manualFilter = new HashMap<String, CustomFilterData>();

    public CrucibleFiltersBean() {
        Arrays.fill(predefinedFilters, false);
    }

    public HashMap<String, CustomFilterData> getManualFilter() {
        return manualFilter;
    }

    public void setManualFilter(HashMap<String, CustomFilterData> manualFilter) {
        this.manualFilter = manualFilter;
    }

    public Boolean[] getPredefinedFilters() {
        return predefinedFilters;
    }

    public void setPredefinedFilters(Boolean[] predefinedFilters) {
        this.predefinedFilters = predefinedFilters;
    }
}
