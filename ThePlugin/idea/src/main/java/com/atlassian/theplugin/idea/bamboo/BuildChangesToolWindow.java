package com.atlassian.theplugin.idea.bamboo;

import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;
import com.atlassian.theplugin.commons.bamboo.Commit;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.filetree.FileTree;
import com.atlassian.theplugin.idea.ui.filetree.FileTreeModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.*;
import java.util.List;
import java.awt.*;

public final class BuildChangesToolWindow {

	public interface ChangesTree extends Expandable {
		void showDiff();
		void showDiffWithLocal();
		void showRepositoryVersion();
	}

	private static final String TOOL_WINDOW_TITLE = "Bamboo Build Changes";

	private static BuildChangesToolWindow instance = new BuildChangesToolWindow();

	private static HashMap<String, CommitDetailsPanel> panelMap = new HashMap<String, CommitDetailsPanel>();

	private BuildChangesToolWindow() {
	}

	public static BuildChangesToolWindow getInstance() {
		return instance;
	}

	public static ChangesTree getChangesTree(String name) {
		return panelMap.get(name);
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

		if (content == null) {
			detailsPanel = new CommitDetailsPanel(contentKey, commits);
			panelMap.remove(contentKey);
			panelMap.put(contentKey, detailsPanel);

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(detailsPanel, contentKey, true);
			content.setIcon(Constants.BAMBOO_COMMITS_ICON);
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			commitDetailsToolWindow.getContentManager().addContent(content);
		}

		commitDetailsToolWindow.getContentManager().setSelectedContent(content);
		commitDetailsToolWindow.show(null);
	}

	private class CommitDetailsPanel extends JPanel implements ChangesTree {
		private static final float SPLIT_RATIO = 0.6f;
		private JTree fileTree;
		private JTable commitsTable;
		private JScrollPane fileScroll;

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

			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc1 = new GridBagConstraints();

			gbc1.gridx = 0;
			gbc1.gridy = 0;
			gbc1.weightx = 1.0;
			gbc1.weighty = 1.0;
			gbc1.fill = GridBagConstraints.BOTH;

			commitsTable = createCommitsTable(commits);
			tablePanel.add(new JScrollPane(commitsTable), gbc1);

			split.setFirstComponent(tablePanel);

			JPanel fileTreePanel = new JPanel();
			fileTreePanel.setLayout(new GridBagLayout());

			gbc1.gridy = 0;
			gbc1.weighty = 0.0;
			gbc1.weightx = 1.0;
			gbc1.fill = GridBagConstraints.HORIZONTAL;
			gbc1.anchor = GridBagConstraints.LINE_START;

			ActionManager manager = ActionManager.getInstance();
			ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.Bamboo.CommitListToolBar");
			ActionToolbar toolbar = manager.createActionToolbar(name, group, true);
			JComponent comp = toolbar.getComponent();
			fileTreePanel.add(comp, gbc1);

			gbc1.gridy = 1;
			JLabel label = new JLabel("Changed Files");
			fileTreePanel.add(label, gbc1);

			gbc1.gridy = 2;
			gbc1.weighty = 1.0;
			gbc1.fill = GridBagConstraints.BOTH;

			fileScroll = new JScrollPane();
			fileTreePanel.add(fileScroll, gbc1);

			split.setSecondComponent(fileTreePanel);

			add(split, gbc);
		}



		private JTable createCommitsTable(final List<Commit> commits) {
			TableModel model = new AbstractTableModel() {
				private String[] columnNames = {"Date", "Author", "Comment"};
				public String getColumnName(int col) {
					return columnNames[col];
				}
				public int getRowCount() { return commits.size(); }
				public int getColumnCount() { return columnNames.length; }
				public Object getValueAt(int row, int col) {
					Commit c = commits.get(row);
					switch (col) {
						case 0:
							return c.getCommitDate();
						case 1:
							return c.getAuthor();
						case 2:
							return c.getComment();
						default:
							return null;
					}
				}
				public boolean isCellEditable(int row, int col) { return false; }
				public void setValueAt(Object value, int row, int col) { }
			};

			final JTable table = new JTable(model);
			table.setShowVerticalLines(false);
			table.setShowHorizontalLines(false);
			table.setShowGrid(false);

			// please someone fix this to not suck :)
			table.getColumnModel().getColumn(0).setPreferredWidth(200);
			table.getColumnModel().getColumn(1).setPreferredWidth(100);
			table.getColumnModel().getColumn(2).setPreferredWidth(2000);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			table.getColumnModel().setColumnMargin(0);
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					Commit c = commits.get(table.getSelectedRow());
					if (c.getFiles().size() > 0) {
						fileTree = new FileTree(new FileTreeModel(c.getFiles()));
						fileScroll.setViewportView(fileTree);
						expand();
					} else {
						fileScroll.setViewportView(new JLabel("no commits", SwingConstants.CENTER));
					}
				}
			});
			return table;
		}

		public void showDiff() {
			// todo
		}

		public void showDiffWithLocal() {
			// todo
		}

		public void showRepositoryVersion() {
			// todo
		}

		public void expand() {
			if (fileTree == null) {
				return;
			}
			for (int row = 0; row < fileTree.getRowCount(); ++row) {
				fileTree.expandRow(row);
			}
		}

		public void collapse() {
			if (fileTree == null) {
				return;
			}
			for (int row = fileTree.getRowCount(); row >= 0; --row) {
				fileTree.collapseRow(row);
			}
		}
	}
}
