/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.atlassian.theplugin.idea.bamboo.*;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.intellij.openapi.actionSystem.*;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class AbstractTableToolWindowPanel extends JPanel {
	private transient ActionToolbar filterEditToolbar;
	protected JPanel toolBarPanel;
    protected JEditorPane editorPane;
    protected JScrollPane tablePane;
    protected ListTableModel listTableModel;
	protected AtlassianTableView table;
	protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
    protected ProjectConfigurationBean projectConfiguration;

    public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	public AbstractTableToolWindowPanel(ProjectConfigurationBean projectConfigurationBean) {
		super(new BorderLayout());

        this.projectConfiguration = projectConfigurationBean;

        setBackground(UIUtil.getTreeTextBackground());

        toolBarPanel = new JPanel(new BorderLayout());
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction(getToolbarActionGroup());
        ActionToolbar actionToolbar = actionManager.createActionToolbar(
                "atlassian.toolwindow.serverToolBar", toolbar, true);
        toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
        add(toolBarPanel, BorderLayout.NORTH);

		editorPane = new ToolWindowBambooContent();
		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
		JScrollPane pane = setupPane(editorPane, wrapBody(getInitialMessage()));
		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
		add(pane, BorderLayout.SOUTH);

		listTableModel = new ListTableModel(getTableColumnProvider().makeColumnInfo());
		listTableModel.setSortable(true);
		table = new AtlassianTableView(listTableModel, getTableConfiguration());
		table.prepareColumns(getTableColumnProvider());

		table.addMouseListener(new PopuMenuMouseAdapter());

		tablePane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePane.setWheelScrollingEnabled(true);
		add(tablePane, BorderLayout.CENTER);

		progressAnimation.configure(this, tablePane, BorderLayout.CENTER);
	}

	protected void addCustomSubmenus(DefaultActionGroup actionGroup, final ActionPopupMenu popup) {
	}

	protected abstract void handlePopupClick(Object selectedObject);
	protected abstract void handleDoubleClick(Object selectedObject);
	protected abstract String getInitialMessage();
    protected abstract String getToolbarActionGroup();
    protected abstract String getPopupActionGroup();
    protected abstract TableColumnProvider getTableColumnProvider();
    protected abstract ProjectToolWindowTableConfiguration getTableConfiguration();

    protected void setScrollPaneViewport(JComponent component) {
        tablePane.setViewportView(component);
    }

    protected JScrollPane setupPane(JEditorPane pane, String initialText) {
		pane.setText(initialText);
		JScrollPane scrollPane = new JScrollPane(pane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		return scrollPane;
	}

	protected String wrapBody(String s) {
		return "<html>" + StausIconBambooListener.BODY_WITH_STYLE + s + "</body></html>";

	}

	public void setStatusMessage(String msg) {
		setStatusMessage(msg, false);
	}

	public void setStatusMessage(String msg, boolean isError) {
		editorPane.setBackground(isError ? Color.RED : Color.WHITE);
		editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
	}

	public AtlassianTableView getTable() {
		return table;
	}

	protected void filterEditToolbarSetVisible(boolean visible) {
        filterEditToolbar.getComponent().setVisible(visible);
    }
	public ActionToolbar transientGetFilterEditToolbar() {
		return filterEditToolbar;
	}

	public void transientSetFilterEditToolbar(ActionToolbar newFilterEditToolbar) {
		this.filterEditToolbar = newFilterEditToolbar;
	}

	protected   void createFilterEditToolBar(String place, String toolbarName) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup filterEditToolBar = (ActionGroup) actionManager.getAction(toolbarName);
        filterEditToolbar = actionManager.createActionToolbar(place,
                filterEditToolBar, true);
        toolBarPanel.add(filterEditToolbar.getComponent(), BorderLayout.SOUTH);
        filterEditToolbarSetVisible(false);
    }

	public abstract void applyAdvancedFilter();
	public abstract void cancelAdvancedFilter();
	public abstract void clearAdvancedFilter();

	private class PopuMenuMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				handleDoubleClick(table.getSelectedObject());
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && table.isEnabled()) {
				final DefaultActionGroup actionGroup = new DefaultActionGroup();

				final ActionGroup configActionGroup = (ActionGroup) ActionManager
						.getInstance().getAction(getPopupActionGroup());
				actionGroup.addAll(configActionGroup);

				final ActionPopupMenu popup =
						ActionManager.getInstance().createActionPopupMenu("Context menu", actionGroup);

				addCustomSubmenus(actionGroup, popup);

				final JPopupMenu jPopupMenu = popup.getComponent();
				jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				handlePopupClick(table.getSelectedObject());
			}
		}
	}
}