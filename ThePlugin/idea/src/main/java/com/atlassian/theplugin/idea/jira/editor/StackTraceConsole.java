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

package com.atlassian.theplugin.idea.jira.editor;

import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;

import java.util.HashMap;

public final class StackTraceConsole {

	private static final String TOOL_WINDOW_TITLE = "JIRA Stack Traces";

	private static StackTraceConsole instance = new StackTraceConsole();

	private HashMap<String, ConsoleView> consoleMap = new HashMap<String, ConsoleView>();

	private StackTraceConsole() {
	}

	public static StackTraceConsole getInstance() {
		return instance;
	}

	public void print(JIRAIssue issue, String key, String txt) {
		ConsoleView console = setupConsole(issue.getKey(), key);
		if (console != null) {
			console.clear();
			console.print(txt, ConsoleViewContentType.NORMAL_OUTPUT);
		}
	}

	private ConsoleView setupConsole(String issueKey, String origin) {
		ConsoleView console;
		String contentKey = issueKey + " " + origin;

		ToolWindowManager twm = ToolWindowManager.getInstance(IdeaHelper.getCurrentProject());
		ToolWindow consoleToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (consoleToolWindow == null) {
			consoleToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			consoleToolWindow.setIcon(IconLoader.getIcon("/icons/tab_jira.png"));
		}

		Content content = consoleToolWindow.getContentManager().findContent(contentKey);

		if (content != null) {
			console = consoleMap.get(contentKey);
		} else {
			TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
			TextConsoleBuilder builder = factory.createBuilder(IdeaHelper.getCurrentProject());
			console = builder.getConsole();
			consoleMap.remove(contentKey);
			consoleMap.put(contentKey, console);

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(console.getComponent(), contentKey, true);
			content.setIcon(IconLoader.getIcon("/icons/tab_jira.png"));
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			consoleToolWindow.getContentManager().addContent(content);
		}
		consoleToolWindow.getContentManager().setSelectedContent(content);
		consoleToolWindow.show(null);

		return console;
	}
}
