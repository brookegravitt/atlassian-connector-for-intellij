package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

public class GoToSourceAction extends AbstractBambooFileActions {

    @Override
    public void update(AnActionEvent e) {
        BambooFileNode bfn = getBambooFileNode(e);
        e.getPresentation().setEnabled(bfn != null && bfn.getPsiFile() != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getData(DataKeys.PROJECT);
        BambooFileNode bfn = getBambooFileNode(e);
        if (bfn != null && currentProject != null) {
            jumpToSource(currentProject, bfn);
        }

    }


}
