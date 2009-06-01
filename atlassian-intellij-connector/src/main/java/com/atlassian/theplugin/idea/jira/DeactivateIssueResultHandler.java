package com.atlassian.theplugin.idea.jira;

/**
 * User: kalamon
* Date: Jun 1, 2009
* Time: 2:14:05 PM
*/
public interface DeactivateIssueResultHandler {
    void success();
    void failure(Throwable problem);
}
