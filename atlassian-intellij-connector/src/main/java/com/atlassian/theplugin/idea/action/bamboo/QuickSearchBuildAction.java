package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiBadServerVersionException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.SearchBuildDialog;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: kalamon
 * Date: Jul 3, 2009
 * Time: 11:38:43 AM
 */
public class QuickSearchBuildAction extends AnAction {
	private static final String NOT_FOUND_TEXT =
			"Unable to find build <b>%1$2s</b> on server <b>%2$2s</b>.<br>"
            + "This is probably because the build is not on this server.<br>"
            + "However, if your Bamboo server's version is earlier than 2.3,<br>"
            + "it could be because the build is too old for the search to find.<br>"
            + "(Bamboo 2.3 offers an improved search API.)<br>"
            + "Click 'Show Details' to see the stack trace.";

	@Override
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
		private final List<BambooBuildAdapter> foundBuilds = new ArrayList<BambooBuildAdapter>();
		private boolean failed = false;
		private final AnActionEvent event;
		@NotNull
		private final Project project;
		private final Collection<BambooServerData> servers;
		private final String planKey;
		private final int buildNumber;
		private final BambooToolWindowPanel buildsWindow;

		private QuickSearchTask(AnActionEvent event, @NotNull Project project, Collection<BambooServerData> servers,
				String planKey, int buildNumber, BambooToolWindowPanel buildsWindow) {
			super(project, "Searching build", true);
			this.event = event;
			this.project = project;
			this.servers = servers;
			this.planKey = planKey;
			this.buildNumber = buildNumber;
			this.buildsWindow = buildsWindow;
		}

		@Override
		public void run(@NotNull final ProgressIndicator indicator) {

			indicator.setFraction(0);

			List<IdeaUiMultiTaskExecutor.ErrorObject> problems =
					new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();

			// find serverReviews on all selected servers
			for (BambooServerData server : servers) {
				try {
					BambooBuildAdapter build =
							IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger())
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

		@Override
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
						//noinspection ThrowableResultOfMethodCallIgnored
						PluginUtil.getLogger().warn(problem.getMessage(), problem.getException());
						errorObjects.add(problem);
					}
                    if (problems.size() > 1) {
					    DialogWithDetails.showExceptionDialog(
                                WindowManager.getInstance().getFrame(project), errorObjects);
                    } else {
                        DialogWithDetails.showExceptionDialog(
                                project, "<html>" + problems.get(0).getMessage(),
                                DialogWithDetails.getExceptionString(problems.get(0).getException()));
                    }
				}
			});
		}

		private void showPopup() {
			if (foundBuilds.size() == 0) {
				Messages.showInfoMessage(project, "Build not found.", PluginUtil.PRODUCT_NAME);
			} else if (foundBuilds.size() == 1) {
				buildsWindow.openBuild(foundBuilds.iterator().next());
			} else if (foundBuilds.size() > 1) {
                ListPopup popup = JBPopupFactory.getInstance().createListPopup(
                        new BuildListPopupStep("Found " + foundBuilds.size() + " builds"));
                popup.showCenteredInCurrentWindow(project);
			}
		}

		public final class BuildListPopupStep extends BaseListPopupStep<BambooBuildAdapter> {
            private static final int LENGHT = 40;

            public BuildListPopupStep(final String title) {
                super(title, foundBuilds, IconLoader.getIcon("/icons/blue-16.png"));
            }

            @NotNull
            @Override
			public String getTextFor(final BambooBuildAdapter value) {
                StringBuilder text = new StringBuilder();

                text.append(value.getPlanKey()).append('-').append(value.getNumber());

                text.append(" (");

                if (value.getServer().getName().length() > LENGHT) {
                    text.append(value.getServer().getName()).substring(0, LENGHT - (2 + 1));
                } else {
                    text.append(value.getServer().getName());
                }

                text.append(')');

                return text.toString();
            }

            @Override
			public PopupStep<BambooBuildAdapter> onChosen(final BambooBuildAdapter selectedValue,
					final boolean finalChoice) {
				buildsWindow.openBuild(selectedValue);
                return null;
            }
        }

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
