package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.connector.intellij.bamboo.BambooServerFacade;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.bamboo.build.BuildDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.BuildLogPanel;
import com.atlassian.theplugin.idea.bamboo.build.CommitDetailsPanel;
import com.atlassian.theplugin.idea.bamboo.build.TestDetailsPanel;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 12:13:51 PM
 */
public class BuildToolWindow extends MultiTabToolWindow {

	private final Project project;
	private final PluginConfiguration pluginConfiguration;
	private static final String TOOL_WINDOW_TITLE = "Builds - Bamboo";

	public BuildToolWindow(@NotNull final Project project, @NotNull final PluginConfiguration pluginConfiguration) {
		super(false);
		this.project = project;
		this.pluginConfiguration = pluginConfiguration;
	}

	public void runTests(AnActionEvent ev, boolean debug) {
		BuildPanel bp = getContentPanel(ev.getPlace());
		if (bp != null) {
			bp.getTestDetailsPanel().runSelectedTests(ev.getDataContext(), debug);
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
					TOOL_WINDOW_TITLE, Constants.BAMBOO_BUILD_PANEL_ICON, Constants.BAMBOO_BUILD_TAB_ICON, null);
		}
	}

	public void showBuildAndRunTest(BambooBuildAdapterIdea build,
			@NotNull final String testPackage, @NotNull final String testClass, @NotNull final String testName) {
		if (build != null) {
			final BuildContentParameters params = new BuildContentParameters(build);
			final String contentKey = getContentKey(params);

			final DataContext dataContext = new DataContext() {
				@Nullable
				public Object getData(@NonNls final String dataId) {
					if (dataId.equalsIgnoreCase("project")) {
						return project;
					}
					return null;
				}
			};

			BuildPanel bp = getContentPanel(contentKey);

			if (bp != null) {
				showToolWindow(project, params,
						TOOL_WINDOW_TITLE, Constants.BAMBOO_BUILD_PANEL_ICON, Constants.BAMBOO_BUILD_TAB_ICON, null);

				bp.selectTestTab();
				bp.getTestDetailsPanel().runTests(dataContext, false, testPackage, testClass, testName);

			} else {
				showToolWindow(project, params,
						TOOL_WINDOW_TITLE, Constants.BAMBOO_BUILD_PANEL_ICON, Constants.BAMBOO_BUILD_TAB_ICON,
						new ToolWindowHandler() {

							public void dataLoaded() {
								BuildPanel bp2 = getContentPanel(contentKey);
								if (bp2 != null) {
									bp2.selectTestTab();
									bp2.getTestDetailsPanel().runTests(dataContext, false, testPackage, testClass, testName);
								}

							}
						});
			}
		}
	}

	@Override
	protected String getContentKey(ContentParameters params) {
		BuildContentParameters bcp = (BuildContentParameters) params;
		return bcp.build.getResultUrl();
	}

	@Override
	protected ContentPanel createContentPanel(ContentParameters params, ToolWindowHandler handler) {
		pluginConfiguration.getGeneralConfigurationData().bumpCounter("b");
		BuildContentParameters bcp = (BuildContentParameters) params;
		return new BuildPanel(bcp, handler);
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

		private final TestDetailsPanel tdp;

		private final JTabbedPane tabs = new JTabbedPane();
		private final BuildContentParameters params;

		private final Timer timer;
		private final CommitDetailsPanel cdp;

		public BuildPanel(final BuildContentParameters params, @Nullable final ToolWindowHandler handler) {
			this.params = params;

			final BuildDetailsPanel bdp = new BuildDetailsPanel(params.build);
			cdp = new CommitDetailsPanel(project, params.build);
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

			new BuildDetailsFetcher(handler).queue();
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

		public void selectTestTab() {
			tabs.setSelectedComponent(tdp);
			setPassedTestsVisible(getContentKey(params), true);
		}

		private class SummaryPanel extends JPanel {

			private final JEditorPane summary;

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

		private class BuildDetailsFetcher extends Task.Backgroundable {
			private final ToolWindowHandler handler;

			public BuildDetailsFetcher(final ToolWindowHandler handler) {
				super(project, "Retrieving Build Details", false);
				this.handler = handler;
			}

			@Override
			public void run(final ProgressIndicator indicator) {

				indicator.setIndeterminate(true);

				BambooBuildAdapterIdea build = params.build;

				final BambooServerFacade bambooFacade = IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger());

				try {
					final BuildDetails details = bambooFacade.getBuildDetails(
							build.getServer(), build.getPlanKey(), build.getNumber());

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							cdp.fillContent(details.getCommitInfo());
							tdp.fillContent(details);
							if (handler != null) {
								handler.dataLoaded();
							}
						}
					});
				} catch (ServerPasswordNotProvidedException e) {
					cdp.showError(e);
					tdp.showError(e);
				} catch (RemoteApiException e) {
					cdp.showError(e);
					tdp.showError(e);
				}
			}
		}
	}
}
