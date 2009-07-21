package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.List;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 12:36:28 PM
 */
public class CommitDetailsPanel extends JPanel implements DataProvider, ActionListener {

	private static final float SPLIT_RATIO = 0.6f;
	protected static final int ROW_HEIGHT = 16;

	private AtlassianTreeWithToolbar fileTree = new AtlassianTreeWithToolbar(TOOLBAR_NAME, (TreeUISetup) null, null);
	private final Project project;
	private final BambooBuildAdapterIdea build;

	private static final String TOOLBAR_NAME = "ThePlugin.Bamboo.CommitListToolBar";

	public CommitDetailsPanel(Project project, final BambooBuildAdapterIdea build) {
		setLayout(new GridBagLayout());

		this.project = project;
		this.build = build;
	}

	public void actionPerformed(ActionEvent e) {
		// ignore
	}

	public void showError(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				add(new JLabel("Failed to retrieve changed files: " + e.getMessage()));
			}
		});
	}

	private class ChangeSetListModel extends DefaultListModel {
		public ChangeSetListModel(List<BambooChangeSet> commits) {
			int i = 0;
			for (BambooChangeSet cs : commits) {
				add(i++, cs);
			}
		}
	}

	private static final class ChangeSetRendererPanel extends JPanel {

		private SelectableLabel comment = new SelectableLabel(true, true, "NOTHING YET", ROW_HEIGHT, false, false);
		private SelectableLabel author = new SelectableLabel(true, true, "NOTHING HERE ALSO", ROW_HEIGHT, true, false);
		private SelectableLabel date = new SelectableLabel(true, true, "NEITHER HERE", ROW_HEIGHT, false, false);

		private ChangeSetRendererPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(comment, gbc);
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			author.setHorizontalAlignment(SwingConstants.RIGHT);
			add(author, gbc);
			gbc.gridx++;
			add(date, gbc);
		}

		void setChangeSet(BambooChangeSet cs) {
			comment.setText(" " + cs.getComment());
			author.setText(cs.getAuthor());
			DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String commitDate = dfo.format(cs.getCommitDate());
			date.setText(", " + commitDate + " ");
		}

		void setSelected(boolean selected) {
			comment.setSelected(selected);
			author.setSelected(selected);
			date.setSelected(selected);
		}
	}

	private static final ChangeSetRendererPanel CHANGEST_RENDERER_PANEL = new ChangeSetRendererPanel();

	public void fillContent(List<BambooChangeSet> commits) {
		if (commits == null || commits.size() == 0) {
			add(new JLabel("No changes in " + build.getPlanKey() + "-" + build.getBuildNumberAsString()));
			return;
		}

		Splitter split = new Splitter(false, SPLIT_RATIO);
		split.setShowDividerControls(true);


		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());

		final JList changesList = new JList() {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		changesList.setModel(new ChangeSetListModel(commits));
		changesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		changesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				fillFileTree((BambooChangeSet) changesList.getSelectedValue());
			}
		});
		changesList.setCellRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				CHANGEST_RENDERER_PANEL.setChangeSet((BambooChangeSet) value);
				CHANGEST_RENDERER_PANEL.setSelected(isSelected);
				CHANGEST_RENDERER_PANEL.validate();
				return CHANGEST_RENDERER_PANEL;
			}
		});
		final JScrollPane scroll = new JScrollPane(changesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listPanel.add(scroll, BorderLayout.CENTER);

		split.setFirstComponent(listPanel);

		JPanel fileTreePanel = new JPanel();
		fileTreePanel.setLayout(new BorderLayout());
		fileTreePanel.add(new JLabel("Changed Files"), BorderLayout.NORTH);

		fileTree.setRootVisible(false);
		fileTree.getTreeComponent().addMouseListener(new NavigateToCodeHandler(build.getPlanKey()));
		fileTreePanel.add(fileTree, BorderLayout.CENTER);

		split.setSecondComponent(fileTreePanel);
		split.setShowDividerControls(false);

		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);

		if (commits.size() == 1) {
			changesList.getSelectionModel().setSelectionInterval(0, 0);
		}
		validate();
	}

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.FILE_TREE)) {
			return fileTree;
		} else if (dataId.equals(Constants.BUILD_CHANGES_WINDOW)) {
			return this;
		}
		return null;
	}

	private void fillFileTree(final BambooChangeSet changeSet) {
		if (changeSet == null) {
			fileTree.setModelProvider(ModelProvider.EMPTY_MODEL_PROVIDER);
		} else {
			fileTree.setModelProvider(new ModelProvider() {
				private AtlassianTreeModel diredModel =
						FileTreeModelBuilder.buildTreeModelFromBambooChangeSet(project, changeSet);
				private AtlassianTreeModel flatModel =
						FileTreeModelBuilder.buildFlatTreeModelFromBambooChangeSet(project, changeSet);

				@Override
				public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state) {
					switch (state) {
						case DIRED:
							return diredModel;
						case FLAT:
							return flatModel;
						default:
							throw new IllegalStateException("Unknown model requested");
					}
				}
			});
		}
		fileTree.setRootVisible(false);
		fileTree.expandAll();
	}

	private final class NavigateToCodeHandler extends MouseAdapter {

		private String place;

		private NavigateToCodeHandler(String place) {
			this.place = place;
		}

		@Nullable
		private BambooFileNode getBambooFileNode(MouseEvent e) {
			final JTree theTree = (JTree) e.getComponent();

			TreePath path = theTree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return null;
			}
			Object o = path.getLastPathComponent();
			if (o instanceof BambooFileNode) {
				return (BambooFileNode) o;
			}
			return null;

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() != 2) {
				return;
			}

			final JTree theTree = (JTree) e.getComponent();

			TreePath path = theTree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return;
			}
			Object o = path.getLastPathComponent();
			if (o instanceof BambooFileNode) {
				BambooFileNode bfn = (BambooFileNode) o;
				PsiFile psiFile = bfn.getPsiFile();
				if (psiFile != null && psiFile.canNavigateToSource()) {
					psiFile.navigate(true);
				}
			}
		}


		@Override
		public void mousePressed(MouseEvent e) {
			processPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			processPopup(e);
		}

		public void processPopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return;
			}

			final JTree theTree = (JTree) e.getComponent();

			TreePath path = theTree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return;
			}
			theTree.setSelectionPath(path);


			final BambooFileNode bfn = getBambooFileNode(e);
			if (bfn == null) {
				return;
			}


			ActionManager aManager = ActionManager.getInstance();
			ActionGroup menu = (ActionGroup) aManager.getAction(TOOLBAR_NAME);
			if (menu == null) {
				return;
			}
			aManager.createActionPopupMenu(place, menu).getComponent().show(fileTree, e.getX(), e.getY());
		}
	}
}
