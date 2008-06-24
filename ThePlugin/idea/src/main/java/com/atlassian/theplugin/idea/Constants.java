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

package com.atlassian.theplugin.idea;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class Constants {
	private Constants() { }

	public static final int DIALOG_MARGIN = 12;

	public static final int BG_COLOR_R = 238;

	public static final int BG_COLOR_G = 238;

	public static final int BG_COLOR_B = 238;
	public static final String HELP_URL_BASE     = "theplugin.help.url.prefix";
	public static final String HELP_CONFIG_PANEL = "theplugin.config";
	public static final String HELP_BAMBOO       = "theplugin.bamboo";

	public static final String HELP_CRUCIBLE     = "theplugin.crucible";
	public static final String HELP_JIRA         = "theplugin.jira";
	public static final String HELP_JIRA_WORKLOG = "theplugin.jira.worklog";
	public static final String HELP_TEST_CONNECTION = "theplugin.testconnection";
	public static final Icon HELP_ICON = IconLoader.getIcon("/actions/help.png");
}
