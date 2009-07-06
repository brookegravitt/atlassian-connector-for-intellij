package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.SearchBuildDialog;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiBadServerVersionException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.awt.*;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * User: kalamon
 * Date: Jul 3, 2009
 * Time: 11:38:43 AM
 */
public class QuickSearchBuildAction extends AnAction {
    private static final String NOT_FOUND_TEXT =
            "Unable to find build <b>%1$2s</b> on server <b>%2$2s</b>.<br>"
                    + "This most likely means that the build does not exist on this server, or if<br>"
                    + "your Bamboo server is older than version 2.3, the build may be too old.<br>"
                    + "See the stack trace for detailed information.";

    public void actionPerformed(final AnActionEvent e) {
        final Project project = IdeaHelper.getCurrentProject(e.getDataContext());
        if (project == null) {
            return;
        }

        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(e);

        if (buildsWindow == null) {
            return;
        }

        final SearchBuildDialog dialog = new SearchBuildDialog(project);
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

            Collection<BambooServerData> servers = buildsWindow.getServers();
            if (servers.size() > 0) {
                ProgressManager.getInstance().run(new QuickSearchTask(e, project, servers,
                        dialog.getPlanKey(), dialog.getBuildNumber(), buildsWindow));
            }
        }
    }

    private final class QuickSearchTask extends Task.Modal {
        private List<BambooBuild> foundBuilds = new ArrayList<BambooBuild>();
        private boolean failed = false;
        private AnActionEvent event;
        @Nullable
        private Project project;
        private Collection<BambooServerData> servers;
        private String planKey;
        private int buildNumber;
        private BambooToolWindowPanel buildsWindow;

        private QuickSearchTask(AnActionEvent event, @Nullable Project project, Collection<BambooServerData> servers,
                String planKey, int buildNumber, BambooToolWindowPanel buildsWindow) {
            super(project, "Searching build", true);
            this.event = event;
            this.project = project;
            this.servers = servers;
            this.planKey = planKey;
            this.buildNumber = buildNumber;
            this.buildsWindow = buildsWindow;
        }

        public void run(@NotNull final ProgressIndicator indicator) {

            indicator.setFraction(0);

            List<IdeaUiMultiTaskExecutor.ErrorObject> problems =
                    new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();

            // find serverReviews on all selected servers
            for (BambooServerData server : servers) {
                try {
                    BambooBuild build = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger())
                            .getBuildForPlanAndNumber(server, planKey, buildNumber, server.getTimezoneOffset());
                    if (build != null) {
                        foundBuilds.add(build);
                    }
                } catch (RemoteApiBadServerVersionException e) {
                    addBuildNotFoundError(problems, server, e);
                } catch (final RemoteApiException e) {
                    Throwable cause = e.getCause();
                    if (cause != null && cause.getMessage().equals("HTTP 404 (Not Found)")) {
                        addBuildNotFoundError(problems, server, e);
                    } else {
                        addError(problems, e);
                    }
                } catch (final ServerPasswordNotProvidedException e) {
                    addError(problems, e);
                }
            }
            failed = problems.size() == servers.size();

            if (failed) {
                reportProblem(problems);
            }
        }

        private void addBuildNotFoundError(List<IdeaUiMultiTaskExecutor.ErrorObject> problems, 
                                     BambooServerData server, RemoteApiException e) {
            String msg = String.format(NOT_FOUND_TEXT, planKey + "-" + buildNumber, server.getName());
            problems.add(new IdeaUiMultiTaskExecutor.ErrorObject(msg, e));
        }

        private void addError(List<IdeaUiMultiTaskExecutor.ErrorObject> problems, Exception e) {
            problems.add(new IdeaUiMultiTaskExecutor.ErrorObject("Error getting build", e));
        }

        public void onSuccess() {
            if (!failed) {
                showPopup();
            }
        }

        private void reportProblem(final List<IdeaUiMultiTaskExecutor.ErrorObject> problems) {

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    List<IdeaUiMultiTaskExecutor.ErrorObject> errorObjects =
                            new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();

                    for (IdeaUiMultiTaskExecutor.ErrorObject problem : problems) {
                        PluginUtil.getLogger().warn(problem.getMessage(), problem.getException());
                        errorObjects.add(problem);
                    }

                    DialogWithDetails.showExceptionDialog(WindowManager.getInstance().getFrame(project), errorObjects);
                }
            });
        }

        private void showPopup() {
            if (foundBuilds.size() == 0) {
                Messages.showInfoMessage(project, "Build not found.", "Atlassian Connector for IntelliJ IDEA");
            } else if (foundBuilds.size() == 1) {
                buildsWindow.openBuild(new BambooBuildAdapterIdea(foundBuilds.iterator().next()));
            } else if (foundBuilds.size() > 1) {
                // todo
                Messages.showInfoMessage(project, "More than one build found", "Atlassian Connector for IntelliJ IDEA");
//                ListPopup popup = JBPopupFactory.getInstance().createListPopup(
//                        new ReviewListPopupStep("Found " + builds.size() + " reviews", builds, bambooWindow));
//                popup.show(event.getInputEvent().getComponent());
            }
        }
//
//    private List<ReviewAdapter> mergeReviewList(final List<ReviewAdapter> localReviews,
//            final List<ReviewAdapter> serverReviews) {
//
//        Set<ReviewAdapter> reviews = new HashSet<ReviewAdapter>();
//
//        reviews.addAll(localReviews);
//        reviews.addAll(serverReviews);
//
//        return new ArrayList<ReviewAdapter>(reviews);
//    }

//    public static final class ReviewListPopupStep extends BaseListPopupStep<ReviewAdapter> {
//        private ReviewListToolWindowPanel reviewsWindow;
//        private static final int LENGHT = 40;
//
//        public ReviewListPopupStep(final String title, final List<ReviewAdapter> reviews,
//                final ReviewListToolWindowPanel reviewsWindow) {
//            super(title, reviews, IconLoader.getIcon("/icons/crucible-16.png"));
//            this.reviewsWindow = reviewsWindow;
//        }
//
//        @NotNull
//        @Override
//        public String getTextFor(final ReviewAdapter value) {
//            StringBuilder text = new StringBuilder();
//
//            text.append(value.getPermId().getId()).append(": ");
//
//            if (value.getName().length() > LENGHT) {
//                text.append(value.getName().substring(0, LENGHT - (2 + 1))).append("...");
//            } else {
//                text.append(value.getName());
//            }
//
//            text.append(" (");
//
//            if (value.getServerData().getName().length() > LENGHT) {
//                text.append(value.getServerData().getName().substring(0, LENGHT - (2 + 1)));
//            } else {
//                text.append(value.getServerData().getName());
//            }
//
//            text.append(')');
//
//            return text.toString();
//        }
//
//        @Override
//        public PopupStep onChosen(final ReviewAdapter selectedValue, final boolean finalChoice) {
//            // add review to the model (to show it in the main list) and open the review
//            reviewsWindow.openReview(selectedValue, true);
//
//            return null;
//        }
//    }

    }

    @Override
    public void update(AnActionEvent event) {
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(event);

        if (buildsWindow == null) {
            event.getPresentation().setEnabled(false);
            return;
        }

        Collection<BambooServerData> servers = buildsWindow.getServers();
        boolean enabled = servers != null && servers.size() > 0;
        event.getPresentation().setEnabled(enabled);
    }
}
