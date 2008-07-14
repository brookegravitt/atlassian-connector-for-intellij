package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.VersionedFileDescriptor;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
* User: jgorycki
* Date: Jul 11, 2008
* Time: 2:45:53 AM
* To change this template use File | Settings | File Templates.
*/
public final class FileTreeModelBuilder {

    private FileTreeModelBuilder() {
        // this is a utility class
    }

    public static AtlassianTreeModel buildTreeModelFromFiles(java.util.List<VersionedFileDescriptor> files) {
		FileTreeModel model = new FileTreeModel(new FileNode("/"));
		for (VersionedFileDescriptor f : files) {
			model.addFile(f);
		}
		model.compactModel(model.getRoot());
		return model;
	}

	private static class FileTreeModel extends AtlassianTreeModel {
		public FileTreeModel(FileNode root) {
			super(root);
		}

		@Override
		public FileNode getRoot() {
			return (FileNode) super.getRoot();	
		}

		public void addFile(VersionedFileDescriptor file) {
			int idx = 1;
			String fileName = file.getFileName();
			FileNode node = (FileNode) getRoot();
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

		private void compactModel(FileNode node) {
			if (node.isLeaf()) {
				return;
			}

			java.util.List<FileNode> ch = new ArrayList<FileNode>();

			for (FileNode n : node.getChildren().values()) {
				ch.add(n);
			}

			node.removeChildren();

			for (FileNode n : ch) {
				compactModel(n);
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

	}
}
