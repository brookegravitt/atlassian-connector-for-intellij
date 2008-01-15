package com.atlassian.theplugin.bamboo;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:50:29 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BambooDisplayComponent {
    void updateBambooStatus(String statusIcon, String fullInfo);
}
