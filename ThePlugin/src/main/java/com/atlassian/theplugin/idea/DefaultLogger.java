package com.atlassian.theplugin.idea;


import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 17, 2008
 * Time: 4:44:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultLogger extends Logger {

    private static String[] LOG_LEVEL_STR = {
        "ERROR",
        "WARN",
        "INFO",
        "VERBOSE",
        "DEBUG"
    };

	public void log(int level, String msg, Throwable t) {
        if (canIgnore(level)) {
            return;
        }

        final PrintStream stream;
        final String decoratedMsg;
        if (level >= LOG_INFO ) {
            // for INFO or below level msgs, print the msg only, nothing else
            decoratedMsg = msg;
            //stream = System.out;
			stream = System.err;
		} else {
            // since we are obfuscating, mName shouldn't be printed.
            decoratedMsg = LOG_LEVEL_STR[level] + ": " + msg;
            stream = System.err;
        }
        stream.print(decoratedMsg);

        if (t != null && (isVerbose() || isDebug())) {
            System.err.println(msg + " : " + t.getMessage());
            t.printStackTrace();
        } else {
            stream.println();
        }
    }
}
