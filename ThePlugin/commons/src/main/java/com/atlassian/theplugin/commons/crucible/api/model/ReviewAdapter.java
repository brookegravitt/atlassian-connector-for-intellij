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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.VirtualFileSystem;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewActionListener;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ReviewAdapter {
    private Review review;
    private CrucibleServerCfg server;
    private static final int HASHCODE_MAGIC = 31;
	private Collection<CrucibleReviewActionListener> listeners = new HashSet<CrucibleReviewActionListener>();

	public ReviewAdapter(Review review, CrucibleServerCfg server) {
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

    public List<Reviewer> getReviewers() throws ValueNotYetInitialized {
        return review.getReviewers();
    }

    public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
        return review.getGeneralComments();
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

	public CrucibleFileInfo getFileByPermId(PermId id) throws ValueNotYetInitialized {
		return review.getFileByPermId(id);
	}

	public CrucibleServerCfg getServer() {
        return server;
    }

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

        if (review != null ? !review.equals(that.review) : that.review != null) {
            return false;
        }
        if (server != null ? !server.equals(that.server) : that.server != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (review != null ? review.hashCode() : 0);
        result = HASHCODE_MAGIC * result + (server != null ? server.hashCode() : 0);
        return result;
    }

	public void setGeneralComments(final List<GeneralComment> generalComments) {
		review.setGeneralComments(generalComments);
	}

	public void addReviewListener(CrucibleReviewActionListener listener) {
		listeners.add(listener);
	}

	public boolean removeReviewListener(CrucibleReviewActionListener listener) {
		return listeners.remove(listener);
	}

	public void addGeneralComment(final GeneralCommentBean comment)
			throws ValueNotYetInitialized, RemoteApiException, ServerPasswordNotProvidedException {

		GeneralComment newComment = CrucibleServerFacadeImpl.getInstance().
				addGeneralComment(getServer(), review.getPermId(), comment);

		((CommentBean) newComment).setAuthor(
				CrucibleUserCacheImpl.getInstance().getUser(getServer(), getServer().getUsername(), false));

		review.getGeneralComments().add(newComment);

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedGeneralComment(this, newComment);
		}
	}

	public void addGeneralCommentReply(final GeneralComment parentComment, final GeneralCommentBean replyComment)
			throws RemoteApiException, ServerPasswordNotProvidedException, ValueNotYetInitialized {

		GeneralComment newReply = CrucibleServerFacadeImpl.getInstance().addGeneralCommentReply(
				getServer(), getPermId(), parentComment.getPermId(), replyComment);

		((CommentBean) newReply).setAuthor(
				CrucibleUserCacheImpl.getInstance().getUser(getServer(), getServer().getUsername(), false));

		for (GeneralComment comment : review.getGeneralComments()) {
			if (comment.equals(parentComment)) {
				comment.getReplies().add(newReply);
				break;
			}
		}

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedGeneralCommentReply(this, parentComment, newReply);
		}

	}


	/**
	 * Removes general review comment from the server and model.
	 * It SHOULD NOT be called from the EVENT DISPATCH THREAD as it calls facade method.
	 * @param generalComment Comment to be removed
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException in case password is missing
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException in case of communication problem
	 */
	public synchronized void removeGeneralComment(final GeneralComment generalComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		// remove comment from the server
		CrucibleServerFacadeImpl.getInstance().removeComment(getServer(), review.getPermId(), generalComment);

		// remove comment from the model
		this.review.removeGeneralComment(generalComment);

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.removedComment(this, generalComment);
		}
	}

	public void addVersionedComment(final CrucibleFileInfo file, final VersionedCommentBean newComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		VersionedComment newVersionedComment = CrucibleServerFacadeImpl.getInstance()
				.addVersionedComment(getServer(), getPermId(), file.getPermId(), newComment);
		((CommentBean) newVersionedComment).setAuthor(
				CrucibleUserCacheImpl.getInstance().getUser(getServer(), getServer().getUsername(), false));
		List<VersionedComment> comments;
		comments = file.getVersionedComments();

		if (comments == null) {
			comments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(getServer(), getPermId(),
					file.getPermId());
			file.setVersionedComments(comments);
		} else {
			comments.add(newComment);
		}

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedVersionedComment(this, file.getPermId(), newVersionedComment);
		}
	}

	public void addVersionedCommentReply(final CrucibleFileInfo file, final VersionedComment parentComment,
			final VersionedCommentBean nComment) throws RemoteApiException, ServerPasswordNotProvidedException {
		VersionedComment newComment = CrucibleServerFacadeImpl.getInstance().addVersionedCommentReply(
				getServer(), getPermId(), parentComment.getPermId(), nComment);

		((CommentBean) newComment).setAuthor(
				CrucibleUserCacheImpl.getInstance().getUser(getServer(), getServer().getUsername(), false));

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedVersionedCommentReply(this, file.getPermId(), parentComment, newComment);
		}
	}

	/**
	 * Removes file comment from the server and model.
	 * It SHOULD NOT be called from the EVENT DISPATCH THREAD as it calls facade method.
	 * @param versionedComment Comment to be removed
	 * @param file file containing the comment
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException in case password is missing
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException in case of communication problem
	 * @throws com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized
	 */
	public void removeVersionedComment(final VersionedComment versionedComment, final CrucibleFileInfo file)
			throws RemoteApiException, ServerPasswordNotProvidedException, ValueNotYetInitialized {

		// remove comment from the server
		CrucibleServerFacadeImpl.getInstance().removeComment(getServer(), review.getPermId(), versionedComment);

		// remove comment from the model
		review.removeVersionedComment(versionedComment, file);

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.removedComment(this, versionedComment);
		}
	}

	public void editGeneralComment(final GeneralComment comment) throws RemoteApiException, ServerPasswordNotProvidedException {

		CrucibleServerFacadeImpl.getInstance().updateComment(getServer(), getPermId(), comment);
		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedGeneralComment(this, comment);
		}
	}

	public void editVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleServerFacadeImpl.getInstance().updateComment(getServer(), getPermId(), comment);

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.createdOrEditedVersionedComment(this, file.getPermId(), comment);
		}
	}

	public void publishGeneralComment(final GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleServerFacadeImpl.getInstance().publishComment(getServer(), getPermId(), comment.getPermId());

		((GeneralCommentBean) comment).setDraft(false);

//dirty hack - probably remote api should return new comment info
//				if (comment instanceof VersionedCommentBean) {
//					((VersionedCommentBean) comment).setDraft(false);
//				}

		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.publishedGeneralComment(this, comment);
		}
	}

	public void publisVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleServerFacadeImpl.getInstance().publishComment(getServer(), getPermId(), comment.getPermId());
		//dirty hack - probably remote api should return new comment info
		//if (comment instanceof VersionedCommentBean) {
		((VersionedCommentBean) comment).setDraft(false);
		//}
		// notify listeners
		for (CrucibleReviewActionListener listener : listeners) {
			listener.publishedVersionedComment(this, file.getPermId(), comment);
		}
	}

	public void setFilesAndVersionedComments(final List<CrucibleFileInfo> files, final List<VersionedComment> comments) {
		review.setFilesAndVersionedComments(files, comments);
//		CrucibleFileInfoManager.getInstance().setFiles(review, files);
	}

	/**
	 * Copies all data from the parameter into itself
	 * @param newReview source of Review data
	 */
	public void fillReview(final Review newReview) {
		try {
			setGeneralComments(newReview.getGeneralComments());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}

		try {
//			setFilesAndVersionedComments(newReview.getFiles(), newReview.getVersionedComments());
			setFiles(newReview.getFiles());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}

		try {
			review.setActions(newReview.getActions());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}
		review.setAllowReviewerToJoin(newReview.isAllowReviewerToJoin());
		review.setAuthor(newReview.getAuthor());
		review.setCloseDate(newReview.getCloseDate());
		review.setCreateDate(newReview.getCreateDate());
		review.setCreator(newReview.getCreator());
		review.setDescription(newReview.getDescription());
		review.setMetricsVersion(newReview.getMetricsVersion());
		review.setModerator(newReview.getModerator());
		review.setName(newReview.getName());
		review.setParentReview(newReview.getParentReview());
//		review.setPermId(newReview.getPermId());
		review.setProjectKey(newReview.getProjectKey());
		review.setRepoName(newReview.getRepoName());
		try {
			review.setReviewers(newReview.getReviewers());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}
		review.setState(newReview.getState());
		review.setSummary(newReview.getSummary());
		try {
			review.setTransitions(newReview.getTransitions());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}
		review.setVirtualFileSystem(newReview.getVirtualFileSystem());

	}

	private void setFiles(final List<CrucibleFileInfo> files) {
		review.setFiles(files);
	}

	public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
		if (review.getFiles() == null) {
			throw new ValueNotYetInitialized("Files collection is empty");
		}
		return review.getFiles();
	}

	/**
	 * @return total number of versioned comments including replies (for all files)
	 */
	public int getNumberOfVersionedComments() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedComments();
	}

	public int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedComments(userName);
	}


	public int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDefects();
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDefects(userName);
	}

	public int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDrafts();
	}

	public int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDrafts(userName);
	}

	public int getNumberOfGeneralComments() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralComments();
	}

	public int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralComments(userName);
	}

	public int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDefects();
	}

	public int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDefects(userName);
	}

	public int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDrafts();
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDrafts(userName);
	}
}
