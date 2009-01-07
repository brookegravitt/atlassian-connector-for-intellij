package com.atlassian.theplugin.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.Content;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Jan 6, 2009
 * Time: 3:08:53 PM
 */
public abstract class MultiTabToolWindow {

	private Map<String, ContentPanel> panelMap;

	protected abstract class ContentPanel extends JPanel {
		public abstract void unregister();
		public abstract String getTitle();
	}

	protected interface ContentParameters { }
	protected abstract String getContentKey(ContentParameters params);
	protected abstract ContentPanel createContentPanel(ContentParameters params);

	protected MultiTabToolWindow(Map<String, ContentPanel> panelMap) {
		this.panelMap = panelMap;
	}

	protected void showToolWindow(final Project project, ContentParameters params,
								  final String baseTitle, final Icon icon) {
		String contentKey = getContentKey(params);
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow itw = twm.getToolWindow(getExistingToolWindowTitle(baseTitle));
		if (itw != null) {
			twm.unregisterToolWindow(getExistingToolWindowTitle(baseTitle));
		}

		ContentPanel contentPanel = null;
		for (String s : panelMap.keySet()) {
			if (s.equals(contentKey)) {
				contentPanel = panelMap.get(contentKey);
				break;
			}
		}

		if (contentPanel == null) {
			contentPanel = createContentPanel(params);
			panelMap.put(contentKey, contentPanel);
		}

		createNewToolWindow(project, baseTitle, icon, contentPanel.getTitle());
	}

	protected void createNewToolWindow(final Project project, final String baseTitle, final Icon icon, String key) {
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		String title = createNewToolWindowTitle(baseTitle);
		twm.unregisterToolWindow(title);
		final ToolWindow issueToolWindow = twm.registerToolWindow(title, true, ToolWindowAnchor.BOTTOM);

		setToolWindowIconCargoCult(issueToolWindow, icon);

		issueToolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
			public void contentRemoved(ContentManagerEvent event) {
				super.contentRemoved(event);
				final String titleToRemove = getExistingToolWindowTitle(baseTitle);
				String key = event.getContent().getTabName();

				panelMap.get(key).unregister();
				panelMap.remove(key);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (panelMap.size() < 2) {
							twm.unregisterToolWindow(titleToRemove);
						}
						if (panelMap.size() == 1) {
							createNewToolWindow(project, baseTitle, icon, null);
						}
					}
				});
			}
		});

		fillToolWindowContents(key, icon, issueToolWindow);
	}

	private void fillToolWindowContents(String key, Icon icon, ToolWindow toolWindow) {
		boolean showTitle = panelMap.size() != 1;
		Content selectedContent = null;
		for (String s : panelMap.keySet()) {

			Content content = toolWindow.getContentManager().getFactory().createContent(panelMap.get(s),
					showTitle ? panelMap.get(s).getTitle() : "", true);

			if (showTitle) {
				content.setIcon(icon);
				content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			}
			if (key != null && key.equals(panelMap.get(s).getTitle())) {
				selectedContent = content;
			} else if (selectedContent == null) {
				selectedContent = content;
			}
			content.setTabName(s);
			toolWindow.getContentManager().addContent(content);
		}
		if (selectedContent != null) {
			toolWindow.getContentManager().setSelectedContent(selectedContent);
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

	protected <T extends ContentPanel> T getContentPanel(String key) {
		return (T) panelMap.get(key);
	}

	private String getExistingToolWindowTitle(String baseTitle) {
		String title = baseTitle;
		if (panelMap.size() == 1) {
			title = panelMap.values().iterator().next().getTitle();
		}
		return title;
	}

	private String createNewToolWindowTitle(String title) {
		return panelMap.size() == 1 ? panelMap.values().iterator().next().getTitle() : title;
	}

	protected void closeToolWindow(String title, AnActionEvent e) {
		Project project = IdeaHelper.getCurrentProject(e);

		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow tw = twm.getToolWindow(getExistingToolWindowTitle(title));
		if (tw != null) {
			String key = e.getPlace();

			for (Content c : tw.getContentManager().getContents()) {
				if (c.getTabName().equals(key)) {
					tw.getContentManager().removeContent(c, true);
					break;
				}
			}
		}
	}
}
