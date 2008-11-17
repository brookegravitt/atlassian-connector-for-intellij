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

package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.scope.packageSet.CustomScopesProvider;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import org.jetbrains.annotations.NonNls;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Apr 21, 2008
 * Time: 4:15:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleReviewScopeProvider implements CustomScopesProvider {
	public static final String SCOPE_NAME = "Crucible Reviews";

	private Map<String, NamedScope> scopes = new HashMap<String, NamedScope>();

	public CrucibleReviewScopeProvider() {
	}
	
	public List<NamedScope> getCustomScopes() {
		return Collections.unmodifiableList(new ArrayList<NamedScope>(scopes.values()));
	}

	public void addScope(String name, ToReviewAbstractPackageSet packageSet) {
		if (scopes.containsKey(name)) {
			throw new IllegalArgumentException("Scope of that name (" + name + ") already exists.");
		}
		packageSet.setName(name);
		scopes.put(name, new NamedScope(name, packageSet));
	}

	public void removeScope(String name) {
		if (!scopes.containsKey(name)) {
			throw new IllegalArgumentException("Scope of that name (" + name + ") does not exists.");
		}
		scopes.remove(name);
	}

	public boolean isScopeDefined(String name) {
		return scopes.containsKey(name);
	}

	public static CrucibleReviewScopeProvider getCrucibleScopeProvider(Project project) {
		CrucibleReviewScopeProvider provider = null;
		CustomScopesProvider[] scopeProviders
				= project.getExtensions(CustomScopesProvider.CUSTOM_SCOPES_PROVIDER);
		for (CustomScopesProvider scopeProvider : scopeProviders) {
			if (scopeProvider instanceof CrucibleReviewScopeProvider) {
				provider = (CrucibleReviewScopeProvider) scopeProvider;
			}
		}
		return provider;
	}


	public abstract static class ToReviewAbstractPackageSet implements PackageSet {

		private String name;

		protected void setName(String name) {
			this.name = name;
		}

		protected ToReviewAbstractPackageSet() {
			super();
		}

		public PackageSet createCopy() {
			return this;
		}

		@NonNls
		public String getText() {
			return "Review " + name;
		}

		public int getNodePriority() {
			return 0;
		}
	}
}
