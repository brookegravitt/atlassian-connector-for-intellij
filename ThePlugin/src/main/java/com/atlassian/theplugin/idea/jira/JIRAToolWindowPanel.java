package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.action.jira.MyIssuesAction;
import com.atlassian.theplugin.idea.action.jira.UnresolvedIssuesAction;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.jira.table.JIRATableColumnProvider;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFactory;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class JIRAToolWindowPanel extends JPanel {
    private JEditorPane editorPane;
    private JPanel toolBarPanel;
    private ListTableModel listTableModel;
    // a simple map to store all selected query fragments.
    private Map<String, JIRAQueryFragment> queryFragments = new HashMap<String, JIRAQueryFragment>();
    private AtlassianTableView table;
    private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private ProjectConfigurationBean projectConfigurationBean;

	public JIRAToolWindowPanel(ProjectConfigurationBean projectConfigurationBean) {
        super(new BorderLayout());

		this.projectConfigurationBean = projectConfigurationBean;

		setBackground(UIUtil.getTreeTextBackground());

        toolBarPanel = new JPanel(new BorderLayout());
        ActionManager aManager = ActionManager.getInstance();
        ActionGroup serverToolBar = (ActionGroup) aManager.getAction("ThePlugin.JIRA.ServerToolBar");
        ActionToolbar actionToolbar = aManager.createActionToolbar(
				"atlassian.toolwindow.serverToolBar", serverToolBar, true);
        toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

        add(toolBarPanel, BorderLayout.NORTH);

        // setup initial query fragments
        queryFragments.put(MyIssuesAction.QF_NAME, new MyIssuesAction());
        queryFragments.put(UnresolvedIssuesAction.QF_NAME, new UnresolvedIssuesAction());

        editorPane = new ToolWindowBambooContent();
        editorPane.setEditorKit(new ClasspathHTMLEditorKit());
        JScrollPane pane = setupPane(editorPane, wrapBody("Select a JIRA server to retrieve your issues."));
        editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
        add(pane, BorderLayout.SOUTH);

		TableColumnInfo[] columns = JIRATableColumnProvider.makeColumnInfo();
		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		table = new AtlassianTableView(listTableModel, projectConfigurationBean);
		table.prepareColumns(columns, JIRATableColumnProvider.makeRendererInfo());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { // on double click, just open the issue
                if (e.getClickCount() == 2) {
                    JIRAIssue issue = (JIRAIssue) table.getSelectedObject();
                    if (issue != null) {
                        BrowserUtil.launchBrowser(issue.getIssueUrl());
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
                    JIRAIssue issue = (JIRAIssue) table.getSelectedObject();

                    if (issue != null) {
                        Point p = new Point(e.getX(), e.getY());
                        JPopupMenu contextMenu = createContextMenu(issue);
                        contextMenu.show(table, p.x, p.y);
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPopupMenu createContextMenu(JIRAIssue issue) {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(makeWebUrlMenu("View", issue.getIssueUrl()));
        contextMenu.add(makeWebUrlMenu("Edit",
				issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey()));
        contextMenu.addSeparator();
        contextMenu.add(new JMenuItem(new CommentIssueAction()));
        contextMenu.add(makeWebUrlMenu("Log Work", issue.getServerUrl()
                + "/secure/CreateWorklog!default.jspa?key=" + issue.getKey()));
        contextMenu.add(makeWebUrlMenu("Commit Changes Against Issue", issue.getServerUrl()
                + "/secure/EditIssue!default.jspa?key=" + issue.getKey()));
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

    private JScrollPane setupPane(JEditorPane pane, String initialText) {
        pane.setText(initialText);
        JScrollPane scrollPane = new JScrollPane(pane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }

    public void refreshIssues() {
        if (IdeaHelper.getAppComponent().getCurrentJIRAServer() != null) {
            updateIssues(IdeaHelper.getAppComponent().getCurrentJIRAServer());
        }
    }

    public void setIssues(List<JIRAIssue> issues) {
        listTableModel.setItems(issues);
        listTableModel.fireTableDataChanged();
        table.setEnabled(true);
        table.setForeground(UIUtil.getActiveTextColor());
        editorPane.setText(wrapBody("Loaded <b>" + issues.size() + "</b> issues."));
    }

    private String wrapBody(String s) {
        return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

    }

    public void selectServer(Server server) {
        if (server != null) {
            final JIRAServer jiraServer = new JIRAServer(server);
            IdeaHelper.getAppComponent().setCurrentJIRAServer(jiraServer);

            FutureTask task = new FutureTask(new Runnable() {
                public void run() {
                    setStatusMessage("Retrieving statuses...");
                    List statuses = jiraServer.getStatuses(); // ensure statuses are cached
                    if (statuses == null) {
                        setStatusMessage("Unable to connect to server.");
                        return;
                    }
                    String msg = "Found <b>" + statuses.size() + "</b> statuses.<br>";
                    setStatusMessage(msg + "Retrieving issue types...");
                    List types = jiraServer.getIssueTypes(); // ensure types are cached
                    msg += "Found <b>" + types.size() + "</b> issue types.<br>";
                    setStatusMessage(msg + "Retrieving projects...");
                    jiraServer.getProjects(); // ensure projects are cached

                    showFilterToolBar();
                    updateIssues(jiraServer);
                }
            }, null);

            new Thread(task).start();
        }
    }

    private void setStatusMessage(String msg) {
        editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
    }

    private void showFilterToolBar() {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup filterToolBarTop = (ActionGroup) actionManager.getAction("ThePlugin.JIRA.FilterToolBarTop");
        ActionGroup filterToolBarBottom = (ActionGroup) actionManager.getAction("ThePlugin.JIRA.FilterToolBarBottom");
        ActionToolbar fTop = actionManager.createActionToolbar("atlassian.toolwindow.filterToolBarTop",
                filterToolBarTop, true);
        toolBarPanel.add(fTop.getComponent(), BorderLayout.CENTER);
        ActionToolbar fBot = actionManager.createActionToolbar("atlassian.toolwindow.filterToolBarBottom",
                filterToolBarBottom, true);
        toolBarPanel.add(fBot.getComponent(), BorderLayout.SOUTH);
    }


    private void updateIssues(final JIRAServer jiraServer) {
        table.setEnabled(false);
        table.setForeground(UIUtil.getInactiveTextColor());

        final Server server = jiraServer.getServer();
        editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">Retrieving your issues from <b>"
                + server.getName() + "</b>...</td></tr></table>"));
        editorPane.setCaretPosition(0);

        FutureTask task = new FutureTask(new Runnable() {
            public void run() {
                JIRAServerFacade serverFacade = JIRAServerFactory.getJIRAServerFacade();

                try {
                    List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
                    query.addAll(queryFragments.values());
                    System.out.println("query = " + query);
                    List result = serverFacade.getIssues(jiraServer.getServer(), query);
                    setIssues(result);
                } catch (JIRAException e) {
                    editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">Error contacting server <b>"
                            + server.getName() + "</b>?</td></tr></table>"));
                }
            }
        }, null);

        new Thread(task).start();
    }

    public void addQueryFragment(String fragmentName, JIRAQueryFragment fragment) {
        if (fragment == null) {
            queryFragments.remove(fragmentName);
        } else {
            queryFragments.put(fragmentName, fragment);
        }
    }

    public List<JIRAIssue> getIssues() {
        return (List<JIRAIssue>) listTableModel.getItems();
    }

    public JIRAIssue getCurrentIssue() {
        return (JIRAIssue) table.getSelectedObject();
    }

    public class CommentIssueAction extends AbstractAction {
        public CommentIssueAction() {
            putValue(Action.NAME, "Add Comment");
        }

        public void actionPerformed(ActionEvent e) {
            JIRAIssue issue = (JIRAIssue) table.getSelectedObject();
            IssueComment issueComment = new IssueComment(
					IdeaHelper.getAppComponent().getCurrentJIRAServer(), getIssues());
            issueComment.setIssue(issue);
            issueComment.show();
        }
    }

}