package com.atlassian.connector.intellij.crucible.content;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

/**
 * User: mwent
 * Date: Mar 12, 2009
 * Time: 3:00:18 PM
 */
public interface ReviewFileContentProvider {

	ReviewFileContent getContent(final ReviewAdapter review,
			final VersionedVirtualFile fileInfo)
			throws ReviewFileContentException;

	CrucibleFileInfo getFileInfo();

	boolean isLocalFileDirty();
}
