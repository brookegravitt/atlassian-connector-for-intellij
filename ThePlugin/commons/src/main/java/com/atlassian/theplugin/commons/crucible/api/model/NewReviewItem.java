package com.atlassian.theplugin.commons.crucible.api.model;

public interface NewReviewItem {
    String getRepositoryName();

    String getFromPath();

    String getFromRevision();

    String getToPath();

    String getToRevision();
}
