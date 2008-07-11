package com.atlassian.theplugin.idea.ui.filetree;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;
import com.intellij.psi.PsiFile;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Jul 11, 2008
* Time: 2:45:53 AM
* To change this template use File | Settings | File Templates.
*/
public class FileTreeModel implements TreeModel {

	private FileNode root;

	private PsiFile a;

	public FileTreeModel(java.util.List<VersionedFileDescriptor> files) {


		root = new FileNode("/");
		for (VersionedFileDescriptor f : files) {
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

	public void addFile(VersionedFileDescriptor file) {
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
