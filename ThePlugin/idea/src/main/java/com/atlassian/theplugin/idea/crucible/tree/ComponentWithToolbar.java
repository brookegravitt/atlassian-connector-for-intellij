package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 31, 2008
 * Time: 2:42:22 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ComponentWithToolbar extends JPanel implements DataProvider {

    private static final String TOOLBAR_PLACE = "tree";

    public ComponentWithToolbar(final String toolbarName) {
        super(new BorderLayout());
        JComponent toolbar = null;
        if (toolbarName != null && toolbarName.length() > 0) {
            ActionManager aManager = ActionManager.getInstance();
            DefaultActionGroup serverToolBar = (DefaultActionGroup) aManager.getAction(toolbarName);
            serverToolBar.setInjectedContext(true);
            if (serverToolBar != null) {
                ActionToolbar actionToolbar = aManager.createActionToolbar(
                        TOOLBAR_PLACE, serverToolBar, true);
                toolbar = actionToolbar.getComponent();
            }

            if (toolbar != null) {
                add(toolbar, BorderLayout.NORTH);
            } else {
                add(new JLabel("Toolbar initialization failed"), BorderLayout.NORTH);
            }
        }
        add(getTreeComponent(), BorderLayout.CENTER);

    }

    protected abstract Component getTreeComponent();

    @Nullable
    public Object getData(@NonNls final String dataId) {
        if (dataId.equals(Constants.FILE_TREE)) {
            return getTreeComponent();
        }
        return null;
    }
}
