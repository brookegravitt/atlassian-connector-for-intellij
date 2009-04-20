package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

public interface OpenDiffAction {
	/**
	 * Open file view based on two file revisions
	 * Will be always invoked in UI thread
	 *
	 * @param displayFile   descriptor of the main file to display
	 * @param referenceFile reference file (e.g. "from file" in diff)
	 */
	void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType);
}