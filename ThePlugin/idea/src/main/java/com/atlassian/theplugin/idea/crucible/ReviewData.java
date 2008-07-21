package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

public interface ReviewData extends Review {
    Server getServer();

    String getReviewUrl();
}
