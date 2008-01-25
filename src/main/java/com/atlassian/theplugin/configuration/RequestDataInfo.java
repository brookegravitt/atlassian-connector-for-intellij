package com.atlassian.theplugin.configuration;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jan 24, 2008
 * Time: 1:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestDataInfo implements RequestData {
    private Date lastPoolingTime = new Date(0);

    public Date getPoolingTime() {
        return lastPoolingTime;
    }

    public void setPoolingTime(Date date) {
        lastPoolingTime = date;
    }

}
