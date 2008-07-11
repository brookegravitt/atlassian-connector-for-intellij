package com.atlassian.theplugin.idea.ui.filetree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.HashMap;

class FileNode extends DefaultMutableTreeNode {

	private Map<String, FileNode> children;
	private String name;

	public FileNode(String fullName) {
		super(fullName);
		name = fullName;
		children = new HashMap<String, FileNode>();
	}

	public void addChild(FileNode child) {
		if (!getChildren().containsKey(child.getName())) {
			getChildren().put(child.getName(), child);
			add(child);
		}
	}

	public void removeChild(FileNode child) {
		if (getChildren().containsKey(child.getName())) {
			getChildren().remove(child.getName());
			remove(child);
		}
	}

	public void removeChildren() {
		getChildren().clear();
		removeAllChildren();
	}

	public boolean hasNode(String fullName) {
		return getChildren().containsKey(fullName);
	}

	public FileNode getNode(String fullName) {
		return getChildren().get(fullName);
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

	public Map<String, FileNode> getChildren() {
		return children;
	}
}
