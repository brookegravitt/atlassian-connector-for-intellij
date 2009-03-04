/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author Lukasz Guminski
 */
public abstract class ComponentWithToolbar extends JPanel implements DataProvider {

    private static final String TOOLBAR_PLACE = "tree";
	protected JScrollPane jScrollPane;

	public ComponentWithToolbar(final String toolbarName, boolean canHaveHorizontalScrollbar) {
        super(new BorderLayout());
        JComponent toolbar = null;
        if (toolbarName != null && toolbarName.length() > 0) {
            ActionManager aManager = ActionManager.getInstance();
            DefaultActionGroup serverToolBar = (DefaultActionGroup) aManager.getAction(toolbarName);
            if (serverToolBar != null) {
				serverToolBar.setInjectedContext(true);
				ActionToolbar actionToolbar = aManager.createActionToolbar(
                        TOOLBAR_PLACE, serverToolBar, true);
                toolbar = actionToolbar.getComponent();
				actionToolbar.setTargetComponent(this);
			}

            if (toolbar != null) {
                add(toolbar, BorderLayout.NORTH);
            } else {
                add(new JLabel("Toolbar initialization failed"), BorderLayout.NORTH);
            }
        }

		jScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				canHaveHorizontalScrollbar
						? JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
						: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(jScrollPane, BorderLayout.CENTER);
	}

    protected abstract Component getTreeComponent();

    @Nullable
    public Object getData(@NonNls final String dataId) {
        if (dataId.equals(Constants.FILE_TREE)) {
            return this;
        }
        return null;
    }
}
