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
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.AbstractTableToolWindowPanel;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.util.memoryvfs.PlainTextMemoryVirtualFile;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BambooTableToolWindowPanel extends AbstractTableToolWindowPanel<BambooBuildAdapterIdea>
		implements BambooStatusListener, DataProvider {
    private final transient BambooServerFacade bambooFacade;
    private static final DateTimeFormatter TIME_DF = DateTimeFormat.forPattern("hh:mm a");
    private TableColumnProvider columnProvider;    
	private final TestResultsToolWindow testResultsToolWindow;
	private final BuildChangesToolWindow buildChangesToolWindow;
	public static final String BAMBOO_ATLASSIAN_TOOLWINDOW_SERVER_TOOL_BAR = "atlassian.bamboo.toolwindow";

	@Override
    protected String getInitialMessage() {
        return "Waiting for Bamboo statuses.";
    }

    @Override
    protected String getToolbarActionGroup() {
        return "ThePlugin.BambooToolWindowToolBar";
    }

    @Override
    protected String getPopupActionGroup() {
        return "ThePlugin.Bamboo.BuildPopupMenu";
    }

    @Override
    protected TableColumnProvider getTableColumnProvider() {
        if (columnProvider == null) {
            columnProvider = new BambooTableColumnProviderImpl();
        }
        return columnProvider;
    }

    @Override
    protected ProjectToolWindowTableConfiguration getTableConfiguration() {
        return projectConfiguration.getBambooConfiguration().getTableConfiguration();
    }

    @Override
    public void applyAdvancedFilter() {
    }

    @Override
    public void cancelAdvancedFilter() {
    }

    @Override
    public void clearAdvancedFilter() {
    }

	@Override
	public String getActionPlace() {
		return BAMBOO_ATLASSIAN_TOOLWINDOW_SERVER_TOOL_BAR + project.getName();
	}

	public BambooTableToolWindowPanel(Project project, ProjectConfigurationBean projectConfigurationBean,
			final TestResultsToolWindow testResultsToolWindow, final BuildChangesToolWindow buildChangesToolWindow) {
        super(project, projectConfigurationBean);        
		this.testResultsToolWindow = testResultsToolWindow;
		this.buildChangesToolWindow = buildChangesToolWindow;
		bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		assert this.testResultsToolWindow != null;
		assert this.buildChangesToolWindow != null;
	}


    @Override
    protected void handlePopupClick(Object selectedObject) {
    }

    @Override
    protected void handleDoubleClick(Object selectedObject) {
        BambooBuildAdapter build = (BambooBuildAdapter) selectedObject;
        final BambooBuildToolWindow window = IdeaHelper.getProjectComponent(project, BambooBuildToolWindow.class);
			if (window != null && build != null) {
				window.open(build.getBuild());
			}
    }


//    public static BambooTableToolWindowPanel getInstance(Project project, ProjectConfigurationBean projectConfigurationBean) {
//
//        BambooTableToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);
//
//        if (window == null) {
//            window = new BambooTableToolWindowPanel(project, projectConfigurationBean);
//            project.putUserData(WINDOW_PROJECT_KEY, window);
//        }
//        return window;
//    }


    private void openLabelDialog(BambooBuildAdapterIdea build) {
        BuildLabelForm buildLabelForm = new BuildLabelForm(build);
        buildLabelForm.show();
        if (buildLabelForm.getExitCode() == 0) {
            labelBuild(build, buildLabelForm.getLabel());
        }
    }

    private void labelBuild(final BambooBuildAdapterIdea build, final String label) {

		Task.Backgroundable labelTask = new Task.Backgroundable(project, "Labeling Build", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
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
		};

		ProgressManager.getInstance().run(labelTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Applying label on build...");
//                try {
//                    bambooFacade.addLabelToBuild(build.getServer(),
//                            build.getBuildKey(), build.getBuildNumber(), label);
//                    setStatusMessage("Label applied on build");
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Label not applied: Password on provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Label not applied: " + e.getMessage());
//                }
//            }
//        }, "atlassian-idea-plugin label build").start();
    }

    public void addLabelToBuild() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
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

		Task.Backgroundable commentTask = new Task.Backgroundable(project, "Commenting Build", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
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
		};

		ProgressManager.getInstance().run(commentTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Adding comment label on build...");
//                try {
//                    bambooFacade.addCommentToBuild(build.getServer(),
//                            build.getBuildKey(), build.getBuildNumber(), commentText);
//                    setStatusMessage("Comment added to build");
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Comment not added: Password not provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Comment not added: " + e.getMessage());
//                }
//
//            }
//        }, "atlassian-idea-plugin comment build").start();
    }

    public void addCommentToBuild() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
        openCommentDialog(build);
    }

    private void executeBuild(final BambooBuildAdapterIdea build) {

		Task.Backgroundable executeTask = new Task.Backgroundable(project, "Starting Build", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
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
		};

		ProgressManager.getInstance().run(executeTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Executing build on plan " + build.getBuildKey());
//                try {
//                    bambooFacade.executeBuild(build.getServer(), build.getBuildKey());
//                    setStatusMessage("Build executed on plan: " + build.getBuildKey());
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Build not executed: Password not provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Build not executed: " + e.getMessage());
//                }
//
//            }
//        }, "atlassian-idea-plugin execute build").start();
    }

    public void runBuild() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
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
                sb.append(TIME_DF.print(lastPollingTime.getTime()));
                sb.append("</b>");
            }
            sb.append(".");
            setStatusMessage((sb.toString()));
        }
    }

    @SuppressWarnings("unchecked")
    public List<BambooBuildAdapterIdea> getBuilds() {
        return listTableModel.getItems();
    }

    public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
        setBuilds(buildStatuses);
    }

    public void resetState() {
        updateBuildStatuses(new ArrayList<BambooBuild>());
    }

    public boolean getExecuteBuildEnabled() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
        return build != null && build.getEnabled();
    }

    private boolean getBamboo2ActionsEnabled() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
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
        BambooBuildAdapterIdea build = table.getSelectedObject();
        if (build != null) {
            BrowserUtil.launchBrowser(build.getBuildResultUrl());
        }
    }

    public boolean canShowFailedTests() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
        if (build == null) {
            return false;
        }
        return (build.isBamboo2() && (build.getTestsFailed() > 0));
    }

    public void showBuildStackTrace() {
        final BambooBuildAdapterIdea build = table.getSelectedObject();

		Task.Backgroundable stackTraceTask = new Task.Backgroundable(project, "Retrieving Build Stack Trace", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
				setStatusMessage("Getting test results for build " + build.getBuildKey() + "...");
                try {
                    BuildDetails details = bambooFacade.getBuildDetails(
                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
                    final List<TestDetails> failedTests = details.getFailedTestDetails();
                    final List<TestDetails> succeededTests = details.getSuccessfulTestDetails();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            testResultsToolWindow.showTestResults(
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
		};

		ProgressManager.getInstance().run(stackTraceTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Getting test results for build " + build.getBuildKey() + "...");
//                try {
//                    BuildDetails details = bambooFacade.getBuildDetails(
//                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
//                    final List<TestDetails> failedTests = details.getFailedTestDetails();
//                    final List<TestDetails> succeededTests = details.getSuccessfulTestDetails();
//                    SwingUtilities.invokeLater(new Runnable() {
//                        public void run() {
//                            testResultsToolWindow.showTestResults(
//                                    build.getBuildKey(), build.getBuildNumber(), failedTests, succeededTests);
//                        }
//                    });
//                    setStatusMessage("Test results for build " + build.getBuildKey() + " received");
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Failed to get test results: Password not provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Failed to get test results: " + e.getMessage());
//                }
//
//            }
//        }, "atlassian-idea-plugin get stack traces").start();
    }

    public boolean canShowChanges() {
        BambooBuildAdapterIdea build = table.getSelectedObject();
        if (build == null) {
            return false;
        }
        return build.isBamboo2();
    }

    public void showChanges() {
        final BambooBuildAdapterIdea build = table.getSelectedObject();

		Task.Backgroundable changesTask = new Task.Backgroundable(project, "Retrieving Build Changes", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
				setStatusMessage("Getting changes for build " + build.getBuildKey() + "...");
                try {
                    BuildDetails details = bambooFacade.getBuildDetails(
                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
                    final List<BambooChangeSet> commits = details.getCommitInfo();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            buildChangesToolWindow.showBuildChanges(
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
		};

		ProgressManager.getInstance().run(changesTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Getting changes for build " + build.getBuildKey() + "...");
//                try {
//                    BuildDetails details = bambooFacade.getBuildDetails(
//                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
//                    final List<BambooChangeSet> commits = details.getCommitInfo();
//                    SwingUtilities.invokeLater(new Runnable() {
//                        public void run() {
//                            buildChangesToolWindow.showBuildChanges(
//                                    build.getBuildKey(), build.getBuildNumber(), commits);
//                        }
//                    });
//                    setStatusMessage("Changes for build " + build.getBuildKey() + " received");
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Failed to get changes: Password not provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Failed to get changes: " + e.getMessage());
//                }
//
//            }
//        }, "atlassian-idea-plugin get changes").start();
    }

	
	public void showBuildLog() {
        final BambooBuildAdapterIdea build = table.getSelectedObject();

		Task.Backgroundable buildLogTask = new Task.Backgroundable(project, "Retrieving Build Log", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
				setStatusMessage("Getting build log: " + build.getBuildKey() + "...");
				try {
					final byte[] log = bambooFacade.getBuildLogs(
							build.getServer(), build.getBuildKey(), build.getBuildNumber());
					final String title = "Bamboo build: "
							+ build.getServer().getName() + ": "
							+ build.getBuildKey() + "-" + build.getBuildNumber();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							PlainTextMemoryVirtualFile vf = new PlainTextMemoryVirtualFile(title, new String(log));
							FileEditorManager.getInstance(project).openFile(vf, true);
						}
					});
					setStatusMessage("Changes for build " + build.getBuildKey() + " received");
				} catch (ServerPasswordNotProvidedException e) {
					setStatusMessage("Failed to get changes: Password not provided for server");
				} catch (RemoteApiException e) {
					setStatusMessage("Failed to get changes: " + e.getMessage());
				}

			}
		};

		ProgressManager.getInstance().run(buildLogTask);

//		new Thread(new Runnable() {
//            public void run() {
//                setStatusMessage("Getting build log: " + build.getBuildKey() + "...");
//                try {
//                    final byte[] log = bambooFacade.getBuildLogs(
//                            build.getServer(), build.getBuildKey(), build.getBuildNumber());
//                    final String title = "Bamboo build: "
//                            + build.getServer().getName() + ": "
//                            + build.getBuildKey() + "-" + build.getBuildNumber();
//                    SwingUtilities.invokeLater(new Runnable() {
//                        public void run() {
//                            PlainTextMemoryVirtualFile vf = new PlainTextMemoryVirtualFile(title, new String(log));
//                            FileEditorManager.getInstance(project).openFile(vf, true);
//                        }
//                    });
//                    setStatusMessage("Changes for build " + build.getBuildKey() + " received");
//                } catch (ServerPasswordNotProvidedException e) {
//                    setStatusMessage("Failed to get changes: Password not provided for server");
//                } catch (RemoteApiException e) {
//                    setStatusMessage("Failed to get changes: " + e.getMessage());
//                }
//
//            }
//        }, "atlassian-idea-plugin get changes").start();
    }

	@Nullable
	public Object getData(@NonNls final String dataId) {
		if (Constants.BAMBOO_BUILD_KEY.getName().equals(dataId)) {
			final BambooBuildAdapterIdea buildAdapterIdea = table.getSelectedObject();
			if (buildAdapterIdea != null) {
				return buildAdapterIdea.getBuild();
			} else {
				return null;
			}
		}
		return null;
	}
}