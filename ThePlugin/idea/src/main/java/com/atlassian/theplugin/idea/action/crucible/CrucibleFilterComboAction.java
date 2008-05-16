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

package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.crucible.api.PredefinedFilter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;

import javax.swing.*;

public class CrucibleFilterComboAction extends ComboBoxAction {
    public static final String QF_NAME = "PredefinedFilter";
    private static final String NO_PREDEFINED_FILTER_TEXT = "Select predefined filter";
    private ComboBoxButton button = null;

    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        if (!(jComponent instanceof ComboBoxButton)) {
            throw new UnsupportedOperationException("This action can only be used as a combobox");
        }

        DefaultActionGroup group = new DefaultActionGroup();
        button = (ComboBoxButton) jComponent;

        for (PredefinedFilter filter : PredefinedFilter.values()) {
            group.add(new AnAction(filter.getFilterName()) {
                public void actionPerformed(AnActionEvent event) {
                    button.setText(event.getPresentation().getText());
                    //IdeaHelper.getJIRAToolWindowPanel(event).addQueryFragment(filter);
                    //IdeaHelper.getJIRAToolWindowPanel(event).refreshIssues();
                }
            });
        }
        return group;
    }

/*
    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
            if (!IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilterUsed()) {
                setComboText(event, NO_SAVED_FILTER_TEXT, false);
            } else {
                if (IdeaHelper.getJIRAToolWindowPanel(event).getFilters().getSavedFilter() != null) {
                    setComboText(event,
                            IdeaHelper.getJIRAToolWindowPanel(event).
                                    getFilters().getSavedFilter().getFilterEntry().get("name"), true);
                } else {
                    setComboText(event, NO_SAVED_FILTER_TEXT, true);
                }
            }
        }
    }

    private void setComboText(AnActionEvent event, String label, boolean enabled) {
        if (button != null) {
            button.setText(label);
            button.setEnabled(enabled);
        } else {
            event.getPresentation().setText(label);
            event.getPresentation().setEnabled(enabled);
        }
    }
*/    
}