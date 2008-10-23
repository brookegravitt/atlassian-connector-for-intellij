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

package com.atlassian.theplugin.idea.ui.tree;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;

public abstract class FilteredModelProvider<E extends Enum<E>> extends ModelProvider {
	private ModelProvider provider;

	private E type;

	protected FilteredModelProvider(ModelProvider provider, E initialFiltering) {
		this.provider = provider;
		type = initialFiltering;
	}

	public void setType(final E type) {
		this.type = type;
	}

	public E getType() {
		return type;
	}

	protected abstract Filter getFilter(E aType);

	@Override
	public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state) {
		return provider.getModel(state).getFilteredModel(getFilter(type));
	}

	private static final Filter FILES_WITH_COMMENTS_ONLY = new Filter() {
		@Override
		public boolean isValid(final AtlassianTreeNode node) {
			if (node instanceof CrucibleFileNode) {
				CrucibleFileNode anode = (CrucibleFileNode) node;
				return anode.getFile().getNumberOfComments() > 0;
			}

			return true;
		}
	};


}
