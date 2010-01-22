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

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.veryquick.embweb.HttpRequestHandler;
import org.veryquick.embweb.Response;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
class IdeHttpServerHandler implements HttpRequestHandler {

	private final Map<String, AbstractDirectClickThroughHandler> registeredHandlers
			= new HashMap<String, AbstractDirectClickThroughHandler>() {{
		put("file", new OpenFileHandler());
		put("issue", new OpenIssueHandler());
		put("build", new OpenBuildHandler());
		put("stacktraceEntry", new OpenStackTraceEntryHandler());
		put("stacktrace", new OpenStackTraceHandler());
		put("review", new OpenReviewHandler());
		put("icon", new AbstractDirectClickThroughHandler() {
			@Override
			public void handle(Map<String, String> parameters) {
			}
		});
	}};

	private final byte[] icon;

	public IdeHttpServerHandler(final byte[] iconArray) {
		this.icon = iconArray;
	}

	public Response handleRequest(final Type type, final String url, final Map<String, String> parameters) {

		final String method = StringUtil.removeLeadingAndTrailingSlashes(url);
		final Response response = new Response();

		if ("supportsCapabilities".equals(method)) {
			if (registeredHandlers.keySet().containsAll(parameters.keySet())) {
				writeIcon(response);
			} else {
				response.setNoContent();
			}
			return response;
		}

		final AbstractDirectClickThroughHandler clickThroughHandler = registeredHandlers.get(method);
		if (clickThroughHandler != null) {
			writeIcon(response);
			ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
				public void run() {
					clickThroughHandler.handle(parameters);
				}
			});
		} else {
			response.setNoContent();
			PluginUtil.getLogger().warn("Unknown command received: [" + method + "]");
		}
		return response;
	}


	private void writeIcon(final Response response) {
		response.setContentType("image/png");
		response.setBinaryContent(icon);
		response.setOk();
	}

	public static void bringIdeaToFront(final Project project) {
		WindowManager.getInstance().getFrame(project).setVisible(true);

		String osName = System.getProperty("os.name");
		osName = osName.toLowerCase();

		if (osName.contains("windows") || osName.contains("mac os x")) {
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(true);
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(false);

		} else { //for linux
			WindowManager.getInstance().getFrame(project).toFront();
		}

		// how to set focus???
		WindowManager.getInstance().getFrame(project).setFocusable(true);
		WindowManager.getInstance().getFrame(project).setFocusableWindowState(true);
		WindowManager.getInstance().getFrame(project).requestFocus();
		WindowManager.getInstance().getFrame(project).requestFocusInWindow();
	}

	private static final class OpenBuildHandler extends AbstractDirectClickThroughHandler {

		public void handle(final Map<String, String> parameters) {
			final String buildKey = parameters.get("build_key");
			String buildNumber = parameters.get("build_number");
			final String serverUrl = parameters.get("server_url");

			final String testPackage = parameters.get("test_package");
			final String testClass = parameters.get("test_class");
			final String testMethod = parameters.get("test_method");

			int buildNumberInt = 0;

			try {
				buildNumberInt = Integer.valueOf(buildNumber);
			} catch (NumberFormatException e) {
				buildNumber = null;
			}

			final int buildNumberIntFinal = buildNumberInt;

			if (isDefined(buildKey) && isDefined(serverUrl) && isDefined(buildNumber)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// try to open received build in all open projects
						for (Project project : ProjectManager.getInstance().getOpenProjects()) {

							final BambooToolWindowPanel panel = IdeaHelper.getBambooToolWindowPanel(project);
							if (panel != null) {
								bringIdeaToFront(project);
								if (isDefined(testPackage) && isDefined(testClass) && isDefined(testMethod)) {
									panel.openBuildAndRunTest(buildKey, buildNumberIntFinal, serverUrl, testPackage, testClass,
											testMethod);
								} else if (!isDefined(testPackage) && !isDefined(testClass) && !isDefined(testMethod)) {
									panel.openBuild(buildKey, buildNumberIntFinal, serverUrl);
								} else {
									reportProblem("Cannot run test. At least one of the params is not provided: "
											+ "test_package, test_class, test_method");
								}
							} else {
								PluginUtil.getLogger().warn(
										"com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel is null");
							}
						}
					}
				});
			} else {
				reportProblem("Cannot open build. Incorrect call. "
						+ "At least one of the params is not provided: build_key, build_number, server_url");
			}
		}
	}

	private static class OpenIssueHandler extends AbstractDirectClickThroughHandler {

		public void handle(final Map<String, String> parameters) {
			final String issueKey = parameters.get("issue_key");
			final String serverUrl = parameters.get("server_url");
			if (isDefined(issueKey) && isDefined(serverUrl)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// try to open received issueKey in all open projects
						for (Project project : ProjectManager.getInstance().getOpenProjects()) {

							final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
							if (panel != null) {
								bringIdeaToFront(project);
								panel.openIssue(issueKey, serverUrl);
							} else {
								reportProblem("com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel is null");
							}
						}
					}
				});
			} else {
				reportProblem("Cannot open issue: issue_key or server_url parameter is null");
			}
		}
	}

	private static class OpenFileHandler extends AbstractDirectClickThroughHandler {

		public void handle(final Map<String, String> parameters) {
			final String file = StringUtil.removePrefixSlashes(parameters.get("file"));
			final String path = StringUtil.removeSuffixSlashes(parameters.get("path"));
			final String vcsRoot = StringUtil.removeSuffixSlashes(parameters.get("vcs_root"));
			final String line = parameters.get("line");
			if (isDefined(file)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						openRequestedFile(path, file, vcsRoot, line);
					}
				});
			} else {
				reportProblem("No file parameter provided.");
			}
		}

		private void openRequestedFile(String path, String file, String vcsRoot, String line) {
			boolean found = false;
			// try to open requested file in all open projects
			for (Project project : ProjectManager.getInstance().getOpenProjects()) {
				String filePath = (path == null ? file : path + "/" + file);
				// find file by name (and path if provided)
				Collection<PsiFile> psiFiles = CodeNavigationUtil.findPsiFiles(project, filePath);
				if (psiFiles == null || psiFiles.size() == 0) {
					psiFiles = new ArrayList<PsiFile>();
					PsiFile psiFile = CodeNavigationUtil.guessCorrespondingPsiFile(project, filePath);
					if (psiFile != null) {
						psiFiles.add(psiFile);
					}
				}

				// narrow found list of files by VCS
				if (psiFiles != null && psiFiles.size() > 0 && isDefined(vcsRoot)) {
					Collection<PsiFile> pf = CodeNavigationUtil.findPsiFilesWithVcsUrl(psiFiles, vcsRoot, project);
					// if VCS narrowed to empty list then return without narrowing
					// VCS could not match because of different configuration in IDE and web client (JIRA, FishEye, etc)
					if (pf != null && pf.size() > 0) {
						psiFiles = pf;
					}
				}
				// open file or show popup if more than one file found
				if (psiFiles != null && psiFiles.size() > 0) {
					found = true;
					if (psiFiles.size() == 1) {
						openFile(project, psiFiles.iterator().next(), line);
					} else if (psiFiles.size() > 1) {
						ListPopup popup = JBPopupFactory.getInstance().createListPopup(new FileListPopupStep(
								"Select File to Open", new ArrayList<PsiFile>(psiFiles), line, project));
						popup.showCenteredInCurrentWindow(project);
					}
					bringIdeaToFront(project);
				}
			}
			// message box showed only if the file was not found at all (in all project)
			if (!found) {
				String msg = "";
				if (ProjectManager.getInstance().getOpenProjects().length > 0) {
					msg = "Project does not contain requested file " + file;
				} else {
					msg = "Please open a project in order to indicate search path for file " + file;
				}
				Messages.showInfoMessage(msg, PluginUtil.PRODUCT_NAME);
			}
		}

		private static void openFile(final Project project, final PsiFile psiFile, final String line) {
			if (psiFile != null) {
				psiFile.navigate(true);	// open file

				final VirtualFile virtualFile = psiFile.getVirtualFile();

				if (virtualFile != null && line != null && line.length() > 0) {	//place cursor in specified line
					try {
						Integer iLine = Integer.valueOf(line);
						if (iLine != null) {
							OpenFileDescriptor display = new OpenFileDescriptor(project, virtualFile, iLine, 0);
							if (display.canNavigateToSource()) {
								display.navigate(false);
							}
						}
					} catch (NumberFormatException e) {
						PluginUtil.getLogger().warn(
								"Wrong line number format when requesting to open file in the IDE ["
										+ line + "]", e);
					}
				}
			}
		}

		private static class FileListPopupStep extends BaseListPopupStep<PsiFile> {
			private final String line;
			private final Project project;

			public FileListPopupStep(final String title, final List<PsiFile> psiFiles, final String line,
					final Project project) {
				super(title, psiFiles);
				this.line = line;
				this.project = project;
			}

			@Override
			public PopupStep onChosen(final PsiFile selectedValue, final boolean finalChoice) {
				openFile(project, selectedValue, line);
				return null;
			}

			@Override
			@NotNull
			public String getTextFor(final PsiFile value) {
				String display = value.getName();
				final VirtualFile virtualFile = value.getVirtualFile();

				if (virtualFile != null) {
					display += " (" + virtualFile.getPath() + ")";
				}
				return display;
			}

			@Override
			public Icon getIconFor(final PsiFile value) {
				final VirtualFile virtualFile = value.getVirtualFile();

				if (virtualFile != null) {
					return virtualFile.getIcon();
				}

				return null;
			}
		}
	}

	private static class OpenReviewHandler extends AbstractDirectClickThroughHandler {

		public void handle(final Map<String, String> parameters) {
			final String reviewKey = parameters.get("review_key");
			final String serverUrl = parameters.get("server_url");
			final String filePath = parameters.get("file_path");
			final String commentId = parameters.get("comment_id");
			if (isDefined(reviewKey) && isDefined(serverUrl)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {

						// try to open received reviewKey in all open projects
						for (final Project project : ProjectManager.getInstance().getOpenProjects()) {

							bringIdeaToFront(project);

							ProgressManager.getInstance().run(new FindAndOpenReviewTask(project,
									"Looking for Review " + reviewKey, false, reviewKey, serverUrl, filePath, commentId));
						}
					}
				});
			} else {
				reportProblem("Cannot open review: review_key or server_url parameter is null");
			}
		}

		private static class FindAndOpenReviewTask extends Task.Modal {
			private final Project project;
			private final String reviewKey;
			private final String serverUrl;
			private final String filePath;
			private final String commentId;

			private ReviewAdapter review;


			public FindAndOpenReviewTask(final Project project, final String title, final boolean cancellable,
					final String reviewKey, final String serverUrl, final String filePath, final String commentId) {

				super(project, title, cancellable);

				this.project = project;
				this.reviewKey = reviewKey;
				this.serverUrl = serverUrl;
				this.filePath = filePath;
				this.commentId = commentId;
			}

			@Override
			public void run(final ProgressIndicator indicator) {
				indicator.setIndeterminate(true);

				// open review
				review = IdeaHelper.getReviewListToolWindowPanel(project).openReviewWithDetails(reviewKey, serverUrl);

				if (review != null && (isDefined(filePath) || isDefined(commentId))) {
					try {
						// get details for review (files and comments)
						IntelliJCrucibleServerFacade.getInstance().fillDetailsForReview(review);
					} catch (RemoteApiException e) {
						PluginUtil.getLogger().warn("Error when retrieving review details", e);
						return;
					} catch (ServerPasswordNotProvidedException e) {
						PluginUtil.getLogger().warn("Missing password exception caught when retrieving review details", e);
						return;
					}

					CrucibleFileInfo file = null;

					// find file
					if (isDefined(filePath)) {
						final Set<CrucibleFileInfo> files;
						try {
							files = review.getFiles();
						} catch (ValueNotYetInitialized e) {
							PluginUtil.getLogger().warn("Files collection not available for review", e);
							return;
						}

						for (final CrucibleFileInfo f : files) {
							if (f.getFileDescriptor().getUrl().endsWith(filePath)) {
								file = f;
								break;
							}
						}
					}

					// find comment
					VersionedComment versionedComment = null;
					Comment versionedCommentReply = null;
					Comment generalComment = null;

					if (isDefined(commentId)) {

						// try to find general comment with specified ID
						final List<Comment> generalComments;
						try {
							generalComments = review.getGeneralComments();
						} catch (ValueNotYetInitialized e) {
							PluginUtil.getLogger().warn("General comments collection not available for review", e);
							return;
						}

						for (Comment comment : generalComments) {
							if (comment.getPermId().getId().equals(commentId)) {
								generalComment = comment;
								break;
							}
							boolean commentFound = false;
							for (Comment reply : comment.getReplies()) {
								if (reply.getPermId().getId().equals(commentId)) {
									commentFound = true;
									generalComment = reply;
									break;
								}
							}
							if (commentFound) {
								break;
							}
						}

						// try to find versioned comment with specified ID if general comment not found
						if (file != null && generalComment == null) {
							final List<VersionedComment> versionedComments = file.getVersionedComments();
							for (VersionedComment comment : versionedComments) {
								if (comment.getPermId().getId().equals(commentId)) {
									versionedComment = comment;
									break;
								}
								boolean commentFound = false;
								for (Comment reply : comment.getReplies()) {
									if (reply.getPermId().getId().equals(commentId)) {
										commentFound = true;
										versionedComment = comment;
										versionedCommentReply = reply;
										break;
									}
								}
								if (commentFound) {
									break;
								}
							}
						}
					}

					if (generalComment != null) {
						// select comment in the tree
						CrucibleHelper.selectGeneralComment(project, review, generalComment);
					}

					if (file != null) {
						if (versionedComment == null) {
							// simply open file (versioned comment not found)
							CrucibleHelper.showVirtualFileWithComments(project, review, file);

							// select file in the tree if general comment not selected
							if (generalComment == null) {
								CrucibleHelper.selectFile(project, review, file);
							}
						} else {
							// open file and focus on comment
							CrucibleHelper.openFileOnComment(project, review, file, versionedComment);

							// select comment in the tree
							if (versionedCommentReply != null) {
								CrucibleHelper.selectVersionedComment(project, review, file, versionedCommentReply);
							} else {
								CrucibleHelper.selectVersionedComment(project, review, file, versionedComment);
							}
						}
					}
				}
			}

			@Override
			public void onSuccess() {
			}
		}
	}
}
