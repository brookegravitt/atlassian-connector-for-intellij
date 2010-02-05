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
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.ReviewDifferenceProducer;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class ReviewAdapter {
	private final Review review;

	private ServerData server;

	private List<CustomFieldDef> metricDefinitions;

	private static final int HASHCODE_MAGIC = 31;

	private CrucibleServerFacade facade;

	private final Collection<CrucibleReviewListener> listeners = new HashSet<CrucibleReviewListener>();
	private final CrucibleProject crucibleProject;

    private Collection<CrucibleReviewListener> getListeners() {
		return listeners;
	}

	public ReviewAdapter(Review review, ServerData server, CrucibleProject crucibleProject) {
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

	public CrucibleProject getCrucibleProject() {
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

	public EnumSet<CrucibleAction> getTransitions() {
		return review.getTransitions();
	}

	public EnumSet<CrucibleAction> getActions() {
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

	public void addReviewListener(CrucibleReviewListener listener) {
		if (!getListeners().contains(listener)) {
			listeners.add(listener);
		}
	}

	public boolean removeReviewListener(CrucibleReviewListener listener) {
		return listeners.remove(listener);
	}

	public void setGeneralComments(final List<Comment> generalComments) {
		review.setGeneralComments(generalComments);
	}

	public void addGeneralComment(final Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {

		Comment newComment = facade.addGeneralComment(getServerData(), review, comment);

		if (newComment != null) {
			review.getGeneralComments().add(newComment);

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedGeneralComment(this, newComment);
			}
		}
	}

	public void addReply(final Comment parentComment, final Comment replyComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		Comment newReply = facade.addReply(getServerData(), replyComment);

		if (newReply != null) {
			// if newReply.getParentComment() is null, I am for NPE here, as normally it should not happen, and
			// I would like not add extra guard here, but know it!
			//noinspection ConstantConditions
			newReply.getParentComment().addReply(newReply);
			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedReply(this, null, parentComment, newReply);
			}
		}
	}

	/**
	 * Removes general review comment from the server and model. It SHOULD NOT be called from the EVENT DISPATCH THREAD
	 * as it calls facade method.
	 *
	 * @param comment Comment to be removed
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          in case password is missing
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException
	 *          in case of communication problem
	 */
	public synchronized void removeComment(final Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		final Comment parentComment = comment.getParentComment();
		// remove comment from the server
		facade.removeComment(getServerData(), review.getPermId(), comment);

		// remove comment from the model
		if (parentComment != null) {
			parentComment.removeReply(comment);
		} else {
			if (comment instanceof GeneralComment) {
				review.removeGeneralComment(comment);
			} else if (comment instanceof VersionedComment) {
				final VersionedComment versionedComment = (VersionedComment) comment;
				review.removeVersionedComment(versionedComment, versionedComment.getCrucibleFileInfo());

			}
		}

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.removedComment(this, comment);
		}
	}

	public void addVersionedComment(final CrucibleFileInfo file, final VersionedComment newComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		VersionedComment newVersionedComment =
				facade.addVersionedComment(getServerData(), review, file.getPermId(), newComment);
		if (newVersionedComment != null) {
			List<VersionedComment> comments;
			comments = file.getVersionedComments();

			if (comments == null) {
				comments = facade.getVersionedComments(getServerData(), review, file);
				file.setVersionedComments(comments);
			} else {
				comments.add(newVersionedComment);
			}

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedVersionedComment(this, file.getPermId(), newVersionedComment);
			}
		}
	}

	// this method does not support yet nested replies
	private <T extends Comment> void replaceComment(List<T> comments, T comment) {
		final ListIterator<T> it = comments.listIterator();
		while (it.hasNext()) {
			Comment reply = it.next();
			if (comment.getPermId().equals(reply.getPermId())) {
				it.set(comment);
				return;
			}
		}

	}

	public void editGeneralComment(final Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.updateComment(getServerData(), getPermId(), comment);
		final Comment parentComment = comment.getParentComment();
		if (parentComment != null) {
			replaceComment(parentComment.getReplies(), comment);
		} else {
			// it's either versioned comment or a top level general comment
			if (comment instanceof VersionedComment) {
				final VersionedComment versionedComment = (VersionedComment) comment;
				replaceComment(versionedComment.getCrucibleFileInfo().getVersionedComments(), versionedComment);
			} else if (comment instanceof GeneralComment) {
				replaceComment(review.getGeneralComments(), comment);
			}
		}
		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.createdOrEditedGeneralComment(this, comment);
		}
	}

	public void editVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		editGeneralComment(comment);
	}

	public void markCommentRead(final Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.markCommentRead(getServerData(), getPermId(), comment.getPermId());

		if (comment.getReadState() != Comment.ReadState.UNKNOWN) {
			(comment).setReadState(Comment.ReadState.READ);
			for (CrucibleReviewListener listener : getListeners()) {
				listener.commentReadStateChanged(this, comment);
			}
		}
	}

	public void markCommentLeaveUnread(final Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.markCommentLeaveUnread(getServerData(), getPermId(), comment.getPermId());

		if (comment.getReadState() != Comment.ReadState.UNKNOWN) {
			(comment).setReadState(Comment.ReadState.LEAVE_UNREAD);
			for (CrucibleReviewListener listener : getListeners()) {
				listener.commentReadStateChanged(this, comment);
			}
		}
	}

	public void markAllCommentsRead() throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.markAllCommentsRead(getServerData(), getPermId());

		List<Comment> gcs = getGeneralComments();
		for (Comment generalComment : gcs) {
			markLeaveUnreadCommentRead(generalComment);
		}
		Set<CrucibleFileInfo> files = getFiles();
		for (CrucibleFileInfo file : files) {
			for (VersionedComment versionedComment : file.getVersionedComments()) {
				markLeaveUnreadCommentRead(versionedComment);
			}
		}
	}

	private void markLeaveUnreadCommentRead(Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		if (comment.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
			facade.markCommentRead(getServerData(), getPermId(), comment.getPermId());
		}
		List<Comment> replies = comment.getReplies();
		for (Comment reply : replies) {
			if (reply.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
				facade.markCommentRead(getServerData(), getPermId(), reply.getPermId());
			}
		}
		if (comment.getReadState() != Comment.ReadState.UNKNOWN) {
			(comment).setReadState(Comment.ReadState.READ);
			for (CrucibleReviewListener listener : listeners) {
				listener.commentReadStateChanged(this, comment);
			}
			for (Comment reply : replies) {
				if (reply.getReadState() != Comment.ReadState.UNKNOWN) {
					(reply).setReadState(Comment.ReadState.READ);
					for (CrucibleReviewListener listener : listeners) {
						listener.commentReadStateChanged(this, reply);
					}
				}
			}
		}
	}

	public void publishGeneralComment(final Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		facade.publishComment(getServerData(), getPermId(), comment.getPermId());

		comment.setDraft(false);

//dirty hack - probably remote api should return new comment info
//				if (comment instanceof VersionedCommentBean) {
//					((VersionedCommentBean) comment).setDraft(false);
//				}

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.publishedGeneralComment(this, comment);
		}
	}

	public void publishVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.publishComment(getServerData(), getPermId(), comment.getPermId());
		//dirty hack - probably remote api should return new comment info
		//if (comment instanceof VersionedCommentBean) {
		(comment).setDraft(false);
		//}
		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.publishedVersionedComment(this, file.getPermId(), comment);
		}
	}

	public void setFilesAndVersionedComments(final Set<CrucibleFileInfo> files, final List<VersionedComment> comments) {
		review.setFilesAndVersionedComments(files, comments);
	}

	/*
	 public List<CrucibleNotification> fillReview(final ReviewAdapter newReview) {
		 return fillReview(newReview.review);
	 }
	*/
	/**
	 * Copies all data from the parameter into itself
	 *
	 * @param newReview source of Review data
	 * @return
	 */
	public synchronized List<CrucibleNotification> fillReview(final ReviewAdapter newReview) {
		boolean reviewChanged = false;

		ReviewDifferenceProducer reviewDifferenceProducer = new ReviewDifferenceProducer(review, newReview.review);
		List<CrucibleNotification> differences = reviewDifferenceProducer.getDiff();
		if (differences != null && differences.size() > 0) {
			reviewChanged = true;
		}
		this.server = newReview.getServerData();
		review.setAuthor(newReview.getAuthor());
		review.setModerator(newReview.getModerator());
		review.setName(newReview.getName());
		review.setProjectKey(newReview.getProjectKey());
		review.setDescription(newReview.getDescription());
		review.setSummary(newReview.getSummary());
		review.setReviewers(newReview.getReviewers());
		if (reviewDifferenceProducer.getCommentChangesCount() > 0) {
			setGeneralComments(newReview.getGeneralComments());
		}
		if (!reviewDifferenceProducer.isShortEqual()) {
			review.setActions(newReview.getActions());
			review.setAllowReviewerToJoin(newReview.isAllowReviewerToJoin());
			review.setCloseDate(newReview.getCloseDate());
			review.setCreateDate(newReview.getCreateDate());
			review.setCreator(newReview.getCreator());
			review.setMetricsVersion(newReview.getMetricsVersion());
			review.setParentReview(newReview.getParentReview());
			review.setRepoName(newReview.getRepoName());
			review.setState(newReview.getState());
			review.setTransitions(newReview.getTransitions());
		}

		if (!reviewDifferenceProducer.isFilesEqual()) {
			setFiles(newReview.getFiles());
		}

		if (reviewChanged) {
			for (CrucibleReviewListener listener : getListeners()) {
				listener.reviewChanged(this, differences);
			}
		}

		return differences;
	}

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
