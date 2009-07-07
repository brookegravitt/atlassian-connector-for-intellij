package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: Jul 7, 2009
 * Time: 11:11:19 AM
 */
public class BuildHistoryPanel extends JPanel {
    private JScrollPane scrollPane;
    private BambooServerFacade facade;
    private Project project;
    private ProgressAnimationProvider progressAnimator;
    private Task.Backgroundable currentTask;
    private JList buildList;
    private DefaultListModel listModel = new DefaultListModel();

    public BuildHistoryPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBackground(UIUtil.getListBackground());
        setOpaque(true);

        facade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());

        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(true);

        buildList = new JList();
        buildList.setModel(listModel);
        buildList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildList.setCellRenderer(RENDERER);

        scrollPane.setViewportView(buildList);

        createListListeners();

        add(scrollPane, BorderLayout.CENTER);

        progressAnimator = new ProgressAnimationProvider();
        progressAnimator.configure(this, scrollPane, BorderLayout.CENTER);
    }

    private void createListListeners() {
        buildList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    openBuild();
                }
            }
        });
        buildList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openBuild();
                }
            }
        });
    }

    private void openBuild() {
        Object[] selected = buildList.getSelectedValues();
        if (selected == null || selected.length == 0) {
            return;
        }
        BambooBuild build = (BambooBuild) selected[0];
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(project);
        if (buildsWindow != null) {
            buildsWindow.openBuild(new BambooBuildAdapterIdea(build));
        }
    }

    //
    // invoke from dispatch thread
    //
    public synchronized void showHistoryForBuild(@NotNull final BambooBuild build) {

        if (currentTask == null) {
            progressAnimator.startProgressAnimation();
        }

        currentTask = new Task.Backgroundable(project, "Getting build history for build " + build.getPlanKey(), false) {
            private Collection<BambooBuild> builds;
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    builds = facade.getRecentBuildsForPlans(build.getServer(), build.getPlanKey(),
                            build.getServer().getTimezoneOffset());
                } catch (ServerPasswordNotProvidedException e) {
                    PluginUtil.getLogger().error(e);
                }
            }

            @Override
            public void onSuccess() {
                synchronized (BuildHistoryPanel.this) {
                    if (currentTask == this) {
                        listModel.clear();
                        for (BambooBuild bambooBuild : builds) {
                            listModel.addElement(bambooBuild);
                        }
                        progressAnimator.stopProgressAnimation();
                        currentTask = null;
                    }
                }
            }
        };
        ProgressManager.getInstance().run(currentTask);
    }

    public synchronized void clearBuildHistory() {
        if (currentTask != null) {
            currentTask = null;
            progressAnimator.stopProgressAnimation();
        }
        listModel.clear();
    }

    public BambooBuild getSelectedBuild() {
        Object[] selected = buildList.getSelectedValues();
        if (selected != null && selected.length > 0) {
            return (BambooBuild) selected[0];
        }
        return null;
    }

    private static class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            BambooBuild build = (BambooBuild) value;
            BambooBuildAdapterIdea adapter = new BambooBuildAdapterIdea(build);
            label.setText(adapter.getPlanKey() + "-" + adapter.getBuildNumberAsString());
            label.setIcon(adapter.getIcon());
            return label;
        }
    }

    private static final Renderer RENDERER = new Renderer();
}
