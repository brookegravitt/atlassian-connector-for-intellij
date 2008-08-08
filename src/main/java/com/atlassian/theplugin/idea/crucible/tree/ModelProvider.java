package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.Filter;
import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 11:09:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelProvider {
	private Filter filter = Filter.ALL;

	public abstract AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.STATE state);
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

	public Filter getNextState(final Filter filter) {
		return Filter.ALL;
	}

	public static final ModelProvider EMPTY_MODEL_PROVIDER = new ModelProvider() {
        private AtlassianTreeModel model = new AtlassianTreeModel(new FolderNode("/", AtlassianClickAction.EMPTY_ACTION));

        public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.STATE state) {
            return model;
        }

	};
}
