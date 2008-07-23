package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class GoToSourceAction extends AnAction {

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

    @Nullable
    private TreePath getSelectedTreePath(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Component component = DataKeys.CONTEXT_COMPONENT.getData(dataContext);
        if (!(component instanceof JTree)) {
            return null;
        }
        final JTree theTree = (JTree) component;
        return theTree.getSelectionPath();
    }

    @Nullable
    private BambooFileNode getBambooFileNode(AnActionEvent e) {
        TreePath treepath = getSelectedTreePath(e);
        if (treepath == null) {
            return null;
        }
        return getBambooFileNode(treepath);
    }


    private void jumpToSource(final Project project, final BambooFileNode bfn) {
        String pathname = bfn.getBambooFileInfo().getFileDescriptor().getUrl();

        PsiFile[] psifiles = PsiManager.getInstance(project).getShortNamesCache().getFilesByName(bfn.getName());

        PsiFile matchingFile = CodeNavigationUtil.guessMatchingFile(pathname, psifiles, project.getBaseDir());
        if (matchingFile == null) {
            // TODO add file selection windo
        } else {
            matchingFile.navigate(true);
        }
    }


    @Nullable
    private BambooFileNode getBambooFileNode(TreePath path) {
        Object o = path.getLastPathComponent();
        if (o instanceof BambooFileNode) {
            return (BambooFileNode) o;
        }
        return null;

    }

}
