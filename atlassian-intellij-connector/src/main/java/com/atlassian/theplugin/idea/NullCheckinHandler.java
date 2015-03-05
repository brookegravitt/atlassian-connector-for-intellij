package com.atlassian.theplugin.idea;

import com.intellij.openapi.vcs.checkin.CheckinHandler;

/**
 * User: kalamon
 * Date: Jun 16, 2009
 * Time: 12:13:27 PM
 *
 * The reason for this class is that CheckinHandler is abstract, but does not define any abstract methods. Go figure
 */
public class NullCheckinHandler extends CheckinHandler {
}
