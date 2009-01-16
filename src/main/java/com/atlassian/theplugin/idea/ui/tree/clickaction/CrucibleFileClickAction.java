package com.atlassian.theplugin.idea.ui.tree.clickaction;

import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.project.Project;

public class CrucibleFileClickAction implements AtlassianClickAction {
	private Project project;
	private ReviewAdapter review;
	private CrucibleFileInfo file;

	public CrucibleFileClickAction(Project project, ReviewAdapter review, CrucibleFileInfo file) {
		this.project = project;
		this.review = review;
		this.file = file;
	}

	public void execute(AtlassianTreeNode node, int noOfClicks) {
		switch (noOfClicks) {
			case 2:
				if (file.getCommitType() != CommitType.Deleted) {
					CrucibleHelper.showVirtualFileWithComments(project, review, file);
				}
				break;
			default:
				break;
		}
	}

}
