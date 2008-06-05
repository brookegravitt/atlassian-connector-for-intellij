package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CrucibleShowCustomFilterAction extends AnAction {

	public void actionPerformed(AnActionEvent e) {
		final CrucibleTableToolWindowPanel cruciblePanel = IdeaHelper.getCrucibleToolWindowPanel(e);

		if (cruciblePanel != null) {
			cruciblePanel.showCrucibleCustomFilter();
		}
	}

	public void update(AnActionEvent event) {
		super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
            event.getPresentation().setVisible(
                    (IdeaHelper.getCrucibleToolWindowPanel(event).getCrucibleVersion() == CrucibleVersion.CRUCIBLE_16));
        } else {
            event.getPresentation().setVisible(false);
        }
	}
}
