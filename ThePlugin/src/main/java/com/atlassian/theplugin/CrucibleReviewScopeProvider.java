package com.atlassian.theplugin;

import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.CustomScopesProvider;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Apr 21, 2008
 * Time: 4:15:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleReviewScopeProvider implements CustomScopesProvider {
	public static final String SCOPE_NAME = "Crucible Reviews";
	public static final NamedScope SCOPE = new NamedScope(SCOPE_NAME, new MyPackageSet());


	public CrucibleReviewScopeProvider() {
	}

	@NotNull
	public List<NamedScope> getCustomScopes() {
		return Collections.singletonList(SCOPE);
	}

	private static class MyPackageSet implements PackageSet {

		public boolean contains(final PsiFile file, final NamedScopesHolder holder) {
			return true;
//			final VirtualFile virtualFile = file.getVirtualFile();
//			if (virtualFile == null) {
//				return false;
//			}
//			final Project project = file.getProject();
//			final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
//			final Change change = changeListManager.getChange(virtualFile);
//			if (change != null) {
//				final LocalChangeList changeList = changeListManager.getChangeList(change);
//				return changeList != null && changeList.equals(changeListManager.getDefaultChangeList());
//			} else {
//				return false;
//			}
		}

		public PackageSet createCopy() {
			return new MyPackageSet();
		}

		@NonNls
		public String getText() {
			return "Review";
		}

		public int getNodePriority() {
			return 0;
		}
	}
}
