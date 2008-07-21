package com.atlassian.theplugin.commons.crucible.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jul 21, 2008
 * Time: 3:48:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleVersionInfoBean implements CrucibleVersionInfo {
    private String buildDate;

    private String releaseNumber;

    public CrucibleVersionInfoBean() {
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getReleaseNumber() {
        return releaseNumber;
    }

    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
    }
}
