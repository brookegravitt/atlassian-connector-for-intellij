package com.atlassian.theplugin.idea.ui.tree;

import com.atlassian.theplugin.idea.ui.tree.comment.FileNameNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 7, 2008
 * Time: 1:28:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleFilterProvider {
	private Filter filter = Filter.ALL;

	public Filter switchFilter() {
		if (filter.equals(Filter.ALL)) {
			filter = FILES_WITH_COMMENTS_ONLY;
			return filter;
		}

		filter = Filter.ALL;
		return filter;
	}


	private static final Filter FILES_WITH_COMMENTS_ONLY = new Filter() {
		public boolean isValid(final AtlassianTreeNode node) {
			if (node instanceof FileNameNode) {
				FileNameNode anode = (FileNameNode) node;
				try {
					return anode.getFile().getNumberOfComments() > 0;
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					return false;
				}
			}			
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
