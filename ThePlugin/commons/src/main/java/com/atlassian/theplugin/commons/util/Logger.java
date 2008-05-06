package com.atlassian.theplugin.commons.util;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-05-02
 * Time: 12:24:36
 * To change this template use File | Settings | File Templates.
 */
public interface Logger {
	boolean isDebugEnabled();

	void error(String msg);

	void error(String msg, Throwable t);

	void error(Throwable t);

	void warn(String msg);

	void warn(String msg, Throwable t);

	void warn(Throwable t);

	void info(String msg);

	void info(String msg, Throwable t);

	void info(Throwable t);

	void verbose(String msg);

	void verbose(String msg, Throwable t);

	void verbose(Throwable t);

	void debug(String msg);

	void debug(String msg, Throwable t);

	void debug(Throwable t);

	void log(int level, String msg, Throwable t);
}
