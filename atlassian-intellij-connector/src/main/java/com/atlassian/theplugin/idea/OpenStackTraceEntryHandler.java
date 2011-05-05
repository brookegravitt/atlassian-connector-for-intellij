package com.atlassian.theplugin.idea;

import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Wojciech Seliga
 */
public class OpenStackTraceEntryHandler extends AbstractDirectClickThroughHandler {

	public void handle(final Map<String, String> parameters) {
		final String stacktraceEntry = parameters.get("stacktraceEntry");
		if (stacktraceEntry == null) {
			reportProblem("Cannot open stacktrace. Incorrect call with params [" + parameters + "]");
			return;
		}

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				if (ProjectManager.getInstance().getOpenProjects().length > 0) {
					Project project = ProjectManager.getInstance().getOpenProjects()[0];
					final OpenFileHyperlinkInfo hyperlinkInfo = getHyperlinkInfo(project, "at " + stacktraceEntry);
					if (hyperlinkInfo != null) {
						IdeHttpServerHandler.bringIdeaToFront(project);
						hyperlinkInfo.navigate(project);
					}
				} else {
					reportProblem("Cannot find any open project while opening stacktrace");
				}
			}

		});

	}

	private static final String AT = "at";
	private static final String AT_PREFIX = AT + " ";
	private static final String STANDALONE_AT = " " + AT + " ";


	@Nullable
	public OpenFileHyperlinkInfo getHyperlinkInfo(@NotNull Project project, @NotNull String line) {
		final GlobalSearchScope mySearchScope = GlobalSearchScope.allScope(project);

		if (!line.contains(".java")) {
			return null; // to make it faster
		}


		int atIndex;
		if (line.startsWith(AT_PREFIX)) {
			atIndex = 0;
		} else {
			atIndex = line.indexOf(STANDALONE_AT);
			if (atIndex < 0) {
				atIndex = line.indexOf(AT_PREFIX);
			}
			if (atIndex < 0) {
				return null;
			}
		}

		final int lparenthIndex = line.indexOf('(', atIndex);
		if (lparenthIndex < 0) {
			return null;
		}
		final int lastDotIndex = line.lastIndexOf('.', lparenthIndex);
		if (lastDotIndex < 0 || lastDotIndex < atIndex) {
			return null;
		}
		String className = line.substring(atIndex + AT.length() + 1, lastDotIndex).trim();
		final int dollarIndex = className.indexOf('$');
		if (dollarIndex >= 0) {
			className = className.substring(0, dollarIndex);
		}

		//String methodName = text.substring(lastDotIndex + 1, lparenthIndex).trim();

		final int rparenthIndex = line.indexOf(')', lparenthIndex);
		if (rparenthIndex < 0) {
			return null;
		}

		final String fileAndLine = line.substring(lparenthIndex + 1, rparenthIndex).trim();

		final int colonIndex = fileAndLine.lastIndexOf(':');
		if (colonIndex < 0) {
			return null;
		}

		final String lineString = fileAndLine.substring(colonIndex + 1);
		final int lineNumber = Integer.parseInt(lineString);

		final PsiFile file = IdeaVersionFacade.getInstance().getPsiClassNavigationElement(project, className);
		if (file == null) {
			return null;
		}

		/*
				   IDEADEV-4976: Some scramblers put something like SourceFile mock instead of real class name.
				  final String filePath = fileAndLine.substring(0, colonIndex).replace('/', File.separatorChar);
				  final int slashIndex = filePath.lastIndexOf(File.separatorChar);
				  final String shortFileName = slashIndex < 0 ? filePath : filePath.substring(slashIndex + 1);
				  if (!file.getName().equalsIgnoreCase(shortFileName)) return null;
				  */

		VirtualFile virtualFile = file.getVirtualFile();
		if (virtualFile == null) {
			return null;
		}


		return new OpenFileHyperlinkInfo(project, virtualFile, lineNumber - 1);
	}

}	

