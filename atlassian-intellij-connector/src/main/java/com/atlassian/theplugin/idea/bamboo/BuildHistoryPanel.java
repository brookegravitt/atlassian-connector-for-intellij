package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * User: kalamon
 * Date: Jul 7, 2009
 * Time: 11:11:19 AM
 */
public class BuildHistoryPanel extends JPanel {

    private static final RendererPanel RENDERER_PANEL = new RendererPanel();
    protected static final int ROW_HEIGHT = 16;

    private final BambooServerFacade facade;
    private final Project project;
    private final ProgressAnimationProvider progressAnimator;
    private Task.Backgroundable currentTask;
    private final JList buildList;
    private final DefaultListModel listModel = new DefaultListModel();
    private final JScrollPane scrollPane;
    private String previousBuild;

    public BuildHistoryPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBackground(UIUtil.getListBackground());
        setOpaque(true);

        facade = IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger());

        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(true);

        buildList = new JList() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        buildList.setFixedCellWidth(ROW_HEIGHT);
        buildList.setModel(listModel);
        buildList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                BambooBuildAdapter buildAdapter = (BambooBuildAdapter) value;
                RENDERER_PANEL.setBuild(buildAdapter);
                RENDERER_PANEL.setSelected(isSelected);
                BuildTreeNode.addTooltipToPanel(buildAdapter, RENDERER_PANEL);
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
            @Override
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
        BambooBuildAdapter build = (BambooBuildAdapter) selected[0];
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(project);
        if (buildsWindow != null) {
            buildsWindow.openBuild(build);
        }
    }

    //
    // getBuilds from dispatch thread
    //

    public synchronized void showHistoryForBuild(@NotNull final BambooBuildAdapter buildDetailsInfo) {
        try {
            String currentBuild =
                    buildDetailsInfo.getServer().getUrl() + ":" + buildDetailsInfo.getPlanKey() + "-"
                            + buildDetailsInfo.getNumber();
            if (previousBuild != null) {
                if (currentBuild.equals(previousBuild)) {
                    scrollPane.setViewportView(buildList);
                    return;
                }
            }
            previousBuild = currentBuild;
        } catch (UnsupportedOperationException e) {
            previousBuild = null;
        }

        if (currentTask == null) {
            startThrobber();
        }

        currentTask =
                new Task.Backgroundable(project, "Getting build history for build " + buildDetailsInfo.getPlanKey(), false) {
                    private Collection<BambooBuildAdapter> builds;

                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        try {
                            builds = facade.getRecentBuildsForPlans(buildDetailsInfo.getServer(), buildDetailsInfo.getPlanKey(),
                                    buildDetailsInfo.getServer().getTimezoneOffset());
                        } catch (ServerPasswordNotProvidedException e) {
                            PluginUtil.getLogger().error(e);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        synchronized (BuildHistoryPanel.this) {
                            if (currentTask == this) {
                                listModel.clear();
                                scrollPane.setViewportView(buildList);
                                if (builds != null) {
                                    for (BambooBuildAdapter bambooBuild : builds) {
                                        listModel.addElement(bambooBuild);
                                    }
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
        scrollPane.setViewportView(null);
//        listModel.clear();
    }

    public synchronized BambooBuildAdapter getSelectedBuild() {
        Object[] selected = buildList.getSelectedValues();
        if (selected != null && selected.length > 0) {
            return (BambooBuildAdapter) selected[0];
        }
        return null;
    }

    private static final class RendererPanel extends JPanel {
        private final JLabel buildIcon = new JLabel();
        private final SelectableLabel buildKey =
                new SelectableLabel(true, true, "NOTHING YET", null, SwingConstants.TRAILING, ROW_HEIGHT);
        private final SelectableLabel buildDate =
                new SelectableLabel(true, true, "NEITHER HERE", null, SwingConstants.TRAILING, ROW_HEIGHT);

        private RendererPanel() {
            setLayout(new FormLayout("3dlu, pref, 1dlu, fill:pref:grow, right:pref, 3dlu", "pref"));
            CellConstraints cc = new CellConstraints();
            setOpaque(false);
            add(buildIcon, cc.xy(2, 1));
            // stupid checkstyle
            add(buildKey, cc.xy(2 + 2, 1));
            buildDate.setHorizontalAlignment(SwingConstants.RIGHT);
            // stupid stupid checkstyle
            add(buildDate, cc.xy(2 + 2 + 1, 1));
        }

        public void setBuild(BambooBuildAdapter build) {
            buildIcon.setIcon(build.getIcon());
            buildKey.setText(build.getPlanKey() + "-" + build.getBuildNumberAsString());
            buildDate.setText(DateUtil.getRelativePastDate(build.getCompletionDate()));
        }

        public void setSelected(boolean selected) {
            buildKey.setSelected(selected);
            buildDate.setSelected(selected);
        }
    }
}
