package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.ui.AtlassianToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 10:35:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianTreeWithToolbar extends JPanel {
    private AtlassianTree tree = new AtlassianTree();
    private ModelProvider modelProvider = ModelProvider.EMPTY_MODEL_PROVIDER;
    private STATE state = STATE.DIRED;

    public AtlassianTreeWithToolbar(String toolbar, final ModelProvider modelProvider) {
        this(toolbar);
        setModelProvider(modelProvider);
    }

    public AtlassianTreeWithToolbar(final String toolbar) {
        super(new BorderLayout());
        add(AtlassianToolbar.createToolbar("tree", toolbar), BorderLayout.NORTH);
        add(tree, BorderLayout.CENTER);
    }

    public void setRootVisible(final boolean isVisible) {
        tree.setRootVisible(isVisible);
    }

    public void setModel(final AtlassianTreeModel model) {
        tree.setModel(model);
        tree.expandAll();
    }

    public void expandAll() {
        tree.expandAll();
    }

    @NotNull
    public void setModelProvider(final ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
        setState(state);
    }

    public STATE getState() {
        return state;
    }

    public void setState(final STATE state) {
        setModel(modelProvider.getModel(state));
        this.state = state;
    }

    public void changeState() {
        setState(getState().getNextState());
    }

    public void collapseAll() {
        tree.collapseAll();
    }

    public enum STATE {
        FLAT {STATE getNextState() {
            return DIRED;
        }},
        DIRED {STATE getNextState() {
            return FLAT;
        }};

        abstract STATE getNextState();
    }
}


