package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JiraPresetFilter;

import javax.swing.*;

/**
 * User: kalamon
 * Date: 2009-11-18
 * Time: 14:17:03
 */
public class JiraPresetFilterTreeNode extends AbstractTreeNode {
    private final JiraPresetFilter filter;

    public JiraPresetFilterTreeNode(JiraPresetFilter filter) {
        super(filter.getName(), null, null);
        this.filter = filter;
    }

    public String toString() {
        return filter.getName();
    }

    public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
        return new JLabel("Invalid renderer");
    }

    public JiraPresetFilter getPresetFilter() {
        return filter;
    }
}
