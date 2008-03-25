package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.CrucibleStatusListener;
import com.atlassian.theplugin.crucible.ReviewDataInfo;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrucibleTableToolWindowPanel extends JPanel implements CrucibleStatusListener {
	private JEditorPane editorPane;
	private ListTableModel listTableModel;
	private AtlassianTableView table;
	private final transient CrucibleServerFacade crucibleFacade;
	private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	private static final DateFormat TIME_DF = new SimpleDateFormat("hh:mm a");

	public CrucibleTableToolWindowPanel(CrucibleServerFacade crucibleFacade,
									  ProjectConfigurationBean projectConfigurationBean) {
		super(new BorderLayout());

		this.crucibleFacade = crucibleFacade;

		setBackground(UIUtil.getTreeTextBackground());

		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.CrucibleToolWindowToolBar");
		add(actionManager.createActionToolbar(
				"atlassian.toolwindow.toolbar", toolbar, true).getComponent(), BorderLayout.NORTH);

		editorPane = new ToolWindowCrucibleContent();
		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
		JScrollPane pane = setupPane(editorPane, wrapBody("Waiting for Crucible review info."));
		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
		add(pane, BorderLayout.SOUTH);

		TableColumnInfo[] columns = CrucibleTableColumnProvider.makeColumnInfo();

		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		table = new AtlassianTableView(listTableModel,
				projectConfigurationBean.getCrucibleConfiguration().getTableConfiguration());
		table.prepareColumns(columns, CrucibleTableColumnProvider.makeRendererInfo());

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { // on double click, just open the issue
				if (e.getClickCount() == 2) {
					CrucibleReviewAdapter review = (CrucibleReviewAdapter) table.getSelectedObject();
					if (review != null) {
						BrowserUtil.launchBrowser(review.getPermaId().getId());
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
		//contextMenu.addSeparator();
		//contextMenu.add(makeAddLabelMenu("Add label", buildAdapter));
		//contextMenu.add(makeAddCommentMenu("Add comment", buildAdapter));
		//contextMenu.addSeparator();
		//contextMenu.add(makeExecuteBuildMenu("Run build", buildAdapter));
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
	
	public void updateReviews(Collection<ReviewDataInfo> reviews) {
		List<CrucibleReviewAdapter> reviewAdapters = new ArrayList<CrucibleReviewAdapter>();
		for (ReviewDataInfo review : reviews) {
			reviewAdapters.add(new CrucibleReviewAdapter(review));
		}
		listTableModel.setItems(reviewAdapters);
		listTableModel.fireTableDataChanged();
		table.setEnabled(true);
		table.setForeground(UIUtil.getActiveTextColor());
		StringBuffer sb = new StringBuffer();
		sb.append("Loaded <b>");
		sb.append(reviews.size());
		sb.append("</b> reviews.");
		editorPane.setText(wrapBody(sb.toString()));
	}

	private String wrapBody(String s) {
		return "<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + s + "</body></html>";

	}

//	private void setStatusMessage(String msg) {
//		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
//	}

	public List<BambooBuildAdapter> getBuilds() {
		return (List<BambooBuildAdapter>) listTableModel.getItems();
	}

	public boolean getExecuteBuildEnabled() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		return build != null && build.getEnabled();
	}

	private boolean getBamboo2ActionsEnabled() {
		BambooBuildAdapter build = (BambooBuildAdapter) table.getSelectedObject();
		if (build != null) {
			return build.isBamboo2() && build.getEnabled();
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

	public AtlassianTableView getTable() {
		return table;
	}
}