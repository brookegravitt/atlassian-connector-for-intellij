package com.atlassian.theplugin.idea.bamboo.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
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
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

/**
 * User: jgorycki
 * Date: Jan 9, 2009
 * Time: 12:59:25 PM
 */
public class BuildLogPanel extends JPanel implements ActionListener {
	private final Project project;

	public BuildLogPanel(Project project, BambooBuildAdapter build) {
		this.project = project;
		setLayout(new BorderLayout());
		final ConsoleView console = setupConsole();
		add(console.getComponent(), BorderLayout.CENTER);
		console.clear();
		fetchAndShowBuildLog(build, console);
	}

	private ConsoleView setupConsole() {
		ConsoleView console;
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder builder = factory.createBuilder(project);
		builder.addFilter(new JavaFileFilter(project));
		builder.addFilter(new UnitTestFilter(project));
		builder.addFilter(new LoggerFilter());
		console = builder.getConsole();
		return console;
	}

	public void fetchAndShowBuildLog(final BambooBuildAdapter build, final ConsoleView consoleView) {
		if (!build.isValid()) {
			Messages.showErrorDialog(project, "Cannot fetch build log for invalid build (without the number)!",
					PluginUtil.PRODUCT_NAME);
			return;
		}

		consoleView.clear();
		consoleView.print("Fetching Bamboo Build Log from the server...", ConsoleViewContentType.NORMAL_OUTPUT);

		Task.Backgroundable buildLogTask = new Task.Backgroundable(project, "Retrieving Build Log", false) {
			@Override
			public void run(@NotNull final ProgressIndicator indicator) {
				try {
					final String log =
							IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()).getBuildLogs(build.getServer(),
									build.getPlanKey(), build.getNumber());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							consoleView.clear();
							consoleView.print(log, ConsoleViewContentType.NORMAL_OUTPUT);
						}
					});
				} catch (ServerPasswordNotProvidedException e) {
					showError(consoleView, e);
				} catch (RemoteApiException e) {
					showError(consoleView, e);
				}
			}
		};
		buildLogTask.queue();
	}

	private void showError(final ConsoleView consoleView, final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				consoleView.clear();
				if (e.getMessage().toUpperCase().startsWith("HTTP 404")) {
					consoleView.print("No build log available", ConsoleViewContentType.ERROR_OUTPUT);
				} else {
					consoleView.print(e.getMessage(), ConsoleViewContentType.ERROR_OUTPUT);
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		// ignore
	}

	public static class JavaFileFilter implements Filter {

		private final TextAttributes hyperlinkAttributes = EditorColorsManager.getInstance().getGlobalScheme()
				.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES);

		private final Project project;
		public static final int ROW_GROUP = 4;
		public static final int COLUMN_GROUP = 6;
		public static final int FILENAME_GROUP = 2;
		public static final int FULLPATH_GROUP = 1;

		public JavaFileFilter(final Project project) {
			this.project = project;
		}

		// match pattern like:
		// /path/to/file/MyClass.java:[10,20]
		// /path/to/file/MyClass.java
		// /path/to/file/MyClass.java:20:30
		// /path/to/file/MyClass.java:20

		// this pattern is too heavy and it does not allow white characters other than space in the path or file name
//		private static final Pattern JAVA_FILE_PATTERN
//				= Pattern.compile("([/\\\\]?[\\S ]*?([^/\\\\]+\\.java))(:\\[?(\\d+)([\\,:](\\d+)\\]?)?)?");

		// this pattern is more lightwave (for some real data it operates 100 times faster then above one - see UnitTest)
		// this pattern also allows white characters other than space in the path/filename (e.g. tab which is allowed by Linux)
		// both patterns will not work in case there is a slash or a backslash in the filename 
		private static final Pattern JAVA_FILE_PATTERN
				= Pattern.compile("^(.*?([^/\\\\]+\\.java))(:\\[?(\\d+)([\\,:](\\d+)\\]?)?)?");

		@Nullable
		public Result applyFilter(final String line, final int textEndOffset) {

//			long start = System.currentTimeMillis();

			if (!line.contains(".java")) {
				return null; // to make it faster
			}

			Result ret = null;

			final Matcher m = findMatchings(line);
			while (m.find()) {
				final String matchedString = m.group();

//	System.out.println("Matched: " + matchedString);

				final String filename = m.group(FILENAME_GROUP);

				if (filename != null && filename.length() > 0) {

//	System.out.println("Filename: " + filename);

//	System.out.println("Path: " +  m.group(FULLPATH_GROUP));

					final PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(
							project, m.group(FULLPATH_GROUP));
					if (psiFile != null) {
						VirtualFile virtualFile = psiFile.getVirtualFile();
						if (virtualFile != null) {

							int focusLine = 0;
							int focusColumn = 0;
							final String rowGroup = m.group(ROW_GROUP);
							final String columnGroup = m.group(COLUMN_GROUP);
							try {
								if (rowGroup != null) {
									focusLine = Integer.parseInt(rowGroup) - 1;

//	System.out.println("Focus line: " + focusLine);
								}
								if (columnGroup != null) {
									focusColumn = Integer.parseInt(columnGroup) - 1;

//	System.out.println("Focus column: " + focusColumn);
								}
							} catch (NumberFormatException e) {
								// just iterate to the next thing
							}
							final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(project, virtualFile,
									focusLine, focusColumn);
							final String relativePath = VfsUtil.getPath(project.getBaseDir(), virtualFile, '/');

//	System.out.println("Relative path: " + relativePath);

							final int startMatchingFileIndex = relativePath != null
									? matchedString.replace('\\', '/').indexOf(relativePath)
									: matchedString.lastIndexOf(filename);

//	System.out.println("Matching file index: " + startMatchingFileIndex);

							final int highlightStartOffset =
									textEndOffset - line.length() + m.start() + startMatchingFileIndex;

//	System.out.println("Highlight start: " + highlightStartOffset);

							final int highlightEndOffset = textEndOffset - line.length() + m.end();

//	System.out.println("Highliht end: " + highlightEndOffset);

							ret = new Result(highlightStartOffset, highlightEndOffset, info, hyperlinkAttributes);
						}
					}
				}
			}

//			long stop = System.currentTimeMillis(); // stop timing
//
//			System.out.println((stop - start) + "\t" + line); // print execution time

			return ret;
		}

		public static Matcher findMatchings(final String line) {
			return JAVA_FILE_PATTERN.matcher(line);
		}
	}

	public static class UnitTestFilter implements Filter {
		private final Project project;

		private static final TextAttributes HYPERLINK_ATTRIBUTES = EditorColorsManager.getInstance().getGlobalScheme()
				.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES);

		public UnitTestFilter(@NotNull final Project project) {
			this.project = project;
		}

		@Nullable
		public Result applyFilter(final String line, final int textEndOffset) {
			for (ClassMatcher.MatchInfo match : ClassMatcher.find(line)) {
				final Result res = handleMatch(line, textEndOffset, match);
				if (res != null) {
					return res;
				}
			}
			return null;
		}

		private Result handleMatch(final String line, final int textEndOffset, final ClassMatcher.MatchInfo match) {
			PsiClass aClass = IdeaVersionFacade.getInstance().findClass(match.getMatch(), project);
			if (aClass == null) {
				return null;
			}
			final PsiFile file = (PsiFile) aClass.getContainingFile().getNavigationElement();
			if (file == null) {
				return null;
			}


			int highlightStartOffset = textEndOffset - line.length() + match.getIndex();
			final int highlightEndOffset = highlightStartOffset + match.getMatch().length();

			VirtualFile virtualFile = file.getVirtualFile();
			if (virtualFile == null) {
				return null;
			}

			int targetLine = 0;
			// special handling of sure-fire report from Maven for JUnit 3 tests: method_name(FQCN)
			if (match.getIndex() > 0 && line.charAt(match.getIndex() - 1) == '('
					&& (match.getIndex() + match.getMatch().length() < line.length()
					&& line.charAt(match.getIndex() + match.getMatch().length()) == ')')) {
				// trying to extract method name which should be just before the '('
				int i = match.getIndex() - 2;
				for (; i >= 0; i--) {
					char currentChar = line.charAt(i);
					if (Character.isWhitespace(currentChar)) {
						break;
					}
				}
				if (i != match.getIndex() - 2) {
					final String methodName = line.substring(i + 1, match.getIndex() - 1);

					// means we found some potential method
					PsiMethod[] methods = aClass.findMethodsByName(methodName, true);
					if (methods != null && methods.length != 0 && methods[0] != null) {
						highlightStartOffset = textEndOffset - line.length() + i + 1;
						final OpenFileDescriptor openFileDescriptor =
								new OpenFileDescriptor(project, virtualFile, methods[0].getTextOffset());
						final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(openFileDescriptor);
						return new Result(highlightStartOffset, highlightEndOffset, info, HYPERLINK_ATTRIBUTES);
					}
				}
			}

			final OpenFileHyperlinkInfo info = new OpenFileHyperlinkInfo(project, virtualFile, targetLine);
			return new Result(highlightStartOffset, highlightEndOffset, info, HYPERLINK_ATTRIBUTES);
		}
	}


	public static class LoggerFilter implements Filter {
		private static final Color DARK_GREEN = new Color(0, 128, 0);
		private static final TextAttributes ERROR_TEXT_ATTRIBUTES = new TextAttributes();
		private static final TextAttributes INFO_TEXT_ATTRIBUTES = new TextAttributes();
		private static final TextAttributes WARNING_TEXT_ATTRIBUTES = new TextAttributes();

		static {
			ERROR_TEXT_ATTRIBUTES.setForegroundColor(Color.RED);
			INFO_TEXT_ATTRIBUTES.setForegroundColor(DARK_GREEN);
			//CHECKSTYLE:MAGIC:OFF
			WARNING_TEXT_ATTRIBUTES.setForegroundColor(new Color(185, 150, 0));
			//CHECKSTYLE:MAGIC:ON
		}

		public LoggerFilter() {
		}

		public Result applyFilter(final String line, final int textEndOffset) {

			if (line.indexOf("\t[INFO]") != -1 || line.indexOf("\tINFO") != -1) {
				final int highlightStartOffset = textEndOffset - line.length();
				return new Result(highlightStartOffset, textEndOffset, null, INFO_TEXT_ATTRIBUTES);
			} else if (line.indexOf("\t[ERROR]") != -1 || line.indexOf("\tERROR") != -1) {
				final int highlightStartOffset = textEndOffset - line.length();
				return new Result(highlightStartOffset, textEndOffset, null, ERROR_TEXT_ATTRIBUTES);
			} else if (line.indexOf("\t[WARNING]") != -1 || line.indexOf("\tWARNING") != -1) {
				final int highlightStartOffset = textEndOffset - line.length();
				return new Result(highlightStartOffset, textEndOffset, null, WARNING_TEXT_ATTRIBUTES);
			}
			return null;
		}
	}
}
