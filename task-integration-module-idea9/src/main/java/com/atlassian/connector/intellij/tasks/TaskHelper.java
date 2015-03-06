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


    public static Object createJiraRepository() {

        JiraRepositoryType jiraRepositoryType = new JiraRepositoryType();
        return jiraRepositoryType.createRepository();
    }
}
