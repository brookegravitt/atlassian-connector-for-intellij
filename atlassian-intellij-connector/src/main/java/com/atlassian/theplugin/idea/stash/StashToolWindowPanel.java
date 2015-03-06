package com.atlassian.theplugin.idea.stash;

import com.atlassian.connector.intellij.stash.PullRequest;
import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.beans.PullRequestBean;
import com.atlassian.connector.intellij.stash.impl.StashServerFacadeImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.bamboo.ThreePanePanel;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashToolWindowPanel extends ThreePanePanel implements DataProvider {


    private StashServerFacade stashServerFacade;
    private StashChangedFilesPanel stashChangedFilesPanel;

    private JLabel leftPanelLabel;

    public StashToolWindowPanel() {
        final JPanel toolBarPanel = new JPanel(new GridBagLayout());
        stashServerFacade = StashServerFacadeImpl.getInstance();
        stashChangedFilesPanel = new StashChangedFilesPanel();
        leftPanelLabel = new JLabel();
        init();
    }

    @Nullable
    public Object getData(String s) {
        return null;
    }

    @Override
    protected JTree getRightTree() {
        Object[] pullRequestArray = stashServerFacade.getPullRequests().toArray();
        JTree tree = new JTree(pullRequestArray);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                PullRequestBean pullRequest = (PullRequestBean) ((JTree.DynamicUtilTreeNode) e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
                StashServerFacadeImpl.getInstance().setCurrentPullRequest(pullRequest);
                leftPanelLabel.setText("<html>" + pullRequest.getTitle() + "<br/> fsadfasd</html>");

                Project project = ProjectManager.getInstance().getOpenProjects()[0];
                ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(project);

                stashAndCheckoutPR(pullRequest);
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

    private void stashAndCheckoutPR(PullRequest pullRequest)
    {
        try {
            Project project = ProjectManager.getInstance().getOpenProjects()[0];
            File root = new File(project.getBasePath());

//            Process fetch = Runtime.getRuntime().exec("git fetch", null, root);
//            fetch.waitFor();

            Process checkout = Runtime.getRuntime().exec("git checkout " + pullRequest.getRef(), null, root);
            checkout.waitFor();
            String s = IOUtils.toString(checkout.getInputStream());

            LocalFileSystem.getInstance().refresh(false);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JComponent getLeftToolBar() {
        JPanel panel = new JPanel(new GridBagLayout());

        return panel;
    }

    @Override
    protected JComponent getLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.add(leftPanelLabel);

        return panel;
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
