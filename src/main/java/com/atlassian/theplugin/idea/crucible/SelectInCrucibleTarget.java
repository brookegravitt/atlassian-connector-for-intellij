package com.atlassian.theplugin.idea.crucible;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * User: mwent
 * Date: Mar 10, 2009
 * Time: 10:56:15 AM
 */
public class SelectInCrucibleTarget implements SelectInTarget {
	@Nullable
	public String getToolWindowId() {
		return null;
	}

	@Nullable
	public String getMinorViewId() {
		return null;
	}

	public float getWeight() {
		return Float.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "Crucible";
	}

	public boolean canSelect(SelectInContext context) {
		final Object selector = context.getSelectorInFile();
		final Project project = context.getProject();
		return findCrucibleContext(selector, project) != null;
	}

	@Nullable
	private String findCrucibleContext(final Object selector, final Project project) {
		if (selector instanceof PsiElement) {
			PsiElement psiElement = (PsiElement) selector;
			return CrucibleHelper.getCommentUrl(project, psiElement);
		}
		return null;
	}

	public void selectIn(SelectInContext context, final boolean requestFocus) {
		final Object selector = context.getSelectorInFile();
		final Project project = context.getProject();
		final String crucibleContext = findCrucibleContext(selector, project);
		if (crucibleContext != null) {
			BrowserUtil.launchBrowser(crucibleContext);
		}
	}
}