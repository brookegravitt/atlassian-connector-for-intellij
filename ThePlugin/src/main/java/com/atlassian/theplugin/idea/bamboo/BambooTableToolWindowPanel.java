package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.bamboo.api.BambooException;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.bamboo.table.BambooColumnInfo;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.FutureTask;

public class BambooTableToolWindowPanel extends JPanel implements BambooStatusListener {
	private JEditorPane editorPane;
	private ListTableModel listTableModel;
	private AtlassianTableView table;
	private final transient BambooServerFacade bambooFacade;
	private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);

	public BambooTableToolWindowPanel(BambooServerFacade bambooFacade) {
		super(new BorderLayout());

		this.bambooFacade = bambooFacade;

		setBackground(UIUtil.getTreeTextBackground());

		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.BambooToolWindowToolBar");
		add(actionManager.createActionToolbar(
				"atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

		editorPane = new ToolWindowBambooContent();
		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
		JScrollPane pane = setupPane(editorPane, wrapBody("Waiting for Bamboo statuses."));
		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
		add(pane, BorderLayout.SOUTH);

		BambooColumnInfo[] columns = BambooTableColumnProvider.makeColumnInfo();
		TableCellRenderer[] renderers = BambooTableColumnProvider.makeRendererInfo();

		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		table = new AtlassianTableView(listTableModel);


		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < model.getColumnCount(); ++i) {
			model.getColumn(i).setResizable(true);
			model.getColumn(i).setPreferredWidth(columns[i].getPrefferedWidth());
			if (renderers[i] != null) {
				model.getColumn(i).setCellRenderer(renderers[i]);
			}
		}

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { // on double click, just open the issue
				if (e.getClickCount() == 2) {
					BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
					if (build != null) {
						BrowserUtil.launchBrowser(build.getBuildResultUrl());
					}
				}
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) { // on right click, show a context menu for this issue
				if (e.isPopupTrigger() && table.isEnabled()) {
					BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();

					if (build != null) {
						Point p = new Point(e.getX(), e.getY());
						JPopupMenu contextMenu = createContextMenu(build);
						contextMenu.show(table, p.x, p.y);
					}
				}
			}
		});

		JScrollPane tablePane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePane.setWheelScrollingEnabled(true);
		//table.setMinimumSize(ED_PANE_MINE_SIZE);
		add(tablePane, BorderLayout.CENTER);
	}

	private JPopupMenu createContextMenu(BambooBuildAdapter buildAdapter) {
		JPopupMenu contextMenu = new JPopupMenu();
		contextMenu.add(makeWebUrlMenu("View", buildAdapter.getBuildResultUrl()));
		contextMenu.addSeparator();
		contextMenu.add(makeAddLabelMenu("Add label", buildAdapter));
		contextMenu.add(makeAddCommentMenu("Add comment", buildAdapter));
		contextMenu.addSeparator();
		contextMenu.add(makeExecuteBuildMenu("Run build", buildAdapter));
		return contextMenu;
	}

	private JMenuItem makeWebUrlMenu(String menuName, final String url) {
		JMenuItem viewInBrowser = new JMenuItem();
		viewInBrowser.setText(menuName);
		viewInBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.launchBrowser(url);
			}
		});
		return viewInBrowser;
	}

	private JMenuItem makeAddLabelMenu(String menuName, final BambooBuildAdapter build) {
		JMenuItem addLabel = new JMenuItem();
		addLabel.setText(menuName);
		addLabel.setEnabled(build.getServer().isBamboo2());
		addLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openLabelDialog(build);
			}
		});
		return addLabel;
	}

	private void openLabelDialog(BambooBuildAdapter build) {
		BuildLabelForm buildLabelForm = new BuildLabelForm(build);
		buildLabelForm.show();
		if (buildLabelForm.getExitCode() == 0) {
			labelBuild(build, buildLabelForm.getLabel());
		}
	}

	private void labelBuild(final BambooBuildAdapter build, final String label) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Applying label on build...");
				try {
					bambooFacade.addLabelToBuild(build.getServer(),
							build.getBuildKey(), build.getBuildNumber(), label);
					setStatusMessage("Label applied on build");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Label not applied: Password on provided for server");
				} catch (BambooException e) {
					setStatusMessage("Label not applied: " + e.getMessage());
				}
			}
		}, null);
		new Thread(task).start();
	}

	public void addLabelToBuild() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		openLabelDialog(build);
	}

	private JMenuItem makeAddCommentMenu(String menuName, final BambooBuildAdapter build) {
		JMenuItem addComment = new JMenuItem();
		addComment.setText(menuName);
		addComment.setEnabled(build.getServer().isBamboo2());
		addComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openCommentDialog(build);
			}
		});
		return addComment;
	}

	private void openCommentDialog(BambooBuildAdapter build) {
		BuildCommentForm buildCommentForm = new BuildCommentForm(build);
		buildCommentForm.show();
		if (buildCommentForm.getExitCode() == 0) {
			commentBuild(build, buildCommentForm.getCommentText());
		}
	}

	private void commentBuild(final BambooBuildAdapter build, final String commentText) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Adding comment label on build...");
				try {
					bambooFacade.addCommentToBuild(build.getServer(),
							build.getBuildKey(), build.getBuildNumber(), commentText);
					setStatusMessage("Comment added to build");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Comment not added: Password on provided for server");
				} catch (BambooException e) {
					setStatusMessage("Comment not added: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task).start();
	}

	public void addCommentToBuild() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		openCommentDialog(build);
	}

	private JMenuItem makeExecuteBuildMenu(String menuName, final BambooBuildAdapter build) {
		JMenuItem executeBuild = new JMenuItem();
		executeBuild.setText(menuName);
		executeBuild.setEnabled(!build.getServer().isBamboo2());

		executeBuild.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				executeBuild(build);
			}
		});
		return executeBuild;
	}

	private void executeBuild(final BambooBuildAdapter build) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Executing build on plan " + build.getBuildKey());
				try {
					bambooFacade.executeBuild(build.getServer(), build.getBuildKey());
					setStatusMessage("Build executed on plan: " + build.getBuildKey());
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Build not executed: Password on provided for server");
				} catch (BambooException e) {
					setStatusMessage("Build not executed: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task).start();
	}

	public void runBuild() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		executeBuild(build);
	}

	private JScrollPane setupPane(JEditorPane pane, String initialText) {
		pane.setText(initialText);
		JScrollPane scrollPane = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		return scrollPane;
	}

	public void setBuilds(Collection<BambooBuild> builds) {
		List<BambooBuildAdapter> buildAdapters = new ArrayList<BambooBuildAdapter>();
		for (BambooBuild build : builds) {
			buildAdapters.add(new BambooBuildAdapter(build));
		}
		listTableModel.setItems(buildAdapters);
		listTableModel.fireTableDataChanged();
		table.setEnabled(true);
		table.setForeground(UIUtil.getActiveTextColor());
		editorPane.setText(wrapBody("Loaded <b>" + builds.size() + "</b> builds."));
	}

	private String wrapBody(String s) {
		return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	private void setStatusMessage(String msg) {
		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}

	public List<BambooBuildAdapter> getBuilds() {
		return (List<BambooBuildAdapter>) listTableModel.getItems();
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		setBuilds(buildStatuses);
	}

	public boolean getExecuteBuildEnabled() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		if (build != null) {
			return !build.getServer().isBamboo2();
		} else {
			return false;
		}
	}

	private boolean getBamboo2ActionsEnabled() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		if (build != null) {
			return build.getServer().isBamboo2();
		} else {
			return false;
		}
	}

	public boolean getLabelBuildEnabled() {
		return getBamboo2ActionsEnabled();
	}

	public boolean getCommentBuildEnabled() {
		return getBamboo2ActionsEnabled();		
	}
}