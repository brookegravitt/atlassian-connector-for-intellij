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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.util.ClassMatcher;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BambooBuildToolWindow {
	private static final String TOOL_WINDOW_TITLE = "Bamboo Build";

	private final Project project;

	private Map<String, ConsoleView> consoleMap = new HashMap<String, ConsoleView>();

	public BambooBuildToolWindow(final Project project) {
		this.project = project;
	}

	public void fetchAndShowBuildLog(@NotNull final BambooBuild bambooBuild,
			@NotNull final ConsoleView consoleView) {

		consoleView.clear();
		consoleView.print("Fetching Bamboo Build Log from the server", ConsoleViewContentType.NORMAL_OUTPUT);

		Task.Backgroundable buildLogTask = new Task.Backgroundable(project, "Retrieving Build Log", false) {
			@Override
			public void run(final ProgressIndicator indicator) {
//				ApplicationManager.getApplication().invokeAndWait(new Runnable() {
//					public void run() {
//
//					}
//				}, ModalityState.defaultModalityState());
				try {
					final byte[] log = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger())
							.getBuildLogs(bambooBuild.getServer(), bambooBuild.getBuildKey(), bambooBuild.getBuildNumber());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							consoleView.clear();
							consoleView.print(new String(log), ConsoleViewContentType.NORMAL_OUTPUT);
						}
					});
				} catch (ServerPasswordNotProvidedException e) {
//					setStatusMessage("Failed to get changes: Password not provided for server");
				} catch (RemoteApiException e) {
//					setStatusMessage("Failed to get changes: " + e.getMessage());
				}

			}
		};
		buildLogTask.queue();
	}


	public void open(@NotNull final BambooBuild bambooBuild) {
		final ConsoleView console = setupConsole(bambooBuild.getBuildKey(), bambooBuild.getBuildNumber());
		if (console != null) {
			console.clear();
			fetchAndShowBuildLog(bambooBuild, console);
//			try {
//				console.print(StringUtil.slurp(getClass().getResourceAsStream("/properties/bamboo.properties")),
//						ConsoleViewContentType.NORMAL_OUTPUT);
//			} catch (IOException e) {
//
//				throw new RuntimeException(e);
//			}
		} else {
			Messages.showErrorDialog(project, "Cannot open Bamboo build window", "Error");
		}
	}


	private ConsoleView setupConsole(String buildKey, String buildNumber) {
		final String contentKey = buildKey + "-" + buildNumber;

		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow consoleToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (consoleToolWindow == null) {
			consoleToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			consoleToolWindow.setIcon(IconLoader.getIcon("/icons/tab_bamboo.png"));
		}

		final ContentManager contentManager = consoleToolWindow.getContentManager();
		Content content = contentManager.findContent(contentKey);

		ConsoleView console;
		if (content != null) {
			console = consoleMap.get(contentKey);
		} else {
			TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
			TextConsoleBuilder builder = factory.createBuilder(project);
			builder.addFilter(new UnitTestFilter(project));
			builder.addFilter(new JavaFileFilter(project));
			builder.addFilter(new LoggerFilter());
			console = builder.getConsole();
			consoleMap.put(contentKey, console);

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(console.getComponent(), contentKey, true);
			content.setIcon(IconLoader.getIcon("/icons/tab_bamboo.png"));
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			contentManager.addContent(content);
		}
		contentManager.setSelectedContent(content);

		consoleToolWindow.show(null);
		return console;
	}

}

class JavaFileFilter implements Filter {

	private static final TextAttributes HYPERLINK_ATTRIBUTES = EditorColorsManager.getInstance().getGlobalScheme()
			.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES);

	private final Project project;

	public JavaFileFilter(final Project project) {
		this.project = project;
	}

	private static final Pattern pattern
			= Pattern.compile("([/\\\\]?[\\S ]*?" + "([^/\\\\]+\\.java))(:\\[(\\d+)\\,(\\d+)\\])?");

	@Nullable
	public Result applyFilter(final String line, final int textEndOffset) {
		if (!line.contains(".java")) {
			return null; // to make it faster
		}
		final Matcher m = pattern.matcher(line);
		while (m.find()) {
			final String matchedString = m.group();
			final String filename = m.group(2);

//			final String filename = FilenameUtils.getName(matchedString);
			if (filename != null && filename.length() > 0) {
				int index = matchedString.lastIndexOf(filename);
//
				final int highlightStartOffset = textEndOffset - line.length() + m.start() + index;
				final int highlightEndOffset = highlightStartOffset + filename.length();
				final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project, m.group(1));
				if (psiFile != null) {
					VirtualFile virtualFile = psiFile.getVirtualFile();
					if (virtualFile != null) {
						int focusLine = 0;
						int focusColumn = 0;
						try {
							focusLine = Integer.parseInt(m.group(4)) - 1;
							focusColumn = Integer.parseInt(m.group(5)) - 1;
						} catch (NumberFormatException e) {
							// just iterate to the next thing
						}
						final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(project, virtualFile,
								focusLine, focusColumn);
						return new Result(highlightStartOffset, highlightEndOffset, info, HYPERLINK_ATTRIBUTES);
					}
				}

			}

		}
		return null;
	}
}


class UnitTestFilter implements Filter {
	private final Project project;

	private static final TextAttributes HYPERLINK_ATTRIBUTES = EditorColorsManager.getInstance().getGlobalScheme()
			.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES);

	public UnitTestFilter(@NotNull final Project project) {
		this.project = project;
	}

	@Nullable
	public Result applyFilter(final String line, final int textEndOffset) {
//	  if (!line.startsWith("Running")) {
//		  return null;
//	  }

		for (ClassMatcher.MatchInfo match : ClassMatcher.find(line)) {
			final PsiManager manager = PsiManager.getInstance(project);
			PsiClass aClass = manager.findClass(match.getMatch(), GlobalSearchScope.allScope(project));
			if (aClass == null) {
				return null;
			}
			final PsiFile file = (PsiFile) aClass.getContainingFile().getNavigationElement();
			if (file == null) {
				return null;
			}


			final int highlightStartOffset = textEndOffset - line.length() + match.getIndex();
			final int highlightEndOffset = highlightStartOffset + match.getMatch().length();

			VirtualFile virtualFile = file.getVirtualFile();
			if (virtualFile == null) {
				return null;
			}
			final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(project, virtualFile, 0);
			return new Result(highlightStartOffset, highlightEndOffset, info, HYPERLINK_ATTRIBUTES);

		}
		return null;
	}
}


class LoggerFilter implements Filter {
	@NonNls
	private static final Color DARK_GREEN = new Color(0, 128, 0);

	public LoggerFilter() {
	}

	public Result applyFilter(final String line, final int textEndOffset) {

		if (line.indexOf("\t[INFO]") != -1 || line.indexOf("\tINFO") != -1) {
//		  final int highlightStartOffset = textEndOffset - line.length();
//		  final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(myProject, myProject.getBaseDir(), 0);
//		  TextAttributes attributes = HYPERLINK_ATTRIBUTES.clone();
//		  attributes.setForegroundColor(Color.PINK);
//		  attributes.setEffectColor(Color.PINK);
//		  return new Result(highlightStartOffset, textEndOffset, info, attributes);
			final int highlightStartOffset = textEndOffset - line.length();
			TextAttributes textAttributes = new TextAttributes();
			textAttributes.setForegroundColor(DARK_GREEN);
			return new Result(highlightStartOffset, textEndOffset, null, textAttributes);
		} else if (line.indexOf("\t[ERROR]") != -1 || line.indexOf("\tERROR") != -1) {
			final int highlightStartOffset = textEndOffset - line.length();
			TextAttributes textAttributes = new TextAttributes();
			textAttributes.setForegroundColor(Color.RED);
			return new Result(highlightStartOffset, textEndOffset, null, textAttributes);
		}
		return null;
	}


}
