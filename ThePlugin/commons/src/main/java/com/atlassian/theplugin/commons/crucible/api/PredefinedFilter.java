package com.atlassian.theplugin.commons.crucible.api;

public enum PredefinedFilter {
    ToReview("toReview", "To Review"),
    RequireMyApproval("requireMyApproval", "Require My Approval"),
    ToSummarize("toSummarize", "To Summarize"),
    OutForReview("outForReview", "Out For Review"),
    Drafts("drafts", "Drafts"),
    Open("open", "Open"),
    Closed("closed", "Closed"),
    Abandoned("trash", "Abandoned"),
    AllOpen("allOpenReviews", "All Open Reviews"),
    AllClosed("allClosedReviews", "All Closed Reviews"),
    All("allReviews", "All Reviews");

    private String filterUrl;
    private String filterName;

    PredefinedFilter(String filterUrl, String filterName) {
        this.filterUrl = filterUrl;
        this.filterName = filterName;
    }
    
    public String getFilterUrl() {
        return filterUrl;
    }

    public String getFilterName() {
        return filterName;
    }
}
