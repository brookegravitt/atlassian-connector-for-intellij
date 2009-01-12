package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAUserNameCache;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.jira.model.JIRAIssueListModel;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelListener;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: jgorycki
 * Date: Dec 23, 2008
 * Time: 3:59:21 PM
 */
public final class IssueToolWindow extends MultiTabToolWindow {
	private static final String TOOL_WINDOW_TITLE = "Issues";
	private static final String[] NONE = {"None"};

	//CHECKSTYLE\:MAGIC\:OFF
	private static final Color HEADER_BACKGROUND_COLOR = new Color(153, 153, 153);
	//CHECKSTYLE\:MAGIC\:ON

	private static JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
	private final Project project;

	public IssueToolWindow(@NotNull final Project project) {
		super(new HashMap<String, ContentPanel>());
		this.project = project;
	}

	private final class IssueContentParameters implements ContentParameters {
		private final JiraServerCfg server;
		// mutable because model may update the issue and we want to know about it (we have listener in place)
		private JIRAIssue issue;
		private final JIRAIssueListModel model;

		private IssueContentParameters(JiraServerCfg server, JIRAIssue issue, JIRAIssueListModel model) {
			this.server = server;
			this.issue = issue;
			this.model = model;
		}
	}

	public void showIssue(JiraServerCfg server, final JIRAIssue issue, JIRAIssueListModel model) {
		showToolWindow(project, new IssueContentParameters(server, issue, model),
				TOOL_WINDOW_TITLE, Constants.JIRA_ISSUE_ICON);
	}

	protected ContentPanel createContentPanel(ContentParameters params) {
		return new IssuePanel((IssueContentParameters) params);
	}

	protected String getContentKey(ContentParameters params) {
		IssueContentParameters icp = (IssueContentParameters) params;
		return icp.server.getUrl() + icp.server.getUsername() + icp.issue.getKey();
	}

	public void setCommentsExpanded(String key, boolean expanded) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.setAllVisible(expanded);
		}
	}

	public void closeToolWindow(AnActionEvent e) {
		closeToolWindow(TOOL_WINDOW_TITLE, e);
	}

	public void refreshComments(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.refreshComments();
		}
	}

	public void addComment(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.addComment();
		}
	}

	public void viewIssueInBrowser(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			BrowserUtil.launchBrowser(ip.params.issue.getIssueUrl());
		}
	}

	public void editIssueInBrowser(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			JIRAIssue issue = ip.params.issue;
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public JIRAIssue getIssue(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			return ip.params.issue;
		}
		return null;
	}

	public void refresh(String key) {
		IssuePanel ip = getContentPanel(key);
		if (ip != null) {
			ip.refresh();
		}
	}

	private class IssuePanel extends ContentPanel implements JIRAIssueListModelListener {
		private DescriptionAndCommentsPanel descriptionAndCommentsPanel;
		private DetailsPanel detailsPanel;
		private SummaryPanel summaryPanel;
		private final IssueContentParameters params;

		public IssuePanel(IssueContentParameters params) {
			this.params = params;

			JTabbedPane tabs = new JTabbedPane();
			detailsPanel = new DetailsPanel();
			tabs.addTab("Details", detailsPanel);
			descriptionAndCommentsPanel = new DescriptionAndCommentsPanel(tabs, 1);
			tabs.addTab("Comments(0)", descriptionAndCommentsPanel);

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN, 0, 0);
			summaryPanel = new SummaryPanel();
			add(summaryPanel, gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);

			if (params.model != null) {
				params.model.addModelListener(this);
			}
			refresh();
		}

		public String getTitle() {
			return "Issue " + params.issue.getKey();
		}

		public void modelChanged(JIRAIssueListModel m) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					refresh();
				}
			});
		}

		public void issuesLoaded(JIRAIssueListModel m, int loadedIssues) {
		}

		public void unregister() {
			if (params.model != null) {
				params.model.removeModelListener(this);
			}
		}

		public void refresh() {
			for (JIRAIssue i : params.model.getIssues()) {
				if (i.getKey().equals(params.issue.getKey())) {
					params.issue = i;
					break;
				}
			}
			descriptionAndCommentsPanel.refreshComments();
			detailsPanel.refresh();
			summaryPanel.refresh();
		}

		private class DetailsPanel extends JPanel {

			private JLabel affectsVersions = new JLabel("Fetching...");
			private JLabel fixVersions = new JLabel("Fetching...");
			private JLabel components = new JLabel("Fetching...");
			private JLabel affectsVersionsLabel = new BoldLabel("Affects Version/s");
			private JLabel fixVersionsLabel = new BoldLabel("Fix Version/s");
			private JLabel componentsLabel = new BoldLabel("Component/s");
			private JScrollPane scroll;

			public DetailsPanel() {
				setLayout(new GridBagLayout());

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;


				scroll = new JScrollPane(createBody());
				scroll.setBorder(BorderFactory.createEmptyBorder());
				add(scroll, gbc);
			}

			private JPanel createBody() {
				JPanel body = new JPanel();

				body.setLayout(new GridBagLayout());

				GridBagConstraints gbc1 = new GridBagConstraints();
				GridBagConstraints gbc2 = new GridBagConstraints();
				gbc1.anchor = GridBagConstraints.FIRST_LINE_END;
				gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc1.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.fill = GridBagConstraints.HORIZONTAL;
				gbc2.weightx = 1.0;
				gbc1.gridx = 0;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;

				body.add(new BoldLabel("Type"), gbc1);
				body.add(new JLabel(params.issue.getType(),
						CachedIconLoader.getIcon(params.issue.getTypeIconUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				body.add(new BoldLabel("Status"), gbc1);
				body.add(new JLabel(params.issue.getStatus(),
						CachedIconLoader.getIcon(params.issue.getStatusTypeUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Priority"), gbc1);
				body.add(new JLabel(params.issue.getPriority(),
						CachedIconLoader.getIcon(params.issue.getPriorityIconUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Assignee"), gbc1);
				// bleeeee :( - assignee ID (String value) equals "-1" for unassigned issues. Oh my...
				if (!params.issue.getAssigneeId().equals("-1")) {
					body.add(new UserLabel(params.issue.getServerUrl(), params.issue.getAssignee(),
							params.issue.getAssigneeId()), gbc2);
				} else {
					body.add(new JLabel("Unassigned"), gbc2);
				}
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Reporter"), gbc1);
				body.add(new UserLabel(params.issue.getServerUrl(), params.issue.getReporter(),
						params.issue.getReporterId()), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Resolution"), gbc1);
				body.add(new JLabel(params.issue.getResolution()), gbc2);
				gbc1.gridx = 2;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;
				gbc1.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				body.add(new BoldLabel("Created"), gbc1);
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
				DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				String t;
				try {
					t = ds.format(df.parse(params.issue.getCreated()));
				} catch (ParseException e) {
					t = "Invalid";
				}
				body.add(new JLabel(t), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				body.add(new BoldLabel("Updated"), gbc1);
				try {
					t = ds.format(df.parse(params.issue.getUpdated()));
				} catch (ParseException e) {
					t = "Invalid";
				}
				body.add(new JLabel(t), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(affectsVersionsLabel, gbc1);
				body.add(affectsVersions, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(fixVersionsLabel, gbc1);
				body.add(fixVersions, gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(componentsLabel, gbc1);
				body.add(components, gbc2);

				gbc1.gridy++;
				gbc1.weighty = 1.0;
				gbc1.fill = GridBagConstraints.VERTICAL;
				body.add(new JPanel(), gbc1);

				return body;
			}

			public JLabel getAffectVersionsLabel() {
				return affectsVersions;
			}

			public JLabel getFixVersionsLabel() {
				return fixVersions;
			}

			public JLabel getComponentsLabel() {
				return components;
			}

			public void setAffectsVersions(String[] versions) {
				if (versions.length < 2) {
					affectsVersionsLabel.setText("Affects Version");
				} else {
					affectsVersionsLabel.setText("Affects Versions");
				}
				setLabelText(getAffectVersionsLabel(), versions);
			}

			public void setFixVersions(String[] versions) {
				if (versions.length < 2) {
					fixVersionsLabel.setText("Fix Version");
				} else {
					fixVersionsLabel.setText("Fix Versions");
				}
				setLabelText(getFixVersionsLabel(), versions);
			}

			public void setComponents(String[] components) {
				if (components.length < 2) {
					componentsLabel.setText("Component");
				} else {
					componentsLabel.setText("Components");
				}

				setLabelText(getComponentsLabel(), components);
			}

			private void setLabelText(JLabel label, String[] texts) {
				if (texts.length == 0) {
					label.setText(NONE[0]);
				} else {

					StringBuffer txt = new StringBuffer();
					for (int i = 0; i < texts.length; ++i) {
						if (i > 0) {
							txt.append(", ");
						}
						txt.append(texts[i]);
					}
					label.setText(txt.toString());
				}
			}

			private String[] getStringArray(java.util.List<JIRAConstant> l) {
				if (l == null) {
					return NONE;
				}
				java.util.List<String> sl = new ArrayList<String>(l.size());
				for (JIRAConstant c : l) {
					sl.add(c.getName());
				}
				return sl.toArray(new String[l.size()]);
			}


			public synchronized void getMoreIssueDetails() {
				if ((params.issue.getAffectsVersions() == null)
						|| (params.issue.getFixVersions() == null)
						|| (params.issue.getComponents() == null)) {

					Runnable runnable = new Runnable() {
						private String[] errorString = null;

						public void run() {

							try {
								if (params.server != null) {
									final JIRAIssue issueDetails = facade.getIssueDetails(params.server, params.issue);
									params.issue.setAffectsVersions(issueDetails.getAffectsVersions());
									params.issue.setFixVersions(issueDetails.getFixVersions());
									params.issue.setComponents(issueDetails.getComponents());
								}
							} catch (JIRAException e) {
								errorString = new String[]{"Unable to retrieve"};
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (errorString == null) {
										setAffectsVersions(getStringArray(params.issue.getAffectsVersions()));
										setFixVersions(getStringArray(params.issue.getFixVersions()));
										setComponents(getStringArray(params.issue.getComponents()));
									} else {
										getAffectVersionsLabel().setForeground(Color.RED);
										getFixVersionsLabel().setForeground(Color.RED);
										getComponentsLabel().setForeground(Color.RED);
										setAffectsVersions(errorString);
										setFixVersions(errorString);
										setComponents(errorString);
									}
								}
							});
						}
					};
					new Thread(runnable, "atlassian-idea-plugin get issue details").start();
				} else {
					setAffectsVersions(getStringArray(params.issue.getAffectsVersions()));
					setFixVersions(getStringArray(params.issue.getFixVersions()));
					setComponents(getStringArray(params.issue.getComponents()));
				}
			}

			public void refresh() {
				scroll.setViewportView(createBody());
				getMoreIssueDetails();
			}
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
				refresh();
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
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JiraIssues.OneIssueToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);

				JComponent comp = toolbar.getComponent();
				add(comp, gbc);
			}

			public void refresh() {
				String txt = "<html><body><a href=\"" + params.issue.getIssueUrl() + "\">"
						+ params.issue.getKey() + "</a> " + params.issue.getSummary() + "</body></html>";
				summary.setText(txt);
			}
		}

		private class ScrollablePanel extends JPanel implements Scrollable {
			private static final int A_LOT = 100000;

			// cheating obviously but this seems to do the right thing, so whatever :)
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(1, A_LOT);
			}

			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 1;
			}

			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 1;
			}

			public boolean getScrollableTracksViewportWidth() {
				return true;
			}

			public boolean getScrollableTracksViewportHeight() {
				return false;
			}
		}

		private class DescriptionAndCommentsPanel extends JPanel {

			private final Splitter splitPane = new Splitter(false, PluginToolWindowPanel.PANEL_SPLIT_RATIO);

			private ScrollablePanel comments = new ScrollablePanel();
			private JScrollPane scroll = new JScrollPane();

			private Border border = BorderFactory.createTitledBorder("Comments");
			private final JTabbedPane tabs;
			private final int tabIndex;

			public DescriptionAndCommentsPanel(JTabbedPane tabs, int tabIndex) {
				this.tabs = tabs;
				this.tabIndex = tabIndex;

				JPanel rightPanel = new JPanel();
				rightPanel.setLayout(new GridBagLayout());

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.fill = GridBagConstraints.HORIZONTAL;

				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JiraIssues.CommentsToolBar");
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);

				JComponent comp = toolbar.getComponent();
				rightPanel.add(comp, gbc);

				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.fill = GridBagConstraints.BOTH;

				gbc.weighty = 1.0;
				comments.setLayout(new VerticalFlowLayout());
				scroll.setViewportView(comments);
				scroll.getViewport().setOpaque(false);
				scroll.setOpaque(false);
				scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				scroll.setBorder(BorderFactory.createEmptyBorder());

				JPanel wrap = new JPanel();
				wrap.setBorder(border);
				wrap.setLayout(new BorderLayout());
				wrap.add(scroll, BorderLayout.CENTER);
				rightPanel.add(wrap, gbc);

				splitPane.setFirstComponent(new DescriptionPanel());
				splitPane.setSecondComponent(rightPanel);

				setLayout(new BorderLayout());
				splitPane.setShowDividerControls(false);
				add(splitPane, BorderLayout.CENTER);
			}

			public void setTitle(String title) {
				border = BorderFactory.createTitledBorder(title);
				setBorder(border);
			}

			public void addComment(JIRAComment c) {
				CommentPanel p = new CommentPanel(comments.getComponents().length + 1, c, params.server, tabs);
				comments.add(p);
			}

			public void clearComments() {
				comments.removeAll();
			}

			public void setAllVisible(boolean visible) {
				for (Component c : comments.getComponents()) {
					((CommentPanel) c).getShowHideButton().setState(visible);
				}
			}

			public void scrollToFirst() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						scroll.getVerticalScrollBar().setValue(0);
					}
				});
			}

			public void addComment() {
				final IssueCommentDialog issueCommentDialog = new IssueCommentDialog(params.issue.getKey());
				issueCommentDialog.show();
				if (issueCommentDialog.isOK()) {
					Runnable runnable = new Runnable() {
						public void run() {
							try {
								if (params.server != null) {
									facade.addComment(params.server, params.issue, issueCommentDialog.getComment());
									EventQueue.invokeLater(new Runnable() {
										public void run() {
											refreshComments();
										}
									});
								}
							} catch (JIRAException e) {
								final String msg = e.getMessage();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										Messages.showMessageDialog(
												"Failed to add comment to issue " + params.issue.getKey() + ": " + msg,
												"Error", Messages.getErrorIcon());
									}
								});
							}
						}
					};
					new Thread(runnable, "atlassian-idea-plugin comment issue from editor").start();
				}
			}

			public void refreshComments() {

				tabs.setTitleAt(tabIndex, "Refreshing comments...");
				final Runnable runnable = new Runnable() {
					public void run() {
						try {
							if (params.server != null) {
								final java.util.List<JIRAComment> cmts =
										facade.getComments(params.server, params.issue);

								for (JIRAComment c : cmts) {
									try {
										JIRAUserBean u = JIRAUserNameCache.getInstance()
												.getUser(params.server, c.getAuthor());
										c.setAuthorFullName(u.getName());
									} catch (JiraUserNotFoundException e) {
										c.setAuthorFullName(c.getAuthor());
									}
								}

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										clearComments();
										resetStackTraces();
										int size = cmts.size();
										if (size > 0) {
											for (JIRAComment c : cmts) {
												addComment(c);
											}
										}
										tabs.setTitleAt(tabIndex, "Comments(" + size + ")");
									}
								});
							}
						} catch (JIRAException e) {
							tabs.setTitleAt(tabIndex, "Unable to retrieve comments");
						}
					}
				};
				new Thread(runnable, "atlassian-idea-plugin refresh comments").start();
			}

			private void resetStackTraces() {
				while (tabs.getTabCount() > 2) {
					tabs.remove(2);
				}

				String stack = Html2text.translate(params.issue.getDescription());
				if (StackTraceDetector.containsStackTrace(stack)) {
					tabs.add("Stack Trace: Description", new StackTracePanel(stack));
				}
			}
		}

		private abstract class AbstractShowHideButton extends JLabel {

			private Icon right = IconLoader.findIcon("/icons/navigate_right_10.gif");
			private Icon down = IconLoader.findIcon("/icons/navigate_down_10.gif");
			private boolean shown = true;

			public AbstractShowHideButton() {
				setHorizontalAlignment(0);
				setIcon(down);
				setToolTipText(getTooltip());
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						click();
					}
				});
			}

			public void setState(boolean visible) {
				shown = visible;
				setIcon(shown ? down : right);
				setComponentVisible(shown);
			}

			public void click() {
				shown = !shown;
				setState(shown);
			}

			protected abstract void setComponentVisible(boolean visible);

			protected abstract String getTooltip();
		}

		private class ShowHideButton extends AbstractShowHideButton {
			private JComponent body;
			private JComponent container;

			public ShowHideButton(JComponent body, JComponent container) {
				this.body = body;
				this.container = container;
			}

			@Override
			protected void setComponentVisible(boolean visible) {
				body.setVisible(visible);
				container.validate();
				container.getParent().validate();
			}

			@Override
			protected String getTooltip() {
				return "Collapse/Expand";
			}
		}

		private class UserLabel extends HyperlinkLabel {
			UserLabel(final String serverUrl, final String userName, final String userNameId, Color color) {
				super(userName, color, HEADER_BACKGROUND_COLOR, color);
				addListener(serverUrl, userNameId);
			}

			UserLabel(final String serverUrl, final String userName, final String userNameId) {
				super(userName);
				addListener(serverUrl, userNameId);
			}

			private void addListener(final String serverUrl, final String userNameId) {
				addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						BrowserUtil.launchBrowser(
								serverUrl
										+ "/secure/ViewProfile.jspa?name="
										+ userNameId);
					}
				});
			}
		}

		private class WhiteLabel extends JLabel {
			public WhiteLabel() {
				setForeground(UIUtil.getTableSelectionForeground());
			}
		}

		private class DescriptionPanel extends JPanel {
			public DescriptionPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridx = 0;
				gbc.gridy = 0;

				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;

				JEditorPane body = new JEditorPane();
				JScrollPane sp = new JScrollPane(body,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				sp.setBorder(BorderFactory.createEmptyBorder());
				sp.setOpaque(false);
				body.setEditable(false);
				body.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});

				body.setOpaque(false);
				body.setBorder(BorderFactory.createEmptyBorder());
				body.setContentType("text/html");
				body.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				body.setText("<html><head></head><body>" + params.issue.getDescription() + "</body></html>");
				sp.getViewport().setOpaque(false);
				body.setCaretPosition(0);
				add(sp, gbc);

				Border b = BorderFactory.createTitledBorder("Description");
				setBorder(b);
				Insets i = b.getBorderInsets(this);
				int minHeight = i.top + i.bottom;
				setMinimumSize(new Dimension(0, minHeight));
			}
		}

		private class CommentPanel extends JPanel {

			private ShowHideButton btnShowHide;

			public CommentPanel(int cmtNumber, final JIRAComment comment, final JiraServerCfg server, JTabbedPane tabs) {
				setOpaque(true);
				setBackground(HEADER_BACKGROUND_COLOR);

				setLayout(new GridBagLayout());
				GridBagConstraints gbc;

				JEditorPane commentBody = new JEditorPane();
				btnShowHide = new ShowHideButton(commentBody, this);
				gbc = new GridBagConstraints();
				gbc.gridx++;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				add(btnShowHide, gbc);

				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
				JLabel commentNumber = new WhiteLabel();
				commentNumber.setText(Integer.valueOf(cmtNumber).toString() + ".");
				add(commentNumber, gbc);

				gbc.gridx++;
				UserLabel ul = new UserLabel(server.getUrl(), comment.getAuthorFullName(),
						comment.getAuthor(), UIUtil.getTableSelectionForeground());
				add(ul, gbc);

				final JLabel hyphen = new WhiteLabel();
				hyphen.setText("-");
				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
				add(hyphen, gbc);

				final JLabel creationDate = new WhiteLabel();
				creationDate.setText(comment.getCreationDate().getTime().toString());
				gbc.gridx++;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.insets = new Insets(0, 0, 0, 0);
				add(creationDate, gbc);

				if (StackTraceDetector.containsStackTrace(comment.getBody())) {
					tabs.add("Stack Trace: Comment #" + cmtNumber, new StackTracePanel(comment.getBody()));
				}

				int gridwidth = gbc.gridx + 1;

				commentBody.setEditable(false);
				commentBody.setOpaque(true);
				commentBody.setBackground(UIUtil.getPanelBackground());
				commentBody.setMargin(new Insets(0, Constants.DIALOG_MARGIN, 0, 0));
				commentBody.setContentType("text/html");
				commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				commentBody.setText("<html><head></head><body>" + comment.getBody() + "</body></html>");
				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.gridwidth = gridwidth;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;
				add(commentBody, gbc);
			}

			public AbstractShowHideButton getShowHideButton() {
				return btnShowHide;
			}
		}

		private class StackTracePanel extends JPanel {
			public StackTracePanel(String stack) {

				TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
				TextConsoleBuilder builder = factory.createBuilder(project);
				ConsoleView console = builder.getConsole();
				console.print(stack, ConsoleViewContentType.NORMAL_OUTPUT);

				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;
				add(console.getComponent(), gbc);
			}
		}
	}
}
