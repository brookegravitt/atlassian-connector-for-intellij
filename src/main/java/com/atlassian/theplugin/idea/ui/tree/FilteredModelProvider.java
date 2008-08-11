package com.atlassian.theplugin.idea.ui.tree;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 7, 2008
 * Time: 1:28:57 PM
 * To change this template use File | Settings | File Templates.
 */
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

	protected abstract Filter getFilter(E type);

	public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.STATE state) {
		return provider.getModel(state).getFilteredModel(getFilter(type));
	}

	private static final Filter FILES_WITH_COMMENTS_ONLY = new Filter() {
		public boolean isValid(final AtlassianTreeNode node) {
			if (node instanceof CrucibleFileNode) {
				CrucibleFileNode anode = (CrucibleFileNode) node;
				try {
					return anode.getFile().getNumberOfComments() > 0;
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					return false;
				}
			}

			return true;
		}
	};


}
