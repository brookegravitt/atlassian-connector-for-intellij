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
}
