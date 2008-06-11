package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import java.util.Arrays;
import java.util.HashMap;

public class CrucibleFiltersBean {
    private Boolean[] predefinedFilters = new Boolean[PredefinedFilter.values().length];
    private HashMap<String, CustomFilterBean> manualFilter = new HashMap<String, CustomFilterBean>();

    public CrucibleFiltersBean() {
        Arrays.fill(predefinedFilters, false);
    }

    public HashMap<String, CustomFilterBean> getManualFilter() {
        return manualFilter;
    }

    public void setManualFilter(HashMap<String, CustomFilterBean> manualFilter) {
        this.manualFilter = manualFilter;
    }

    public Boolean[] getPredefinedFilters() {
        return predefinedFilters;
    }

    public void setPredefinedFilters(Boolean[] predefinedFilters) {
        this.predefinedFilters = predefinedFilters;
    }
}
