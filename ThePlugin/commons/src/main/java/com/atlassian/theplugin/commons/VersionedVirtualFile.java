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

package com.atlassian.theplugin.commons;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

/**
 * @author lguminski
 */
public class VersionedVirtualFile {
	private String revision;
	private String url;
    private String repoUrl;
    private VirtualFileSystem fileSystem;

	public VersionedVirtualFile(String path, String revision, VirtualFileSystem fileSystem) {
		this.revision = revision;
		this.url = path;
        //this.repoUrl = repoUrl;
        this.fileSystem = fileSystem;
	}


	public byte[] contentsToByteArray() throws IOException {
		return new byte[0];
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getName() {
        return FilenameUtils.getName(getUrl());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }    

    public void setFileSystem(VirtualFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public VirtualFileSystem getFileSystem() {
		return fileSystem;
	}

    public String getAbsoluteUrl() {
        return (repoUrl != null ? repoUrl : "") + url;
    }
}
