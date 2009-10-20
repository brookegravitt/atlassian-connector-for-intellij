package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.crucible.CrucibleCreatePostCommitReviewForm;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

public class RefreshCommittedChanges extends AnAction {

	public RefreshCommittedChanges() {
		setEnabledInModalContext(true);
	}

	public RefreshCommittedChanges(final String text) {
		super(text);
		setEnabledInModalContext(true);
	}

	public RefreshCommittedChanges(final String text, final String description, final Icon icon) {
		super(text, description, icon);
		setEnabledInModalContext(true);
	}

	public void actionPerformed(final AnActionEvent e) {
		CrucibleCreatePostCommitReviewForm b = e.getData(CrucibleCreatePostCommitReviewForm.COMMITTED_CHANGES_BROWSER_KEY);
		b.updateChanges();
	}
}
