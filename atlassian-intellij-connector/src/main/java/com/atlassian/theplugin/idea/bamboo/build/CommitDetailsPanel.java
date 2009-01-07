package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ModelProvider;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 12:36:28 PM
 */
public class CommitDetailsPanel extends JPanel implements DataProvider {

	private static final float SPLIT_RATIO = 0.6f;
	private AtlassianTreeWithToolbar fileTree = new AtlassianTreeWithToolbar(TOOLBAR_NAME);
	private final Project project;
	private final BambooBuildAdapterIdea build;

	private static final String TOOLBAR_NAME = "ThePlugin.Bamboo.CommitListToolBar";

	public CommitDetailsPanel(Project project, final BambooBuildAdapterIdea build) {
		setLayout(new GridBagLayout());

		this.project = project;
		this.build = build;

		Task.Backgroundable changesTask = new Task.Backgroundable(project, "Retrieving Build Stack Trace", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
				try {
					BambooServerFacade bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
					BuildDetails details = bambooFacade.getBuildDetails(
							build.getServer(), build.getBuildKey(), build.getBuildNumber());
					final List<BambooChangeSet> commits = details.getCommitInfo();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							fillContent(commits);
						}
					});
				} catch (ServerPasswordNotProvidedException e) {
					showError(e);
				} catch (RemoteApiException e) {
					showError(e);
				}
			}
		};
		ProgressManager.getInstance().run(changesTask);
	}

	private void showError(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				add(new JLabel("Failed to retrieve build changes: " + e.getMessage()));
			}
		});
	}

	private void fillContent(List<BambooChangeSet> commits) {
		if (commits == null || commits.size() == 0) {
			add(new JLabel("No build in " + build.getBuildKey()));
			return;
		}

		Splitter split = new Splitter(false, SPLIT_RATIO);
		split.setShowDividerControls(true);


		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		final AtlassianTableView<BambooChangeSet> commitsTable = createCommitsTable(commits);
		tablePanel.add(new JScrollPane(commitsTable), BorderLayout.CENTER);

		split.setFirstComponent(tablePanel);

		JPanel fileTreePanel = new JPanel();
		fileTreePanel.setLayout(new BorderLayout());

		JLabel label = new JLabel("Changed Files");
		fileTreePanel.add(label, BorderLayout.NORTH);

		fileTree.setRootVisible(false);
		fileTree.getTreeComponent().addMouseListener(new NavigateToCodeHandler(build.getBuildKey()));
		fileTreePanel.add(fileTree, BorderLayout.CENTER);

		split.setSecondComponent(fileTreePanel);

		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);

		if (commits.size() == 1) {
			commitsTable.getSelectionModel().setSelectionInterval(0, 0);
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


	private static class AuthorColumn extends TableColumnInfo<BambooChangeSet, String> {
		private static final int PREFERRED_WIDTH = 100;

		@Override
		public String getColumnName() {
			return "Author";
		}

		@Override
		public Class getColumnClass() {
			return String.class;
		}

		@Override
		public int getPrefferedWidth() {
			return PREFERRED_WIDTH;
		}

		@Override
		public String valueOf(final BambooChangeSet obj) {
			return obj.getAuthor();
		}

		@Override
		public Comparator<BambooChangeSet> getComparator() {
			return new Comparator<BambooChangeSet>() {
				public int compare(BambooChangeSet o, BambooChangeSet o1) {
					return o.getAuthor().compareTo(o1.getAuthor());
				}
			};
		}

	}

	private static class DateColumn extends TableColumnInfo<BambooChangeSet, Date> {
		private static final int PREFERRED_WIDTH = 100;

		@Override
		public String getColumnName() {
			return "Date";
		}

		@Override
		public Class getColumnClass() {
			return Date.class;
		}

		@Override
		public int getPrefferedWidth() {
			return PREFERRED_WIDTH;
		}

		@Override
		public Date valueOf(BambooChangeSet o) {
			return o.getCommitDate();
		}

		@Override
		public Comparator<BambooChangeSet> getComparator() {
			return new Comparator<BambooChangeSet>() {
				public int compare(BambooChangeSet o, BambooChangeSet o1) {
					return o1.getCommitDate().compareTo(o.getCommitDate());
				}
			};
		}
	}

	private static class CommentColumn extends TableColumnInfo<BambooChangeSet, String> {
		private static final int PREFERRED_WIDTH = 600;

		@Override
		public String getColumnName() {
			return "Comment";
		}

		@Override
		public Class getColumnClass() {
			return String.class;
		}

		@Override
		public int getPrefferedWidth() {
			return PREFERRED_WIDTH;
		}

		@Override
		public String valueOf(BambooChangeSet o) {
			return o.getComment();
		}

		@Override
		public Comparator<BambooChangeSet> getComparator() {
			return new Comparator<BambooChangeSet>() {
				public int compare(BambooChangeSet o, BambooChangeSet o1) {
					return o.getComment().compareTo(o1.getComment());
				}
			};
		}
	}

	private AtlassianTableView<BambooChangeSet> createCommitsTable(final List<BambooChangeSet> commits) {
		TableColumnProvider prov = new TableColumnProvider() {
			public TableColumnInfo[] makeColumnInfo() {
				return new TableColumnInfo[]{ new AuthorColumn(), new DateColumn(), new CommentColumn() };
			}

			public TableCellRenderer[] makeRendererInfo() {
				return new TableCellRenderer[]{ null, null, null };
			}
		};
		final AtlassianTableView<BambooChangeSet> atv = new AtlassianTableView<BambooChangeSet>(prov,
				new ListTableModel<BambooChangeSet>(prov.makeColumnInfo(), commits, 0), null);
		atv.addItemSelectedListener(new TableItemSelectedListener<BambooChangeSet>() {
			public void itemSelected(AtlassianTableView<BambooChangeSet> table) {
				final BambooChangeSet c = table.getSelectedObject();
				if (c == null) {
					fileTree.setModelProvider(ModelProvider.EMPTY_MODEL_PROVIDER);
				} else {
					fileTree.setModelProvider(new ModelProvider() {
						private AtlassianTreeModel diredModel =
								FileTreeModelBuilder.buildTreeModelFromBambooChangeSet(project, c);
						private AtlassianTreeModel flatModel =
								FileTreeModelBuilder.buildFlatTreeModelFromBambooChangeSet(project, c);

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
		});
		return atv;
	}

	private static final class NavigateToCodeHandler extends MouseAdapter {

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
			aManager.createActionPopupMenu(place, menu).getComponent().show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
