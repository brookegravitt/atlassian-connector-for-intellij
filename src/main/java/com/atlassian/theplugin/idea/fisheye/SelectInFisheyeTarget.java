package com.atlassian.theplugin.idea.fisheye;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: Mar 10, 2009
 * Time: 10:56:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class SelectInFisheyeTarget implements SelectInTarget {
	public String getToolWindowId() {
		return null;
	}

	public String getMinorViewId() {
		return null;
	}

	public float getWeight() {
		return Float.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "Fisheye";
	}

	public boolean canSelect(SelectInContext context) {
		final Object selector = context.getSelectorInFile();
		final Project project = context.getProject();
		return findFisheyeLink(selector, project) != null;
	}

	private String findFisheyeLink(final Object selector, final Project project) {
		if (selector instanceof PsiElement) {
			PsiElement psiElement = (PsiElement) selector;
			String url = FisheyeUrlHelper.getFisheyeUrl(psiElement, project);
			return url;
		}
		return null;
	}

	public void selectIn(SelectInContext context, final boolean requestFocus) {
		final Object selector = context.getSelectorInFile();
		final Project project = context.getProject();
		final String fisheyeUrl = findFisheyeLink(selector, project);
		if (fisheyeUrl != null) {
			BrowserUtil.launchBrowser(fisheyeUrl);
		}
	}
}