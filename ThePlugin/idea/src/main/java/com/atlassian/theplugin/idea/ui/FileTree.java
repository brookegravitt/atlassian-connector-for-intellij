package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.bamboo.BuildChangesToolWindow;
import com.atlassian.theplugin.commons.bamboo.CommitFile;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jgorycki
 * Date: Jul 11, 2008
 * Time: 2:02:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileTree extends JTree {
	DefaultTreeCellRenderer renderer = new FileTreeCellRenderer();


	public FileTree(java.util.List<CommitFile> files) {
		super();
		setModel(new FileTreeModel(files));
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		renderer.setOpenIcon(IconLoader.getIcon("/nodes/folderOpen.png"));
		renderer.setClosedIcon(IconLoader.getIcon("/nodes/folder.png"));
		setCellRenderer(renderer);
		setRootVisible(true);
	}

	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree,
													  Object value,
													  boolean selected,
													  boolean expanded,
													  boolean leaf,
													  int row,
													  boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(
					tree, value, selected, expanded, leaf, row, hasFocus);

			try {
				FileNode node = (FileNode) value;
				if (node.isLeaf()) {
					FileTypeManager mgr = FileTypeManager.getInstance();
					FileType type = mgr.getFileTypeByFileName(node.getName());
					setIcon(type.getIcon());
				}
			} catch (ClassCastException e) {
				// should not happen, making compiler happy
				setIcon(null);
			}

			return c;
		}
	}

	private class FileTreeModel implements TreeModel {

		private FileNode root;

		public FileTreeModel(java.util.List<CommitFile> files) {
			root = new FileNode("/");
			for (CommitFile f : files) {
				addFile(f);
			}
			compactTree(root);
		}

		private void compactTree(FileNode node) {
			if (node.isLeaf()) {
				return;
			}

			java.util.List<FileNode> ch = new ArrayList<FileNode>();

			for (FileNode n : node.children.values()) {
				ch.add(n);
			}

			node.removeChildren();

			for (FileNode n : ch) {
				compactTree(n);
				if (n.getChildCount() == 1) {
					FileNode cn = (FileNode) n.getFirstChild();
					if (!cn.isLeaf()) {
						String newName = n.getName() + "/" + cn.getName();
						cn.setName(newName);
						node.addChild(cn);
					} else {
						node.addChild(n);
					}
				} else {
					node.addChild(n);
				}
			}
		}

		public void addFile(CommitFile file) {
			int idx = 1;
			String fileName = file.getFileName();
			FileNode node = root;
			do {
				int newIdx = file.getFileName().indexOf('/', idx);
				if (newIdx != -1) {
					String newNodeName = fileName.substring(idx, newIdx);
					if (!node.hasNode(newNodeName)) {
						FileNode newNode = new FileNode(newNodeName);
						node.addChild(newNode);
						node = newNode;
					} else {
						node = node.getNode(newNodeName);
					}
				}
				idx = newIdx + 1;
			} while (idx > 0);
			node.addChild(new LeafFileNode(file));
		}

		public Object getRoot() {
			return root;
		}

		public Object getChild(Object parent, int index) {
			return ((FileNode) parent).getChildAt(index);
		}

		public int getChildCount(Object parent) {
			return ((FileNode) parent).children.size();
		}

		public boolean isLeaf(Object node) {
			return ((FileNode) node).children.size() == 0;
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		public int getIndexOfChild(Object parent, Object child) {
			return ((FileNode) parent).getIndex((FileNode) child);
		}

		public void addTreeModelListener(TreeModelListener l) {
		}

		public void removeTreeModelListener(TreeModelListener l) {
		}
	}



	private class FileNode extends DefaultMutableTreeNode {

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

	private class LeafFileNode extends FileNode {

		private CommitFile file;

		public LeafFileNode(CommitFile file) {
			super(file.getFileName().substring(file.getFileName().lastIndexOf('/') + 1));
			this.file = file;
		}

		public String toString() {
			String name = super.toString();
			return name + " - " + file.getRevision();
		}
	}

}
