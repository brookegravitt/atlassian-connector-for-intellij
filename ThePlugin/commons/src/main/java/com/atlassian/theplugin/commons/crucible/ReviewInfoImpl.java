/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.crucible.api.model.*;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;


public class ReviewInfoImpl implements ReviewInfo {
    private final Review review;
	private final Server server;


    public ReviewInfoImpl(Review review, Server server) {
        this.review = review;
        this.server = server;
    }

    public String getReviewUrl() {
		String baseUrl = server.getUrlString();
		while (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0,  baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + review.getPermaId().getId();

	}

	public List<Reviewer> getReviewers() {
        if (review instanceof DetailedReview) {
            return ((DetailedReview)review).getReviewers();
        } else {
            return new ArrayList<Reviewer>();
        }
    }

	public Server getServer() {
		return server;
	}


	public User getAuthor() {
		return review.getAuthor();
	}

	public User getCreator() {
		return review.getCreator();
	}

	public String getDescription() {
		return review.getDescription();
	}

	public User getModerator() {
		return review.getModerator();
	}

	public String getName() {
		return review.getName();
	}

	public PermId getParentReview() {
		return review.getParentReview();
	}

	public PermId getPermaId() {
		return review.getPermaId();
	}

	public String getProjectKey() {
		return review.getProjectKey();
	}

	public String getRepoName() {
		return review.getRepoName();
	}

	public State getState() {
		return review.getState();
	}

    public int getMetricsVersion() {
        return review.getMetricsVersion();
    }

    public Date getCreateDate() {
        return review.getCreateDate();
    }

    public Date getCloseDate() {
        return review.getCloseDate();
    }

    public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewInfoImpl that = (ReviewInfoImpl) o;

		if (!review.getPermaId().getId().equals(that.review.getPermaId().getId())) {
			return false;
		}
		if (server != null ? !server.equals(that.server) : that.server != null) {
			return false;
		}

		return true;
	}

	private static final int ONE_EFF = 31;

	public int hashCode() {
		int result;
		result = review.getPermaId().getId().hashCode();
		result = ONE_EFF * result + (server != null ? server.hashCode() : 0);
		return result;
	}

}
