package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.BuildStatus;
import static com.atlassian.theplugin.bamboo.BuildStatus.BUILD_FAILED;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.content.ContentManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This listener fires notification tooltip if bamboo build has changes status form SUCCEED to FAILED
 */
public class BambooStatusListenerImpl implements BambooStatusListener {

	//private BambooStatusDisplay bambooDisplay;
	private Project project;

	private Map buildPrevStatus = new HashMap<String, BuildStatus>(0);
	private static final Color BACKGROUND_COLOR = new Color(255, 214, 214);

	/**
	 *
	 * @param aProject reference to project
	 */
	public BambooStatusListenerImpl(Project aProject /*BambooStatusDisplay display*/) {
		//bambooDisplay = display;
		this.project = aProject;
	}

	public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
		String tooltipContent = new String();

		BuildStatus status = BuildStatus.BUILD_SUCCEED;

		if (buildStatuses != null && buildStatuses.size() > 0) {
			for (BambooBuild buildInfo : buildStatuses) {
				switch (buildInfo.getStatus()) {
					case BUILD_FAILED:
						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							if (buildPrevStatus.get(buildInfo.getBuildKey()) == BuildStatus.BUILD_SUCCEED) {
								// build has changes status from SUCCEED to FAILED
								status = BUILD_FAILED;
								// prepare information
								tooltipContent += createHtmlRow(buildInfo.getBuildKey(), buildInfo.getBuildNumber(), buildInfo.getBuildResultUrl());
							}
							buildPrevStatus.remove(buildInfo.getBuildKey());
						}

						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());

						break;
					case UNKNOWN:
						// no action here
						break;
					case BUILD_SUCCEED:

						if (buildPrevStatus.containsKey(buildInfo.getBuildKey())) {
							buildPrevStatus.remove(buildInfo.getBuildKey());
						}
						buildPrevStatus.put(buildInfo.getBuildKey(), buildInfo.getStatus());

						break;
					default:
						throw new IllegalStateException("Unexpected build status encountered");
				}
			}
		}

		if (status == BuildStatus.BUILD_FAILED) {

			JEditorPane content = new JEditorPane();
			content.setEditable(false);
			content.setContentType("text/html");
			content.setBackground(BACKGROUND_COLOR);
			content.setText(tooltipContent);

			content.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent	e) {
						ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ThePluginProjectComponent.TOOL_WINDOW_NAME);
						ContentManager contentManager = toolWindow.getContentManager();
						toolWindow.activate(null);
						contentManager.setSelectedContent(contentManager.getContent(1));
					}
				});

			content.addHyperlinkListener(new GenericHyperlinkListener());

			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(content, BorderLayout.CENTER);

			// fire notification popup
			WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(panel, null);
		}

		//bambooDisplay.updateBambooStatus(status, "");
	}

	private String createHtmlRow(String buildKey, String buildNumber, String url) {

		return "<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif; font-weight: bold; color: red\">" + "<a href=\"" + url + "\">" + buildKey + "-" + buildNumber + "</a> failed" +"</div><br />";

	}
}
