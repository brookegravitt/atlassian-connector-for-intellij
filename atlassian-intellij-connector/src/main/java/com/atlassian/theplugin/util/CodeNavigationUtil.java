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

import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public final class CodeNavigationUtil {

	private CodeNavigationUtil() {
		// this is utility class
	}

	/**
	 * Note: must be run from event dispatch thread or inside read-action only!
	 */
	@Nullable
	public static PsiFile guessMatchingFile(String pathname, PsiFile[] psifiles, VirtualFile baseDir) {
		// PL-822 fix
		// for making it work if pathname comes from different OS than the file itself
		// e.g. Bamboo works on Windows, send pathname and the user has a project opened in IDEA on Linux
		pathname = pathname.replace('\\', '/');

		PsiFile bestMatch = null;

		int difference = 0;

		if (psifiles != null) {

			for (PsiFile psiFile : psifiles) {
				// we use hard-coded '/' as separator in order to make string comparison platform independent
				String absolutePath = psiFile.getVirtualFile().getUrl();
				if (absolutePath == null) {
					continue;
				}

				int diff = StringUtils.indexOfDifference(StringUtils.reverse(pathname)
						, StringUtils.reverse(absolutePath));
				if (diff >= FilenameUtils.getName(absolutePath).length()
						&& (diff > difference || absolutePath.equals(pathname))) {
					difference = diff;
					bestMatch = psiFile;
					if (absolutePath.equals(pathname)) {
						break;
					}
				}
			}
		}
		return bestMatch;
	}

	/**
	 * Get files from the provided collection which absolute path contains provided filePath
	 *
	 * @param filePath searched file path
	 * @param psiFiles collection of files to search for filePath
	 * @return collection of files matching provided filePath
	 */
	static Collection<PsiFile> getMatchingFiles(String filePath, final PsiFile[] psiFiles) {
		filePath = filePath.replace('\\', '/');

		Collection<PsiFile> match = new ArrayList<PsiFile>();

        if (psiFiles == null) {
            return match;
        }

		for (PsiFile psiFile : psiFiles) {
			String absolutePath = psiFile.getVirtualFile().getUrl();
			absolutePath = absolutePath.replace('\\', '/');
			if (absolutePath == null) {
				continue;
			}

			if (absolutePath.endsWith(FilenameUtils.getName(filePath)) && absolutePath.contains(filePath)) {
				match.add(psiFile);
			}
		}
		return match;
	}

	/**
	 * Note: must be run from event dispatch thread or inside read-action only!
	 */
	@Nullable
	public static PsiFile guessCorrespondingPsiFile(final Project project, final String filepath) {
		final PsiFile[] psifiles = IdeaVersionFacade.getInstance().getFiles(FilenameUtils.getName(filepath), project);
		return CodeNavigationUtil.guessMatchingFile(filepath, psifiles, project.getBaseDir());
	}

	@Nullable
	public static PsiFile guessCorrespondingPsiFile(PsiFile[] psifiles, final Project project, final String filepath) {
		return CodeNavigationUtil.guessMatchingFile(filepath, psifiles, project.getBaseDir());
	}

	/**
	 * Note: must be run from event dispatch thread or inside read-action only!
	 *
	 * @param project  project
	 * @param filePath filePath
	 * @return collection of matching PsiFiles
	 */
	@Nullable
	public static Collection<PsiFile> findPsiFiles(final Project project, final String filePath) {
		// find files (do not care about path - IDEA7 compatibility)
		final PsiFile[] psifiles = IdeaVersionFacade.getInstance().getFiles(FilenameUtils.getName(filePath), project);

		return CodeNavigationUtil.getMatchingFiles(filePath, psifiles);
	}

    /**
     * In the collection of provided files looks for those which match vcs url
     *
     * @param psiFiles collection of files to search
     * @param vcsUrl   searched vcs url
     * @param project  project
     * @return collection of found PsiFiles
     */
    public static Collection<PsiFile> findPsiFilesWithVcsUrl(final Collection<PsiFile> psiFiles, final String vcsUrl,
            final Project project) {
        Collection<PsiFile> retFiles = new ArrayList<PsiFile>();
        if (psiFiles != null && vcsUrl != null && project != null) {
            for (PsiFile psiFile : psiFiles) {
                String repositoryUrl = VcsIdeaHelper.getRepositoryRootUrlForFile(project, psiFile.getVirtualFile());
                if (repositoryUrl != null && repositoryUrl.equals(vcsUrl) || repositoryUrl == null) {
                    retFiles.add(psiFile);
                }
            }
        }

        return retFiles;
    }


}
