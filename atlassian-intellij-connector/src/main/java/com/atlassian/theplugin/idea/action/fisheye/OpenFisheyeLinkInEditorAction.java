package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.fisheye.SourceCodeLinkParser;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class OpenFisheyeLinkInEditorAction extends AnAction {
	@Override
	public void actionPerformed(final AnActionEvent event) {
		Project project = IdeaHelper.getCurrentProject(event);
		if (project != null) {
			String url = Messages.showInputDialog(project, "Copy FishEye link", "Fisheye source pointer", null);
			if (url != null && !"".equals(url)) {
				SourceCodeLinkParser parser = new SourceCodeLinkParser(url);
				parser.parse();
				if (parser.getPath() != null) {
					PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project, parser.getPath());
					if (psiFile != null) {
						final VirtualFile virtualFile = psiFile.getVirtualFile();
						if (virtualFile == null) {
							Messages.showErrorDialog(project,
									"No corresponding file found for PSI element", "Unable to open file");
							return;
						}
						if (parser.getRevision() != null) {
							VcsIdeaHelper.openFile(project, virtualFile, parser.getRevision(), parser.getLine(), 0, null);
						} else {
							FileEditorManager fem = FileEditorManager.getInstance(project);
							OpenFileDescriptor desc = new OpenFileDescriptor(project, virtualFile, 0, 0);
							fem.openTextEditor(desc, true);
						}
					} else {
						Messages.showErrorDialog(project, "File not found in the project", "Unable to open file");
					}
				} else {
					Messages.showErrorDialog(project, "File not found in the project", "Unable to open file");
				}
			}
		}

	}

	@Override
	public void update(final AnActionEvent event) {
		if (IdeaHelper.getCurrentProject(event) == null) {
			event.getPresentation().setEnabled(false);
		} else {
			event.getPresentation().setEnabled(true);
		}
	}
}
