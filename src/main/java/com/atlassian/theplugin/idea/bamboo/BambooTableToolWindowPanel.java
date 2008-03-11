package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BambooTableToolWindowPanel extends JPanel implements BambooStatusListener {
    private JEditorPane editorPane;
    private ListTableModel listTableModel;
    private TableView table;
	private final BambooServerFacade bambooFacade;
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
        JScrollPane pane = setupPane(editorPane, wrapBody("Bamboo."));
        editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
        add(pane, BorderLayout.SOUTH);

        listTableModel = new ListTableModel(BambooTableColumnProvider.makeColumnInfo());
        listTableModel.setSortable(true);

		table = new AtlassianTableView(listTableModel);

		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < model.getColumnCount(); ++i) {
			//System.out.println("resizable = " + model.getColumn(i).getResizable());
			model.getColumn(i).setResizable(true);
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

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPopupMenu createContextMenu(BambooBuildAdapter buildAdapter) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(makeWebUrlMenu("View", buildAdapter.getBuildResultUrl()));
        contextMenu.addSeparator();
		contextMenu.add(makeAddLabelMenu("Add label", buildAdapter));
		contextMenu.add(makeAddCommentMenu("Add comment", buildAdapter));		
        contextMenu.addSeparator();
//		contextMenu.add(makeWebUrlMenu("Run build", buildAdapter.getServer()));
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
        addLabel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {				
				BuildLabelForm buildLabelForm = new BuildLabelForm(bambooFacade, build);
				buildLabelForm.show();
			}
        });
        return addLabel;
    }

    private JMenuItem makeAddCommentMenu(String menuName, final BambooBuildAdapter build) {
        JMenuItem addLabel = new JMenuItem();
        addLabel.setText(menuName);
        addLabel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				BuildCommentForm buildCommentForm = new BuildCommentForm(bambooFacade, build);
				buildCommentForm.show();
			}
        });
        return addLabel;
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

    public List<BambooBuildAdapter> getIssues() {
        return (List<BambooBuildAdapter>) listTableModel.getItems();
    }

    public BambooBuild getCurrentIssue() {
        return (BambooBuild) table.getSelectedObject();
    }

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		setBuilds(buildStatuses);
	}
}