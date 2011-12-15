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

package com.atlassian.connector.intellij.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReviewAdapter {
	private final Review review;

	private ServerData server;

	private List<CustomFieldDef> metricDefinitions;

	private static final int HASHCODE_MAGIC = 31;

	private CrucibleServerFacade facade;

	private final ExtendedCrucibleProject crucibleProject;



	public ReviewAdapter(Review review, ServerData server, ExtendedCrucibleProject crucibleProject) {
		this.review = review;
		this.server = server;
		this.crucibleProject = crucibleProject;

		facade = IntelliJCrucibleServerFacade.getInstance();

		if (server != null && review != null) {
			try {
				metricDefinitions = facade.getMetrics(getServerData(), review.getMetricsVersion());
			} catch (RemoteApiException e) {
				// not critical error, exception can be swallowed
				// metrics definitions are cached anyway in session, so should be accessible wihtout calling the server
			} catch (ServerPasswordNotProvidedException e) {
				// not critical error, exception can be swallowed
				// metrics definitions are cached anyway in session, so should be accessible wihtout calling the server
			}
		}
	}

	public ReviewAdapter(ReviewAdapter reviewAdapter) {
		this(reviewAdapter.review, reviewAdapter.server, reviewAdapter.getCrucibleProject());
	}

	public boolean isCompleted() {
		return review.isCompleted();
	}

	public CrucibleServerFacade getFacade() {
		return facade;
	}

	public void setFacade(CrucibleServerFacade newFacade) {
		facade = newFacade;
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

	public ExtendedCrucibleProject getCrucibleProject() {
		return crucibleProject;
	}

	public String getRepoName() {
		return review.getRepoName();
	}

	public State getState() {
		return review.getState();
	}

	public boolean isAllowReviewerToJoin() {
		return review.isAllowReviewerToJoin();
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

	public String getSummary() {
		return review.getSummary();
	}

	public Set<Reviewer> getReviewers() {
		return review.getReviewers();
	}

	public List<Comment> getGeneralComments() {
		return review.getGeneralComments();
	}

	public Set<CrucibleAction> getTransitions() {
		return review.getTransitions();
	}

	public Set<CrucibleAction> getActions() {
		return review.getActions();
	}

	public CrucibleFileInfo getFileByPermId(PermId id) {
		return review.getFileByPermId(id);
	}

	@NotNull
	public String getReviewUrl() {
		String baseUrl = server.getUrl();
		while (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + getPermId().getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewAdapter that = (ReviewAdapter) o;

		return !(review != null ? !review.equals(that.review) : that.review != null)
				&& !(server != null ? !server.getServerId().equals(that.server.getServerId()) : that.server != null);
	}

	@Override
	public int hashCode() {
		int result;
		result = (review != null ? review.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (server != null ? server.getServerId().hashCode() : 0);
		return result;
	}





	/*
	 public List<CrucibleNotification> fillReview(final ReviewAdapter newReview) {
		 return fillReview(newReview.review);
	 }
	*/

	private void setFiles(final Set<CrucibleFileInfo> files) {
		review.setFiles(files);
        //add notification
	}

	public Set<CrucibleFileInfo> getFiles() {        
		return review.getFiles();
	}

	/**
	 * @return total number of versioned comments including replies (for all files)
	 *
	 */
	public int getNumberOfVersionedComments() {
		return review.getNumberOfVersionedComments();
	}

	public int getNumberOfVersionedComments(final String userName) {
		return review.getNumberOfVersionedComments(userName);
	}

	public int getNumberOfVersionedCommentsDefects() {
		return review.getNumberOfVersionedCommentsDefects();
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) {
		return review.getNumberOfVersionedCommentsDefects(userName);
	}

	public int getNumberOfVersionedCommentsDrafts() {
		return review.getNumberOfVersionedCommentsDrafts();
	}

	public int getNumberOfGeneralCommentsDrafts(final String userName) {
		return review.getNumberOfGeneralCommentsDrafts(userName);
	}

	public int getNumberOfGeneralComments() {
		return review.getNumberOfGeneralComments();
	}

	public int getNumberOfGeneralComments(final String userName) {
		return review.getNumberOfGeneralComments(userName);
	}

	public int getNumberOfGeneralCommentsDefects() {
		return review.getNumberOfGeneralCommentsDefects();
	}

	public int getNumberOfGeneralCommentsDefects(final String userName) {
		return review.getNumberOfGeneralCommentsDefects(userName);
	}

	public int getNumberOfGeneralCommentsDrafts() {
		return review.getNumberOfGeneralCommentsDrafts();
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) {
		return review.getNumberOfVersionedCommentsDrafts(userName);
	}

	public int getNumberOfUnreadComments() {
		return review.getNumberOfUnreadComments();
	}

	public List<CustomFieldDef> getMetricDefinitions() {
		return metricDefinitions;
	}

	@Override
	public String toString() {
		return review.getPermId().getId() + ": " + review.getName() + " (" + server.getName() + ')';
	}


	public ServerData getServerData() {
		return server;

	}

	public Review getReview() {
		return review;
	}
}
