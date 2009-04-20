package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.idea.fisheye.FisheyeUrlHelper;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * User: jgorycki
 * Date: Mar 24, 2009
 * Time: 3:05:56 PM
 */
public class JumpToFisheyeAction extends AbstractBambooFileActions {
	public void actionPerformed(AnActionEvent event) {
		Project currentProject = event.getData(DataKeys.PROJECT);

		BambooFileNode bfn = getBambooFileNode(event);
		if (bfn != null && currentProject != null) {
			PsiFile file = guessPsiFile(currentProject, bfn);
			if (file != null) {
				String url = FisheyeUrlHelper.getFisheyeUrlForRevision(file, bfn.getRevision(), currentProject);
				if (url != null) {
					BrowserUtil.launchBrowser(url);
				}
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		if (e.getPresentation().isEnabled()) {
			boolean enabled = false;
			Project currentProject = e.getData(DataKeys.PROJECT);
			BambooFileNode bfn = getBambooFileNode(e);
			if (bfn != null && currentProject != null) {
				PsiFile file = guessPsiFile(currentProject, bfn);
				if (file != null) {
					String url = FisheyeUrlHelper.getFisheyeUrlForRevision(file, bfn.getRevision(), currentProject);
					if (url != null) {
						enabled = true;
					}
				}
			}
			e.getPresentation().setEnabled(enabled);
		}
	}
}
