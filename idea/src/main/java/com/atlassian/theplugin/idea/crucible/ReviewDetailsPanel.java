package com.atlassian.theplugin.idea.crucible;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.table.TableView;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ui.AtlassianTableView;

import javax.swing.*;
import java.awt.*;

import thirdparty.javaworld.ClasspathHTMLEditorKit;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 12, 2008
 * Time: 1:09:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewDetailsPanel extends JPanel {
	private ReviewDataInfoAdapter reviewData = null;
	public static final String PANEL_NAME = "Details";
	private JPanel toolBarPanel;
	private ListTableModel listTableModel;

	public ReviewDetailsPanel() {
		super(new BorderLayout());

        setBackground(UIUtil.getTreeTextBackground());

        toolBarPanel = new JPanel(new BorderLayout());
//        ActionManager actionManager = ActionManager.getInstance();
//        ActionGroup toolbar = (ActionGroup) actionManager.getAction(getToolbarActionGroup());
//        ActionToolbar actionToolbar = actionManager.createActionToolbar(
//                "atlassian.toolwindow.serverToolBar", toolbar, true);
//        toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
        add(toolBarPanel, BorderLayout.NORTH);

//		editorPane = new ToolWindowBambooContent();
//		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
//		JScrollPane pane = setupPane(editorPane, wrapBody(getInitialMessage()));
//		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
//		add(pane, BorderLayout.SOUTH);
//
		TableColumnInfo[] columns = new TableColumnInfo[0];

		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		TableView table = new TableView(listTableModel);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().setColumnMargin(0);

		table.setMinRowHeight(20);
		table.setAutoResizeMode(TableView.AUTO_RESIZE_OFF);


		JScrollPane tablePane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePane.setWheelScrollingEnabled(true);
		add(tablePane, BorderLayout.CENTER);
//
//		progressAnimation.configure(this, tablePane, BorderLayout.CENTER);
	}

	private String getToolbarActionGroup() {
		return "ThePlugin.ReviewDetailsToolbar";
	}

	public void setReviewData(ReviewDataInfoAdapter reviewData) {
		this.reviewData = reviewData;
	}
}
