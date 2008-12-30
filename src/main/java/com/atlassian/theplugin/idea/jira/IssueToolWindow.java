package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.PluginToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAUserNameCache;
import com.atlassian.theplugin.jira.api.*;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Dec 23, 2008
 * Time: 3:59:21 PM
 */
public final class IssueToolWindow {
	private static final String TOOL_WINDOW_TITLE = "JIRA Issues";

	//CHECKSTYLE\:MAGIC\:OFF
	private static final Color HEADER_BACKGROUND_COLOR = new Color(153, 153, 153);
	//CHECKSTYLE\:MAGIC\:ON

	private static final String[] NONE = {"None"};

	private static Map<String, IssuePanel> panelMap = new HashMap<String, IssuePanel>();

	private static JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();

	private IssueToolWindow() { }

	public static void showIssue(final Project project, JiraServerCfg server, final JIRAIssue issue) {
		String contentKey = getContentKey(server, issue);

		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		ToolWindow itw = twm.getToolWindow(getExistingToolWindowTitle());
		if (itw != null) {
			twm.unregisterToolWindow(getExistingToolWindowTitle());
		}

		IssuePanel issuePanel = null;
		for (String s : panelMap.keySet()) {
			if (s.equals(contentKey)) {
				issuePanel = panelMap.get(contentKey);
				break;
			}
		}

		if (issuePanel == null) {
			issuePanel = new IssuePanel(project, server, issue);
			panelMap.put(contentKey, issuePanel);
		}

		createNewToolWindow(project, issue);
	}

	private static void createNewToolWindow(final Project project, final JIRAIssue issue) {
		final ToolWindowManager twm = ToolWindowManager.getInstance(project);
		String title = createNewToolWindowTitle();
		twm.unregisterToolWindow(title);
		final ToolWindow issueToolWindow = twm.registerToolWindow(title, true, ToolWindowAnchor.BOTTOM);

		setToolWindowIconCargoCult(issueToolWindow, Constants.JIRA_ISSUE_ICON);

		issueToolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
			public void contentRemoved(ContentManagerEvent event) {
				super.contentRemoved(event);
				final String titleToRemove = getExistingToolWindowTitle();
				panelMap.remove(event.getContent().getTabName());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (panelMap.size() < 2) {
							twm.unregisterToolWindow(titleToRemove);
						}
						if (panelMap.size() == 1) {
							createNewToolWindow(project, null);
						}
					}
				});
			}
		});

		fillToolWindowContents(issue, issueToolWindow);
	}

	private static void setToolWindowIconCargoCult(ToolWindow toolWindow, Icon icon) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		Icon i = new ImageIcon(bi);
		toolWindow.setIcon(i);
	}

	private static void fillToolWindowContents(JIRAIssue issue, ToolWindow issueToolWindow) {
		boolean showTitle = panelMap.size() != 1;
		Content selectedContent = null;
		for (String s : panelMap.keySet()) {

			Content content = issueToolWindow.getContentManager().getFactory().createContent(panelMap.get(s),
					showTitle ? panelMap.get(s).issue.getKey() : "", true);

			if (showTitle) {
				content.setIcon(Constants.JIRA_ISSUE_ICON);
				content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			}
			if (issue != null && issue.getKey().equals(panelMap.get(s).issue.getKey())) {
				selectedContent = content;
			} else if (selectedContent == null) {
				selectedContent = content;
			}
			content.setTabName(s);
			issueToolWindow.getContentManager().addContent(content);
		}
		if (selectedContent != null) {
			issueToolWindow.getContentManager().setSelectedContent(selectedContent);
		}
		issueToolWindow.show(null);
	}

	private static String getExistingToolWindowTitle() {
		String title = TOOL_WINDOW_TITLE;
		if (panelMap.size() == 1) {
			title = panelMap.values().iterator().next().issue.getKey();
		}
		return title;
	}

	private static String createNewToolWindowTitle() {
		return panelMap.size() == 1 ? panelMap.values().iterator().next().issue.getKey() : TOOL_WINDOW_TITLE;
	}

	private static String getContentKey(JiraServerCfg server, JIRAIssue issue) {
		return server.getUrl() + server.getUsername() + issue.getKey();
	}

	public static void setCommentsExpanded(String key, boolean expanded) {
		IssuePanel ip = panelMap.get(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.setAllVisible(expanded);
		}
	}

	public static void refreshComments(String key) {
		IssuePanel ip = panelMap.get(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.refreshComments();
		}
	}

	public static void addComment(String key) {
		IssuePanel ip = panelMap.get(key);
		if (ip != null) {
			ip.descriptionAndCommentsPanel.addComment();
		}
	}

	public static void viewIssueInBrowser(String key) {
		IssuePanel ip = panelMap.get(key);
		if (ip != null) {
			JIRAIssue issue = ip.issue;
			BrowserUtil.launchBrowser(issue.getServerUrl() + "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}

	public static void editIssueInBrowser(String key) {
		IssuePanel ip = panelMap.get(key);
		if (ip != null) {
			BrowserUtil.launchBrowser(ip.issue.getIssueUrl());
		}
	}

	private static class IssuePanel extends JPanel {
		private final Project project;
		private final JiraServerCfg server;
		private final JIRAIssue issue;
		private DescriptionAndCommentsPanel descriptionAndCommentsPanel;

		public IssuePanel(Project project, JiraServerCfg server, JIRAIssue issue) {
			this.project = project;
			this.server = server;
			this.issue = issue;

			JTabbedPane tabs = new JTabbedPane();
			DetailsPanel detailsPanel = new DetailsPanel();
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
			add(new SummaryPanel(), gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);

			descriptionAndCommentsPanel.refreshComments();
			detailsPanel.getMoreIssueDetails();
		}

		private static class BoldLabel extends JLabel {
			public BoldLabel(String text) {
				super(text);
				setFont(getFont().deriveFont(Font.BOLD));
			}

			public BoldLabel() {
				this("");
			}
		}

		private class DetailsPanel extends JPanel {

			private JLabel affectsVersions = new JLabel("Fetching...");
			private JLabel fixVersions = new JLabel("Fetching...");
			private JLabel components = new JLabel("Fetching...");
			private JLabel affectsVersionsLabel = new BoldLabel("Affects Version/s");
			private JLabel fixVersionsLabel = new BoldLabel("Fix Version/s");
			private JLabel componentsLabel = new BoldLabel("Component/s");

			public DetailsPanel() {
				JPanel body = new JPanel();

				setLayout(new GridBagLayout());
				body.setLayout(new GridBagLayout());

				GridBagConstraints gbc1 = new GridBagConstraints();
				GridBagConstraints gbc2 = new GridBagConstraints();
				gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc2.fill = GridBagConstraints.HORIZONTAL;
				gbc2.weightx = 1.0;
				gbc1.gridx = 0;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;

				body.add(new BoldLabel("Type"), gbc1);
				body.add(new JLabel(issue.getType(), CachedIconLoader.getIcon(issue.getTypeIconUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Status"), gbc1);
				body.add(new JLabel(issue.getStatus(), CachedIconLoader.getIcon(issue.getStatusTypeUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Priority"), gbc1);
				body.add(new JLabel(issue.getPriority(), CachedIconLoader.getIcon(issue.getPriorityIconUrl()),
						SwingConstants.LEFT), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Assignee"), gbc1);
				// bleeeee :( - assignee ID (String value) equals "-1" for unassigned issues. Oh my...
				if (!issue.getAssigneeId().equals("-1")) {
					body.add(new UserLabel(issue.getServerUrl(), issue.getAssignee(), issue.getAssigneeId()), gbc2);
				} else {
					body.add(new JLabel("Unassigned"), gbc2);
				}
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Reporter"), gbc1);
				body.add(new UserLabel(issue.getServerUrl(), issue.getReporter(), issue.getReporterId()), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Resolution"), gbc1);
				body.add(new JLabel(issue.getResolution()), gbc2);
				gbc1.gridx = 2;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;
				body.add(new BoldLabel("Created"), gbc1);
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
				DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				String t;
				try {
					t = ds.format(df.parse(issue.getCreated()));
				} catch (ParseException e) {
					t = "Invalid";
				}
				body.add(new JLabel(t), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Updated"), gbc1);
				try {
					t = ds.format(df.parse(issue.getUpdated()));
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

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;
				JScrollPane scroll = new JScrollPane(body);
				scroll.setBorder(BorderFactory.createEmptyBorder());
				add(scroll, gbc);

				Border b = BorderFactory.createTitledBorder("Details");
				setBorder(b);
				Insets i = b.getBorderInsets(this);
				setMinimumSize(new Dimension(0, i.top + i.bottom));
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
					label.setText("None");
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
				if ((issue.getAffectsVersions() == null)
						|| (issue.getFixVersions() == null)
						|| (issue.getComponents() == null)) {

					Runnable runnable = new Runnable() {
						private String[] errorString = null;

						public void run() {

							try {
								if (server != null) {
									final JIRAIssue issueDetails = facade.getIssueDetails(server, issue);
									issue.setAffectsVersions(issueDetails.getAffectsVersions());
									issue.setFixVersions(issueDetails.getFixVersions());
									issue.setComponents(issueDetails.getComponents());
								}
							} catch (JIRAException e) {
								errorString = new String[]{"Unable to retrieve"};
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (errorString == null) {
										setAffectsVersions(getStringArray(issue.getAffectsVersions()));
										setFixVersions(getStringArray(issue.getFixVersions()));
										setComponents(getStringArray(issue.getComponents()));
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
					setAffectsVersions(getStringArray(issue.getAffectsVersions()));
					setFixVersions(getStringArray(issue.getFixVersions()));
					setComponents(getStringArray(issue.getComponents()));
				}
			}
		}

		private class SummaryPanel extends JPanel {
			public SummaryPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridy = 0;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				JEditorPane summary = new JEditorPane();
				summary.setContentType("text/html");
				summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				String txt = "<html><body><a href=\"" + issue.getIssueUrl() + "\">"
						+ issue.getKey() + "</a> " + issue.getSummary() + "</body></html>";
				summary.setText(txt);
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
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(server, issue), group, true);

				JComponent comp = toolbar.getComponent();
				add(comp, gbc);
			}
		}

		private static class ScrollablePanel extends JPanel implements Scrollable {
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
				ActionToolbar toolbar = manager.createActionToolbar(getContentKey(server, issue), group, true);

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
				CommentPanel p = new CommentPanel(comments.getComponents().length + 1, c, server, tabs);
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
				final IssueCommentDialog issueCommentDialog = new IssueCommentDialog(issue.getKey());
				issueCommentDialog.show();
				if (issueCommentDialog.isOK()) {
					Runnable runnable = new Runnable() {
						public void run() {
							try {
								if (server != null) {
									facade.addComment(server, issue, issueCommentDialog.getComment());
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
												"Failed to add comment to issue " + issue.getKey() + ": " + msg,
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
				clearComments();
				resetStackTraces();
				tabs.setTitleAt(tabIndex, "Refreshing comments...");
				final Runnable runnable = new Runnable() {
					public void run() {
						try {
							if (server != null) {
								final java.util.List<JIRAComment> cmts = facade.getComments(server, issue);

								for (JIRAComment c : cmts) {
									try {
										JIRAUserBean u = JIRAUserNameCache.getInstance().getUser(server, c.getAuthor());
										c.setAuthorFullName(u.getName());
									} catch (JiraUserNotFoundException e) {
										c.setAuthorFullName(c.getAuthor());
									}
								}

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
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

				String stack = Html2text.translate(issue.getDescription());
				if (StackTraceDetector.containsStackTrace(stack)) {
					tabs.add("Stack Trace: Description", new StackTracePanel(stack));
				}
			}
		}

		private abstract static class AbstractShowHideButton extends JLabel {

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

		private static class ShowHideButton extends AbstractShowHideButton {
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

		private static class UserLabel extends HyperlinkLabel {
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

		private static class WhiteLabel extends JLabel {
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
				body.setText("<html><head></head><body>" + issue.getDescription() + "</body></html>");
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
