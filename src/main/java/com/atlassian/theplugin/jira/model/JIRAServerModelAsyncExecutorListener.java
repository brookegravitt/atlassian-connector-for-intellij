package com.atlassian.theplugin.jira.model;

import java.util.List;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:52:23 PM
 */
public interface JIRAServerModelAsyncExecutorListener<T> {
	void finished(List<T> results);
}
