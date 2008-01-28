package com.atlassian.theplugin.configuration;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jan 24, 2008
 * Time: 1:34:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RequestData {
	void setPollingTime(Date date);

	Date getPollingTime();

}
