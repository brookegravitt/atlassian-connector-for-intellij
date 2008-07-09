package com.atlassian.theplugin.idea.bamboo;

import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;
import com.atlassian.theplugin.commons.bamboo.Commit;
import com.atlassian.theplugin.commons.bamboo.CommitFile;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.util.*;
import java.util.List;
import java.awt.*;

public final class BuildChangesToolWindow {

	private static final String TOOL_WINDOW_TITLE = "Bamboo Build Changes";

	private static BuildChangesToolWindow instance = new BuildChangesToolWindow();

	private static HashMap<String, CommitDetailsPanel> panelMap = new HashMap<String, CommitDetailsPanel>();

	private BuildChangesToolWindow() {
	}

	public static BuildChangesToolWindow getInstance() {
		return instance;
	}

	public void showBuildChanges(String buildKey, String buildNumber, List<Commit> commits) {
		CommitDetailsPanel detailsPanel;
		String contentKey = buildKey + "-" + buildNumber;


		ToolWindowManager twm = ToolWindowManager.getInstance(IdeaHelper.getCurrentProject());
		ToolWindow commitDetailsToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (commitDetailsToolWindow == null) {
			commitDetailsToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			commitDetailsToolWindow.setIcon(Constants.BAMBOO_COMMITS_ICON);
		}

		Content content = commitDetailsToolWindow.getContentManager().findContent(contentKey);

		synchronized (panelMap) {
			if (content != null) {
				detailsPanel = panelMap.get(contentKey);
			} else {
				detailsPanel = new CommitDetailsPanel(contentKey, commits);
				panelMap.remove(contentKey);
				panelMap.put(contentKey, detailsPanel);

				PeerFactory peerFactory = PeerFactory.getInstance();
				content = peerFactory.getContentFactory().createContent(detailsPanel, contentKey, true);
				content.setIcon(Constants.BAMBOO_COMMITS_ICON);
				content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
				commitDetailsToolWindow.getContentManager().addContent(content);
			}
		}
		commitDetailsToolWindow.getContentManager().setSelectedContent(content);
		commitDetailsToolWindow.show(null);
	}

	private class CommitDetailsPanel extends JPanel {
		private static final float SPLIT_RATIO = 0.6f;

		public CommitDetailsPanel(String name, final List<Commit> commits) {
			super();
			setLayout(new GridBagLayout());

			Splitter split = new Splitter(false, SPLIT_RATIO);
			split.setShowDividerControls(true);

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;

			JPanel treePanel = new JPanel();
			treePanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc1 = new GridBagConstraints();

			ActionManager manager = ActionManager.getInstance();
			ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.Bamboo.TestResultsToolBar");
			ActionToolbar toolbar = manager.createActionToolbar(name, group, true);
//			JComponent comp = toolbar.getComponent();

			gbc1.gridx = 0;
			gbc1.gridy = 0;
			gbc1.weightx = 1.0;
			gbc1.weighty = 0.0;
			gbc1.fill = GridBagConstraints.HORIZONTAL;

//			treePanel.add(comp, gbc1);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
			for (Commit c : commits) {
				String info = c.getAuthor() + " - " + c.getCommitDate().toString();
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
				root.add(node);
				for (CommitFile f : c.getFiles()) {
					String fName = f.getFileName() + " - " + f.getRevision();
					DefaultMutableTreeNode fNode = new DefaultMutableTreeNode(fName);
					node.add(fNode);
				}
			}

			DefaultTreeModel tm = new DefaultTreeModel(root);
			JTree tree = new JTree(tm);
			tree.setRootVisible(false);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			gbc1.gridy = 1;
			gbc1.weighty = 1.0;
			gbc1.fill = GridBagConstraints.BOTH;

			treePanel.add(new JScrollPane(tree), gbc1);

			split.setFirstComponent(treePanel);

			JPanel fileTreePanel = new JPanel();
			fileTreePanel.setLayout(new GridBagLayout());

			gbc1.gridy = 0;
			gbc1.weighty = 0.0;
			gbc1.fill = GridBagConstraints.NONE;
			gbc1.anchor = GridBagConstraints.LINE_START;

			JLabel label = new JLabel("Commit Files");

			Dimension d = label.getPreferredSize();
			d.height = toolbar.getMaxButtonHeight();
			label.setMinimumSize(d);
			label.setPreferredSize(d);
			fileTreePanel.add(label, gbc1);

			gbc1.gridy = 1;
			gbc1.weighty = 1.0;
			gbc1.fill = GridBagConstraints.BOTH;

			if (commits.size() > 0) {
				fileTreePanel.add(new JScrollPane(createFileTree(commits.get(0).getFiles())), gbc1);
			} else {
				fileTreePanel.add(new JLabel("No commits"), gbc1);
			}

			split.setSecondComponent(fileTreePanel);

			add(split, gbc);
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

			public boolean hasNode(String fullName) {
				return children.containsKey(fullName);
			}

			public FileNode getNode(String fullName) {
				return children.get(fullName);
			}

			public String toString() {
				String s = super.toString();
				int idx = s.lastIndexOf('/');
				if (idx == -1) {
					return s;
				}
				return s.substring(idx + 1);
			}

			public String getName() {
				return name;
			}
		}

		private class LeafFileNode extends FileNode {

			private CommitFile file;

			public LeafFileNode(CommitFile file) {
				super(file.getFileName().substring(file.getFileName().lastIndexOf('/')));
				this.file = file;
			}

			public String toString() {
				String name = super.toString();
				return name + " - " + file.getRevision();
			}
		}

		private class FileTreeModel implements TreeModel {

			private FileNode root;

            public FileTreeModel(List<CommitFile> files) {
                root = new FileNode("/");
                for (CommitFile f : files) {
                    addFile(f);
                }
            }

            public void addFile(CommitFile file) {
                int idx = 1;
                String fileName = file.getFileName();
                FileNode node = root;
                do {
                    int newIdx = file.getFileName().indexOf('/', idx);
                    if (newIdx != -1) {
                        String newNodeName = fileName.substring(1, newIdx);
                        if (!node.hasNode(newNodeName)) {
                            FileNode newNode = new FileNode(fileName.substring(1, newIdx));
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

        private JTree createFileTree(List<CommitFile> files) {
            JTree tree = new JTree(new FileTreeModel(files));
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
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
            };
			renderer.setOpenIcon(IconLoader.getIcon("/nodes/folderOpen.png"));
			renderer.setClosedIcon(IconLoader.getIcon("/nodes/folder.png"));
			tree.setCellRenderer(renderer);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			tree.setRootVisible(false);
            return tree;
        }
    }
}
