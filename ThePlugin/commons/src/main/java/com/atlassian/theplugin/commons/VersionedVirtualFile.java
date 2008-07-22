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

import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 14, 2008
 * Time: 11:50:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionedVirtualFile {
	private String revision;
	private String url;
	private VirtualFileSystem fileSystem;

	public VersionedVirtualFile(String path, String revision, VirtualFileSystem fileSystem) {
		super();
		this.revision = revision;
		this.url = path;
		this.fileSystem = fileSystem;
	}

	public boolean isDirectory() {
		return false;  
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
		return AbstractHttpSession.getLastComponentFromUrl(getUrl());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setFileSystem(VirtualFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public VirtualFileSystem getFileSystem() {
		return fileSystem;
	}
}
