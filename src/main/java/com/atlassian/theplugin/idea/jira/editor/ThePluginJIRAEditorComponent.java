package com.atlassian.theplugin.idea.jira.editor;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.editor.vfs.MemoryVirtualFile;
import com.atlassian.theplugin.idea.jira.IssueComment;
import com.atlassian.theplugin.idea.jira.CachedIconLoader;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;

public class ThePluginJIRAEditorComponent implements ApplicationComponent, FileEditorProvider {

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
        if (!(virtualFile instanceof MemoryVirtualFile)) {
            shouldIAccept = false;
        }
        return shouldIAccept;
	}

	@NotNull
	public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
		String issueFromFileName = virtualFile.getNameWithoutExtension();
		JIRAIssue issue = IdeaHelper.getCurrentJIRAToolWindowPanel().getCurrentIssue();
		if (issueFromFileName.equals(issue.getKey())) {
			return new JIRAFileEditor(issue);
		}
		return new JIRAFileEditor();

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

	private class CommentsPanel extends JPanel {

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

        public void addComment(JIRAIssue issue, JIRAComment c, JIRAServer server) {
            CommentPanel p = new CommentPanel(issue, c, server);
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

     private abstract class AbstractShowHideButton extends JLabel {

        private Icon right = IconLoader.findIcon("/icons/navigate_right_10.gif");
        private Icon down = IconLoader.findIcon("/icons/navigate_down_10.gif");
        private boolean shown = true;

        public AbstractShowHideButton() {
            setHorizontalAlignment(0);
            setIcon(down);
            setToolTipText(getTooltip());
            addMouseListener(new MouseAdapter() {
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

    private class ShowHideButton extends AbstractShowHideButton {
        private JComponent body;
        private JComponent container;

        public ShowHideButton(JComponent body, JComponent container) {
            this.body = body;
            this.container = container;
        }

        protected void setComponentVisible(boolean visible) {
            body.setVisible(visible);
            container.validate();
            container.getParent().validate();
        }

        protected String getTooltip() {
            return "Collapse/Expand";
        }
    }

    private class UserLabel extends HyperlinkLabel {
		UserLabel(final String serverUrl, final String userNameId) {
			super(userNameId, UIUtil.getTableSelectionForeground(),
					UIUtil.getTableSelectionBackground(), UIUtil.getTableSelectionForeground());
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

	private class BoldLabel extends JLabel {
		public BoldLabel(String text) {
			super(text);
			setFont(getFont().deriveFont(Font.BOLD));
		}

		public BoldLabel() {
			this("");
		}
	}

	private class WhiteLabel extends BoldLabel {
		public WhiteLabel() {
			super();
			setForeground(UIUtil.getTableSelectionForeground());
		}
	}
	
	private class CommentPanel extends JPanel {

		private ShowHideButton btnShowHide;

		public CommentPanel(final JIRAIssue issue, final JIRAComment comment, final JIRAServer server) {
			setOpaque(true);
			setBackground(UIUtil.getTableSelectionBackground());
			
			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();

			JEditorPane commentBody = new JEditorPane();
            btnShowHide = new ShowHideButton(commentBody, this);
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			add(btnShowHide, gbc);

			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
			UserLabel ul = new UserLabel(server.getServer().getUrlString(), comment.getAuthor());
			add(ul, gbc);

			final JLabel hyphen = new WhiteLabel();
            hyphen.setText("-");
			gbc = new GridBagConstraints();
			gbc.gridx = 3;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
			add(hyphen, gbc);

			final JLabel creationDate = new WhiteLabel();
			creationDate.setText(comment.getCreationDate().getTime().toString());
			gbc = new GridBagConstraints();
			gbc.gridx = 4;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			add(creationDate, gbc);

			if (StackTraceDetector.containsStackTrace(comment.getBody())) {
				HyperlinkLabel analyze = new HyperlinkLabel("Analyse stack trace", UIUtil.getTableSelectionForeground(),
					UIUtil.getTableSelectionBackground(), UIUtil.getTableSelectionForeground());
				analyze.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						StackTraceConsole.getInstance().print(issue,
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
			commentBody.setText("<html><head></head><body>" + comment.getBody() + "</body></html>");
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 6;
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

    private class DescriptionPanel extends JPanel {
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

	private class DetailsPanel extends JPanel {

		private JLabel affectsVersions = new JLabel("Fetching...");
		private JLabel fixVersions = new JLabel("Fetching...");
		private JLabel components = new JLabel("Fetching...");

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
			gbc2.gridx = 1;
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
			gbc1.gridy++;
			gbc2.gridy++;
			body.add(new BoldLabel("Created"), gbc1);
			body.add(new JLabel(issue.getCreated()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Updated"), gbc1);
            body.add(new JLabel(issue.getUpdated()), gbc2);
			gbc1.gridy++;
			gbc2.gridy++;
			body.add(new BoldLabel("Affects Version/s"), gbc1);
			body.add(affectsVersions, gbc2);
			gbc1.gridy++;
			gbc2.gridy++;
			body.add(new BoldLabel("Fix Version/s"), gbc1);
			body.add(fixVersions, gbc2);
			gbc1.gridy++;
			gbc2.gridy++;
			body.add(new BoldLabel("Component/s"), gbc1);
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
	}

	private class SummaryPanel extends JPanel {

		private DetailsPanel details;

		public SummaryPanel(final JIRAIssue issue) {
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
			Color bg = UIUtil.getTableSelectionBackground();
			Color fg = UIUtil.getTableSelectionForeground();
			String bgColor = String.format("#%1$2X%2$2X%3$2X", bg.getRed(), bg.getGreen(), bg.getBlue());
			String fgColor = String.format("#%1$2X%2$2X%3$2X", fg.getRed(), fg.getGreen(), fg.getBlue());
			String txt = "<html><body bgcolor=" + bgColor + " color=" + fgColor
					+ "><font size=\"+1\"><a href=\"" + issue.getIssueUrl() + "\">"
					+ issue.getKey() + "</a> " + issue.getSummary() + "</font></body></html>";
			summary.setText(txt);
            summary.setEditable(false);
            summary.setFont(summary.getFont().deriveFont(Font.BOLD));
            summary.setOpaque(false);
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
			p.setBorder(BorderFactory.createTitledBorder("Summary"));
			p.setBorder(BorderFactory.createLineBorder(bg, 6));
			GridBagConstraints gbcp = new GridBagConstraints();
			gbcp.fill = GridBagConstraints.BOTH;
			gbcp.weightx = 1.0;
			gbcp.weighty = 1.0;
			gbcp.gridx = 0;
			gbcp.gridy = 0;
			p.add(summary, gbcp);
			add(p, gbc);

			gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			Splitter split = new Splitter(true);
			split.setFirstComponent(new DescriptionPanel(issue));
			details = new DetailsPanel(issue);
			split.setSecondComponent(details);
			split.setShowDividerControls(true);
			split.setHonorComponentsMinimumSize(true);
			add(split, gbc);
			if (issue.getDescription().length() == 0) {
				split.setProportion(0);
			}
        }

		public void setAffectsVersions(String[] versions) {
			setLabelText(details.getAffectVersionsLabel(), versions);
		}

		public void setFixVersions(String[] versions) {
			setLabelText(details.getFixVersionsLabel(), versions);
		}

		public void setComponents(String[] components) {
			setLabelText(details.getComponentsLabel(), components);
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

    public class JIRAFileEditor implements FileEditor {

		private final JIRAServerFacade facade;
		private final JIRAServer server;

		private JPanel mainPanel;
		private JIRAIssue issue;
        private CommentsPanel commentsPanel;
		private SummaryPanel summaryPanel;
		private boolean hasStackTrace;

		public JIRAFileEditor() {
			mainPanel = new JPanel();
			mainPanel.setBackground(Color.RED);
			// todo: fix this
			mainPanel.add(new JLabel("Can't view issue, something is wrong"));
			facade = null;
			server = null;
		}

		public JIRAFileEditor(JIRAIssue issue) {
			this.issue = issue;
			facade = JIRAServerFacadeImpl.getInstance();
			server = IdeaHelper.getCurrentJIRAServer();
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
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			Splitter split = new Splitter(true);
			split.setShowDividerControls(true);
			split.setHonorComponentsMinimumSize(true);
			summaryPanel = new SummaryPanel(issue);
			split.setFirstComponent(summaryPanel);
            commentsPanel = new CommentsPanel(issue);
			split.setSecondComponent(commentsPanel);
			mainPanel.add(split, gbc);
			getMoreIssueDetails();
			refreshComments();
		}

        public void addComment() {
            final IssueComment issueComment = new IssueComment(issue.getKey());
            issueComment.show();
            if (issueComment.isOK()) {
                FutureTask task = new FutureTask(new Runnable() {
                    public void run() {
                        try {
                            facade.addComment(server.getServer(), issue, issueComment.getComment());
                            refreshComments();
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
                }, null);
                new Thread(task, "atlassian-idea-plugin comment issue from editor").start();
            }
        }

		public void getMoreIssueDetails() {
			FutureTask task = new FutureTask(new Runnable() {

				private String[] getStringArray(List<JIRAConstant> l) {
					List<String> sl = new ArrayList<String>(l.size());
					for (JIRAConstant c : l) {
						 sl.add(c.getName());
					}
					return sl.toArray(new String[l.size()]);
				}

				public void run() {
					try {
						final JIRAIssue issueDetails = facade.getIssueDetails(server.getServer(), issue);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								summaryPanel.setAffectsVersions(getStringArray(issueDetails.getAffectsVersions()));
								summaryPanel.setFixVersions(getStringArray(issueDetails.getFixVersions()));
								summaryPanel.setComponents(getStringArray(issueDetails.getComponents()));
							}
						});
					} catch (JIRAException e) {
						summaryPanel.setAffectsVersions(new String[]{new String("Cannot retrieve: " + e.getMessage())});
						summaryPanel.setFixVersions(new String[]{new String("Cannot retrieve: " + e.getMessage())});
						summaryPanel.setComponents(new String[]{new String("Cannot retrieve: " + e.getMessage())});
					}
				}
			}, null);
			new Thread(task, "atlassian-idea-plugin get issue details").start();

		}

		public void refreshComments() {
            commentsPanel.clearComments();
            commentsPanel.setTitle("Fetching comments...");
            FutureTask task = new FutureTask(new Runnable() {
                public void run() {
                    try {
                        final List<JIRAComment> comments = facade.getComments(server.getServer(), issue);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                int size = comments.size();
                                if (size > 0) {
                                    commentsPanel.setTitle("Comments (" + comments.size() + ")");
                                    for (JIRAComment c : comments) {
                                        commentsPanel.addComment(issue, c, server);
									}
                                    commentsPanel.validate();
                                    commentsPanel.scrollToFirst();
                                } else {
                                    commentsPanel.setTitle("No comments");
                                }
                            }
                        });
                    } catch (JIRAException e) {
                        commentsPanel.setTitle("Cannot fetch comments: " + e.getMessage());
                    }
                }
            }, null);
            new Thread(task, "atlassian-idea-plugin refresh comments").start();
        }

		public JIRAIssue getIssue() {
			return issue;
		}

		public boolean hasStackTraceInDescription() {
			return hasStackTrace;
		}

		public void analyzeDescriptionStackTrace() {
			StackTraceConsole.getInstance().print(issue, "description",	Html2text.translate(issue.getDescription()));
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