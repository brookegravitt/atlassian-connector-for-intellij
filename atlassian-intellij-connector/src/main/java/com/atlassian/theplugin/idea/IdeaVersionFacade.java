package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.exception.PatchCreateErrorException;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diff.BinaryContent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser;
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdeaVersionFacade {

	private static final int IDEA_8_0 = 8000;
	private static final int IDEA_8_0_1 = 9164;
	private static final int IDEA_8_1_3 = 9886;
	private static final int IDEA_9_EAP = 10000;
	private static final int IDEA_X_EAP_GR4 = 96;
	private static final int IDEA_9_GR4 = 95;
	private static final int IDEA_9_COMMUNITY_1 = 93;

	private boolean isIdea7;
	private boolean isIdea8;
	private boolean isIdea9;
	private boolean isIdeaX;
	private boolean communityEdition = false;


	private static final String IDEA_9_REGEX_STRING = "((IU)|(IC))-(\\d+)\\.(\\d+)";
	private static final Pattern IDEA_9_REGEX = Pattern.compile(IDEA_9_REGEX_STRING);


	private IdeaVersionFacade() {
		// there is no getBuild().asString() in IDEA 8.0 and older, so we need to use
		// deprecated getBuildNumber() method here...
		@SuppressWarnings("deprecation")
		String ver = ApplicationInfo.getInstance().getBuildNumber();
		Matcher m = IDEA_9_REGEX.matcher(ver);
		final int group4 = m.matches() && m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;

		if (m.matches() && (group4 == IDEA_9_GR4 || group4 == IDEA_9_COMMUNITY_1)) {
			isIdea9 = true; // hmm, actually we should check if m.group(4) is 90. But let's leave it for now
			communityEdition = m.group(3) != null;
		} else if (m.matches() && (group4 >= IDEA_X_EAP_GR4)) {
			isIdeaX = true;
			communityEdition = m.group(3) != null;
		} else {

			try {
				int v = Integer.parseInt(ver);
				isIdea8 = v > IDEA_8_0;
				isIdea9 = v > IDEA_9_EAP;
				isIdea7 = v < IDEA_8_0;

			} catch (NumberFormatException e) {
				LoggerImpl.getInstance().error(e);
			}
		}

	}

	private boolean isIdea() {
		return isIdea7 || isIdea8 || isIdea9 || isIdea9;
	}

	public static boolean isInstanceOfPsiDocToken(Object obj) {
		Class psiDocTokenClass = null;

		try {
			psiDocTokenClass = Class.forName("com.intellij.psi.javadoc.PsiDocToken");
		} catch (ClassNotFoundException e) {
			return false;
		}

		return psiDocTokenClass != null && psiDocTokenClass.isInstance(obj);
	}

	private static IdeaVersionFacade instance;

	public static IdeaVersionFacade getInstance() {
		if (instance == null) {
			instance = new IdeaVersionFacade();
		}
		return instance;
	}

	public int getAffectedVcsesSize(final CommitChangeListDialog dialog) {

		try {
			Class commitChangeListDialogClass = Class
					.forName("com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog");
			Method getAffectedVcsesMethod = commitChangeListDialogClass.getMethod("getAffectedVcses");
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) getAffectedVcsesMethod.invoke(dialog);

			if (list != null) {
				return list.size();
			} else {
				return 0;
			}
		} catch (final Exception e) {
			//for some IDEA X versions 96.X, 97.X, 98.X (preview, EAPs) this method is not available
			// but is present in stable version since 99.5
		}


		//CommitChangeListDialog.getAffectedVcsesImplement not implemented for Idea X EAP
		return 1;
	}

	public String getChangeListId(LocalChangeList changeList) {
		try {
			Class localChangeListClass = Class.forName("com.intellij.openapi.vcs.changes.LocalChangeListImpl");
			if (!isIdea7) {
				Method getIdMethod = localChangeListClass.getMethod("getId");
				return (String) getIdMethod.invoke(changeList);
			} else {
				Method getNameMethod = localChangeListClass.getMethod("getName");
				return (String) getNameMethod.invoke(changeList);

			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	public boolean psiClassNavigate(final Project project, String className) {
		Object psiClassObject = findPsiClass(className, project);
		if (psiClassObject != null) {
			try {
				Class psiClass = Class.forName("com.intellij.psi.PsiClass");
				Method navigateMethod = psiClass.getMethod("navigate", Boolean.TYPE);
				navigateMethod.invoke(psiClassObject, true);
			} catch (Exception e) {
				//e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;

	}

	public PsiFile getPsiClassNavigationElement(final Project project, final String name) {
		PsiFile navigationElement = null;
		try {
			Object cls = findPsiClass(name, project);

			if (cls == null) {
				return null;
			}
			Class psiClass = Class.forName("com.intellij.psi.PsiClass");
			Method getContainingFileMethod = psiClass.getMethod("getContainingFile");
			PsiFile psiFile = (PsiFile) getContainingFileMethod.invoke(cls);
			navigationElement = (PsiFile) psiFile.getNavigationElement();

		} catch (Exception e) {
			 return null;
		}
		return navigationElement;
	}

	public boolean beMethodConfiguraion(Project project, RunConfiguration config, Object psiMethod) {
		try {
			Class jUnitConfigurationClass = Class.forName("com.intellij.execution.junit.JUnitConfiguration");
			Method beMethodConfigurationMethod = jUnitConfigurationClass.getMethod("beMethodConfiguration", Location.class);
			beMethodConfigurationMethod.invoke(config, PsiLocation.fromPsiElement(project, (PsiElement) psiMethod));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	public boolean beClassConfiguration(RunConfiguration config, Object psiClass) {
		try {
			Class jUnitConfigurationClass = Class.forName("com.intellij.execution.junit.JUnitConfiguration");
			Class psiClassInterface = Class.forName("com.intellij.psi.PsiClass");
			Method beClassConfigurationMethod = jUnitConfigurationClass
					.getMethod("beClassConfiguration", psiClassInterface);
			beClassConfigurationMethod.invoke(config, psiClass);
			return true;
		} catch (Exception e) {
			return false;
		}


	}

	public boolean createTestConfiguration(RunConfiguration config, String packageName) {

		try {
			Class jUnitConfigurationClass = Class.forName("com.intellij.execution.junit.JUnitConfiguration");
			Class jUnitConfigurationDataClass = Class.forName("com.intellij.execution.junit.JUnitConfiguration.Data");
			Method getPersistentDataMethod = jUnitConfigurationClass.getMethod("getPersistentData");
			Object persitentData = getPersistentDataMethod.invoke(config);
			Field testPackageField = jUnitConfigurationDataClass.getField("TEST_PACKAGE");//static
			Field testObjectField = jUnitConfigurationDataClass.getField("TEST_OBJECT");
			Field packageNameField = jUnitConfigurationDataClass.getField("PACKAGE_NAME");

			//conf.getPersistentData().TEST_OBJECT = JUnitConfiguration.TEST_PACKAGE;
			testObjectField.set(persitentData, testPackageField.get(null));

			//conf.getPersistentData().PACKAGE_NAME = toString();
			testPackageField.set(persitentData, packageName);

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	/*
		* @returns PsiClass
		* */
	public Object findPsiClass(String name, Project project) {
		Object cls = null;
		try {
			if (isIdea8 || isIdea9 || isIdeaX) {
				Class javaPsiFacadeClass = Class.forName("com.intellij.psi.JavaPsiFacade");
				Method getInstance = javaPsiFacadeClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method findClass = javaPsiFacadeClass.getMethod("findClass", String.class, GlobalSearchScope.class);
				cls = findClass.invoke(inst, name, GlobalSearchScope.allScope(project));
			} else if (isIdea()) {
				Class psiManagerClass = Class.forName("com.intellij.psi.PsiManager");
				Method getInstance = psiManagerClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method findClass = psiManagerClass.getMethod("findClass", String.class, GlobalSearchScope.class);
				cls = findClass.invoke(inst, name, GlobalSearchScope.allScope(project));
			}
		} catch (Exception e) {
			//JAVAPSIFacade is not available
			return null;
		}
		return cls;
	}

	public void setEmptyText(CommittedChangesTreeBrowser tb, String text) {
		if (isIdea7 || isIdea8 || isIdea9) {
			try {
				Class commitedChagesTreeBrowserClass =
						Class.forName("com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser");
				Method setEmptyTextMethod = commitedChagesTreeBrowserClass.getMethod("seEmptyText", String.class);
				setEmptyTextMethod.invoke(tb, text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public PsiFile[] getFiles(String filePath, Project project) {
		PsiFile[] psiFiles = null;
		try {
			if (isIdea8 || isIdea9 || isIdeaX) {
				Class fileNameIndexClass = Class.forName("com.intellij.psi.search.FilenameIndex");
				Method getFilesByName = fileNameIndexClass.getMethod("getFilesByName", Project.class,
						String.class, GlobalSearchScope.class);
				Class projectScopeClass = Class.forName("com.intellij.psi.search.ProjectScope");
				Method getProjectScope = projectScopeClass.getMethod("getProjectScope", Project.class);
				GlobalSearchScope scope = (GlobalSearchScope) getProjectScope.invoke(null, project);
				psiFiles = (PsiFile[]) getFilesByName.invoke(null, project, filePath, scope);
			} else if (isIdea()) {
				Class psiManagerClass = Class.forName("com.intellij.psi.PsiManager");
				Method getInstance = psiManagerClass.getMethod("getInstance", Project.class);
				Object inst = getInstance.invoke(null, project);
				Method getShortNamesCache = psiManagerClass.getMethod("getShortNamesCache");
				Class psiShortNamesCacheClass = Class.forName("com.intellij.psi.search.PsiShortNamesCache");
				Object cacheInstance = getShortNamesCache.invoke(inst);
				Method getFilesByName = psiShortNamesCacheClass.getMethod("getFilesByName", String.class);
				psiFiles = (PsiFile[]) getFilesByName.invoke(cacheInstance, filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return psiFiles;
	}

	public Collection<VirtualFile> getLocalChangeListVirtualFiles(Project project, java.util.List<Change> selectedChanges) {
		final Collection<VirtualFile> vFiles = new ArrayList<VirtualFile>();
		try {
			int v = 0;
			if (!isIdea9 && !isIdeaX) {
				// there is no getBuild().asString() in IDEA 8.0 and older, so we need to use
				// deprecated getBuildNumber() method here...
				@SuppressWarnings("deprecation")
				String ver = ApplicationInfo.getInstance().getBuildNumber();
				v = Integer.parseInt(ver);
			}

			if (isIdeaX || isIdea9 || v > IDEA_8_0_1) {
				Class changeClass = Class.forName("com.atlassian.theplugin.commons.crucible.api.model.changes.Change");
				Method getVirtualFile = changeClass.getMethod("getVirtualFile");
				for (Change c : selectedChanges) {
					vFiles.add((VirtualFile) getVirtualFile.invoke(c));
				}
			} else {
				for (Change c : selectedChanges) {
					if (c.getAfterRevision() != null && c.getAfterRevision().getFile() != null) {
						vFiles.add(c.getAfterRevision().getFile().getVirtualFile());
					}


				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vFiles;
	}

	public Color getEditorBackgroundColor(EditorEx editor) {

		Class editorClass = null;
		try {
			editorClass = Class.forName("com.intellij.openapi.editor.ex.EditorEx");
			Method getBackgroundColorMethod;
			if (isIdeaX || !isIdea()) {
				getBackgroundColorMethod = editorClass.getMethod("getBackgroundColor");
			} else {
				getBackgroundColorMethod = editorClass.getMethod("getBackroundColor");
			}
			return (Color) getBackgroundColorMethod.invoke(editor);
		} catch (Exception e) {
			e.printStackTrace();
			return Color.WHITE;
		}

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
			int v = 0;
			if (!isIdea9 && !isIdeaX && !isIdea()) {
				// there is no getBuild().asString() in IDEA 8.0 and older, so we need to use
				// deprecated getBuildNumber() method here...
				@SuppressWarnings("deprecation")
				String ver = ApplicationInfo.getInstance().getBuildNumber();
				v = Integer.parseInt(ver);
			}
			Class browserClass = Class.forName("com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser");
			Constructor[] constructors = browserClass.getConstructors();
			if (isIdeaX || isIdea9 || v >= IDEA_8_1_3 || !isIdea()) {
				return (MultipleChangeListBrowser) constructors[0]
						.newInstance(project, changeListManager.getChangeLists(),
								changeList, changeListManager.getDefaultChangeList(), true, true, null, null);
			} else if (v > IDEA_8_0_1) {
				return (MultipleChangeListBrowser) constructors[0]
						.newInstance(project, changeListManager.getChangeLists(),
								changeList, null, true, true, null);
			} else {
				return (MultipleChangeListBrowser) constructors[0]
						.newInstance(project, changeListManager.getChangeLists(),
								changeList, null, true, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addActionToDiffGroup(@NotNull AnAction action) {
		if (isIdea8 || isIdea9 || isIdeaX || !isIdea()) {
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
			if (isIdea8 || isIdea9 || isIdeaX || !isIdea()) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public List buildPatch(Project project, Collection<Change> changes, String basePresentalbeUrl)
			throws PatchCreateErrorException {
		List list = null;
		try {
			if (isIdea8()) {
				Class patchBuilderClass = Class.forName("com.intellij.openapi.diff.impl.patch.PatchBuilder");
				Method buildPatchMethod = patchBuilderClass
						.getMethod("buildPatch", Collection.class, String.class, boolean.class, boolean.class);
				list = (List) buildPatchMethod.invoke(null, changes, basePresentalbeUrl, false, false);
				return list;
			} else if (isIdea9()) {
				Class patchBuilderClass = Class.forName("com.intellij.openapi.diff.impl.patch.TextPatchBuilder");
				Method buildPatchMethod = patchBuilderClass
						.getMethod("buildPatch", Collection.class, String.class, boolean.class);
				list = (List) buildPatchMethod.invoke(null, changes, basePresentalbeUrl, false);
				return list;

			} else if (!isIdea7() && !isIdea8()) {
				Class patchBuilderClass = Class.forName("com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder");
				Method buildPatchMethod = patchBuilderClass
						.getMethod("buildPatch", Project.class, Collection.class, String.class, boolean.class);
				list = (List) buildPatchMethod.invoke(null, project, changes, basePresentalbeUrl, false);
				return list;
			}
		} catch (Exception e) {
			PluginUtil.getLogger().error("Cannot create patch", e.getCause());
			throw new PatchCreateErrorException("Cannot create patch", e.getCause());
		}
		return null;
	}

	public void runTests(RunnerAndConfigurationSettings settings, DataContext dataContext, boolean debug) {
		try {
			if (isIdea8 || isIdea9 || isIdeaX) {
				Class executorClass = Class.forName("com.intellij.execution.Executor");
				Class defaultDebugExecutorClass = Class
						.forName("com.intellij.execution.executors.DefaultDebugExecutor");
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
						Object executionEnvironment = c.newInstance(runner, settings, dataContext);
						Method execute = runnerClass.getMethod("execute", executorClass, executionEnvironmentClass);
						execute.invoke(runner, executor, executionEnvironment);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} else if (!isIdea()) {
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
					execute.invoke(strategyInstance, settings, runner, dataContext);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BinaryContent createBinaryContent(@NotNull final VirtualFile virtualFile) {
		BinaryContent content = null;
		FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(virtualFile);
		try {
			Class binaryContentClass = Class.forName("com.intellij.openapi.diff.BinaryContent");
			Constructor constructor = binaryContentClass
					.getConstructor(new Class[]{byte[].class,
							isIdea8 || isIdea9 ? Charset.class : String.class, FileType.class});
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
			if (isIdea8 || isIdea9 || isIdeaX || !isIdea()) {
				Class enumClass = Class
						.forName("com.intellij.openapi.vcs.changes.committed.CommittedChangesBrowserUseCase");
				Method valueOf = enumClass.getMethod("valueOf", String.class);
				if (!isIdeaX) {
					setItems.invoke(browser, list, flag, valueOf.invoke(null, "COMMITTED"));
				} else {
					setItems.invoke(browser, list, valueOf.invoke(null, "COMMITTED"));
				}
			} else {
				setItems.invoke(browser, list, flag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public enum OperationStatus {
		INFO,
		WARNING,
		ERROR
	}

	public void fireNotification(final Project project, final JComponent content, String message,
			String iconName, OperationStatus status, final Color color) {
/*
		if (isIdea8 || isIdea9) {
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
					notifyByBalloon.getBuilds(toolWindowManager,
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final WindowManager windowManager = WindowManager.getInstance();
				if (windowManager != null) {
					final StatusBar statusBar = windowManager.getStatusBar(project);
					if (statusBar != null) {
						statusBar.fireNotificationPopup(content, color);
					}
				}
			}
		});
//		}
	}

	public boolean isIdea7() {
		return isIdea7;
	}

	public boolean isIdea8() {
		return isIdea8;
	}

	public boolean isIdea9() {
		return isIdea9;
	}

	public boolean isIdeaX() {
		return isIdeaX;
	}

	public boolean isCommunityEdition() {
		return communityEdition;
	}

	public boolean openStackTrace(@NotNull Project project, String stracktrace, String title) {
		if (isIdea8 || isIdea9 || isIdeaX || !isIdea()) {
			try {
				final Class<?> analyzeStackTraceUtil = Class.forName("com.intellij.unscramble.AnalyzeStacktraceUtil");
				for (Method method : analyzeStackTraceUtil.getMethods()) {
					if ("addConsole".equals(method.getName())) {
						final ConsoleView consoleView = (ConsoleView) method.invoke(null, project, null, title);
						final Method printStackTraceMethod
								= analyzeStackTraceUtil.getMethod("printStacktrace", ConsoleView.class, String.class);
						printStackTraceMethod.invoke(null, consoleView, stracktrace);
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				final ConsoleView consoleView = createConsoleView(project, title);
				printStacktrace(consoleView, stracktrace);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		return false;
	}


	public static void printStacktrace(final ConsoleView consoleView, final String unscrambledTrace) {
		consoleView.clear();
		consoleView.print(unscrambledTrace + "\n", ConsoleViewContentType.ERROR_OUTPUT);
		consoleView.performWhenNoDeferredOutput(
				new Runnable() {
					public void run() {
						consoleView.scrollTo(0);
					}
				}
		);
	}


	private static ConsoleView createConsoleView(Project project, String tabTitle) throws Exception {
		ConsoleView consoleview = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
		// this code as reflections (sucks terribly, but as a guest developer I have no time to improve m2 build
		// infrastructure for the whole project to support IntelliJ version specific dependencies
		//
		//  JavaProgramRunner runner = ExecutionRegistry.getInstance().getDefaultRunner();
		//
		final Class<?> executionRegistryClass = Class.forName("com.intellij.execution.ExecutionRegistry");
		final Method getInstanceMethod = executionRegistryClass.getMethod("getInstance");
		final Object executionRegistry = getInstanceMethod.invoke(null);
		final Method getDefaultRunnerMethod = executionRegistryClass.getMethod("getDefaultRunner");
		final Object javaProgramRunner = getDefaultRunnerMethod.invoke(executionRegistry);

		DefaultActionGroup toolbarActions = new DefaultActionGroup();
		MyConsolePanel consoleComponent = new MyConsolePanel(consoleview, toolbarActions);
		RunContentDescriptor descriptor = new RunContentDescriptor(consoleview, null, consoleComponent, tabTitle) {
			public boolean isContentReuseProhibited() {
				return true;
			}
		};

		// such code as reflections
		// final CloseAction closeAction = new CloseAction(runner, descriptor, project);
		//
		final Class<?> closeActionClass = Class.forName("com.intellij.execution.ui.CloseAction");
		final Class<?> javaProgramRunnerClass = Class.forName("com.intellij.execution.runners.JavaProgramRunner");

		final Constructor<?> constructor = closeActionClass.getConstructor(
				javaProgramRunnerClass, RunContentDescriptor.class, Project.class);
		final AnAction closeAction = (AnAction) constructor.newInstance(javaProgramRunner, descriptor, project);

		// such code as reflections
		// consoleview.createUpDownStacktraceActions();
		//
		toolbarActions.add(closeAction);
		final Method createUpDownStacktraceActionsMethod = ConsoleView.class.getMethod("createUpDownStacktraceActions");
		AnAction[] defaultActions = (AnAction[]) createUpDownStacktraceActionsMethod.invoke(consoleview);
		for (AnAction action : defaultActions) {
			toolbarActions.add(action);
		}

		// as reflections
		// ExecutionManager.getInstance(project).getContentManager().showRunContent(runner, descriptor);
		final Method showRunContentMethod = RunContentManager.class.getMethod("showRunContent",
				javaProgramRunnerClass, RunContentDescriptor.class);
		showRunContentMethod
				.invoke(ExecutionManager.getInstance(project).getContentManager(), javaProgramRunner, descriptor);

		return consoleview;
	}

	private static final class MyConsolePanel extends JPanel {
		public MyConsolePanel(ExecutionConsole consoleView, ActionGroup toolbarActions) {
			super(new BorderLayout());
			JPanel toolbarPanel = new JPanel(new BorderLayout());
			toolbarPanel.add(ActionManager.getInstance()
					.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).getComponent());
			add(toolbarPanel, BorderLayout.WEST);
			add(consoleView.getComponent(), BorderLayout.CENTER);
		}
	}

}
