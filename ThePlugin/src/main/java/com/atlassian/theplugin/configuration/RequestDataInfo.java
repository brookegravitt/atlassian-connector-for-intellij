package com.atlassian.theplugin.configuration;

import java.util.Date;

public class RequestDataInfo implements RequestData {
	private Date pollingTime = new Date(0);

	public Date getPollingTime() {
		return (Date) pollingTime.clone();
	}

	public void setPollingTime(Date date) {
		pollingTime = (Date) date.clone();
	}

}
