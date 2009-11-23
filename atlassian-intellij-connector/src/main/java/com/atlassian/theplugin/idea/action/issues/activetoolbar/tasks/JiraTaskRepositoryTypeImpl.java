package com.atlassian.theplugin.idea.action.issues.activetoolbar.tasks;

import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * User: pmaruszak
 */
public class JiraTaskRepositoryTypeImpl extends TaskRepositoryTypeImpl {    
    private static final String JIRA_REPOSITORY_TYPE_CLASS = "com.intellij.tasks.JiraRepositoryType";
    private Object jiraRepositoryTypeObj;
    private final ClassLoader classLoader;
    private Class jiraRepositoryTypeClass;

    public JiraTaskRepositoryTypeImpl(Object repositoryTypeObj, ClassLoader classLoader) {
        super(repositoryTypeObj, classLoader);
        
        this.jiraRepositoryTypeObj = repositoryTypeObj;
        this.classLoader = classLoader;
        try {
            jiraRepositoryTypeClass = classLoader.loadClass(JIRA_REPOSITORY_TYPE_CLASS);
        } catch (ClassNotFoundException e) {
            PluginUtil.getLogger().error("Cannot load class " + JIRA_REPOSITORY_TYPE_CLASS);
        }
    }



    @Nullable
    public JiraRepository createRepository() {
        if (jiraRepositoryTypeClass == null) {
            return null;
        }

        try {
            Method createRepository = null;
            createRepository = jiraRepositoryTypeClass.getMethod("createRepository");
            return new JiraRepository(createRepository.invoke(jiraRepositoryTypeObj), classLoader);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot create repository", e);
        }

        return null;
    }

    @Override
   public List<TaskRepository> getRepositories() {
        List<TaskRepository> repositories = new ArrayList<TaskRepository>();

        if (jiraRepositoryTypeClass == null) {
            return repositories;
        }

        try {
            Method getRepositories = null;
            getRepositories = jiraRepositoryTypeClass.getMethod("getRepositories");
            List jiraRepos = (List) getRepositories.invoke(jiraRepositoryTypeObj);
            for (Object r : jiraRepos) {
                repositories.add(new JiraRepository(r, classLoader));
            }
        } catch (Exception e) {
            PluginUtil.getLogger().error("Cannot get repositories", e);
        }

        return repositories;
    }

    public Object getRepositoryTypeObj() {
       return jiraRepositoryTypeObj;
   }

   public Class getTaskRepositoryTypeClass() {
       return jiraRepositoryTypeClass;
   }
}
