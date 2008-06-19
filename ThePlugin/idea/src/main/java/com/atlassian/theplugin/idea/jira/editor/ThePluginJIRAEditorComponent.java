package com.atlassian.theplugin.idea.jira.editor;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.jira.editor.vfs.MemoryVirtualFile;
import com.atlassian.theplugin.idea.jira.IssueComment;
import com.atlassian.theplugin.idea.ui.CollapsiblePanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ConsoleView;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

    private class CommentsPanel2 extends JPanel {
        private JScrollPane scroll;
        private JPanel holder;

        public CommentsPanel2() {
            holder = new JPanel();
            holder.setLayout(new VerticalFlowLayout());

            scroll = new JScrollPane(holder,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setWheelScrollingEnabled(true);
            add(scroll);
        }

        public void addComment(JIRAComment c) {
            JEditorPane commentBody = new JEditorPane();
			commentBody.setContentType("text/html");
			commentBody.setText("<html><head></head><body>" + c.getBody() + "</body></html>");
            CollapsiblePanel cp = new CollapsiblePanel(true, false,
                          c.getAuthor() + " - " + c.getCreationDate().getTime().toString());
            cp.setContent(commentBody);
            holder.add(cp);
        }
    }

	private class CommentsPanel extends JPanel {

		private JPanel comments = new JPanel();
        private BoldLabel titleLabel = new BoldLabel("Comments");
        private JScrollPane scroll = new JScrollPane();
        private List<CommentPanel> commentList = new ArrayList<CommentPanel>();

		public CommentsPanel(JIRAIssue issue) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
			gbc.fill = GridBagConstraints.NONE;
            add(titleLabel, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
            gbc.gridx = 0;
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
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroll.setBorder(BorderFactory.createEmptyBorder());

			updateSize();
            add(scroll, gbc);

            scroll.addComponentListener(new ComponentListener() {
                public void componentResized(ComponentEvent e) {
                    updateSize();
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                }

                public void componentHidden(ComponentEvent e) {
                }
            });
        }

        public void setTitle(String title) {
            titleLabel.setText(title);
        }

        public void addComment(JIRAIssue issue, JIRAComment c, JIRAServer server) {
            CommentPanel p = new CommentPanel(issue, c, server);
            comments.add(p);
            commentList.add(p);
            updateSize();
		}

        public void clearComments() {
            commentList.clear();
            comments.removeAll();
            updateSize();
        }

        public void setAllVisible(boolean visible) {
            for (CommentPanel c : commentList) {
                c.getShowHideButton().setState(visible);
            }
        }

        private void updateSize() {
            int w = scroll.getWidth();
            JScrollBar sb = scroll.getVerticalScrollBar();
            w -= sb.getWidth();
            int h = 0;
            Component[] comps = comments.getComponents();
            for (Component c : comps) {
                h += c.getHeight();
            }
            // todo: needed to add some padding so that last comment shows regardless of panel size.
            // If somebody knows how to do it better, please fix this
            comments.setPreferredSize(new Dimension(w, h + 300));
            comments.validate();
        }

        public void scrollToFirst() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    comments.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                    scroll.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

     private abstract class AbstractShowHideButton extends JLabel {

        private Icon right = IconLoader.findIcon("/icons/navigate_right_10.gif");
        private Icon down = IconLoader.findIcon("/icons/navigate_down_10.gif");
        public boolean shown = true;

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

/*
    private class ShowHideAllCommentsButton extends AbstractShowHideButton {
        private List<CommentPanel> components;
        private JComponent container;

        public ShowHideAllCommentsButton(List<CommentPanel> components, JComponent container) {
            this.components = components;
            this.container = container;
        }

        protected void setComponentVisible(boolean visible) {
            for (CommentPanel c : components) {
                c.getShowHideButton().setState(visible);
            }
            container.validate();
            container.getParent().validate();
        }

        protected String getTooltip() {
            return "Collapse/Expand All";
        }
    }

*/
    private class CommentPanel extends JPanel {

		private ShowHideButton btnShowHide;

        public CommentPanel(final JIRAIssue issue, final JIRAComment comment, final JIRAServer server) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();

			JEditorPane commentBody = new JEditorPane();
            btnShowHide = new ShowHideButton(commentBody, this);
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			add(btnShowHide, gbc);

			HyperlinkLabel authorName = new HyperlinkLabel(comment.getAuthor());
			authorName.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					BrowserUtil.launchBrowser(
                            server.getServer().getUrlString()
                            + "/secure/ViewProfile.jspa?name="
							+ comment.getAuthor());
				}
			});
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
			add(authorName, gbc);

			final JLabel label1 = new JLabel();
            label1.setText("-");
			gbc = new GridBagConstraints();
			gbc.gridx = 3;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
			add(label1, gbc);

			final JLabel creationDate = new JLabel();
			creationDate.setText(comment.getCreationDate().getTime().toString());
			gbc = new GridBagConstraints();
			gbc.gridx = 4;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			add(creationDate, gbc);

			HyperlinkLabel analyze = new HyperlinkLabel("Analyse stack trace");
			analyze.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					StackTraceConsole.getInstance().print(issue,
							"comment: " + comment.getAuthor() + " - " + creationDate.getText(), comment.getBody());
				}
			});
			gbc.gridx = 5;
			gbc.gridy = 0;
			gbc.weightx = 0.0;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
			add(analyze, gbc);

			commentBody.setEditable(false);
            commentBody.setOpaque(false);
            commentBody.setContentType("text/html");
			commentBody.setText("<html><head></head><body>" + comment.getBody() + "</body></html>");
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 6;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
			add(commentBody, gbc);
		}

        public AbstractShowHideButton getShowHideButton() {
            return btnShowHide;
        }
    }

    private class BoldLabel extends JLabel {
        public BoldLabel(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.BOLD));
        }
    }

    private class DescriptionPanel extends JPanel {

		private static final int MIN_HEIGHT = 200;
		private JEditorPane body;
        private ShowHideButton btnShowHide;

        public DescriptionPanel(final JIRAIssue issue, final String description) {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            final JLabel descriptionLabel = new BoldLabel("Description");
			gbc.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            add(descriptionLabel, gbc);

            body = new JEditorPane();
			JScrollPane sp = new JScrollPane(body,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setOpaque(false);
			sp.addComponentListener(new ComponentListener() {
				public void componentResized(ComponentEvent e) {
					body.setPreferredSize(new Dimension(e.getComponent().getWidth(), MIN_HEIGHT));
				}

				public void componentMoved(ComponentEvent e) {
				}

				public void componentShown(ComponentEvent e) {
					getParent().getParent().validate();
				}

				public void componentHidden(ComponentEvent e) {
					getParent().getParent().validate();
				}
			});
			btnShowHide = new ShowHideButton(sp, this);
			gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            add(btnShowHide, gbc);

			gbc.gridx = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add (new JPanel(), gbc);


			HyperlinkLabel analyze = new HyperlinkLabel("Analyse stack trace");
			analyze.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					StackTraceConsole.getInstance().print(issue, "description", Html2text.translate(description));
				}
			});
			gbc.gridx = 3;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
			add(analyze, gbc);

            body.setEditable(false);
            body.setOpaque(false);
            body.setBorder(BorderFactory.createEmptyBorder());
            body.setContentType("text/html");
            body.setText("<html><head></head><body>" + description + "</body></html>");
			gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 4;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN, 0);
            gbc.fill = GridBagConstraints.BOTH;
            add(sp, gbc);
			Dimension d = body.getPreferredSize();
			d.height = MIN_HEIGHT;
			sp.setMinimumSize(d);
            sp.setPreferredSize(d);
            sp.getViewport().setOpaque(false);
			body.setCaretPosition(0);
        }

        public void setContentsVisible(boolean visible) {
            btnShowHide.setState(visible);
        }
    }

    private class SummaryPanel extends JPanel {

        public SummaryPanel(JIRAIssue issue) {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc1 = new GridBagConstraints();
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc1.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
			gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.weightx = 1.0;
            gbc1.gridx = 0;
            gbc2.gridx = 1;
            gbc1.gridy = 0;
            gbc2.gridy = 0;
            add(new BoldLabel("Summary"), gbc1);
            JEditorPane summary = new JEditorPane();
            summary.setBorder(BorderFactory.createEmptyBorder());
            summary.setContentType("text/plain");
            summary.setText(issue.getSummary());
            summary.setEditable(false);
            summary.setOpaque(false);
            add(summary, gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            GridBagConstraints gbc3 = new GridBagConstraints();
            gbc3.gridx = gbc1.gridx;
            gbc3.gridy = gbc1.gridy;
            gbc3.fill = GridBagConstraints.BOTH;
            gbc3.gridwidth = 2;
            String d = issue.getDescription();
            DescriptionPanel description = new DescriptionPanel(issue, d);
            add(description, gbc3);
            if (d.length() == 0) {
                description.setContentsVisible(false);
            }
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Status"), gbc1);
            add(new JLabel(issue.getStatus()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Reporter"), gbc1);
            add(new JLabel(issue.getReporter()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Assignee"), gbc1);
            add(new JLabel(issue.getAssignee()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Resolution"), gbc1);
            add(new JLabel(issue.getResolution()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Created"), gbc1);
            add(new JLabel(issue.getCreated()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new BoldLabel("Updated"), gbc1);
            add(new JLabel(issue.getUpdated()), gbc2);

        }
    }

    public class JIRAFileEditor implements FileEditor {

		private final JIRAServerFacade facade;
		private final JIRAServer server;

		private JPanel mainPanel;
		private JIRAIssue issue;
        private CommentsPanel commentsPanel;

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
			setupUI();
		}

		private void setupUI() {
			mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			JTextArea warning = new JTextArea(
					"Warning: this is panel work in progress.\n"
                    + "It is not finished, we know it is ugly and buggy.\n"
                    + "Please, don't post bugs against this feature yet :)!");
			warning.setForeground(Color.RED);
			warning.setEditable(false);
			warning.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			mainPanel.add(warning, gbc);
			gbc.gridy++;
            gbc.insets = new Insets(
                    Constants.DIALOG_MARGIN, 0, 0, 0);
			JLabel labelIssue = new JLabel(issue.getKey());
            labelIssue.setHorizontalAlignment(SwingConstants.CENTER);
            labelIssue.setForeground(Color.BLUE);
            Font f = labelIssue.getFont();
            Font boldBigFont = f.deriveFont(Font.BOLD, f.getSize2D() * 2);
            labelIssue.setFont(boldBigFont);
            mainPanel.add(labelIssue, gbc);
            gbc.gridy++;
            gbc.insets = new Insets(
                    Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN);
            mainPanel.add(new SummaryPanel(issue), gbc);
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.gridy++;
            commentsPanel = new CommentsPanel(issue);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1;
            mainPanel.add(commentsPanel, gbc);
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