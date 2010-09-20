package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.xstream.JDomProjectConfigurationDao;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: kalamon
 * Date: 2010-09-20
 * Time: 13:48:41
 */
public class PasswordStorage {

    private static Boolean haveSafe = null;

    public static void loadPasswordsFromSecureStore(
            Project project, ProjectConfiguration configuration, JDomProjectConfigurationDao cfgFactory) {

        if (!hasPasswordSafe()) {
            return;
        }
        
        for (ServerCfg server : configuration.getServers()) {
            if (!server.isPasswordStored()) {
                continue;
            }
            String password = load(project, server.getServerId().toString());
            if (password != null) {
                server.setPassword(password);
            } else {
                if (server.getPassword() == null) {
                    server.setPassword("");
                }
            }
        }
        // save back to migrate passwords
        savePasswordsToSecureStore(project, configuration, cfgFactory);
    }

    public static void savePasswordsToSecureStore(
            Project project, ProjectConfiguration configuration, JDomProjectConfigurationDao cfgFactory) {

        if (!hasPasswordSafe()) {
            cfgFactory.save(configuration);
            return;
        }

        Map<String, String> tempPasswordMap = new HashMap<String, String>();
        for (ServerCfg server : configuration.getServers()) {
            if (server.isPasswordStored()) {
                store(project, server.getServerId().toString(), server.getPassword());
            } else {
                remove(project, server.getServerId().toString());
            }
            tempPasswordMap.put(server.getServerId().toString(), server.getPassword());

            // blank out password for storing configs in the config files. Real passwords are now kept in PasswordSafe
            server.setPassword("");
        }

        cfgFactory.save(configuration);

        for (ServerCfg server : configuration.getServers()) {
            server.setPassword(tempPasswordMap.get(server.getServerId().toString()));
        }
    }

    private static final String IDEA_9_REGEX_STRING = "((IU)|(IC))-(\\d+)\\.(\\d+)";
    private static final Pattern IDEA_9_REGEX = Pattern.compile(IDEA_9_REGEX_STRING);
    private static final int IDEA_9_GR4 = 95;
    private static final int IDEA_903_GR5 = 429;

    private static boolean hasPasswordSafe() {
        if (haveSafe != null) {
            return haveSafe;
        }

        // there is no getBuild().asString() in IDEA 8.0 and older, so we need to use
        // deprecated getBuildNumber() method here...
        @SuppressWarnings("deprecation")
        String ver = ApplicationInfo.getInstance().getBuildNumber();
        Matcher m = IDEA_9_REGEX.matcher(ver);

        final int group4 = m.matches() && m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;

        if (m.matches() && group4 == IDEA_9_GR4) {
            final int group5 = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
            haveSafe = group5 >= IDEA_903_GR5;
        } else {
            haveSafe = m.matches() && group4 > IDEA_9_GR4;
        }

        return haveSafe;
    }

    private static void store(Project project, String key, String value) {
        try {
            // PasswordSafe.getInstance().storePassword(project, PasswordStorage.class, key, value);
            Object safe = getPasswordSafeInstance();
            Method storePassword = safe.getClass().getMethod(
                    "storePassword", Project.class, Class.class, String.class, String.class);
            storePassword.invoke(safe, project, PasswordStorage.class, key, value);
        } catch (Exception e) {
            LoggerImpl.getInstance().error(e);
        }
    }

    private static String load(Project project, String key) {
        try {
            // PasswordSafe.getInstance().getPassword(project, PasswordStorage.class, key);
            Object safe = getPasswordSafeInstance();
            Method getPassword = safe.getClass().getMethod("getPassword", Project.class, Class.class, String.class);
            return getPassword.invoke(safe, project, PasswordStorage.class, key).toString();
        } catch (Exception e) {
            LoggerImpl.getInstance().error(e);
        }
        return null;
    }

    private static void remove(Project project, String key) {
        try {
            // PasswordSafe.getInstance().removePassword(project, PasswordStorage.class, key);
            Object safe = getPasswordSafeInstance();
            Method removePassword = safe.getClass().getMethod("removePassword", Project.class, Class.class, String.class);
            removePassword.invoke(safe, project, PasswordStorage.class, key);
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
