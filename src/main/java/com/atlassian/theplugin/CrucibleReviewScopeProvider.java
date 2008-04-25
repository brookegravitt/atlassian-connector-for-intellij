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
	public static final NamedScope SCOPE = new NamedScope(SCOPE_NAME, new ToReviewPackageSet("CR-6"));


	public CrucibleReviewScopeProvider() {
	}

	@NotNull
	public List<NamedScope> getCustomScopes() {
		return Collections.EMPTY_LIST;
	}

	private static class ToReviewPackageSet implements PackageSet {
		private String review;

		public ToReviewPackageSet(String review) {
			this.review = review;
		}

		public boolean contains(final PsiFile file, final NamedScopesHolder holder) {
			return true;
		}

		public PackageSet createCopy() {
			return new ToReviewPackageSet(review);
		}

		@NonNls
		public String getText() {
			return "Review " + review;
		}

		public int getNodePriority() {
			return 0;
		}
	}
}
