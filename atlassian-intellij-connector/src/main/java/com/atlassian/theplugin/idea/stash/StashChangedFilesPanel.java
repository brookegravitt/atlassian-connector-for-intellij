package com.atlassian.theplugin.idea.stash;

import com.atlassian.connector.intellij.stash.Change;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class StashChangedFilesPanel extends JPanel {
    private	JTree tree;

    public StashChangedFilesPanel() {
        super(new BorderLayout());
        tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        this.add(tree);
        this.add(new JLabel("Changed files"), BorderLayout.NORTH);

        changeContents(Collections.<Change>emptyList());

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();

                if (node == null) return;

                Object nodeInfo = node.getUserObject();
                String path = nodeInfo.toString();

                DataContext dataContext = DataManager.getInstance().getDataContext();
                Project project = (Project) dataContext.getData(DataConstants.PROJECT);

                VirtualFile fileByRelativePath = project.getBaseDir().findFileByRelativePath(path);

                FileEditorManager.getInstance(project).openFile(fileByRelativePath, true);
            }
        });
    }

    public void changeContents(List<Change> paths)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("");

        tree.expandRow(0);
        tree.setRootVisible(false);

        for (Change path : paths) {
            DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(path.getFilePath());
            rootNode.add(newChild);
        }

        tree.setModel(new DefaultTreeModel(rootNode));
    }
}
