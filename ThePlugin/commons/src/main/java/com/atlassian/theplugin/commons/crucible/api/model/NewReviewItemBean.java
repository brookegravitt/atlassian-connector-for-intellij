package com.atlassian.theplugin.commons.crucible.api.model;

public class NewReviewItemBean implements NewReviewItem {
    private String repositoryName;
    private String fromPath;
    private String fromRevision;
    private String toPath;
    private String toRevision;

    public NewReviewItemBean() {
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getFromRevision() {
        return fromRevision;
    }

    public void setFromRevision(String fromRevision) {
        this.fromRevision = fromRevision;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public String getToRevision() {
        return toRevision;
    }

    public void setToRevision(String toRevision) {
        this.toRevision = toRevision;
    }
}
