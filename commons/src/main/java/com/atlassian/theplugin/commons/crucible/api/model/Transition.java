package com.atlassian.theplugin.commons.crucible.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jun 14, 2008
 * Time: 9:06:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Transition {
    State getState();

    State getNextState();

    String getActionName();

    String getDisplayName();
}
