package com.atlassian.theplugin.idea;

import com.intellij.execution.*;
//import com.intellij.execution.impl.RunDialog;
//import com.intellij.execution.runners.ProgramRunner;
//import com.intellij.execution.runners.ExecutionEnvironment;
//import com.intellij.execution.executors.DefaultDebugExecutor;
//import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.LightweightHint;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class IdeaVersionFacade {

	private static final int IDEA_8_0 = 8000;
    private boolean isIdea8;
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
			}  else {
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

    public void showEditorHints(LightweightHint lightweightHint, Editor anEditor, Point point) {
	    try {
//			if (isIdea8) {
//				// TODO: do it after 8.0 final. Right now the code in Diana EAP is unstable and a moving target
//			}

			if (!isIdea8) {
				Class hintManagerClass = Class.forName("com.intellij.codeInsight.hint.HintManager");
				Method getInstance = hintManagerClass.getMethod("getInstance");
				Object inst = getInstance.invoke(null);
				Method showEditorHint = hintManagerClass.getMethod("showEditorHint",
						LightweightHint.class, Editor.class, Point.class, int.class, int.class, boolean.class);
				Field hideByAnyKey = hintManagerClass.getField("HIDE_BY_ANY_KEY");
				Field hideByTextChange = hintManagerClass.getField("HIDE_BY_TEXT_CHANGE");
				Field hideByOtherHint = hintManagerClass.getField("HIDE_BY_OTHER_HINT");
				Field hideByScrolling = hintManagerClass.getField("HIDE_BY_SCROLLING");
				int hbak = (Integer) hideByAnyKey.get(null);
				int hbtc = (Integer) hideByTextChange.get(null);
				int hboh = (Integer) hideByOtherHint.get(null);
				int hbs = (Integer) hideByScrolling.get(null);

				showEditorHint.invoke(inst, lightweightHint, anEditor, point, hbak | hbtc | hboh | hbs, -1, false);
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

    public void runTests(RunnerAndConfigurationSettings settings, AnActionEvent ev, boolean debug) {
	    try {
//			if (isIdea8) {
//                Executor executor;
//                if (debug) {
//                    executor = ExecutorRegistry.getInstance().getExecutorById(DefaultDebugExecutor.EXECUTOR_ID);
//                } else {
//                    executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
//                }
//
//                final RunDialog dialog = new RunDialog(IdeaHelper.getCurrentProject(ev), executor);
//                dialog.show();
//                if (!dialog.isOK()) return;
//
//                ProgramRunner runner = RunnerRegistry.getInstance().getRunner(executor.getId(), settings.getConfiguration());
//                if (runner != null) {
//                    try {
//                        runner.execute(executor, new ExecutionEnvironment(runner, settings, ev.getDataContext()));
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//			}

			if (!isIdea8) {
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
        }
    }

	public void setTestMethod(RunConfiguration configuration, Location<PsiMethod> location) {
		JUnitConfiguration conf = (JUnitConfiguration) configuration;
		conf.beMethodConfiguration(location);
	}

	public void setTestClass(RunConfiguration configuration, PsiClass cls) {
		JUnitConfiguration conf = (JUnitConfiguration) configuration;
		conf.beClassConfiguration(cls);
	}
}
