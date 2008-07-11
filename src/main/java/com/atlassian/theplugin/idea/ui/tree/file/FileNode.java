package com.atlassian.theplugin.idea.ui.tree.file;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

public class FileNode extends AtlassianTreeNode {

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

	public ColoredTreeCellRenderer getTreeCellRenderer() {
		return FileNodeRenderer.getInstance();
	}

	private static class FileNodeRenderer extends ColoredTreeCellRenderer {
		private static FileNodeRenderer instance;

		public static FileNodeRenderer getInstance() {
			if (instance == null) {
				instance = new FileNodeRenderer();
			}
			return instance;
		}

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			FileNode node = (FileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
		}
	}
}
