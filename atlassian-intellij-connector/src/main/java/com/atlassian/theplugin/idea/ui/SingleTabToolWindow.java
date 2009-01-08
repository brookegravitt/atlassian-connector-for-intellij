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
package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * User: pmaruszak
 */
public abstract class SingleTabToolWindow {
	//CHECKSTYLE\:MAGIC\:OFF
	protected static final Color HEADER_BACKGROUND_COLOR = new Color(153, 153, 153);
	//CHECKSTYLE\:MAGIC\:ON
	protected static final String[] NONE = { "None" };

	protected static Map<String, JPanel> panelMap = new HashMap<String, JPanel>();
	protected final Project project;
	protected final CrucibleReviewListModel reviewListModel;

	public SingleTabToolWindow(Project project, CrucibleReviewListModel reviewListModel) {

		this.project = project;
		this.reviewListModel = reviewListModel;
	}

	private ContentPanel contentPanel;

	public ContentPanel getContentPanel() {
		return contentPanel;
	}

	protected abstract class ContentPanel extends JPanel {
		public abstract void unregister();

		public abstract String getKey();

		public abstract ContentParameters getContentParameters();
	}

	protected interface ContentParameters {
	}

	protected abstract String getContentKey(ContentParameters params);

	protected abstract ContentPanel createContentPanel(ContentParameters params);


	protected void showToolWindow(ContentParameters params,
								  final String baseTitle, final Icon icon) {
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow itw = twm.getToolWindow(getExistingToolWindowTitle(baseTitle));
		if (itw != null) {
			twm.unregisterToolWindow(getExistingToolWindowTitle(baseTitle));
		}

		contentPanel = createContentPanel(params);
		createNewToolWindow(baseTitle, icon, contentPanel.getKey());

	}

	protected void createNewToolWindow(final String baseTitle, final Icon icon, String key) {
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		twm.unregisterToolWindow(baseTitle);
		final ToolWindow toolWindow = twm.registerToolWindow(getExistingToolWindowTitle(baseTitle), true, ToolWindowAnchor.BOTTOM);

		setToolWindowIconCargoCult(toolWindow, icon);

		toolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
			public void contentRemoved(ContentManagerEvent event) {
				super.contentRemoved(event);
				final String titleToRemove = getExistingToolWindowTitle(getExistingToolWindowTitle(baseTitle));

				contentPanel.unregister();
				contentPanel = null;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						twm.unregisterToolWindow(titleToRemove);
						createNewToolWindow(getExistingToolWindowTitle(baseTitle), icon, null);

					}
				});
			}
		});

		createToolWindowContent(key, icon, toolWindow);
	}

	private void createToolWindowContent(String key, Icon icon, ToolWindow toolWindow) {
		Content content =
				(toolWindow.getContentManager().getContents().length > 0)
						? toolWindow.getContentManager().getContents()[0] : null;

		if (content != null) {
			toolWindow.getContentManager().removeContent(content, true);
		}

		content = toolWindow.getContentManager().getFactory().createContent(contentPanel, "", true);

		toolWindow.getContentManager().addContent(content);
		if (content != null) {
			toolWindow.getContentManager().setSelectedContent(content);
		}
		toolWindow.show(null);
	}

	private static void setToolWindowIconCargoCult(ToolWindow toolWindow, Icon icon) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		Icon i = new ImageIcon(bi);
		toolWindow.setIcon(i);
	}


	private String getExistingToolWindowTitle(String baseTitle) {
		String title = baseTitle;
		if (contentPanel != null) {
			title = baseTitle + ": " + contentPanel.getKey();
		}
		return title;
	}

}
