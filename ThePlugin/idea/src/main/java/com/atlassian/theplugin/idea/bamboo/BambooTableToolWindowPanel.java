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

package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.configuration.ProjectToolWindowTableConfiguration;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.util.memoryvfs.PlainTextMemoryVirtualFile;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;

import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

public class BambooTableToolWindowPanel extends AbstractTableToolWindowPanel implements BambooStatusListener {
	private static final Key<BambooTableToolWindowPanel> WINDOW_PROJECT_KEY
            = Key.create(BambooTableToolWindowPanel.class.getName());
	private final transient BambooServerFacade bambooFacade;
	private static final DateFormat TIME_DF = new SimpleDateFormat("hh:mm a");
	private static BambooTableToolWindowPanel instance;
    private TableColumnProvider columnProvider;
	private Project project;

	protected String getInitialMessage() {
        return "Waiting for Bamboo statuses.";
    }

    protected String getToolbarActionGroup() {
        return "ThePlugin.BambooToolWindowToolBar";
    }

    protected String getPopupActionGroup() {
        return "ThePlugin.Bamboo.BuildPopupMenu";
    }

    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new BambooTableColumnProviderImpl();
        }
        return columnProvider;
    }

    protected ProjectToolWindowTableConfiguration getTableConfiguration() {
        return this.projectConfiguration.getBambooConfiguration().getTableConfiguration();
    }

	public void applyAdvancedFilter() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void cancelAdvancedFilter() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void clearAdvancedFilter() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public BambooTableToolWindowPanel(Project project, ProjectConfigurationBean projectConfigurationBean) {
        super(projectConfigurationBean);
		this.project = project;
		bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
    }


	protected void handlePopupClick(Object selectedObject) {
    }

    protected void handleDoubleClick(Object selectedObject) {
        BambooBuildAdapter build = (BambooBuildAdapter) selectedObject;
        if (build != null) {
            BrowserUtil.launchBrowser(build.getBuildResultUrl());
        }
    }


	public static BambooTableToolWindowPanel getInstance(Project project, ProjectConfigurationBean projectConfigurationBean) {

        BambooTableToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

        if (window == null) {
            window = new BambooTableToolWindowPanel(project, projectConfigurationBean);
            project.putUserData(WINDOW_PROJECT_KEY, window);
        }
        return window;
    }


	private void openLabelDialog(BambooBuildAdapterIdea build) {
		BuildLabelForm buildLabelForm = new BuildLabelForm(build);
		buildLabelForm.show();
		if (buildLabelForm.getExitCode() == 0) {
			labelBuild(build, buildLabelForm.getLabel());
		}
	}

	private void labelBuild(final BambooBuildAdapterIdea build, final String label) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Applying label on build...");
				try {
					bambooFacade.addLabelToBuild(build.getServer(),
							build.getBuildKey(), build.getBuildNumber(), label);
					setStatusMessage("Label applied on build");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Label not applied: Password on provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Label not applied: " + e.getMessage());
				}
			}
		}, null);
		new Thread(task, "atlassian-idea-plugin label build").start();
	}

	public void addLabelToBuild() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		openLabelDialog(build);
	}

	private void openCommentDialog(BambooBuildAdapterIdea build) {
		BuildCommentForm buildCommentForm = new BuildCommentForm(build);
		buildCommentForm.show();
		if (buildCommentForm.getExitCode() == 0) {
			commentBuild(build, buildCommentForm.getCommentText());
		}
	}

	private void commentBuild(final BambooBuildAdapterIdea build, final String commentText) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Adding comment label on build...");
				try {
					bambooFacade.addCommentToBuild(build.getServer(),
							build.getBuildKey(), build.getBuildNumber(), commentText);
					setStatusMessage("Comment added to build");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Comment not added: Password not provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Comment not added: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task, "atlassian-idea-plugin comment build").start();
	}

	public void addCommentToBuild() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		openCommentDialog(build);
	}

	private void executeBuild(final BambooBuildAdapterIdea build) {
		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Executing build on plan " + build.getBuildKey());
				try {
					bambooFacade.executeBuild(build.getServer(), build.getBuildKey());
					setStatusMessage("Build executed on plan: " + build.getBuildKey());
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Build not executed: Password not provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Build not executed: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task, "atlassian-idea-plugin execute build").start();
	}

	public void runBuild() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		executeBuild(build);
	}

	private void setBuilds(Collection<BambooBuild> builds) {
		boolean haveErrors = false;
		List<BambooBuildAdapterIdea> buildAdapters = new ArrayList<BambooBuildAdapterIdea>();
		Date lastPollingTime = null;
		for (BambooBuild build : builds) {
			if (!haveErrors) {
				if (build.getStatus() == BuildStatus.UNKNOWN) {
					setStatusMessage(build.getMessage(), true);
					haveErrors = true;
				}
			}
			if (build.getPollingTime() != null) {
				lastPollingTime = build.getPollingTime();
			}
			buildAdapters.add(new BambooBuildAdapterIdea(build));
		}

		// remember selection
		int selectedItem = table.getSelectedRow();

		listTableModel.setItems(buildAdapters);
		listTableModel.fireTableDataChanged();
		table.setEnabled(true);
		table.setForeground(UIUtil.getActiveTextColor());

		// restore selection
		table.getSelectionModel().setSelectionInterval(selectedItem, selectedItem);

		if (!haveErrors) {
			StringBuffer sb = new StringBuffer();
			sb.append("Loaded <b>");
			sb.append(builds.size());
			sb.append("</b> builds");
			if (lastPollingTime != null) {
				sb.append(" at  <b>");
				sb.append(TIME_DF.format(lastPollingTime));
				sb.append("</b>");
			}
			sb.append(".");
			setStatusMessage((sb.toString()));
		}
	}

	public List<BambooBuildAdapterIdea> getBuilds() {
		return (List<BambooBuildAdapterIdea>) listTableModel.getItems();
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		setBuilds(buildStatuses);
	}

	public void resetState() {
		updateBuildStatuses(new ArrayList<BambooBuild>());
	}

	public boolean getExecuteBuildEnabled() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		return build != null && build.getEnabled();
	}

	private boolean getBamboo2ActionsEnabled() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
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

    public void viewBuild() {
        BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
        BrowserUtil.launchBrowser(build.getBuildResultUrl());
    }

	public boolean canShowFailedTests() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		if (build == null) {
			return false;
		}
		return (build.isBamboo2() && (build.getTestsFailed() > 0));
	}

	public void showBuildStackTrace() {
		final BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();

		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Getting test results for build " + build.getBuildKey() + "...");
				try {
					BuildDetails details = bambooFacade.getBuildDetails(
							build.getServer(), build.getBuildKey(), build.getBuildNumber());
					final List<TestDetails> failedTests = details.getFailedTestDetails();
                    final List<TestDetails> succeededTests = details.getSuccessfulTestDetails();
                    SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							TestResultsToolWindow.getInstance().showTestResults(
									build.getBuildKey(), build.getBuildNumber(), failedTests, succeededTests);
						}
					});
					setStatusMessage("Test results for build " + build.getBuildKey() + " received");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Failed to get test results: Password not provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Failed to get test results: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task, "atlassian-idea-plugin get stack traces").start();
	}

	public boolean canShowChanges() {
		BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();
		if (build == null) {
			return false;
		}
		return build.isBamboo2();
	}

	public void showChanges() {
		final BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();

		FutureTask task = new FutureTask(new Runnable() {
			public void run() {
				setStatusMessage("Getting changes for build " + build.getBuildKey() + "...");
				try {
					BuildDetails details = bambooFacade.getBuildDetails(
							build.getServer(), build.getBuildKey(), build.getBuildNumber());
					final List<BambooChangeSet> commits = details.getCommitInfo();
                    SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							BuildChangesToolWindow.getInstance().showBuildChanges(
									build.getBuildKey(), build.getBuildNumber(), commits);
						}
					});
					setStatusMessage("Changes for build " + build.getBuildKey() + " received");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Failed to get changes: Password not provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Failed to get changes: " + e.getMessage());
				}

			}
		}, null);
		new Thread(task, "atlassian-idea-plugin get changes").start();
	}

    public void showBuildLog() {
        final BambooBuildAdapterIdea build = (BambooBuildAdapterIdea) table.getSelectedObject();

        FutureTask task = new FutureTask(new Runnable() {
            public void run() {
                setStatusMessage("Getting build log: " + build.getBuildKey() + "...");
                try {
                    final byte[] log = bambooFacade.getBuildLogs(
                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
                    final String title = "Bamboo build: " + build.getServer().getName() + ": " + build.getBuildKey() + "-" + build.getBuildNumber();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            PlainTextMemoryVirtualFile vf = new PlainTextMemoryVirtualFile(title, new String(log));
                            FileEditorManager.getInstance(IdeaHelper.getCurrentProject()).openFile(vf, true);
                        }
                    });
                    setStatusMessage("Changes for build " + build.getBuildKey() + " received");
                } catch (ServerPasswordNotProvidedException e) {
                    setStatusMessage("Failed to get changes: Password not provided for server");
                } catch (RemoteApiException e) {
                    setStatusMessage("Failed to get changes: " + e.getMessage());
                }

            }
        }, null);
        new Thread(task, "atlassian-idea-plugin get changes").start();
    }
}