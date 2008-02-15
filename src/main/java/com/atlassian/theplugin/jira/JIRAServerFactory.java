package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.bamboo.BambooServerFacade;
import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 5:17:44 PM
 * To change this template use File | Settings | File Templates.
 */
public final class JIRAServerFactory
{
	private static JIRAServerFacade facade = new JIRAServerFacadeImpl();

	private JIRAServerFactory() {
	}

	public static JIRAServerFacade getJIRAServerFacade() {
		return facade;
	}
}