package com.atlassian.theplugin.idea.jira;

/**
 * User: kalamon
* Date: Jun 1, 2009
* Time: 2:14:05 PM
*/
public interface ActiveIssueResultHandler {
    void success();
    void failure(Throwable problem);
    void cancel(String problem);
}
