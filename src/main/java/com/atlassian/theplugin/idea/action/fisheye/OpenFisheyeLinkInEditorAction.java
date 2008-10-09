package com.atlassian.theplugin.idea.action.fisheye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.fisheye.SourceCodeLinkParser;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.gargoylesoftware.htmlunit.TextUtil;

import java.util.Scanner;
import java.util.regex.MatchResult;

public class OpenFisheyeLinkInEditorAction extends AnAction {
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
						if (parser.getRevision() != null) {
							VcsIdeaHelper.openFile(project, psiFile.getVirtualFile(), parser.getRevision(), parser.getLine(), 0,
									null);
						} else {
							FileEditorManager fem = FileEditorManager.getInstance(project);
							OpenFileDescriptor desc = new OpenFileDescriptor(project, psiFile.getVirtualFile(), 0, 0);
							if (desc != null) {
								fem.openTextEditor(desc, true);
							}
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

	public void update(final AnActionEvent event) {
		if (IdeaHelper.getCurrentProject(event) == null) {
			event.getPresentation().setEnabled(false);
		} else {
			event.getPresentation().setEnabled(true);
		}
	}
}
