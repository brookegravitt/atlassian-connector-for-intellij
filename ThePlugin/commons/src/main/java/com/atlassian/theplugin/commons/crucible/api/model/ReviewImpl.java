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

/**
 * @author Jacek Jaroczynski
 */
public class ReviewImpl implements Review {

	public User getAuthor() {
		return null;
	}

	public User getCreator() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public User getModerator() {
		return null;
	}

	public String getName() {
		return null;
	}

	public PermId getParentReview() {
		return null;
	}

	public PermId getPermId() {
		return null;
	}

	public String getProjectKey() {
		return null;
	}

	public String getRepoName() {
		return null;
	}

	public State getState() {
		return null;
	}

	public boolean isAllowReviewerToJoin() {
		return false;
	}

	public int getMetricsVersion() {
		return 0;
	}

	public Date getCreateDate() {
		return null;
	}

	public Date getCloseDate() {
		return null;
	}

	public String getSummary() {
		return null;
	}

	public List<Reviewer> getReviewers() throws ValueNotYetInitialized {
		return null;
	}

	public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
		return null;
	}

	public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
		return null;
	}// TODO jgorycki: this is evil and shouldn't be here. I hope to be able to fix this some day :)
	// jjaroczynski: good luck jgorycki

	public String getServerUrl() {
		return null;
	}

	public CrucibleFileInfo getFileByPermId(final PermId id) {
		return null;
	}

	public List<Action> getTransitions() throws ValueNotYetInitialized {
		return null;
	}

	public List<Action> getActions() throws ValueNotYetInitialized {
		return null;
	}

	public VirtualFileSystem getVirtualFileSystem() {
		return null;
	}

	public void removeGeneralComment(final GeneralComment comment) {

	}

	public void removeVersionedComment(final VersionedComment versionedComment) {

	}

	public void setFilesAndVersionedComments(final List<CrucibleFileInfo> files, final List<VersionedComment> commentList) {

	}

	public List<CrucibleFileInfo> getFiles() {
		return null;
	}

	public void setReviewers(final List<Reviewer> reviewers) {

	}

	public void setGeneralComments(final List<GeneralComment> generalComments) {

	}

	public void setTransitions(final List<Action> transitions) {

	}

	public void setActions(final List<Action> actions) {

	}

	public void setVirtualFileSystem(final VirtualFileSystem virtualFileSystem) {

	}

	public void setAuthor(final User value) {

	}

	public void setCreator(final User value) {

	}

	public void setDescription(final String value) {

	}

	public void setModerator(final User value) {

	}

	public void setName(final String value) {

	}

	public void setParentReview(final PermId value) {

	}

	public void setPermId(final PermId value) {

	}

	public void setProjectKey(final String value) {

	}

	public void setRepoName(final String value) {

	}

	public void setState(final State value) {

	}

	public void setAllowReviewerToJoin(final boolean allowReviewerToJoin) {

	}

	public void setMetricsVersion(final int metricsVersion) {

	}

	public void setCreateDate(final Date createDate) {

	}

	public void setCloseDate(final Date closeDate) {

	}

	public void setSummary(final String summary) {

	}

	public int getNumberOfVersionedComments() {
		return 0;
	}

	public int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralComments() throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized {
		return 0;
	}

	public void setFiles(final List<CrucibleFileInfo> files) {

	}

}
