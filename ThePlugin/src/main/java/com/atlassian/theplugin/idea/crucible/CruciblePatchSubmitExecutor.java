package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CruciblePatchSubmitExecutor implements CommitExecutor {
	private final Project project;

	public CruciblePatchSubmitExecutor(Project project) {
		this.project = project;
	}

	@NotNull
	public Icon getActionIcon() {
		//TODO: implement method getActionIcon
		throw new UnsupportedOperationException("method getActionIcon not implemented");
	}

	@Nls
	public String getActionText() {
		return "Crucible Patch...";
	}

	@Nls
	public String getActionDescription() {
		return "Creates a patch from the files that would be commited and sends it for review to the Crucible server.";
	}

	public CommitSession createCommitSession() {
		return new CruciblePatchSubmitCommitSession(project);
	}
}
