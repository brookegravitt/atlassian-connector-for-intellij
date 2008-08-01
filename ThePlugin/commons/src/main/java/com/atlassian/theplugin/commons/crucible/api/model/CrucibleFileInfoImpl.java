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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 12:24:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleFileInfoImpl implements CrucibleFileInfo {
    private VersionedVirtualFile fileDescriptor;
    private VersionedVirtualFile oldFileDescriptor;
    private String repositoryName;

    private int numberOfComments = 0;

    /**
     * How many people think the file contains defects
     */
    private int numberOfDefects = 0;

    private List<VersionedComment> versionedComments;
    private PermId permId;

    public CrucibleFileInfoImpl(VersionedVirtualFile fileDescriptor, VersionedVirtualFile oldFileDescriptor) {
        this.fileDescriptor = fileDescriptor;
        this.oldFileDescriptor = oldFileDescriptor;
    }

    public VersionedVirtualFile getOldFileDescriptor() {
        return oldFileDescriptor;
    }

    public void setOldFileDescriptor(VersionedVirtualFile oldFileDescriptor) {
        this.oldFileDescriptor = oldFileDescriptor;
    }

    public int getNumberOfComments() throws ValueNotYetInitialized {
        return getVersionedComments().size();
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getNumberOfDefects() throws ValueNotYetInitialized {
        int counter = 0;
        for (VersionedComment comment : getVersionedComments()) {
            if (comment.isDefectApproved()) {
                counter++;
            }
        }
        return counter;
    }

    public PermId getPermId() {
        return permId;
    }


    public void setNumberOfDefects(int numberOfDefects) {
        this.numberOfDefects = numberOfDefects;
    }

    public void setFileDescriptor(VersionedVirtualFile fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public VersionedVirtualFile getFileDescriptor() {
        return fileDescriptor;
    }

    public void setPermId(PermId permId) {
        this.permId = permId;
    }

    @Override
    public String toString() {
        VersionedVirtualFile oldFile = getOldFileDescriptor();
        VersionedVirtualFile newFile = getFileDescriptor();
        if (oldFile != null && oldFile.getUrl().length() > 0
                && newFile != null && newFile.getUrl().length() > 0) {
            return oldFile.getUrl() + " (mod)";
        } else if (oldFile != null && oldFile.getUrl().length() > 0) {
            return newFile.getUrl() + " (del)";
        } else if (newFile != null && newFile.getUrl().length() > 0) {
            return oldFile.getUrl() + " (new)";
        } else {
            return "(unknown state)";
        }

    }

    public void setVersionedComments(List<VersionedComment> commentList) {
        versionedComments = commentList;
    }

	public void addVersionedComment(VersionedComment comment) {
		versionedComments.add(comment);
	}


	public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
        if (versionedComments == null) {
            throw new ValueNotYetInitialized("Object trasferred only partially");
        }
        return versionedComments;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }    
}