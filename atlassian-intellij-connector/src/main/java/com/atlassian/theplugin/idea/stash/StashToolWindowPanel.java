package com.atlassian.theplugin.idea.stash;

import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.beans.PullRequestBean;
import com.atlassian.connector.intellij.stash.impl.StashServerFacadeImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.bamboo.ThreePanePanel;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashToolWindowPanel extends ThreePanePanel implements DataProvider {


    private StashServerFacade stashServerFacade;
    private StashChangedFilesPanel stashChangedFilesPanel;

    public StashToolWindowPanel() {
        final JPanel toolBarPanel = new JPanel(new GridBagLayout());
        stashServerFacade = StashServerFacadeImpl.getInstance();
        stashChangedFilesPanel = new StashChangedFilesPanel();
        init();
    }

    @Nullable
    public Object getData(String s) {
        return null;
    }

    @Override
    protected JTree getRightTree() {
        JTree tree = new JTree(stashServerFacade.getPullRequests().toArray());

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                PullRequestBean pullRequest = (PullRequestBean) ((JTree.DynamicUtilTreeNode)e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
                StashServerFacadeImpl.getInstance().setCurrentPullRequest(pullRequest);

                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(project);

                projectComponent.getFileEditorListener().scanOpenEditors();

                loadChangedFiles();
            }
        });

        return tree;
    }

    private void loadChangedFiles()
    {
        stashChangedFilesPanel.changeContents(stashServerFacade.getChangedFiles());
    }

    @Override
    protected JComponent getLeftToolBar() {
        return new JPanel(new GridBagLayout());
    }

    @Override
    protected JComponent getLeftPanel() {
        return new JPanel(new GridBagLayout());
    }

    @Override
    protected JComponent getRightMostPanel() {
        return stashChangedFilesPanel;
    }

    @Override
    protected JComponent getRightMostToolBar() {
        return new JPanel(new GridBagLayout());
    }
}
