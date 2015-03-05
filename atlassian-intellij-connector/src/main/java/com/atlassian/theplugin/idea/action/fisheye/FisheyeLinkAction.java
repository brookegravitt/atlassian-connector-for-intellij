package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.fisheye.FisheyeUrlHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class FisheyeLinkAction extends AbstractFisheyeAction {

	protected abstract void performUrlAction(final String url, Editor editor);

	@Override
	public void actionPerformed(final AnActionEvent event) {
		final VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
		final Editor editor = event.getData(PlatformDataKeys.EDITOR);
		if (editor == null || virtualFile == null) {
			return;
		}

		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return;
		}

		String url = FisheyeUrlHelper.getFisheyeUrl(virtualFile, editor, project);
		performUrlAction(url, editor);
	}

	public interface FisheyeAction {
		void run(String url);
	}

}
