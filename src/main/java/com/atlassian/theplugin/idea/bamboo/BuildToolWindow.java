package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.bamboo.build.BuildDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.CommitDetailsPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ide.BrowserUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
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

		private JTabbedPane tabs = new JTabbedPane();
		private final BuildContentParameters params;

		public BuildPanel(BuildContentParameters params) {
			this.params = params;

			tabs.addTab("Details", new BuildDetailsPanel(params.build));
			tabs.addTab("Changes", new CommitDetailsPanel(project, params.build));

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN, 0, 0);
			add(new SummaryPanel(), gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);
		}

		public void unregister() {
		}

		public String getTitle() {
			return params.build.getBuildKey() + "-" + params.build.getBuildNumber();
		}

		private class SummaryPanel extends JPanel {

			private JEditorPane summary;

			public SummaryPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridy = 0;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				summary = new JEditorPane();
				summary.setContentType("text/html");
				summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				setSummaryText();
				summary.setEditable(false);
				summary.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});

				summary.setFont(summary.getFont().deriveFont(Font.BOLD));
				summary.setOpaque(false);
				JPanel p = new JPanel();
				p.setLayout(new GridBagLayout());
				GridBagConstraints gbcp = new GridBagConstraints();
				gbcp.fill = GridBagConstraints.BOTH;
				gbcp.weightx = 1.0;
				gbcp.weighty = 1.0;
				gbcp.gridx = 0;
				gbcp.gridy = 0;
				p.add(summary, gbcp);
				add(p, gbc);

				gbc.gridy++;

				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.BuildToolWindowToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);

				JComponent comp = toolbar.getComponent();
				add(comp, gbc);
			}

			public void setSummaryText() {
				String txt = "<html><body><a href=\"" + params.build.getBuildUrl() + "\">"
						+ params.build.getBuildName() + "</a> "
						+ params.build.getProjectName() + "</body></html>";
				summary.setText(txt);
			}

		}
	}
}
