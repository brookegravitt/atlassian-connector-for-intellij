package com.atlassian.theplugin.commons;


/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 12:21:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class BambooFileInfoImpl implements BambooFileInfo {
	private VersionedVirtualFile fileDescriptor;

	public BambooFileInfoImpl(VersionedVirtualFile fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

	public BambooFileInfoImpl(VirtualFileSystem fileSystem, String file, String revision) {
		this(new VersionedVirtualFile(file, revision, fileSystem));
	}

	public VersionedVirtualFile getFileDescriptor() {
		return fileDescriptor;
	}

	public void setFileDescriptor(VersionedVirtualFile fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}

}
