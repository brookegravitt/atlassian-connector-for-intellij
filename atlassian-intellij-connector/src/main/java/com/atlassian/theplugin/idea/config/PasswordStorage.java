package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationDao;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * User: kalamon
 * Date: 2010-09-20
 * Time: 13:48:41
 */
public class PasswordStorage {

    private static Boolean haveSafe = null;

    public static void loadPasswordsFromSecureStore(
            Project project, Project defaultProject, ProjectConfiguration configuration, JDomProjectConfigurationDao cfgFactory) {

        if (!hasPasswordSafe()) {
            return;
        }
        
        for (ServerCfg server : configuration.getServers()) {
//            if (!server.isPasswordStored()) {
//                continue;
//            }
            String password = load(project, defaultProject, server.getServerId().toString());
            if (password != null) {
                server.setPassword(password);
            } else {
                if (server.getPassword() == null) {
                    server.setPassword("");
                }
            }
        }
        // save back to migrate passwords
//        savePasswordsToSecureStore(project, defaultProject, configuration, cfgFactory);
    }

    public static void savePasswordsToSecureStore(
            Project project, Project defaultProject, ProjectConfiguration configuration, JDomProjectConfigurationDao cfgFactory) {

        if (!hasPasswordSafe()) {
            cfgFactory.save(configuration);
            return;
        }

        Map<String, String> tempPasswordMap = new HashMap<String, String>();
        for (ServerCfg server : configuration.getServers()) {
//            if (server.isPasswordStored()) {
                store(project, defaultProject, server.getServerId().toString(), server.getPassword());
//            } else {
//                remove(project, defaultProject, server.getServerId().toString());
//            }
            tempPasswordMap.put(server.getServerId().toString(), server.getPassword());

            // blank out password for storing configs in the config files. Real passwords are now kept in PasswordSafe
            server.setPassword("");
        }

        cfgFactory.save(configuration);

        for (ServerCfg server : configuration.getServers()) {
            server.setPassword(tempPasswordMap.get(server.getServerId().toString()));
        }
    }

    public static boolean setPassword(Project project, String password) {
        if (!hasPasswordSafe() || project == null) {
            return false;
        }
        store(project, null, project.getLocationHash(), password);
        return true;
    }

    public static String getPassword(Project project) {
        if (!hasPasswordSafe() || project == null) {
            return null;
        }
        return load(project, null, project.getLocationHash());
    }

    private static boolean hasPasswordSafe() {
        IdeaVersionFacade versionFacade = IdeaVersionFacade.getInstance();
        return versionFacade.hasPasswordSafe();
    }

    private static void store(Project project, @Nullable Project defaultProject, String key, String value) {
        try {
            // PasswordSafe.getInstance().storePassword(project, PasswordStorage.class, key, value);
            Object safe = getPasswordSafeInstance();
            Method storePassword = safe.getClass().getMethod(
                    "storePassword", Project.class, Class.class, String.class, String.class);
            if (defaultProject != null) {
                storePassword.invoke(safe, defaultProject, PasswordStorage.class, key, value);
            } else {
                storePassword.invoke(safe, project, PasswordStorage.class, key, value);
            }
        } catch (Exception e) {
            LoggerImpl.getInstance().error(e);
        }
    }

    private static String load(Project project, @Nullable Project defaultProject, String key) {
        try {
            // PasswordSafe.getInstance().getPassword(project, PasswordStorage.class, key);
            Object safe = getPasswordSafeInstance();
            Method getPassword = safe.getClass().getMethod("getPassword", Project.class, Class.class, String.class);
            if (getPassword != null) {
                String pwd = "";
                Object o = null;
                if (defaultProject != null) {
                    o = getPassword.invoke(safe, defaultProject, PasswordStorage.class, key);
                } else {
                    o = getPassword.invoke(safe, project, PasswordStorage.class, key);
                }
                pwd = o != null ? o.toString() : null;
                return pwd;
            } else {
                return null;
            }
        } catch (Exception e) {
            LoggerImpl.getInstance().error(e);
        }
        return null;
    }

    private static void remove(Project project, Project defaultProject, String key) {
        try {
            // PasswordSafe.getInstance().removePassword(project, PasswordStorage.class, key);
            Object safe = getPasswordSafeInstance();
            Method removePassword = safe.getClass().getMethod("removePassword", Project.class, Class.class, String.class);
            removePassword.invoke(safe, project, PasswordStorage.class, key);
            if (defaultProject != null) {
                removePassword.invoke(safe, defaultProject, PasswordStorage.class, key);
            }
        } catch (Exception e) {
            LoggerImpl.getInstance().error(e);
        }
    }

    private static Object getPasswordSafeInstance()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class aClass = Class.forName("com.intellij.ide.passwordSafe.PasswordSafe");
        Method getInstance = aClass.getMethod("getInstance");
        return getInstance.invoke(null);
    }
}
