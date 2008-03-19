package com.atlassian.theplugin.idea;

import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 19, 2008
 * Time: 10:27:32 AM
 * To change this template use File | Settings | File Templates.
 */

public abstract class Logger {

    /**
     * For backward compatibility, support the overriding the factory
     * with a singleton instance of the logger.
     */
    private static Logger singleton = null;
	public static final String LOGGER_CATEGORY = "com.atlassian.theplugin";


	public boolean isDebugEnabled() {
		return debug;
	}

	public interface Factory {
        Logger getLoggerInstance(String category);
    }

    private static Factory factoryInstance = new Factory() {
        public Logger getLoggerInstance(String category) {
            return new DefaultLogger();
        }
    };

    public static Logger getInstance(String category) {
        if (singleton != null) {
            return singleton;
        }
        return factoryInstance.getLoggerInstance(category);
    }

    public static Logger getInstance() {
        return getInstance(LOGGER_CATEGORY);
    }

    public static void setFactory(Factory factory) {
        factoryInstance = factory;
    }

    private static boolean debug = false;

    private static boolean verbose = false;

    /**
     * If set, this instance will be returned by all requests made to getInstance,
     * overriding the factory implementation and thereby providing backward
     * compatibility.
     *
     * @param instance
     */
    public static void setInstance(Logger instance) {
        singleton = instance;
    }

    protected Logger() {
    }

    //these values mirror Ant's values
    public static final int LOG_ERR = 0;
    public static final int LOG_WARN = 1;
    public static final int LOG_INFO = 2;
    public static final int LOG_VERBOSE = 3;
    public static final int LOG_DEBUG = 4;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
    }

    public static boolean isDebug() {
        return Logger.debug;
    }

    public static boolean isVerbose() {
        return Logger.verbose;
    }

    public static void setVerbose(boolean verbose) {
        Logger.verbose = verbose;
    }

    public void error(String msg) {
        log(LOG_ERR, msg, null);
    }

    public void error(String msg, Throwable t) {
        log(LOG_ERR, msg, t);
    }

    public void error(Throwable t) {
        log(LOG_ERR, (t != null) ? t.getMessage() : "Exception", t);
    }

    public void warn(String msg) {
        log(LOG_WARN, msg, null);
    }

    public void warn(String msg, Throwable t) {
        log(LOG_WARN, msg, t);
    }

    public void warn(Throwable t) {
        log(LOG_WARN, (t != null) ? t.getMessage() : "Exception", t);
    }

	public void error(@NonNls String message, Throwable t, @NonNls String... details) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void info(String msg) {
        log(LOG_INFO, msg, null);
    }

    public void info(String msg, Throwable t) {
        log(LOG_INFO, msg, t);
    }

    public void info(Throwable t) {
        log(LOG_INFO, (t != null) ? t.getMessage() : "Exception", t);
    }



	public void verbose(String msg) {
        log(LOG_VERBOSE, msg, null);
    }

    public void verbose(String msg, Throwable t) {
        log(LOG_VERBOSE, msg, t);
    }

    public void verbose(Throwable t) {
        log(LOG_VERBOSE, (t != null) ? t.getMessage() : "Exception", t);
    }

    public void debug(String msg) {
        log(LOG_DEBUG, msg, null);
    }

    public void debug(String msg, Throwable t) {
        log(LOG_DEBUG, msg, t);
    }

    public void debug(Throwable t) {
        log(LOG_DEBUG, (t != null) ? t.getMessage() : "Exception", t);
    }

    public static boolean canIgnore(int level) {
        if (!debug && (level == LOG_DEBUG)) {
            return true;
        }
        return !(verbose || debug) && (level == LOG_VERBOSE);
    }

    public abstract void log(int level, String msg, Throwable t);

    static class NullLogger /*extends Logger*/ {

        public void log(int level, String msg, Throwable t) {
            //no-op
        }
	}
}


