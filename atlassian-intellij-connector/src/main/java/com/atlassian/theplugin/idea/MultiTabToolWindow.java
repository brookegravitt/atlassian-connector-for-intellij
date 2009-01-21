package com.atlassian.theplugin.idea;

import com.intellij.openapi.actionSystem.AnActionEvent;
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
 * User: jgorycki
 * Date: Jan 6, 2009
 * Time: 3:08:53 PM
 */
public abstract class MultiTabToolWindow {

	private Map<String, ContentPanel> panelMap = new HashMap <String, ContentPanel>();

	protected abstract class ContentPanel extends JPanel {
		public abstract void unregister();
		public abstract String getTitle();
	}

	protected interface ContentParameters { }
	protected abstract String getContentKey(ContentParameters params);
	protected abstract ContentPanel createContentPanel(ContentParameters params);

	private boolean singleTabMode;

	protected MultiTabToolWindow(boolean singleTabMode) {
		this.singleTabMode = singleTabMode;
	}

	protected void showToolWindow(final Project project, ContentParameters params,
								  final String title, final Icon icon) {
		String contentKey = getContentKey(params);
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow itw = twm.getToolWindow(title);

		if (itw == null) {
			itw = createNewToolWindow(project, title, icon);
		}

		ContentPanel contentPanel = null;
		for (String s : panelMap.keySet()) {
			if (s.equals(contentKey)) {
				contentPanel = panelMap.get(contentKey);
				break;
			}
		}

		if (singleTabMode) {
			if (contentPanel != null) {
				Content c = itw.getContentManager().findContent(panelMap.get(contentKey).getTitle());
				itw.getContentManager().setSelectedContent(c);
			} else {
				if (panelMap.size() > 0) {
					itw.getContentManager().removeAllContents(true);
				}
				contentPanel = createContentPanel(params);
				panelMap.put(contentKey, contentPanel);
				fillToolWindowContents(contentKey, contentPanel, icon, itw);
			}
		} else {
			if (contentPanel == null) {
				contentPanel = createContentPanel(params);
				panelMap.put(contentKey, contentPanel);
				fillToolWindowContents(contentKey, contentPanel, icon, itw);
			} else {
				Content c = itw.getContentManager().findContent(panelMap.get(contentKey).getTitle());
				itw.getContentManager().setSelectedContent(c);
			}
		}
	}

	protected ToolWindow createNewToolWindow(final Project project, final String title, final Icon icon) {
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		final ToolWindow issueToolWindow = twm.registerToolWindow(title, true, ToolWindowAnchor.BOTTOM);

		setToolWindowIcon(issueToolWindow, icon);

		issueToolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
			public void contentRemoved(ContentManagerEvent event) {
				super.contentRemoved(event);
				String key = event.getContent().getTabName();

				panelMap.get(key).unregister();
				panelMap.remove(key);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (panelMap.size() == 0) {
							twm.unregisterToolWindow(title);
						}
					}
				});
			}
		});
		return issueToolWindow;
	}

	private void fillToolWindowContents(String key, ContentPanel panel, Icon icon, ToolWindow toolWindow) {
		Content content = toolWindow.getContentManager().getFactory().createContent(panel, panel.getTitle(), true);

		content.setIcon(icon);
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

		content.setTabName(key);
		toolWindow.getContentManager().addContent(content);

		toolWindow.getContentManager().setSelectedContent(content);
		toolWindow.show(null);
	}

	private static void setToolWindowIcon(ToolWindow toolWindow, Icon icon) {
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

	protected void closeToolWindow(String title, AnActionEvent e) {
		Project project = IdeaHelper.getCurrentProject(e);

		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow tw = twm.getToolWindow(title);
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
