/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.idea.PluginToolWindow;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

public final class Util {
	public static final String HTML_NEW_LINE = "<br />";

	private Util() {
	}


	/**
	 * Transforms provided text into simple HTML equivalent (multiline, honoring whitespaces). 
	 * @param text plain text to tranform into HTML
	 * @return tranformed text or <code>null</code> if <code>text</code> was null
	 */
	@Nullable
	public static String textToMultilineHtml(@Nullable String text) {
		if (text == null) {
			return null;
		}
		return StringEscapeUtils.escapeHtml(text).replace("\n", HTML_NEW_LINE).replace("  ", "&nbsp; ")
				.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp; ");
	}

	public static ServerType toolWindowPanelsToServerType(PluginToolWindow.ToolWindowPanels panel) throws ThePluginException {
		switch (panel) {
			case BUILDS:
				return ServerType.BAMBOO_SERVER;
			case ISSUES:
				return ServerType.JIRA_SERVER;
            case PULL_REQUESTS:
                return ServerType.STASH_SERVER;
			default:
				throw new ThePluginException("Unrecognized tool window type");
		}
	}
}
