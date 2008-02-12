package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diff.LineTokenizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class CruciblePatchSubmitCommitSession implements CommitSession {
	private static final Logger LOG = Logger.getLogger("#com.intellij.openapi.diff.DiffData");

	{
		LOG.setLevel(Level.ALL);
	}

	@SuppressWarnings("unused")
	private final Project project;
	private static final int LINES_OF_CONTEXT = 3;
	private static final String WHITE_SPACE = " ";

	public CruciblePatchSubmitCommitSession(Project project) {
		this.project = project;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI() {
		return null;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI(Collection<Change> changes, String commitMessage) {
		return null;
	}

	public boolean canExecute(Collection<Change> changes, String commitMessage) {
		return changes.size() > 0;
	}

	public void execute(Collection<Change> changes, String commitMessage) {
		System.out.println("Sending to the Crucible server: " + commitMessage);
		String patch = generateUnifiedDiff(changes);

		ApplicationManager.getApplication().invokeAndWait(new CruciblePatchUploader(commitMessage, patch), ModalityState.defaultModalityState());
	}

	String generateUnifiedDiff(Collection<Change> changes) {
		final int linesOfContext = LINES_OF_CONTEXT;

		StringBuilder sb = new StringBuilder();
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

			sb.append(generateUnifiedDiffBody(diff, beforeLines, afterLines, linesOfContext));

		}
		return sb.toString();
	}

	private StringBuilder generateUnifiedDiffBody(Diff.Change diff, String[] beforeLines, String[] afterLines, int linesOfContext) {
		int previousLine = 0;
		int origStart = 0;
		int origSpan = 0;
		int afterStart = 0;
		int afterSpan = 0;
		int i = 0;
		int lastLine = 0;
		String strChange = "";
		StringBuilder sb = new StringBuilder();


		while (diff != null) {
			lastLine = 0;
			origStart = Math.max(lastLine, diff.line0 - linesOfContext);
			afterStart = Math.max(lastLine, diff.line1 - linesOfContext);
			origSpan = 0;
			afterSpan = 0;

			do {

				i = Math.max(lastLine, diff.line0 - linesOfContext);

				// Display the unaltered lines (skipping some if there's too many)
				for (; i < diff.line0; i++) {
					strChange += " " + beforeLines[i];
					origSpan += 1;
					afterSpan += 1;
				}

				// Display the deleted and/or inserted lines for this difference
				for (i = 0; i < diff.deleted; i++) {
					strChange += "-" + beforeLines[diff.line0 + i];
				}
				for (i = 0; i < diff.inserted; i++) {
					strChange += "+" + afterLines[diff.line1 + i];
				}
				;

				previousLine = diff.line0 + diff.deleted;
				origSpan += diff.deleted;
				afterSpan += diff.inserted;
				// Display any remaining lines (plus skip some if there's too many)

				if (diff.link != null) {
					lastLine = Math.min(previousLine + linesOfContext, diff.link.line0);

				} else {
					lastLine = previousLine + linesOfContext;
				}

				lastLine = Math.min(beforeLines.length, lastLine);
				for (i = previousLine; i < lastLine; i++) {
					strChange += " " + beforeLines[i];
					origSpan += 1;
					afterSpan += 1;

				}
				diff = diff.link;
			} while (diff != null && lastLine >= diff.line0 - linesOfContext);

			sb.append(String.format("@@ -%d,%d +%d,%d @@\n", origStart, origSpan, afterStart, afterSpan));
			sb.append(strChange);
			strChange = "";

		}

		return sb;


        ///
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

	private String getPath(ContentRevision revision) {
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

	public void executionCanceled() {
	}


}
