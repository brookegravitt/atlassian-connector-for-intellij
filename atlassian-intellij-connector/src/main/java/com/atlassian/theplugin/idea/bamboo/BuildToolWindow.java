package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.bamboo.build.BuildDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.BuildLogPanel;
import com.atlassian.theplugin.idea.bamboo.build.CommitDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.TestDetailsPanel;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 12:13:51 PM
 */
public class BuildToolWindow extends MultiTabToolWindow {

	private final Project project;
    private PluginConfiguration pluginConfiguration;
    private static final String TOOL_WINDOW_TITLE = "Builds - Bamboo";

	public BuildToolWindow(@NotNull final Project project, @NotNull final PluginConfiguration pluginConfiguration) {
		super(false);
		this.project = project;
        this.pluginConfiguration = pluginConfiguration;
    }

	public void runTests(AnActionEvent ev, boolean debug) {
		BuildPanel bp = getContentPanel(ev.getPlace());
		if (bp != null) {
			bp.getTestDetailsPanel().runTests(ev, debug);
		}
	}

	public boolean canRunTests(String key) {
		BuildPanel bp = getContentPanel(key);
		return bp != null && bp.getTestDetailsPanel().canRunTests();
	}

	public void jumpToSource(String key) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			bp.getTestDetailsPanel().jumpToSource();
		}
	}

	public boolean canJumpToSource(String key) {
		BuildPanel bp = getContentPanel(key);
		return bp != null && bp.getTestDetailsPanel().canJumpToSource();
	}

	public void setPassedTestsVisible(String key, boolean visible) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			bp.getTestDetailsPanel().setPassedTestsVisible(visible);
		}
	}

	public boolean isPassedTestsVisible(String key) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			return bp.getTestDetailsPanel().isPassedTestsVisible();
		}
		return false;
	}

	public void expandTests(String key) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			bp.getTestDetailsPanel().expand();
		}
	}

	public void collapseTestTree(String key) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			bp.getTestDetailsPanel().collapse();
		}
	}

	private final class BuildContentParameters implements ContentParameters {
		private final BambooBuildAdapterIdea build;
		private BuildContentParameters(BambooBuildAdapterIdea build) {
			this.build = build;
		}
	}

	public void showBuild(BambooBuildAdapterIdea build) {
		if (build != null) {
		   showToolWindow(project, new BuildContentParameters(build),
				   TOOL_WINDOW_TITLE, Constants.BAMBOO_BUILD_PANEL_ICON, Constants.BAMBOO_BUILD_TAB_ICON);
		}
	}

	@Override
	protected String getContentKey(ContentParameters params) {
		BuildContentParameters bcp = (BuildContentParameters) params;
		return bcp.build.getResultUrl();
	}

	@Override
	protected ContentPanel createContentPanel(ContentParameters params) {
        pluginConfiguration.getGeneralConfigurationData().bumpCounter("b");
		BuildContentParameters bcp = (BuildContentParameters) params;
		return new BuildPanel(bcp);
	}

	public void closeToolWindow(AnActionEvent e) {
		closeToolWindow(TOOL_WINDOW_TITLE, e);
	}

	public void viewBuildInBrowser(String key) {
		BuildPanel bp = getContentPanel(key);
		if (bp != null) {
			BrowserUtil.launchBrowser(bp.params.build.getResultUrl());
		}
	}

	public BambooBuildAdapterIdea getBuild(String key) {
		BuildPanel p = getContentPanel(key);
		if (p != null) {
			return p.params.build;
		}
		return null;
	}

	private class BuildPanel extends ContentPanel {

		private static final int ONE_MINUTE = 60000;

		private TestDetailsPanel tdp;

		private JTabbedPane tabs = new JTabbedPane();
		private final BuildContentParameters params;

		private Timer timer;

		public BuildPanel(BuildContentParameters params) {
			this.params = params;

			final BuildDetailsPanel bdp = new BuildDetailsPanel(params.build);
			final CommitDetailsPanel cdp = new CommitDetailsPanel(project, params.build);
			tdp = new TestDetailsPanel(project, params.build, getContentKey(params));
			final BuildLogPanel blp = new BuildLogPanel(project, params.build);
			timer = new Timer(ONE_MINUTE, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					bdp.actionPerformed(e);
					cdp.actionPerformed(e);
					tdp.actionPerformed(e);
					blp.actionPerformed(e);
				}
			});
			tabs.addTab("Details", bdp);
			tabs.addTab("Changes", cdp);
			tabs.addTab("Tests", tdp);
			tabs.addTab("Build Log", blp);

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

			timer.setRepeats(true);
			timer.setInitialDelay(ONE_MINUTE);
			timer.start();
		}

		public TestDetailsPanel getTestDetailsPanel() {
			return tdp;
		}

		@Override
		public void unregister() {
			timer.stop();
		}

		@Override
		public String getTitle() {
			return params.build.getPlanKey() + "-" + params.build.getBuildNumberAsString();
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
						+ params.build.getPlanKey() + "</a> "
						+ params.build.getProjectName() + " - " + params.build.getPlanName() + "</body></html>";
				summary.setText(txt);
			}

		}
	}
}
