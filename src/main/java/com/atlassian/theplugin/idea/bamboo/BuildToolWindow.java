package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.bamboo.build.BuildDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.CommitDetailsPanel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 12:13:51 PM
 */
public class BuildToolWindow extends MultiTabToolWindow {

	private final Project project;
	private static final String TOOL_WINDOW_TITLE = "Builds";

	public BuildToolWindow(@NotNull final Project project) {
		super(new HashMap<String, ContentPanel>());
		this.project = project;
	}

	private final class BuildContentParameters implements ContentParameters {
		private final BambooBuildAdapterIdea build;
		private BuildContentParameters(BambooBuildAdapterIdea build) {
			this.build = build;
		}
	}

	public void showBuild(BambooBuildAdapterIdea build) {
	   	showToolWindow(project, new BuildContentParameters(build),
				   TOOL_WINDOW_TITLE, Constants.BAMBOO_BUILD_ICON);
	}

	protected String getContentKey(ContentParameters params) {
		BuildContentParameters bcp = (BuildContentParameters) params;
		return bcp.build.getBuildUrl();
	}

	protected ContentPanel createContentPanel(ContentParameters params) {
		BuildContentParameters bcp = (BuildContentParameters) params;
		return new BuildPanel(bcp);
	}

	private class BuildPanel extends ContentPanel {

		private BambooBuildAdapterIdea build;

		private JTabbedPane tabs = new JTabbedPane();

		public BuildPanel(BuildContentParameters params) {
			build = params.build;

			tabs.addTab("Details", new BuildDetailsPanel(build));
			tabs.addTab("Changes", new CommitDetailsPanel(project, build));

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
//			gbc.weighty = 0.0;
//			gbc.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN, 0, 0);
//			summaryPanel = new SummaryPanel();
//			add(summaryPanel, gbc);
//			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);
		}

		public void unregister() {
		}

		public String getTitle() {
			return build.getBuildKey() + "-" + build.getBuildNumber();
		}
	}
}
