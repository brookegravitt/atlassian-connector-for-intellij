package com.atlassian.theplugin.idea.stash;

import com.atlassian.connector.intellij.stash.StashServerFacade;
import com.atlassian.connector.intellij.stash.impl.StashServerFacadeImpl;
import com.atlassian.theplugin.idea.bamboo.ThreePanePanel;
import com.intellij.openapi.actionSystem.DataProvider;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashToolWindowPanel extends ThreePanePanel implements DataProvider {



    public StashToolWindowPanel() {
        final JPanel toolBarPanel = new JPanel(new GridBagLayout());
        init();
    }

    @Nullable
    public Object getData(String s) {
        return null;
    }

    @Override
    protected JTree getRightTree() {
        StashServerFacade stashServerFacade = new StashServerFacadeImpl();
        JTree tree = new JTree(stashServerFacade.getPullRequests().toArray());

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                
            }
        });

        return tree;
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
        return new JPanel(new GridBagLayout());
    }

    @Override
    protected JComponent getRightMostToolBar() {
        return new JPanel(new GridBagLayout());
    }
}
