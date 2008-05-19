package com.atlassian.theplugin.commons.crucible.api;


public class CustomFilterData implements CustomFilter {
    private String title;
    private String[] state;
    private String author;
    private String moderator;
    private String creator;
    private String reviewer;
    private boolean orRoles = false;
    private boolean complete;
    private boolean allReviewersComplete;
    private String projectKey;


    public CustomFilterData() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getState() {
        return state;
    }

    public void setState(String[] state) {
        this.state = state;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isAllReviewersComplete() {
        return allReviewersComplete;
    }

    public void setAllReviewersComplete(boolean allReviewersComplete) {
        this.allReviewersComplete = allReviewersComplete;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isOrRoles() {
        return orRoles;
    }

    public void setOrRoles(boolean orRoles) {
        this.orRoles = orRoles;
    }
}
