package com.atlassian.theplugin.idea.ui.filetree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jul 11, 2008
* Time: 2:47:33 AM
* To change this template use File | Settings | File Templates.
*/
class FileNode extends DefaultMutableTreeNode {

	public Map<String, FileNode> children;
	private String name;

	public FileNode(String fullName) {
		super(fullName);
		name = fullName;
		children = new HashMap<String, FileNode>();
	}

	public void addChild(FileNode child) {
		if (!children.containsKey(child.getName())) {
			children.put(child.getName(), child);
			add(child);
		}
	}

	public void removeChild(FileNode child) {
		if (children.containsKey(child.getName())) {
			children.remove(child.getName());
			remove(child);
		}
	}

	public void removeChildren() {
		children.clear();
		removeAllChildren();
	}

	public boolean hasNode(String fullName) {
		return children.containsKey(fullName);
	}

	public FileNode getNode(String fullName) {
		return children.get(fullName);
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public String toString() {
		return getName();
	}
}
