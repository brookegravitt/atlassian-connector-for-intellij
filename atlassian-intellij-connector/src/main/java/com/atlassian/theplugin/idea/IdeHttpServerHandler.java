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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
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
import java.util.List;
import java.util.Map;

/**
 * @author Jacek Jaroczynski
 */
class IdeHttpServerHandler implements HttpRequestHandler {
	private byte[] icon;

	public IdeHttpServerHandler(final byte[] iconArray) {
		this.icon = iconArray;
	}

	public Response handleRequest(final Type type, final String url, final Map<String, String> parameters) {

		final String method = StringUtil.removeTrailingSlashes(url);

		Response response = new Response();

		if (method.equals("icon")) {
			writeIcon(response);
		} else if (method.equals("file")) {
			writeIcon(response);
			handleOpenFileRequest(parameters);
		} else if (method.equals("issue")) {
			writeIcon(response);
			handleOpenIssueRequest(parameters);
		} else if (method.equals("review")) {
			writeIcon(response);
			handleOpenReviewRequest(parameters);
		} else {
			response.setNoContent();
			PluginUtil.getLogger().warn("Unknown command received: [" + method + "]");
		}

		return response;
	}

	private void handleOpenReviewRequest(final Map<String, String> parameters) {
		final String reviewKey = parameters.get("review_key");
		final String serverUrl = parameters.get("server_url");
		final String filePath = parameters.get("file_path");
		final String commentId = parameters.get("comment_id");
		if (reviewKey != null && serverUrl != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {

					// try to open received reviewKey in all open projects
					for (final Project project : ProjectManager.getInstance().getOpenProjects()) {

						bringIdeaToFront(project);

						ProgressManager.getInstance().run(new FindAndOpenReviewTask(
								project, "Looking for Review " + reviewKey, false, reviewKey, serverUrl, filePath, commentId));
					}
				}
			});
		} else {
			PluginUtil.getLogger().warn("Cannot open review: review_key or server_url parameter is null");
		}
	}

	private void handleOpenFileRequest(final Map<String, String> parameters) {
		final String file = StringUtil.removePrefixSlashes(parameters.get("file"));
		final String path = StringUtil.removeSuffixSlashes(parameters.get("path"));
		final String vcsRoot = StringUtil.removeSuffixSlashes(parameters.get("vcs_root"));
		final String line = parameters.get("line");
		if (file != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					boolean found = false;
					// try to open requested file in all open projects
					for (Project project : ProjectManager.getInstance().getOpenProjects()) {
						String filePath = (path == null ? file : path + "/" + file);

						// find file by name (and path if provided)
						Collection<PsiFile> psiFiles = CodeNavigationUtil.findPsiFiles(project, filePath);

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
						}
						bringIdeaToFront(project);
					}

					// message box showed only if the file was not found at all (in all project)
					if (!found) {
						Messages.showInfoMessage("Cannot find file " + file, PluginUtil.PRODUCT_NAME);
					}
				}
			});
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

	private static boolean isDefined(final String param) {
		return param != null && param.length() > 0;
	}

	private void handleOpenIssueRequest(final Map<String, String> parameters) {
		final String issueKey = parameters.get("issue_key");
		final String serverUrl = parameters.get("server_url");
		if (issueKey != null && serverUrl != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					boolean found = false;
					// try to open received issueKey in all open projects
					for (Project project : ProjectManager.getInstance().getOpenProjects()) {

						if (IdeaHelper.getIssueListToolWindowPanel(project).openIssue(issueKey, serverUrl)) {
							found = true;
						}

						bringIdeaToFront(project);
					}

					if (!found) {
						Messages.showInfoMessage("Cannot find issue " + issueKey, PluginUtil.PRODUCT_NAME);
					}
				}
			});
		} else {
			PluginUtil.getLogger().warn("Cannot open issue: issue_key or server_url parameter is null");
		}
	}

	private static void bringIdeaToFront(final Project project) {
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

	private void writeIcon(final Response response) {
		response.setContentType("image/png");
		response.setBinaryContent(icon);
		response.setOk();
	}

	private static class FileListPopupStep extends BaseListPopupStep<PsiFile> {
		private String line;
		private Project project;

		public FileListPopupStep(final String title, final List<PsiFile> psiFiles, final String line, final Project project) {
			super(title, psiFiles);
			this.line = line;
			this.project = project;
		}

		public PopupStep onChosen(final PsiFile selectedValue, final boolean finalChoice) {
			openFile(project, selectedValue, line);
			return null;
		}

		@NotNull
		public String getTextFor(final PsiFile value) {
			String display = value.getName();
			final VirtualFile virtualFile = value.getVirtualFile();

			if (virtualFile != null) {
				display += " (" + virtualFile.getPath() + ")";
			}
			return display;
		}

		public Icon getIconFor(final PsiFile value) {
			final VirtualFile virtualFile = value.getVirtualFile();

			if (virtualFile != null) {
				return virtualFile.getIcon();
			}

			return null;
		}
	}

	private static class FindAndOpenReviewTask extends Task.Modal {
		private Project project;
		private String reviewKey;
		private String serverUrl;
		private String filePath;
		private String commentId;

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

		public void run(final ProgressIndicator indicator) {
			indicator.setIndeterminate(true);

			// open review
			review = IdeaHelper.getReviewListToolWindowPanel(project).openReview(reviewKey, serverUrl);
			try {
				if (review != null && isDefined(filePath)) {
					// get details for review (files and comments)
					CrucibleServerFacadeImpl.getInstance().getDetailsForReview(review);
					for (final CrucibleFileInfo file : review.getFiles()) {
						if (file.getFileDescriptor().getUrl().endsWith(filePath)) {
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									// open requested file
									if (isDefined(commentId)) {
										final List<VersionedComment> comments = file.getVersionedComments();
										boolean commentFound = false;
										for (VersionedComment comment : comments) {
											if (comment.getPermId().getId().equals(commentId)) {
												commentFound = true;
												CrucibleHelper.openFileOnCommentAndSelectComment(
														project, review, file, comment);
												break;
											}
											for (Comment reply : comment.getReplies()) {
												if (reply.getPermId().getId().equals(commentId)) {
													commentFound = true;
													CrucibleHelper.openFileOnCommentAndSelectComment(
															project, review, file, comment);
													break;
												}
											}
											if (commentFound) {
												break;
											}
										}

										// comment not found, simply open file
										if (!commentFound) {
											CrucibleHelper.showVirtualFileWithComments(project, review, file);
										}

									} else {
										CrucibleHelper.showVirtualFileWithComments(project, review, file);
									}
								}
							});
							break;
						}
					}
				}
			} catch (RemoteApiException e) {
				PluginUtil.getLogger().warn("Error when retrieving review details", e);
			} catch (ServerPasswordNotProvidedException e) {
				PluginUtil.getLogger().warn(
						"Missing password exception caught when retrieving review details", e);
			} catch (ValueNotYetInitialized e) {
				PluginUtil.getLogger().warn("Files collection not initialized", e);
			}
		}

		public void onSuccess() {
			if (review == null) {
				Messages.showInfoMessage("Cannot find review " + reviewKey, PluginUtil.PRODUCT_NAME);
			} else {
				bringIdeaToFront(project);
			}
		}
	}
}
