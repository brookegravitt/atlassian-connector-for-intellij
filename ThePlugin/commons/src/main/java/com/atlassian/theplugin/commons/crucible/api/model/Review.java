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

    List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized;

	// TODO jgorycki: this is evil and shouldn't be here. I hope to be able to fix this some day :)
	String getServerUrl();
	
//    List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized;

	List<CrucibleReviewItemInfo> getReviewItems();

	CrucibleFileInfo getFileByPermId(PermId id);
	
	CrucibleFileInfo getFileByReviewInfo(CrucibleReviewItemInfo info);
	
	List<Action> getTransitions() throws ValueNotYetInitialized;

    List<Action> getActions() throws ValueNotYetInitialized;

    VirtualFileSystem getVirtualFileSystem();

	CrucibleServerCfg getServer();

	String getReviewUrl();

	Review getInnerReviewObject();

	void setGeneralComments(final List<GeneralComment> generalComments);
}