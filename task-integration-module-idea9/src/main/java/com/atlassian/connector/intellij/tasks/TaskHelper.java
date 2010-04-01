package com.atlassian.connector.intellij.tasks;

import com.intellij.openapi.project.Project;
import com.intellij.tasks.jira.JiraRepository;
import com.intellij.tasks.jira.JiraRepositoryType;
import org.jetbrains.annotations.Nullable;

/**
 * @user pmaruszak
 * @date Feb 2, 2010
 */
public final class TaskHelper {


//
//    @Nullable
//    public static ActivationProcessingManager getActivationProcessingThread(Project p) {
//        return getProjectComponent(p, ActivationProcessingManager.class);
//    }
    
    @Nullable
    public static PluginTaskManager getPluginTaskManager(final Project project) {
        return getProjectComponent(project, PluginTaskManager.class);
    }

    	@Nullable
	public static <T> T getProjectComponent(final Project project, final Class<T> clazz) {
		if (project == null || project.isDisposed()) {
			return null;
		}
		return clazz.cast(project.getPicoContainer().getComponentInstanceOfType(clazz));
	}


    public static Object findJiraTask(final JiraRepository repo, final String issueKey) {
        return repo.findTask(issueKey);
    }

//      @Nullable
//    public static Object findJiraTask(final Object repo, final String issueKey) {
//        try {
//            Class jiraRepoClass = Class.forName("com.intellij.tasks.jira.JiraRepository");
//            Method findTaskMethod = jiraRepoClass.getMethod("findTask", String.class);
//            return  findTaskMethod.invoke(repo, issueKey);
//        } catch (Exception e) {
//           PluginUtil.getLogger().error("Cannot create LocalTask " + e.getMessage());
//        }
//
//        return null;
//    }
    public static Object createJiraRepository() {
//        try {
//            Class jiraRepositoryTypeClass = Class.forName("com.intellij.tasks.jira.JiraRepositoryType");
//            Object jiraRepositoryTypeObj = jiraRepositoryTypeClass.newInstance();
//
//            if (jiraRepositoryTypeClass == null || jiraRepositoryTypeObj == null) {
//                return null;
//            }
//
//            Method createRepositoryMethod = jiraRepositoryTypeClass.getMethod("createRepository");
//            Object jiraRepositoryObj = createRepositoryMethod.invoke(jiraRepositoryTypeObj);
//
//             return jiraRepositoryObj;
//        } catch (Exception e) {
//            PluginUtil.getLogger().error("Cannot create repository", e);
//        }
//
//        return null;
        JiraRepositoryType jiraRepositoryType = new JiraRepositoryType();
        return jiraRepositoryType.createRepository();
    }
}
