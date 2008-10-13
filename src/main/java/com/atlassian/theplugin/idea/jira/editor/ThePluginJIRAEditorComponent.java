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

package com.atlassian.theplugin.idea.jira.editor;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.idea.jira.IssueComment;
import com.atlassian.theplugin.idea.jira.editor.vfs.JiraIssueVirtualFile;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAUserNameCache;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.util.ColorToHtml;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ThreeComponentsSplitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// TODO all this whole class should be rather project component I think (wseliga)
public class ThePluginJIRAEditorComponent implements ApplicationComponent, FileEditorProvider {

	//CHECKSTYLE\:MAGIC\:OFF
	private static final Color HEADER_BACKGROUND_COLOR = new Color(153, 153, 153);
	//CHECKSTYLE\:MAGIC\:ON
	
	@NonNls
	@NotNull
	public String getComponentName() {
		return "ThePluginJIRAEditorComponent";
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
		boolean shouldIAccept = true;
        // todo: probably not too pretty and there IS a possibility
        // that some other custom editor will intercept our JIRA "file"
        // - as it now has no extension. Is there a better way to do this?
        if (!(virtualFile instanceof JiraIssueVirtualFile)) {
            shouldIAccept = false;
        }
        return shouldIAccept;
	}

	@NotNull
	public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
		if (virtualFile instanceof JiraIssueVirtualFile) {
			JiraIssueVirtualFile jivf = (JiraIssueVirtualFile) virtualFile;
			return new JIRAFileEditor(project, jivf.getIssue());
		}
		throw new IllegalArgumentException("needs a JiraVirtualFile argument, has " + virtualFile.getClass().toString());
	}

	public void disposeEditor(@NotNull FileEditor fileEditor) {
	}

	@NotNull
	public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
		return DummyFileEditorState.DUMMY;
	}

	public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {
	}

	@NotNull
	@NonNls
	public String getEditorTypeId() {
		return getComponentName();
	}

	@NotNull
	public FileEditorPolicy getPolicy() {
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}

	private static class DummyFileEditorState implements FileEditorState {
		public static final FileEditorState DUMMY = new DummyFileEditorState();

		public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
			return false;
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

	private static class CommentsPanel extends JPanel {

		private ScrollablePanel comments = new ScrollablePanel();
        private JScrollPane scroll = new JScrollPane();
        private List<CommentPanel> commentList = new ArrayList<CommentPanel>();

		private Border border = BorderFactory.createTitledBorder("Comments");

		public CommentsPanel(JIRAIssue issue) {
			setBorder(border);
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
			gbc.fill = GridBagConstraints.NONE;

            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;

			ActionManager manager = ActionManager.getInstance();
			ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JIRA.CommentsToolBar");
			ActionToolbar toolbar = manager.createActionToolbar(issue.getKey(), group, true);

            JComponent comp = toolbar.getComponent();
            add(comp, gbc);

			gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            comments.setLayout(new VerticalFlowLayout());
			scroll.setViewportView(comments);
			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroll.setBorder(BorderFactory.createEmptyBorder());
            add(scroll, gbc);
        }

        public void setTitle(String title) {
			border = BorderFactory.createTitledBorder(title);
			setBorder(border);
        }

        public void addComment(final Project project, JIRAIssue issue, JIRAComment c, JIRAServer server) {
            CommentPanel p = new CommentPanel(project, issue, c, server);
            comments.add(p);
            commentList.add(p);
		}

        public void clearComments() {
            commentList.clear();
            comments.removeAll();
        }

        public void setAllVisible(boolean visible) {
            for (CommentPanel c : commentList) {
                c.getShowHideButton().setState(visible);
            }
        }

       public void scrollToFirst() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scroll.getVerticalScrollBar().setValue(0);
                }
            });
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
            setIcon(shown ?  down : right);
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

	private static class BoldLabel extends JLabel {
		public BoldLabel(String text) {
			super(text);
			setFont(getFont().deriveFont(Font.BOLD));
		}

		public BoldLabel() {
			this("");
		}
	}

	private static class WhiteLabel extends JLabel {
		public WhiteLabel() {
			setForeground(UIUtil.getTableSelectionForeground());
		}
	}
	
	private static class CommentPanel extends JPanel {

		private ShowHideButton btnShowHide;
        private static final int GRID_WIDTH = 6;

		public CommentPanel(final Project project, final JIRAIssue issue, final JIRAComment comment, final JIRAServer server) {
			setOpaque(true);
			setBackground(HEADER_BACKGROUND_COLOR);
			
			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
            int gridx = 1;

            JEditorPane commentBody = new JEditorPane();
            btnShowHide = new ShowHideButton(commentBody, this);
            gbc = new GridBagConstraints();
			gbc.gridx = gridx++;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			add(btnShowHide, gbc);

            gbc = new GridBagConstraints();
			gbc.gridx = gridx++;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
			UserLabel ul = new UserLabel(server.getServer().getUrl(), comment.getAuthorFullName(),
					comment.getAuthor(), UIUtil.getTableSelectionForeground());
			add(ul, gbc);

			final JLabel hyphen = new WhiteLabel();
            hyphen.setText("-");
			gbc = new GridBagConstraints();
			gbc.gridx = gridx++;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
			add(hyphen, gbc);

			final JLabel creationDate = new WhiteLabel();
			creationDate.setText(comment.getCreationDate().getTime().toString());
			gbc = new GridBagConstraints();
			gbc.gridx = gridx++;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			add(creationDate, gbc);

			if (StackTraceDetector.containsStackTrace(comment.getBody())) {
				HyperlinkLabel analyze = new HyperlinkLabel("Analyse Stack Trace", UIUtil.getTableSelectionForeground(),
					HEADER_BACKGROUND_COLOR, UIUtil.getTableSelectionForeground());
				analyze.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						StackTraceConsole stackTraceConsole = IdeaHelper.getProjectComponent(project, StackTraceConsole.class);
						stackTraceConsole.print(issue,
								"comment: " + comment.getAuthor() + " - " + creationDate.getText(), comment.getBody());
					}
				});
				gbc.gridx++;
				gbc.gridy = 0;
				gbc.weightx = 0.0;
				gbc.anchor = GridBagConstraints.EAST;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
				add(analyze, gbc);
			}

			commentBody.setEditable(false);
			commentBody.setOpaque(true);
			commentBody.setBackground(UIUtil.getPanelBackground());
			commentBody.setMargin(new Insets(0, Constants.DIALOG_MARGIN, 0, 0));
			commentBody.setContentType("text/html");
			commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			commentBody.setText("<html><head></head><body>" + comment.getBody() + "</body></html>");
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = GRID_WIDTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
			add(commentBody, gbc);
		}

        public AbstractShowHideButton getShowHideButton() {
            return btnShowHide;
        }
    }

    private static class DescriptionPanel extends JPanel {
		public DescriptionPanel(final JIRAIssue issue) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;

			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;

			JEditorPane	body = new JEditorPane();
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

	private static class DetailsPanel extends JPanel {

		private JLabel affectsVersions = new JLabel("Fetching...");
		private JLabel fixVersions = new JLabel("Fetching...");
		private JLabel components = new JLabel("Fetching...");
		private JLabel affectsVersionsLabel = new BoldLabel("Affects Version/s");
		private JLabel fixVersionsLabel = new BoldLabel("Fix Version/s");
		private JLabel componentsLabel = new BoldLabel("Component/s");

		public DetailsPanel(final JIRAIssue issue) {
			JPanel body = new JPanel();

			setLayout(new GridBagLayout());
			body.setLayout(new GridBagLayout());

			GridBagConstraints gbc1 = new GridBagConstraints();
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
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
            body.add(new UserLabel(issue.getServerUrl(), issue.getAssignee(), issue.getAssigneeId()), gbc2);
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
			DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
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

	}

	private static class SummaryPanel extends JPanel {

		private static final int THICKNESS = 6;

        public SummaryPanel(final Project project, final JIRAIssue issue) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			ActionManager manager = ActionManager.getInstance();
			ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.JIRA.EditorToolBar");
			ActionToolbar toolbar = manager.createActionToolbar(issue.getKey(), group, true);

            JComponent comp = toolbar.getComponent();
            add(comp, gbc);

            gbc.gridy = 1;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			JEditorPane summary = new JEditorPane();
            summary.setContentType("text/html");
			summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			Color bg = HEADER_BACKGROUND_COLOR;
			Color fg = UIUtil.getTableSelectionForeground();
			String bgColor = ColorToHtml.getHtmlFromColor(bg);
			String fgColor = ColorToHtml.getHtmlFromColor(fg);
			String txt = "<html><body bgcolor=" + bgColor + " color=" + fgColor
					+ "><font size=\"+1\"><a href=\"" + issue.getIssueUrl() + "\">"
					+ issue.getKey() + "</a> " + issue.getSummary() + "</font></body></html>";
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
            summary.setBackground(bg);
            summary.setOpaque(true);
			JPanel p = new JPanel();
			p.setBackground(bg); 
			p.setLayout(new GridBagLayout());
			p.setBorder(BorderFactory.createLineBorder(bg, THICKNESS));
			GridBagConstraints gbcp = new GridBagConstraints();
			gbcp.fill = GridBagConstraints.BOTH;
			gbcp.weightx = 1.0;
			gbcp.weighty = 1.0;
			gbcp.gridx = 0;
			gbcp.gridy = 0;
			p.add(summary, gbcp);

			if (StackTraceDetector.containsStackTrace(Html2text.translate(issue.getDescription()))) {
				HyperlinkLabel analyze = new HyperlinkLabel("Analyse Stack Trace", UIUtil.getTableSelectionForeground(),
					HEADER_BACKGROUND_COLOR, UIUtil.getTableSelectionForeground());
				gbcp.weightx = 0.0;
				gbcp.weighty = 0.0;
				gbcp.gridx = 1;

				analyze.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						StackTraceConsole stackTraceConsole = IdeaHelper.getProjectComponent(project, StackTraceConsole.class);
						stackTraceConsole.print(issue, "description", Html2text.translate(issue.getDescription()));
					}
				});
				gbcp.anchor = GridBagConstraints.EAST;
				analyze.setBackground(bg);
				p.add(analyze, gbcp);
			}
			

			add(p, gbc);
        }
	}

    public static class JIRAFileEditor implements FileEditor {

		private final JIRAServerFacade facade;
		private final JIRAServer server;

		private JPanel mainPanel;
		private final Project project;
		private JIRAIssue issue;
        private CommentsPanel commentsPanel;
		private SummaryPanel summaryPanel;
		private DetailsPanel detailsPanel;
		private boolean hasStackTrace;
		private static final int PREFERRED_HEIGHT_FIRST = 200;
		private static final int PREFERRED_HEIGHT_SECOND = 400;

		private JIRAFileEditor() {
			mainPanel = new JPanel();
			mainPanel.setBackground(Color.RED);
			// todo: fix this
			mainPanel.add(new JLabel("Can't view issue, something is wrong"));
			facade = null;
			server = null;
			project = null;
		}

		public JIRAFileEditor(Project project, JIRAIssue issue) {
			this.project = project;
			this.issue = issue;
			facade = JIRAServerFacadeImpl.getInstance();
			server = IdeaHelper.getCurrentJIRAServer(project);
            editorMap.put(issue.getKey(), this);

			hasStackTrace = StackTraceDetector.containsStackTrace(Html2text.translate(issue.getDescription()));

			setupUI();
		}

		private void setupUI() {
			mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;

			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;

			summaryPanel = new SummaryPanel(project, issue);
			mainPanel.add(summaryPanel, gbc);

			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;

			final ThreeComponentsSplitter split = new ThreeComponentsSplitter(true);
			split.setShowDividerControls(true);
			split.setHonorComponentsMinimumSize(true);

			final DescriptionPanel descriptionPanel = new DescriptionPanel(issue);
			split.setFirstComponent(descriptionPanel);
			
			detailsPanel = new DetailsPanel(issue);
			split.setInnerComponent(detailsPanel);

            commentsPanel = new CommentsPanel(issue);
			split.setLastComponent(commentsPanel);


			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					int first;
					if (issue.getDescription().length() == 0) {
						first = descriptionPanel.getMinimumSize().height;
					} else {
						first = Math.min(descriptionPanel.getPreferredSize().height, PREFERRED_HEIGHT_FIRST);
					}
					int second = Math.min(detailsPanel.getPreferredSize().height, PREFERRED_HEIGHT_SECOND);
					split.setFirstSize(first);
					// fixme: does not work properly anyways
					// components seem to have screwed up preferred sizes :( 
					split.setLastSize(mainPanel.getHeight() - summaryPanel.getHeight() - first - second);
				}
			});

			mainPanel.add(split, gbc);

			getMoreIssueDetails();
			refreshComments();
		}

		private String[] getStringArray(List<JIRAConstant> l) {
			List<String> sl = new ArrayList<String>(l.size());
			for (JIRAConstant c : l) {
				 sl.add(c.getName());
			}
			return sl.toArray(new String[l.size()]);
		}

		public void addComment() {
            final IssueComment issueComment = new IssueComment(issue.getKey());
            issueComment.show();
            if (issueComment.isOK()) {
				Runnable runnable = new Runnable() {
					public void run() {
                        try {
							if (server != null) {
								facade.addComment(server.getServer(), issue, issueComment.getComment());
								refreshComments();
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

		public synchronized void getMoreIssueDetails() {
			if ((issue.getAffectsVersions() == null)
					|| (issue.getFixVersions() == null)
					|| (issue.getComponents() == null)) {

				Runnable runnable = new Runnable() {
					private String[] errorString = null;

					public void run() {

						try {
							if (server != null) {
								final JIRAIssue issueDetails = facade.getIssueDetails(server.getServer(), issue);
								issue.setAffectsVersions(issueDetails.getAffectsVersions());
								issue.setFixVersions(issueDetails.getFixVersions());
								issue.setComponents(issueDetails.getComponents());
							}
						} catch (JIRAException e) {
							errorString = new String[] { "Unable to retrieve" };
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (errorString == null) {
									detailsPanel.setAffectsVersions(getStringArray(issue.getAffectsVersions()));
									detailsPanel.setFixVersions(getStringArray(issue.getFixVersions()));
									detailsPanel.setComponents(getStringArray(issue.getComponents()));
								} else {
									detailsPanel.getAffectVersionsLabel().setForeground(Color.RED);
									detailsPanel.getFixVersionsLabel().setForeground(Color.RED);
									detailsPanel.getComponentsLabel().setForeground(Color.RED);
									detailsPanel.setAffectsVersions(errorString);
									detailsPanel.setFixVersions(errorString);
									detailsPanel.setComponents(errorString);
								}
							}
						});
					}
				};
				new Thread(runnable, "atlassian-idea-plugin get issue details").start();
			} else {
				detailsPanel.setAffectsVersions(getStringArray(issue.getAffectsVersions()));
				detailsPanel.setFixVersions(getStringArray(issue.getFixVersions()));
				detailsPanel.setComponents(getStringArray(issue.getComponents()));
			}
		}

		public void refreshComments() {
            commentsPanel.clearComments();
            commentsPanel.setTitle("Fetching comments...");
            final Runnable runnable = new Runnable() {
                public void run() {
                    try {
						if (server != null) {
							final List<JIRAComment> comments = facade.getComments(server.getServer(), issue);

							for (JIRAComment c : comments) {
								JIRAUserBean u = JIRAUserNameCache.getInstance().getUser(server, c.getAuthor());
								c.setAuthorFullName(u.getName());
							}

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									int size = comments.size();
									if (size > 0) {
										commentsPanel.setTitle("Comments (" + comments.size() + ")");
										for (JIRAComment c : comments) {
											commentsPanel.addComment(project, issue, c, server);
										}
										commentsPanel.validate();
										commentsPanel.scrollToFirst();
									} else {
										commentsPanel.setTitle("No comments");
									}
								}
							});
						}
					} catch (JIRAException e) {
                        commentsPanel.setTitle("Unable to retrieve comments");
                    }
                }
            };
            new Thread(runnable, "atlassian-idea-plugin refresh comments").start();
        }

		public JIRAIssue getIssue() {
			return issue;
		}

		public boolean hasStackTraceInDescription() {
			return hasStackTrace;
		}

		public void analyzeDescriptionStackTrace() {
			StackTraceConsole stackTraceConsole = IdeaHelper.getProjectComponent(project, StackTraceConsole.class);
			stackTraceConsole.print(issue, "description", Html2text.translate(issue.getDescription()));
		}

		public void setCommentsExpanded(boolean expanded) {
            commentsPanel.setAllVisible(expanded);
        }
        
        @NotNull
		public JComponent getComponent() {
			return mainPanel;
		}

		@Nullable
		public JComponent getPreferredFocusedComponent() {
			return mainPanel;
		}

		@NonNls
		@NotNull
		public String getName() {
			return "JIRA Issue View";
		}

		@NotNull
		public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
			return DummyFileEditorState.DUMMY;
		}

		public void setState(@NotNull FileEditorState fileEditorState) {
		}

		public boolean isModified() {
			return false;  
		}

		public boolean isValid() {
			return true;
		}

		public void selectNotify() {
		}

		public void deselectNotify() {
		}

		public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
		}

		public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
		}

		@Nullable
		public BackgroundEditorHighlighter getBackgroundHighlighter() {
			return null;
		}

		@Nullable
		public FileEditorLocation getCurrentLocation() {
			return null;
		}

		@Nullable
		public StructureViewBuilder getStructureViewBuilder() {
			return null;
		}

		public <T> T getUserData(Key<T> tKey) {
			return null;
		}

		public <T> void putUserData(Key<T> tKey, T t) {
		}

		public void dispose() {
            editorMap.remove(issue.getKey());
        }
	}

    private static HashMap<String, JIRAFileEditor> editorMap = new HashMap<String, JIRAFileEditor>();

    public static JIRAFileEditor getEditorByKey(String key) {
        if (editorMap.containsKey(key)) {
            return editorMap.get(key);
        }
        return null;
    }
}