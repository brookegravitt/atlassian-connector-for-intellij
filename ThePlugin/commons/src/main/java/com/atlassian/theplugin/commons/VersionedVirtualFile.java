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
