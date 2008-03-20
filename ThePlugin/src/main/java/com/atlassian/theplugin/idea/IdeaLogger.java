package com.atlassian.theplugin.idea;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 17, 2008
 * Time: 4:40:34 PM
 */
public class IdeaLogger extends Logger {

    private com.intellij.openapi.diagnostic.Logger ideaLog;

    public IdeaLogger(com.intellij.openapi.diagnostic.Logger ideaLog) {
        super();
        this.ideaLog = ideaLog;


	}


    public void log(int level, String aMsg, Throwable t) {

        switch (level) {
            case Logger.LOG_VERBOSE:
            case Logger.LOG_DEBUG:
                ideaLog.debug(aMsg);
                if (t != null) {
                    ideaLog.debug(t);
                }

				break;

            case Logger.LOG_INFO:
                ideaLog.info(aMsg);
                if (t != null) {
                    ideaLog.info(t);
                }
                break;

            case Logger.LOG_ERR:
                if (t != null) {
                    ideaLog.info("ERROR:" + aMsg, t);
                } else {
                    ideaLog.info("ERROR:" + aMsg);
                }
                break;

            default:
                ideaLog.debug("<unknown log level> " + aMsg);
                break;
        }
    }
}
