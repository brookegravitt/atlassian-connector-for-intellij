package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 12, 2008
 * Time: 12:22:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleTreeRootNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = 0L;

	private CrucibleChangeSet crucibleChangeSet;

	CrucibleTreeRootNode(CrucibleChangeSet infoAdapater) {
		crucibleChangeSet = infoAdapater;
	}

	CrucibleTreeRootNode() {
		
	}

	public String toString() {
		if (crucibleChangeSet != null) {
			return crucibleChangeSet.getProjectKey() + ", " + crucibleChangeSet.getPermaId()
                    + ", " + crucibleChangeSet.getName();
		} else {
			return "No Review is selected";
		}
	}



	public CrucibleChangeSet getCrucibleChangeSet() {
		return crucibleChangeSet;
	}

	public void setCrucibleChangeSet(CrucibleChangeSet crucibleChangeSet) {
		this.crucibleChangeSet = crucibleChangeSet;
	}
}
