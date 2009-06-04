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

import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
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
		} else {
			response.setNoContent();
			PluginUtil.getLogger().warn("Unknown command received: [" + method + "]");
		}

		return response;
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

						// find file by path
						Collection<PsiFile> psiFiles = CodeNavigationUtil.findPsiFiles(project, filePath);

						// narrow found files by VCS
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

	private void openFile(final Project project, final PsiFile psiFile, final String line) {
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

	private boolean isDefined(final String param) {
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

	private void bringIdeaToFront(final Project project) {
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

	private class FileListPopupStep extends BaseListPopupStep<PsiFile> {
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
				display += " (" + virtualFile.getUrl() + ")";
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
}
