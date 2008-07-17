package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:38:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GeneralComment extends Comment {
	List<GeneralComment> getReplies();
}
