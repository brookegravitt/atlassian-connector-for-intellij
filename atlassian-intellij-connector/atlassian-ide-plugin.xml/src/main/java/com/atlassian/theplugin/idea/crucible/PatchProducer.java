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

package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.diff.LineTokenizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;
import com.intellij.vcsUtil.VcsUtil;

import java.util.Collection;

public class PatchProducer {
	@SuppressWarnings("unused")
	private static final int LINES_OF_CONTEXT = 3;
    private Collection<Change> changes;
    private Project project;

    public PatchProducer(Project project, Collection<Change> changes) {
        this.project = project;
        this.changes = changes;
	}

	public String generateUnifiedDiff() {
		StringBuilder sb = new StringBuilder();
		boolean firstFile = true;
		
		for (Change fileChange : changes) {
			ContentRevision beforeRevision = fileChange.getBeforeRevision();
			ContentRevision afterRevision = fileChange.getAfterRevision();

			String[] beforeLines = getLines(beforeRevision);
			String[] afterLines = getLines(afterRevision);

			String beforePath = getPath(beforeRevision);
			String afterPath = getPath(afterRevision);

			if (afterPath == null) {
				afterPath = beforePath;
			}
			if (beforePath == null) {
				beforePath = afterPath;
			}

			if (!firstFile) {
				sb.append("\n"); //add new line before header for all files except first

			}
			sb.append("Index: ");
			sb.append(beforePath).append('\n');
			sb.append("===================================================================\n");
			sb.append("--- ").append(beforePath).append("\t(");
			sb.append(getRevisionStr(beforeRevision)).append(")\n");
			sb.append("+++ ").append(afterPath).append("\t(");
			sb.append(getRevisionStr(afterRevision)).append(")\n");

			Diff.Change diff = null;
			if (beforeLines != null && afterLines != null) {
				diff = Diff.buildChanges(beforeLines, afterLines);
			}

			generateUnifiedDiffBody(sb, diff, beforeLines, afterLines, LINES_OF_CONTEXT);
			firstFile = false;

		}
		return sb.toString();
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param sb			 StringBuilder to add stuff to
	 * @param diff		   starting change in file
	 * @param beforeLines	lines of oryginal file
	 * @param afterLines	 lines of changed file
	 * @param linesOfContext number of lines that should preceed and follow change @return formatted as unified diff text
	 *                       body without oryginal and changed file name header
	 */

	private void generateUnifiedDiffBody(
			StringBuilder sb, Diff.Change diff, String[] beforeLines, String[] afterLines, int linesOfContext) {

		while (diff != null) {
			int lastLine = 1;
			final int origStart = beforeLines.length == 0 ? 0 : Math.max(lastLine, diff.line0 - linesOfContext + 1);
			final int afterStart = afterLines.length == 0 ? 0 : Math.max(lastLine, diff.line1 - linesOfContext + 1);
			int origSpan = 0;
			int afterSpan = 0;

			final int atLineInsertionPoint = sb.length();

			do {

				int i = Math.max(lastLine, diff.line0 - linesOfContext);

				// Display the unaltered lines (skipping some if there's too many)
				for (; i < diff.line0; i++) {
					sb.append(' ').append(beforeLines[i]);
					origSpan += 1;
					afterSpan += 1;
				}

				// Display the deleted and/or inserted lines for this difference
				for (i = 0; i < diff.deleted; i++) {
					sb.append('-').append(beforeLines[diff.line0 + i]);
				}
				for (i = 0; i < diff.inserted; i++) {
					sb.append('+').append(afterLines[diff.line1 + i]);
				}

				final int previousLine = diff.line0 + diff.deleted;
				origSpan += diff.deleted;
				afterSpan += diff.inserted;
				// Display any remaining lines (plus skip some if there's too many)

				//select place where remoteapi of context (after change) should ends
				if (diff.link != null) {
					lastLine = Math.min(previousLine + linesOfContext, diff.link.line0);

				} else { //this is a last change so ......
					lastLine = previousLine + linesOfContext;
				}

				lastLine = Math.min(beforeLines.length, lastLine);
				for (i = previousLine; i < lastLine; i++) {
					sb.append(' ').append(beforeLines[i]);
					origSpan += 1;
					afterSpan += 1;

				}
				diff = diff.link;
			} while (diff != null && lastLine >= diff.line0 - linesOfContext);

			sb.insert(atLineInsertionPoint, String.format("@@ -%d,%d +%d,%d @@\n", origStart, origSpan, afterStart, afterSpan));
		}
	}


	private static final String[] EMPTY_STR_ARRAY = new String[0];

	private static String[] getLines(ContentRevision revision) {
		if (revision == null) {
			return EMPTY_STR_ARRAY;
		}
		String content;
		try {
			content = revision.getContent();
			if (content == null) {
				return EMPTY_STR_ARRAY;
			}
		} catch (VcsException e) {
			return EMPTY_STR_ARRAY;
		}
		return new LineTokenizer(content).execute();
	}

	/* made it protected so that unit test may override */
	protected String getPath(ContentRevision revision) {
		if (revision == null) {
			return null;
		}
		FilePath filePath = revision.getFile();
		VirtualFile vcsRoot = VcsUtil.getVcsRootFor(project, filePath);
		return FileUtil.getRelativePath(VfsUtil.virtualToIoFile(vcsRoot), filePath.getIOFile());
	}

	private static String getRevisionStr(ContentRevision revision) {
		if (revision == null) {
			return "working copy";
		}
		VcsRevisionNumber revisionNumber = revision.getRevisionNumber();
		if (revisionNumber == VcsRevisionNumber.NULL) {
			return "working copy";
		} else {
			return "revision " + revisionNumber.asString();
		}
	}
}