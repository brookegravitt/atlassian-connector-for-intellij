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

package com.atlassian.theplugin.idea.crucible;


import com.atlassian.theplugin.commons.bamboo.StausIconBambooListener;
import com.atlassian.theplugin.commons.crucible.CrucibleFiltersBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.bamboo.ToolWindowBambooContent;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.events.ShowReviewEvent;
import com.atlassian.theplugin.idea.ui.CollapsibleTable;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Key;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CrucibleTableToolWindowPanel extends JPanel implements CrucibleStatusListener, TableItemSelectedListener {

    private static final Key<CrucibleTableToolWindowPanel> WINDOW_PROJECT_KEY
            = Key.create(CrucibleTableToolWindowPanel.class.getName());
    private Project project;
    private transient ActionToolbar filterEditToolbar;
    private static CrucibleTableToolWindowPanel instance;
    private TableColumnProvider columnProvider;

    private CrucibleCustomFilterPanel crucibleCustomFilterPanel;

    private ProjectConfigurationBean projectCfg;
    private JPanel toolBarPanel;
    private JPanel dataPanelsHolder;
    private ToolWindowBambooContent editorPane;
    private CrucibleReviewActionListener listener = new CrucibleReviewActionListener();

    public CrucibleFiltersBean getFilters() {
        return filters;
    }

    private transient CrucibleFiltersBean filters;

    protected JScrollPane tablePane;
    protected ListTableModel listTableModel;
    protected static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
    protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

    protected TableColumnProvider tableColumnProvider = new CrucibleTableColumnProviderImpl();

    private ReviewData selectedItem;

    private Map<PredefinedFilter, CollapsibleTable> tables = new HashMap<PredefinedFilter, CollapsibleTable>();
    private Map<String, CollapsibleTable> customTables = new HashMap<String, CollapsibleTable>();
    private CollapsibleTable crucible15Table = null;

    private static CrucibleServerFacade serverFacade;
    private CrucibleVersion crucibleVersion = CrucibleVersion.UNKNOWN;
    private static final String TO_REVIEW_AS_ACTIVE_REVIEWER = "To review as active reviewer";

    protected String getInitialMessage() {

        return "Waiting for Crucible review info.";
    }

    protected String getToolbarActionGroup() {
        return "ThePlugin.CrucibleToolWindowToolBar";
    }

    protected String getPopupActionGroup() {
        return "ThePlugin.Crucible.ReviewPopupMenu";
    }

    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new CrucibleTableColumnProviderImpl();
        }
        return columnProvider;
    }

    public void applyAdvancedFilter() {
        if (crucibleCustomFilterPanel.getFilter() != null) {
            CustomFilterBean filter = crucibleCustomFilterPanel.getFilter();

            filters.getManualFilter().put(filter.getTitle(), filter);
            projectCfg.
                    getCrucibleConfiguration().getCrucibleFilters().getManualFilter().put(filter.getTitle(), filter);
            CrucibleStatusChecker checker = null;
            ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent();
            if (projectComponent != null) {
                checker = projectComponent.getCrucibleStatusChecker();
            }
            refreshReviews(checker);
        }
        hideCrucibleCustomFilter();
    }

    public void cancelAdvancedFilter() {
        hideCrucibleCustomFilter();
    }


    public void clearAdvancedFilter() {
    }

    public static CrucibleTableToolWindowPanel getInstance(com.intellij.openapi.project.Project project,
                                                           ProjectConfigurationBean projectConfigurationBean) {

        CrucibleTableToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

        if (window == null) {
            window = new CrucibleTableToolWindowPanel(project, projectConfigurationBean);
            project.putUserData(WINDOW_PROJECT_KEY, window);
            serverFacade = CrucibleServerFacadeImpl.getInstance();
        }
        return window;
    }

    public CrucibleTableToolWindowPanel(Project project, ProjectConfigurationBean projectConfigurationBean) {
        super(new BorderLayout());
        this.project = project;
        this.projectCfg = projectConfigurationBean;

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

        dataPanelsHolder = new JPanel();
        dataPanelsHolder.setLayout(new VerticalFlowLayout());

        tablePane = new JScrollPane(dataPanelsHolder,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablePane.setWheelScrollingEnabled(true);

        add(tablePane, BorderLayout.CENTER);

        progressAnimation.configure(this, tablePane, BorderLayout.CENTER);

        //crucibleFacade = CrucibleServerFacadeImpl.getInstance();
        createFilterEditToolBar("atlassian.toolwindow.crucibleFilterEditToolBar", "ThePlugin.Crucible.FilterEditToolBar");
        this.crucibleCustomFilterPanel = new CrucibleCustomFilterPanel();
        filters = projectCfg.getCrucibleConfiguration().getCrucibleFilters();
    }

    private void switchToCrucible16Filter() {
        for (int i = 0;
             i < projectCfg.getCrucibleConfiguration().getCrucibleFilters().getPredefinedFilters().length; ++i) {

            if (crucible15Table != null) {
                dataPanelsHolder.remove(crucible15Table);
                crucible15Table = null;
            }
            showPredefinedFilter(
                    PredefinedFilter.values()[i],
                    projectCfg.getCrucibleConfiguration().getCrucibleFilters().getPredefinedFilters()[i],
                    null);
        }

        for (String s : projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().keySet()) {
            CustomFilterBean filter = projectCfg.getCrucibleConfiguration()
                    .getCrucibleFilters().getManualFilter().get(s);

            if (filter.isEnabled()) {
                this.showCustomFilter(true, null);
                break;
            }
        }
    }

    /**
     * Method adds or removes CollapsibleTable for given filter type
     *
     * @param filter  predefined filter type
     * @param visible panel added when true, removed when false
     */
    public void showPredefinedFilter(PredefinedFilter filter, boolean visible, CrucibleStatusChecker checker) {
        if (visible) {
            CollapsibleTable table = new CollapsibleTable(
                    tableColumnProvider,
                    projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                    filter.getFilterName(),
                    "atlassian.toolwindow.serverToolBar",
                    "ThePlugin.CrucibleReviewToolBar",
                    "Context menu",
                    getPopupActionGroup());
            table.addItemSelectedListener(this);
            TableView.restore(projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                    table.getTable());

            dataPanelsHolder.add(table);
            tables.put(filter, table);

            refreshReviews(checker);
        } else {
            if (tables.containsKey(filter)) {
                dataPanelsHolder.remove(tables.get(filter));
                tables.remove(filter);
            }
        }
        dataPanelsHolder.validate();
        tablePane.repaint();
    }

    public void switchToCrucible15Filter() {
        crucible15Table = new CollapsibleTable(
                tableColumnProvider,
                projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                TO_REVIEW_AS_ACTIVE_REVIEWER,
                "atlassian.toolwindow.serverToolBar",
                "ThePlugin.CrucibleReviewToolBar",
                "Context menu",
                getPopupActionGroup());
        crucible15Table.addItemSelectedListener(this);
        TableView.restore(projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                crucible15Table.getTable());

        for (CollapsibleTable table : tables.values()) {
            dataPanelsHolder.remove(table);
        }
        for (CollapsibleTable table : customTables.values()) {
            dataPanelsHolder.remove(table);
        }

        tables.clear();
        customTables.clear();
        dataPanelsHolder.add(crucible15Table);
        dataPanelsHolder.validate();
        tablePane.repaint();
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

    protected void setStatusMessage(String msg) {
        setStatusMessage(msg, false);
    }

    protected void setStatusMessage(String msg, boolean isError) {
        editorPane.setBackground(isError ? Color.RED : Color.WHITE);
        editorPane.setText(wrapBody("<table width=\"100%\"><tr><td colspan=\"2\">" + msg + "</td></tr></table>"));
    }

    public ProgressAnimationProvider getProgressAnimation() {
        return progressAnimation;
    }

    public CrucibleVersion getCrucibleVersion() {
        return crucibleVersion;
    }

    /*
    Crucible 1.5
     */
    public void updateReviews(Collection<ReviewData> reviews) {
        this.crucibleVersion = CrucibleVersion.CRUCIBLE_15;
        if (crucible15Table == null) {
            switchToCrucible15Filter();
        }
        List<?> reviewDatas = new ArrayList<ReviewData>(reviews);
        crucible15Table.getListTableModel().setItems(reviewDatas);
        crucible15Table.getListTableModel().fireTableDataChanged();
        crucible15Table.getTable().revalidate();
        crucible15Table.getTable().setEnabled(true);
        crucible15Table.getTable().setForeground(UIUtil.getActiveTextColor());
        crucible15Table.setTitle(TO_REVIEW_AS_ACTIVE_REVIEWER + " (" + reviewDatas.size() + ")");
        crucible15Table.expand();

        StringBuffer sb = new StringBuffer();
        sb.append("Loaded <b>");
        sb.append(reviews.size());
        sb.append(" open code reviews</b> for you.");
        setStatusMessage(sb.toString());
    }

    /*
    Crucible 1.6
     */
    public void updateReviews(Map<PredefinedFilter, List<ReviewData>> reviews, Map<String,
            List<ReviewData>> customFilterReviews) {

        this.crucibleVersion = CrucibleVersion.CRUCIBLE_16;
        if (tables.isEmpty()) {
            switchToCrucible16Filter();
        }
        int reviewCount = 0;
        for (PredefinedFilter predefinedFilter : reviews.keySet()) {
            List<ReviewData> reviewList = reviews.get(predefinedFilter);
            if (reviewList != null) {
                CollapsibleTable table = tables.get(predefinedFilter);
                if (table != null) {
                    table.getListTableModel().setItems(reviewList);
                    table.getListTableModel().fireTableDataChanged();
                    table.getTable().revalidate();
                    table.setEnabled(true);
                    table.setForeground(UIUtil.getActiveTextColor());
                    table.setTitle(predefinedFilter.getFilterName() + " (" + reviewList.size() + ")");
                }
                reviewCount += reviewList.size();
            }
        }

        for (String filterName : customFilterReviews.keySet()) {
            List<ReviewData> reviewList = customFilterReviews.get(filterName);
            if (reviewList != null) {
                CollapsibleTable table = customTables.get(filterName);
                if (table != null) {
                    table.getListTableModel().setItems(reviewList);
                    table.getListTableModel().fireTableDataChanged();
                    table.getTable().revalidate();
                    table.setEnabled(true);
                    table.setForeground(UIUtil.getActiveTextColor());
                    table.setTitle(filterName + " (" + reviewList.size() + ")");
                }
                reviewCount += reviewList.size();
            }
        }


        StringBuffer sb = new StringBuffer();
        sb.append("Loaded <b>");
        sb.append(reviewCount);
        sb.append(" code reviews</b> for defined filters.");
        setStatusMessage(sb.toString());
    }

    public void itemSelected(Object item, int noClicks) {
        selectedItem = (ReviewData) item;
        if (noClicks == 2) {
            if (item != null && item instanceof ReviewData) {
                ReviewData review = (ReviewData) item;
                IdeaHelper.getReviewActionEventBroker().trigger(new ShowReviewEvent(
                        listener, review));
            }
        }
    }

    public void resetState() {
    }

    public final void hideCrucibleCustomFilter() {
        setScrollPaneViewport(dataPanelsHolder);
        filterEditToolbarSetVisible(false);
    }

    public void collapseAllPanels() {
        for (CollapsibleTable collapsibleTable : tables.values()) {
            collapsibleTable.collapse();
        }
        for (CollapsibleTable collapsibleTable : customTables.values()) {
            collapsibleTable.collapse();
        }
    }

    public void expandAllPanels() {
        for (CollapsibleTable collapsibleTable : tables.values()) {
            collapsibleTable.expand();
        }
        for (CollapsibleTable collapsibleTable : customTables.values()) {
            collapsibleTable.expand();
        }
    }

    protected void filterEditToolbarSetVisible(boolean visible) {
        filterEditToolbar.getComponent().setVisible(visible);
    }

    protected void createFilterEditToolBar(String place, String toolbarName) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup filterEditToolBar = (ActionGroup) actionManager.getAction(toolbarName);
        filterEditToolbar = actionManager.createActionToolbar(place,
                filterEditToolBar, true);
        toolBarPanel.add(filterEditToolbar.getComponent(), BorderLayout.SOUTH);
        filterEditToolbarSetVisible(false);
    }

    protected void setScrollPaneViewport(JComponent component) {
        tablePane.setViewportView(component);
    }

    public ReviewData getSelectedReview() {
        if (selectedItem != null) {
            return this.selectedItem;
        }
        return null;
    }

    public PermId getSelectedReviewId() {
        if (selectedItem != null) {
            return this.selectedItem.getPermId();
        }
        return null;
    }

    private void showCrucibleCustomFilterPanel() {
        filterEditToolbarSetVisible(true);
        setScrollPaneViewport(crucibleCustomFilterPanel.$$$getRootComponent$$$());
    }

    public void showCrucibleCustomFilter() {
        if (!projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().isEmpty()) {
            for (String filterName : projectCfg.getCrucibleConfiguration()
                    .getCrucibleFilters().getManualFilter().keySet()) {

                crucibleCustomFilterPanel.setFilter(
                        projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().get(filterName));
                break;
            }
            showCrucibleCustomFilterPanel();
        } else {
            addCustomFilter();
        }
    }

    public void addCustomFilter() {
        String newName = FilterNameUtil.suggestNewName(
                projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter());
        CustomFilterBean newFilter = new CustomFilterBean();
        newFilter.setTitle(newName);
        crucibleCustomFilterPanel.setFilter(newFilter);
        showCrucibleCustomFilterPanel();
    }

    public void removeCustomFilter() {

    }

    public void removeItemSelectedListener(TableItemSelectedListener tableListener) {
        for (CollapsibleTable table : tables.values()) {
            table.removeItemSelectedListener(tableListener);
        }


        for (CollapsibleTable table : customTables.values()) {
            table.removeItemSelectedListener(tableListener);
        }
    }


    public void showCustomFilter(boolean visible, CrucibleStatusChecker checker) {
        if (!projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().isEmpty()) {
            for (String filterName : projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().keySet()) {
                CustomFilterBean filter
                        = projectCfg.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().get(filterName);
                if (visible) {
                    if (!customTables.containsKey(filter.getTitle())) {
                        CollapsibleTable table = new CollapsibleTable(
                                tableColumnProvider,
                                projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                                filter.getTitle(),
                                "atlassian.toolwindow.serverToolBar",
                                "ThePlugin.CrucibleReviewToolBar",
                                "Context menu",
                                getPopupActionGroup());
                        table.addItemSelectedListener(this);
                        TableView.restore(projectCfg.getCrucibleConfiguration().getTableConfiguration(),
                                table.getTable());

                        dataPanelsHolder.add(table);
                        customTables.put(filter.getTitle(), table);

                        refreshReviews(checker);
                    }
                } else {
                    if (customTables.containsKey(filter.getTitle())) {
                        dataPanelsHolder.remove(customTables.get(filter.getTitle()));
                        customTables.remove(filter.getTitle());
                    }
                }
            }
            dataPanelsHolder.validate();
            tablePane.repaint();
        }
    }

    public void refreshReviews(final CrucibleStatusChecker checker) {
        if (checker != null) {
            if (checker.canSchedule()) {
                final ProgressAnimationProvider animator = getProgressAnimation();
                final Logger log = PluginUtil.getLogger();

                new Thread(new Runnable() {
                    public void run() {
                        Thread t = new Thread(checker.newTimerTask(), "Manual Crucible panel refresh (checker)");
                        animator.startProgressAnimation();
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            log.warn(e.toString());
                        } finally {
                            animator.stopProgressAnimation();
                        }
                    }
                }, "Manual Crucible panel refresh").start();
            }
        }
    }

    public ProjectConfigurationBean getProjectCfg() {
        return projectCfg;
    }

/*

    public void getReviewComments() {
        if (selectedItem != null) {
    try {

        List<CustomFieldDef> metrics = serverFacade.getMetrics(selectedItem.getServer(), selectedItem.getMetricsVersion());
        for (CustomFieldDef metric : metrics) {
            System.out.println("metric.getName() = " + metric.getName());
        }

        List<GeneralComment> comments = serverFacade.getComments(selectedItem.getServer(), selectedItem.getPermId());
        for (GeneralComment comment : comments) {
            System.out.println("comment.getMessage() = " + comment.getMessage() + ", defect: " + comment.isDefectRaised());
            for (String key : comment.getCustomFields().keySet()) {
                CustomField field = comment.getCustomFields().get(key);
                System.out.println("key: " + key + ", field.getHrValue() = " + field.getHrValue());
            }
        }

        GeneralCommentBean gc = new GeneralCommentBean();
        gc.setUser("mwent");
        gc.setCreateDate(new Date());
        gc.setMessage("ala ma kota");
        gc.setDefectRaised(false);
        gc.setDraft(true);
        CustomFieldBean v1 = new CustomFieldBean();
        v1.setConfigVersion(3);
        v1.setFieldScope("comment");
        v1.setType(CustomFieldValueType.INTEGER);
        v1.setValue(Integer.valueOf(1));
        gc.getCustomFields().put("rank", v1);

        CustomFieldBean v2 = new CustomFieldBean();
        v2.setConfigVersion(3);
        v2.setFieldScope("comment");
        v2.setType(CustomFieldValueType.INTEGER);
        v2.setValue(Integer.valueOf(3));
        gc.getCustomFields().put("classification", v2);


        GeneralComment gc1 = serverFacade.addGeneralComment(selectedItem.getServer(), selectedItem.getPermId(), gc);
        System.out.println("gc1.getPermId().getId() = " + gc1.getPermId().getId());

        gc.getCustomFields().clear();
        gc.setPermId(gc1.getPermId());
        gc.setMessage("Ciekawe czy to bedzie widac");
        CustomFieldBean v3 = new CustomFieldBean();
        v3.setConfigVersion(3);
        v3.setFieldScope("comment");
        v3.setType(CustomFieldValueType.INTEGER);
        v3.setValue(Integer.valueOf(0));
        gc.getCustomFields().put("rank", v3);

        CustomFieldBean v4 = new CustomFieldBean();
        v4.setConfigVersion(3);
        v4.setFieldScope("comment");
        v4.setType(CustomFieldValueType.INTEGER);
        v4.setValue(Integer.valueOf(8));
        gc.getCustomFields().put("classification", v4);

        serverFacade.updateGeneralComment(selectedItem.getServer(), selectedItem.getPermId(), gc);

        GeneralCommentBean reply = new GeneralCommentBean();
        reply.setUser("mwent");
        reply.setCreateDate(new Date());
        reply.setMessage("Ola ma psa - to jest reply");
        reply.setDraft(true);

        GeneralComment rc = serverFacade.addReply(selectedItem.getServer(), selectedItem.getPermId(), gc1.getPermId(), reply);

        reply.setMessage("A ja nie mam zwiarzaka");
        serverFacade.updateReply(selectedItem.getServer(), selectedItem.getPermId(), gc1.getPermId(), rc.getPermId(), reply);



        List<ReviewItem> items = serverFacade.getFiles(selectedItem.getServer(), selectedItem.getPermId());
        for (ReviewItem item : items) {

            VersionedCommentBean vc = new VersionedCommentBean();
            vc.setDraft(true);
            vc.setUser("mwent");
            vc.setCreateDate(new Date());
            vc.setMessage("ala ma kota");
            vc.setDefectRaised(false);
            CustomFieldBean v5 = new CustomFieldBean();
            v5.setConfigVersion(3);
            v5.setFieldScope("comment");
            v5.setType(CustomFieldValueType.INTEGER);
            v5.setValue(Integer.valueOf(1));
            vc.getCustomFields().put("rank", v5);

            CustomFieldBean v6 = new CustomFieldBean();
            v6.setConfigVersion(3);
            v6.setFieldScope("comment");
            v6.setType(CustomFieldValueType.INTEGER);
            v6.setValue(Integer.valueOf(4));
            vc.getCustomFields().put("classification", v6);

            ReviewItemIdBean id = new ReviewItemIdBean();
            id.setId(item.getPermId().getId());
            vc.setReviewItemId(id);

            serverFacade.addVersionedComment(selectedItem.getServer(), item.getPermId(), vc);

            vc.getCustomFields().clear();
            CustomFieldBean v7 = new CustomFieldBean();
            v7.setConfigVersion(3);
            v7.setFieldScope("comment");
            v7.setType(CustomFieldValueType.INTEGER);
            v7.setValue(Integer.valueOf(0));
            vc.getCustomFields().put("rank", v7);

            CustomFieldBean v8 = new CustomFieldBean();
            v8.setConfigVersion(3);
            v8.setFieldScope("comment");
            v8.setType(CustomFieldValueType.INTEGER);
            v8.setValue(Integer.valueOf(5));
            vc.getCustomFields().put("classification", v8);



            vc.setToStartLine(15);
            vc.setToEndLine(19);

            serverFacade.addVersionedComment(selectedItem.getServer(), item.getPermId(), vc);

            vc.getCustomFields().clear();
            CustomFieldBean v9 = new CustomFieldBean();
            v9.setConfigVersion(3);
            v9.setFieldScope("comment");
            v9.setType(CustomFieldValueType.INTEGER);
            v9.setValue(Integer.valueOf(1));
            vc.getCustomFields().put("rank", v9);

            CustomFieldBean v10 = new CustomFieldBean();
            v10.setConfigVersion(3);
            v10.setFieldScope("comment");
            v10.setType(CustomFieldValueType.INTEGER);
            v10.setValue(Integer.valueOf(6));
            vc.getCustomFields().put("classification", v10);


            vc.setFromStartLine(25);
            vc.setFromEndLine(29);

            vc.setToStartLine(31);
            vc.setToEndLine(36);

            serverFacade.addVersionedComment(selectedItem.getServer(), item.getPermId(), vc);

        }
        serverFacade.publishAllCommentsForReview(selectedItem.getServer(), selectedItem.getPermId());

        serverFacade.completeReview(selectedItem.getServer(), selectedItem.getPermId(), true);

        serverFacade.completeReview(selectedItem.getServer(), selectedItem.getPermId(), false);

        serverFacade.approveReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.summarizeReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.closeReview(selectedItem.getServer(), selectedItem.getPermId(), "To jest summary");
        serverFacade.reopenReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.abandonReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.recoverReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.approveReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.summarizeReview(selectedItem.getServer(), selectedItem.getPermId());
        serverFacade.abandonReview(selectedItem.getServer(), selectedItem.getPermId());

        List<CrucibleAction> crucibleActions
                = serverFacade.getAvailableActions(selectedItem.getServer(), selectedItem.getPermId());
        for (CrucibleAction crucibleAction : crucibleActions) {
            System.out.println("crucibleAction = " + crucibleAction.getName());
        }

    } catch (RemoteApiException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (ServerPasswordNotProvidedException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
            
        }
    }
 */

}