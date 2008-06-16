package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 16, 2008
 * Time: 10:34:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class TreeFolderNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = -1192703287399203269L;
	private String name ;


	TreeFolderNode(String  name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return super.toString();	//To change body of overridden methods use File | Settings | File Templates.
	}
}
