package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;

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

	public static AtlassianTreeModel buildTreeModelFromCrucibleChangeSet(ReviewData changeSet)
			throws ValueNotYetInitialized {
		FileNode root = new CrucibleChangeSetTitleNode(changeSet);
		FileTreeModel model = new FileTreeModel(root);
		for (CrucibleFileInfo f : changeSet.getFiles()) {
			model.addFile(root, f);
		}
		model.compactModel(model.getRoot());
		return model;
	}

	public static AtlassianTreeModel buildTreeModelFromBambooChangeSet(BambooChangeSet changeSet) {
		FileNode root = new FileNode("/");
		FileTreeModel model = new FileTreeModel(root);
		for (BambooFileInfo f : changeSet.getFiles()) {
			model.addFile(root, f);
		}
		model.compactModel(model.getRoot());
		return model;
	}

	public static AtlassianTreeModel buildFlatTreeModelFromBambooChangeSet(BambooChangeSet changeSet) {
		FileTreeModel model = new FileTreeModel(new FileNode("/"));
		for (BambooFileInfo f : changeSet.getFiles()) {
			model.getRoot().addChild(new BambooFileNode(f));
		}
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

		public void addFile(FileNode root, BambooFileInfo file) {
			FileNode node = createPlace(root, file);
			node.addChild(new BambooFileNode(file));
		}

		public void addFile(FileNode root, CrucibleFileInfo file) {
			FileNode node = createPlace(root, file);
			node.addChild(new CrucibleFileNode(file));
		}


		private FileNode createPlace(FileNode root, VersionedFileInfo file) {
			int idx = 0;
			String fileName = file.getFileDescriptor().getUrl();
			FileNode node = root;
			do {
				int newIdx = fileName.indexOf('/', idx);
				if (newIdx != -1) {
					String newNodeName = fileName.substring(idx, newIdx);
					if (newNodeName.length() > 0) {
						if (!node.hasNode(newNodeName)) {
							FileNode newNode = new FileNode(newNodeName);
							node.addChild(newNode);
							node = newNode;
						} else {
							node = node.getNode(newNodeName);
						}
					}
				}
				idx = newIdx + 1;
			} while (idx > 0);
			return node;
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
