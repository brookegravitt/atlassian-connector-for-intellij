package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

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

    private static final RendererPanel RENDERER_PANEL = new RendererPanel();
    protected static final int ROW_HEIGHT = 16;

    private BambooServerFacade facade;
    private Project project;
    private ProgressAnimationProvider progressAnimator;
    private Task.Backgroundable currentTask;
    private JList buildList;
    private DefaultListModel listModel = new DefaultListModel();
    private JScrollPane scrollPane;

    public BuildHistoryPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBackground(UIUtil.getListBackground());
        setOpaque(true);

        facade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());

        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(true);

        buildList = new JList() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        buildList.setModel(listModel);
        buildList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
                BambooBuild build = (BambooBuild) value;
                RENDERER_PANEL.setBuild(new BambooBuildAdapterIdea(build));
                RENDERER_PANEL.setSelected(isSelected);
                return RENDERER_PANEL;
            }
        });

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
        Object[] selected;

        synchronized (this) {
            selected = buildList.getSelectedValues();
        }
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
            startThrobber();
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
                        stopThrobber();
                        currentTask = null;
                    }
                }
            }
        };
        ProgressManager.getInstance().run(currentTask);
    }

    private void stopThrobber() {
        setBorder(BorderFactory.createEmptyBorder());
        progressAnimator.stopProgressAnimation();
    }

    private void startThrobber() {
        setBorder(scrollPane.getBorder());
        progressAnimator.startProgressAnimation();
    }

    public synchronized void clearBuildHistory() {
        if (currentTask != null) {
            currentTask = null;
            stopThrobber();
        }
        listModel.clear();
    }

    public synchronized BambooBuild getSelectedBuild() {
        Object[] selected = buildList.getSelectedValues();
        if (selected != null && selected.length > 0) {
            return (BambooBuild) selected[0];
        }
        return null;
    }

    private static class RendererPanel extends JPanel {
        private JLabel buildIcon = new JLabel();
        private SelectableLabel buildKey =
                new SelectableLabel(true, true, "NOTHING YET", null, SwingConstants.TRAILING, ROW_HEIGHT);
        private SelectableLabel buildDate =
                new SelectableLabel(true, true, "NEITHER HERE", ROW_HEIGHT, false, false);

        private RendererPanel() {
            setLayout(new FormLayout("3dlu, pref, 1dlu, fill:pref:grow, right:pref", "pref"));
            CellConstraints cc = new CellConstraints();
            setOpaque(false);
            add(buildIcon, cc.xy(2, 1));
            // stupid checkstyle
            add(buildKey, cc.xy(2 + 2, 1));
            buildDate.setHorizontalAlignment(SwingConstants.RIGHT);
            // stupid stupid checkstyle
            add(buildDate, cc.xy(2 + 2 + 1, 1));
        }

        public void setBuild(BambooBuildAdapterIdea build) {
            buildIcon.setIcon(build.getIcon());
            buildKey.setText(build.getPlanKey() + "-" + build.getBuildNumberAsString());
            buildDate.setText(DateUtil.getRelativePastDate(build.getCompletionDate()) + "  ");
        }

        public void setSelected(boolean selected) {
            buildKey.setSelected(selected);
            buildDate.setSelected(selected);
        }
    }
}
