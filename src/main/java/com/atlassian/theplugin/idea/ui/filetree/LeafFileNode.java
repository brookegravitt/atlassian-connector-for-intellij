package com.atlassian.theplugin.idea.ui.filetree;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;

class LeafFileNode extends FileNode {

	private VersionedFileDescriptor file;

	public LeafFileNode(VersionedFileDescriptor file) {
		super(file.getFileName().substring(file.getFileName().lastIndexOf('/') + 1));
		this.file = file;
	}

	public String getRevision() {
		return file.getRevision();
	}
}
