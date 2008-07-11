package com.atlassian.theplugin.idea.ui.filetree;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jul 11, 2008
* Time: 2:47:50 AM
* To change this template use File | Settings | File Templates.
*/
class LeafFileNode extends FileNode {

	private VersionedFileDescriptor file;

	public LeafFileNode(VersionedFileDescriptor file) {
		super(file.getFileName().substring(file.getFileName().lastIndexOf('/') + 1));
		this.file = file;
	}

	public String toString() {
		String name = super.toString();
		return name + " - " + file.getRevision();
	}
}
