package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.VirtualFileSystem;

import java.util.Date;
import java.util.List;

public class ReviewDataImpl implements ReviewData {
    private Review review;
    private Server server;

    public ReviewDataImpl(Review review, Server server) {
        this.review = review;
        this.server = server;
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

    public PermId getPermId() {
        return review.getPermId();
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

    public List<Reviewer> getReviewers() throws ValueNotYetInitialized {
        return review.getReviewers();
    }

    public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
        return review.getGeneralComments();
    }

    public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
        return review.getVersionedComments();
    }

    public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
        return review.getFiles();
    }

    public List<Action> getTransitions() throws ValueNotYetInitialized {
        return review.getTransitions();
    }

    public List<Action> getActions() throws ValueNotYetInitialized {
        return review.getActions();
    }

    public VirtualFileSystem getVirtualFileSystem() {
        return review.getVirtualFileSystem();
    }

    public Server getServer() {
        return server;
    }

    public String getReviewUrl() {
		String baseUrl = server.getUrlString();
		while (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + getPermId().getId();
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewDataImpl that = (ReviewDataImpl) o;

        if (review != null ? !review.equals(that.review) : that.review != null) return false;
        if (server != null ? !server.equals(that.server) : that.server != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (review != null ? review.hashCode() : 0);
        result = 31 * result + (server != null ? server.hashCode() : 0);
        return result;
    }
}
