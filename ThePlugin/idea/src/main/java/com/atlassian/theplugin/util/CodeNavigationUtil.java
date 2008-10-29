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

package com.atlassian.theplugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

public final class CodeNavigationUtil {

    private CodeNavigationUtil() {
        // this is utility class
    }

	/**
	 * Note: must be run from event dispatch thread or inside read-action only!
	 */
	@Nullable
	public static PsiFile guessMatchingFile(String pathname, PsiFile[] psifiles, VirtualFile baseDir) {
		String bestMatchPath = null;
		PsiFile bestMatch = null;
		for (PsiFile psiFile : psifiles) {
			// we use hard-coded '/' as separator in order to make string comparison platform independent
			String relativePath = VfsUtil.getPath(baseDir, psiFile.getVirtualFile(), '/');
			if (relativePath == null) {
				continue;
			}
			if (pathname.endsWith(relativePath) == true) {
				if (bestMatch == null || (relativePath.length() > bestMatchPath.length())) {
					bestMatchPath = relativePath;
					bestMatch = psiFile;
				}

			}
		}
		return bestMatch;
	}

	/**
	 * Note: must be run from event dispatch thread or inside read-action only!
	 */
	@Nullable
	public static PsiFile guessCorrespondingPsiFile(final Project project, final String filepath) {
        final PsiFile[] psifiles = IdeaVersionFacade.getInstance().getFiles(FilenameUtils.getName(filepath), project);
		return CodeNavigationUtil.guessMatchingFile(filepath, psifiles, project.getBaseDir());
	}

}
