package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 3:05:56 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CrucibleFileInfo extends VersionedFileInfo {
	VersionedVirtualFile getOldFileDescriptor();

	int getNumberOfComments() throws ValueNotYetInitialized;

	int getNumberOfDefects() throws ValueNotYetInitialized;

	PermId getPermId();

	List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized;
}
