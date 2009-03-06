package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.AbstractOpenDiffToolAction;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.DocumentContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsBundle;

public class OpenBambooDiffToolAction extends AbstractOpenDiffToolAction {
	public OpenBambooDiffToolAction(final Project project, final String filename, final String fromRevision,
			final String toRevision) {
		super(project, filename, fromRevision, toRevision);
	}

	protected DiffRequest getDiffRequest(final Project aProject, final DocumentContent referenceDoc,
			final DocumentContent displayDoc) {
		return new DiffRequest(aProject) {
			@Override
			public DiffContent[] getContents() {
				return (new DiffContent[]{
						referenceDoc,
						displayDoc
				});
			}

			@Override
			public String[] getContentTitles() {
				return (new String[]{
						VcsBundle.message("diff.content.title.repository.version",
								fromRevision),
						VcsBundle.message("diff.content.title.repository.version",
								toRevision)
				});
			}

			@Override
			public String getWindowTitle() {
				return filename;
			}
		};
	}
}