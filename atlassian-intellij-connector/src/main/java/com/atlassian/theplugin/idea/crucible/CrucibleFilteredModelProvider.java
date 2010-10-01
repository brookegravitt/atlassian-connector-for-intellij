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

import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.tree.FilteredModelProvider;
import com.intellij.util.Icons;

import javax.swing.*;

public abstract class CrucibleFilteredModelProvider extends FilteredModelProvider<CrucibleFilteredModelProvider.Filter> {
	protected CrucibleFilteredModelProvider(ModelProvider provider, final Filter initialFiltering) {
		super(provider, initialFiltering);
	}

	public enum Filter {
		FILES_WITH_COMMENTS_ONLY(Icons.ABSTRACT_CLASS_ICON, "Files with comments only") {
			@Override
			public Filter getNextState() {
				return FILES_ALL;
			}},

		FILES_ALL(Icons.ANONYMOUS_CLASS_ICON, "All files from review") {
			@Override
			public Filter getNextState() {
				return FILES_WITH_COMMENTS_ONLY;
			}};
		private Icon icon;
		private String string;

		Filter(final Icon icon, final String string) {
			this.icon = icon;
			this.string = string;
		}

		public abstract Filter getNextState();

		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return string;
		}

	}
}
