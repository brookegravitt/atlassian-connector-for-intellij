package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diff.LineTokenizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.util.diff.Diff;
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
		StringBuilder sb = new StringBuilder();
		for (Change fileChange : changes) {
			ContentRevision beforeRevision = fileChange.getBeforeRevision();
			ContentRevision afterRevision = fileChange.getAfterRevision();

			String[] beforeLines = getLines(beforeRevision);
			String[] afterLines = getLines(afterRevision);

			String beforePath = getPath(beforeRevision);
			String afterPath = getPath(afterRevision);
			sb.append("Index: ");
			sb.append(beforePath).append('\n');
			sb.append("===================================================================\n");
			sb.append("--- ").append(beforePath).append("\t(");
			sb.append(getRevisionStr(beforeRevision)).append(")\n");
			sb.append("+++ ").append(afterPath).append("\t(");
			sb.append(getRevisionStr(afterRevision)).append(")\n");


			Diff.Change change = Diff.buildChanges(beforeLines, afterLines);
			while (null != change) {
				int origStart = change.line0;
				int origSpan = change.deleted;
				int afterStart = change.line1;
				int afterSpan = change.inserted;

				sb.append(String.format("@@ -%d,%d +%d,%d @@\n", origStart, origSpan, afterStart, afterSpan));

				for (int i = 0; i < change.deleted; ++i) {
					sb.append("-");
					sb.append(beforeLines[change.line0 + i]);
				}
				for (int i = 0; i < change.inserted; ++i) {
					sb.append("+");
					sb.append(afterLines[change.line1 + i]);
				}

				change = change.link;
			}
		}
		ApplicationManager.getApplication().invokeAndWait(new CruciblePatchUploader(commitMessage, sb.toString()), ModalityState.defaultModalityState());
	}

	private static final String[] EMPTY_STR_ARRAY = new String[0];

	private static String[] getLines(ContentRevision revision) {
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

	private static String getPath(ContentRevision revision) {
		FilePath filePath = revision.getFile();
		return filePath.getPath();
	}

	private static String getRevisionStr(ContentRevision revision) {
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
