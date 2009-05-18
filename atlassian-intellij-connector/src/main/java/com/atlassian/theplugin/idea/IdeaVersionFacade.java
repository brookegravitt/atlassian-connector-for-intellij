package com.atlassian.theplugin.idea;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diff.BinaryContent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.LightweightHint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public final class IdeaVersionFacade {

	private static final int IDEA_8_0 = 8000;
	private boolean isIdea8;
	private static final int IDEA_8_0_1 = 9164;

	private IdeaVersionFacade() {
		String ver = ApplicationInfo.getInstance().getBuildNumber();
		int v = Integer.parseInt(ver);
		isIdea8 = v > IDEA_8_0;
	}

	private static IdeaVersionFacade instance;

	public static IdeaVersionFacade getInstance() {
		if (instance == null) {
			instance = new IdeaVersionFacade();
		}
		return instance;
	}

	public PsiClass findClass(String name, Project project) {
		PsiClass cls = null;
		try {
			if (isIdea8) {
				Class javaPsiFacadeClass = Class.forName("com.intellij.psi.JavaPsiFacade");
				Method getInstance = javaPsiFacadeClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method findClass = javaPsiFacadeClass.getMethod("findClass", String.class, GlobalSearchScope.class);
				cls = (PsiClass) findClass.invoke(inst, name, GlobalSearchScope.allScope(project));
			} else {
				Class psiManagerClass = Class.forName("com.intellij.psi.PsiManager");
				Method getInstance = psiManagerClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method findClass = psiManagerClass.getMethod("findClass", String.class, GlobalSearchScope.class);
				cls = (PsiClass) findClass.invoke(inst, name, GlobalSearchScope.allScope(project));
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return cls;
	}

	public PsiFile[] getFiles(String filePath, Project project) {
		PsiFile[] psiFiles = null;
		try {
			if (isIdea8) {
				Class filenameIndexClass = Class.forName("com.intellij.psi.search.FilenameIndex");
				Method getFilesByName = filenameIndexClass.getMethod("getFilesByName", Project.class,
						String.class, GlobalSearchScope.class);
				Class projectScopeClass = Class.forName("com.intellij.psi.search.ProjectScope");
				Method getProjectScope = projectScopeClass.getMethod("getProjectScope", Project.class);
				GlobalSearchScope scope = (GlobalSearchScope) getProjectScope.invoke(null, project);
				psiFiles = (PsiFile[]) getFilesByName.invoke(null, project, filePath, scope);
			} else {
				Class psiManagerClass = Class.forName("com.intellij.psi.PsiManager");
				Method getInstance = psiManagerClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method getShortNamesCache = psiManagerClass.getMethod("getShortNamesCache");
				Class psiShortNamesCacheClass = Class.forName("com.intellij.psi.search.PsiShortNamesCache");
				Object cacheInstance = getShortNamesCache.invoke(inst);
				Method getFilesByName = psiShortNamesCacheClass.getMethod("getFilesByName", String.class);
				psiFiles = (PsiFile[]) getFilesByName.invoke(cacheInstance, filePath);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return psiFiles;
	}

	public MultipleChangeListBrowser getChangesListBrowser(Project project, ChangeListManager changeListManager,
			final Collection<Change> changes) {
		try {

			final ArrayList<Change> changeList;
			if (changes == null) {
				changeList = new ArrayList<Change>(
						changeListManager.getDefaultChangeList().getChanges());
			} else {
				changeList = new ArrayList<Change>(changes);
			}
			String ver = ApplicationInfo.getInstance().getBuildNumber();
			int v = Integer.parseInt(ver);
			if (v > IDEA_8_0_1) {
				Class browserClass = Class.forName("com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser");
				Constructor[] constructors = browserClass.getConstructors();
				return (MultipleChangeListBrowser) constructors[0].newInstance(project, changeListManager.getChangeLists(),
						changeList, null, true, true, null);
			} else {
				Class browserClass = Class.forName("com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser");
				Constructor[] constructors = browserClass.getConstructors();
				return (MultipleChangeListBrowser) constructors[0].newInstance(project, changeListManager.getChangeLists(),
						changeList, null, true, true);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

    public void addActionToDiffGroup(@NotNull AnAction action) {
        if (isIdea8) {
            DefaultActionGroup diffToolbar =
                    (DefaultActionGroup) ActionManager.getInstance().getAction("DiffPanel.Toolbar");
            if (diffToolbar != null) {
                diffToolbar.add(action);
            }
        }
    }

	public void showEditorHints(LightweightHint lightweightHint, Editor anEditor, Point point) {
		try {
			Class hintManagerClass = Class.forName("com.intellij.codeInsight.hint.HintManager");
			Method getInstance = hintManagerClass.getMethod("getInstance");
			Object inst = getInstance.invoke(null);
			Class mgrClass;
			if (isIdea8) {
				Class hintManagerImplClass = Class.forName("com.intellij.codeInsight.hint.HintManagerImpl");
				mgrClass = hintManagerImplClass;
				inst = hintManagerImplClass.cast(inst);
			} else {
				mgrClass = hintManagerClass;
			}

			Method showEditorHint = mgrClass.getMethod("showEditorHint",
					LightweightHint.class, Editor.class, Point.class, int.class, int.class, boolean.class);
			Field hideByAnyKey = mgrClass.getField("HIDE_BY_ANY_KEY");
			Field hideByTextChange = mgrClass.getField("HIDE_BY_TEXT_CHANGE");
			Field hideByOtherHint = mgrClass.getField("HIDE_BY_OTHER_HINT");
			Field hideByScrolling = mgrClass.getField("HIDE_BY_SCROLLING");
			int hbak = (Integer) hideByAnyKey.get(null);
			int hbtc = (Integer) hideByTextChange.get(null);
			int hboh = (Integer) hideByOtherHint.get(null);
			int hbs = (Integer) hideByScrolling.get(null);

			showEditorHint.invoke(inst, lightweightHint, anEditor, point, hbak | hbtc | hboh | hbs, -1, false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public void runTests(RunnerAndConfigurationSettings settings, AnActionEvent ev, boolean debug) {
		try {
			if (isIdea8) {
				Class executorClass = Class.forName("com.intellij.execution.Executor");
				Class defaultDebugExecutorClass = Class.forName("com.intellij.execution.executors.DefaultDebugExecutor");
				Class defaultRunExecutorClass = Class.forName("com.intellij.execution.executors.DefaultRunExecutor");
				Class executorRegistryClass = Class.forName("com.intellij.execution.ExecutorRegistry");
				Method getInstance = executorRegistryClass.getMethod("getInstance");
				Object executorRegistryInstance = getInstance.invoke(null);
				Method getExecutorById = executorRegistryClass.getMethod("getExecutorById", String.class);
				Class selectedExecutorClass = debug ? defaultDebugExecutorClass : defaultRunExecutorClass;
				Field executorIdField = selectedExecutorClass.getField("EXECUTOR_ID");
				String executorId = (String) executorIdField.get(null);
				Object executor = getExecutorById.invoke(executorRegistryInstance, executorId);

				Class runnerClass = Class.forName("com.intellij.execution.runners.ProgramRunner");
				Class runnerRegistryClass = Class.forName("com.intellij.execution.RunnerRegistry");
				getInstance = runnerRegistryClass.getMethod("getInstance");
				Object runnerRegistryInstance = getInstance.invoke(null);
				Method getId = executorClass.getMethod("getId");
				String id = (String) getId.invoke(executor);
				Method getRunner = runnerRegistryClass.getMethod("getRunner", String.class, RunProfile.class);
				Object runner = getRunner.invoke(runnerRegistryInstance, id, settings.getConfiguration());
				if (runner != null) {
					try {
						Class executionEnvironmentClass = Class.forName(
								"com.intellij.execution.runners.ExecutionEnvironment");
						Constructor c = executionEnvironmentClass.getConstructor(
								runnerClass, RunnerAndConfigurationSettings.class, DataContext.class);
						Object executionEnvironment = c.newInstance(runner, settings, ev.getDataContext());
						Method execute = runnerClass.getMethod("execute", executorClass, executionEnvironmentClass);
						execute.invoke(runner, executor, executionEnvironment);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} else {
				Class javaProgramRunnerClass = Class.forName("com.intellij.execution.runners.JavaProgramRunner");
				Class executionRegistryClass = Class.forName("com.intellij.execution.ExecutionRegistry");
				Class runStrategyClass = Class.forName("com.intellij.execution.runners.RunStrategy");

				Method getInstance = executionRegistryClass.getMethod("getInstance");
				Object registryInstance = getInstance.invoke(null);

				getInstance = runStrategyClass.getMethod("getInstance");
				Object strategyInstance = getInstance.invoke(null);

				Method getRunner = executionRegistryClass.getMethod(debug ? "getDebuggerRunner" : "getDefaultRunner");

				Object runner = getRunner.invoke(registryInstance);

				Method execute = runStrategyClass.getMethod("execute",
						RunnerAndConfigurationSettings.class, javaProgramRunnerClass, DataContext.class);

				try {
					execute.invoke(strategyInstance, settings, runner, ev.getDataContext());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	public BinaryContent createBinaryContent(@NotNull final VirtualFile virtualFile) {
		BinaryContent content = null;
		FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(virtualFile);
		try {
			Class binaryContentClass = Class.forName("com.intellij.openapi.diff.BinaryContent");
			Constructor constructor = binaryContentClass
					.getConstructor(new Class[]{byte[].class, isIdea8 ? Charset.class : String.class, FileType.class});
			return (BinaryContent) constructor.newInstance(virtualFile.contentsToByteArray(), null, fileType);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return content;
	}

	public void setCommitedChangesList(@NotNull final CommittedChangesTreeBrowser browser,
			@NotNull final java.util.List<CommittedChangeList> list, final boolean flag) {
		try {
			Method setItems = null;
			for (Method method : browser.getClass().getMethods()) {
				if (method.getName().equals("setItems")) {
					setItems = method;
					break;
				}
			}
			if (setItems == null) {
				return;
			}
			if (isIdea8) {
				Class enumClass = Class.forName("com.intellij.openapi.vcs.changes.committed.CommittedChangesBrowserUseCase");
				Method valueOf = enumClass.getMethod("valueOf", String.class);
				setItems.invoke(browser, list, flag, valueOf.invoke(null, "COMMITTED"));
			} else {
				setItems.invoke(browser, list, flag);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public enum OperationStatus {
		INFO, WARNING, ERROR
	}

	public void fireNofification(Project project, JComponent content, String message, String iconName, OperationStatus status,
			Color color) {
/*
		if (isIdea8) {
			ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
			if (toolWindowManager != null) {
				try {
					Method notifyByBalloon = null;
					for (Method method : toolWindowManager.getClass().getMethods()) {
						if (method.getName().equals("notifyByBalloon")) {
							Class<?>[] params = method.getParameterTypes();
							if (params.length == 5) {
								notifyByBalloon = method;
								break;
							}
						}
					}
					if (notifyByBalloon == null) {
						return;
					}
					Class messageTypeClass = Class.forName("com.intellij.openapi.ui.MessageType");
					HyperlinkListener listener = new HyperlinkListener() {
						public void hyperlinkUpdate(final HyperlinkEvent e) {
							if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
								URL url = e.getURL();
								BrowserUtil.launchBrowser(url.toString());
							}
						}
					};
					System.out.println("message = " + message);
					notifyByBalloon.invoke(toolWindowManager,
							new Object[]{PluginToolWindow.TOOL_WINDOW_NAME,
									messageTypeClass.getField(status.toString()).get(null),
									message, IconLoader.getIcon(iconName), listener});
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
*/
		final WindowManager windowManager = WindowManager.getInstance();
		if (windowManager != null) {
			final StatusBar statusBar = windowManager.getStatusBar(project);
			if (statusBar != null) {
				statusBar.fireNotificationPopup(content, color);
			}
		}
//		}
	}
}
