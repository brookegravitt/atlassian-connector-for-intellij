package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.AbstractOpenDiffToolAction;
import com.intellij.openapi.diff.DiffContent;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.DocumentContent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsBundle;

public class OpenCrucibleDiffToolAction extends AbstractOpenDiffToolAction {
	private final CrucibleFileInfo reviewItem;

	public OpenCrucibleDiffToolAction(final Project project, final CrucibleFileInfo reviewItem, final String fromRevision,
			final String toRevision) {
		super(project, reviewItem.getFileDescriptor().getAbsoluteUrl(), fromRevision, toRevision);
		this.reviewItem = reviewItem;
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
				switch (reviewItem.getRepositoryType()) {
					case SCM:
						return (new String[]{
								VcsBundle.message("diff.content.title.repository.version",
										fromRevision),
								VcsBundle.message("diff.content.title.repository.version",
										toRevision)
						});
					default:
						return (new String[]{
								"Before change",
								"After change"});
				}
			}

			@Override
			public String getWindowTitle() {
				return reviewItem.getFileDescriptor().getAbsoluteUrl();
			}
		};
	}
}

