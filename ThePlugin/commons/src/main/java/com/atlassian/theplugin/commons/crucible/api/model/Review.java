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
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.Date;
import java.util.List;

public interface Review {

	// todo add server configuration (Server object)

	User getAuthor();

	User getCreator();

	String getDescription();

	User getModerator();

	String getName();

	PermId getParentReview();

	PermId getPermId();

	String getProjectKey();

	String getRepoName();

	State getState();

    boolean isAllowReviewerToJoin();

    int getMetricsVersion();

    Date getCreateDate();

    Date getCloseDate();

    String getSummary();

    List<Reviewer> getReviewers() throws ValueNotYetInitialized;

    List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized;

//    List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized;

//    List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized;

//	List<CrucibleReviewItemInfo> getReviewItems();

	CrucibleFileInfo getFileByPermId(PermId id) throws ValueNotYetInitialized;
	
	List<Action> getTransitions() throws ValueNotYetInitialized;

    List<Action> getActions() throws ValueNotYetInitialized;

    VirtualFileSystem getVirtualFileSystem();

	void removeGeneralComment(final GeneralComment comment);

	void removeVersionedComment(final VersionedComment vComment, final CrucibleFileInfo file) throws ValueNotYetInitialized;

	void setFilesAndVersionedComments(List<CrucibleFileInfo> files, List<VersionedComment> commentList);

	List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized;

	void setReviewers(List<Reviewer> reviewers);

	void setGeneralComments(List<GeneralComment> generalComments);

	void setTransitions(List<Action> transitions);

	void setActions(List<Action> actions);

	void setVirtualFileSystem(VirtualFileSystem virtualFileSystem);

	void setAuthor(User value);

	void setCreator(User value);

	void setDescription(String value);

	void setModerator(User value);

	void setName(String value);

	void setParentReview(PermId value);

	void setPermId(PermId value);

	void setProjectKey(String value);

	void setRepoName(String value);

	void setState(State value);

	void setAllowReviewerToJoin(boolean allowReviewerToJoin);

	void setMetricsVersion(int metricsVersion);

	void setCreateDate(Date createDate);

	void setCloseDate(Date closeDate);

	void setSummary(String summary);

//	void setReviewItems(List<CrucibleReviewItemInfo> reviewItems);

	int getNumberOfVersionedComments() throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized;

	int getNumberOfGeneralComments() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized;

	int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized;

	int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized;


	int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized;

	int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized;

	void setFiles(final List<CrucibleFileInfo> files);
}