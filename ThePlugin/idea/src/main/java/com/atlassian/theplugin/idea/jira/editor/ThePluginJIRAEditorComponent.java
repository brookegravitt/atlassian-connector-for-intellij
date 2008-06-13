package com.atlassian.theplugin.idea.jira.editor;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.HelpUrl;
import com.atlassian.theplugin.idea.ui.CollapsiblePanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jdom.Element;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.beans.PropertyChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

public class ThePluginJIRAEditorComponent implements ApplicationComponent, FileEditorProvider {

	public static final String SUPPORTED_EXTENSION = "JIRAIssue";

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
		boolean shouldIAccept = false;
		String extension = virtualFile.getExtension();
		if (extension != null) {
		 	shouldIAccept = extension.equals(SUPPORTED_EXTENSION);
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
                          c.getAuthor() + " - " + c.getCreationDate().getTime().toString(),  null, null);
            cp.setContent(commentBody);
            holder.add(cp);
        }
    }

    private class CommentsPanel extends JPanel {

		private JPanel comments = new JPanel();
		private JScrollPane scroll = new JScrollPane();

		public CommentsPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			add(new JLabel("Comments"), gbc);
			gbc.gridy++;
			add(new JSeparator(SwingConstants.HORIZONTAL));
			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            comments.setLayout(new VerticalFlowLayout());
			scroll.setViewportView(comments);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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

		public void addComment(JIRAComment c, JIRAServer server) {
			comments.add(new CommentPanel(c, server));
            updateSize();
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
            comments.setPreferredSize(new Dimension(w, h + 100));
        }
    }

	private class CommentPanel extends JPanel {

		private JEditorPane commentBody;
		private HyperlinkLabel authorName;
		private JLabel creationDate;
		private JLabel btnShowHide;
        private Icon right = IconLoader.findIcon("/icons/navigate_right_10.gif");
        private Icon down = IconLoader.findIcon("/icons/navigate_down_10.gif");

        public CommentPanel(final JIRAComment comment, final JIRAServer server) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();

			btnShowHide = new JLabel();
			btnShowHide.setHorizontalAlignment(0);
            btnShowHide.setIcon(down);
            btnShowHide.addMouseListener(new MouseAdapter() {
				public boolean shown = true;
				public void mouseClicked(MouseEvent e) {
					shown = !shown;
                    btnShowHide.setIcon(shown ?  down : right);
					commentBody.setVisible(shown);
					validate();
					getParent().validate();
                }
			});
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			add(btnShowHide, gbc);

			authorName = new HyperlinkLabel(comment.getAuthor());
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

			creationDate = new JLabel();
			creationDate.setText(comment.getCreationDate().getTime().toString());
			gbc = new GridBagConstraints();
			gbc.gridx = 4;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			add(creationDate, gbc);

			commentBody = new JEditorPane();
            commentBody.setEditable(false);
            commentBody.setOpaque(false);
            commentBody.setContentType("text/html");
			commentBody.setText("<html><head></head><body>" + comment.getBody() + "</body></html>");
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 5;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
            gbc.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
			add(commentBody, gbc);
		}
	}

    private class SummaryPanel extends JPanel {

        private JIRAIssue issue;

        public SummaryPanel(JIRAIssue issue) {
            this.issue = issue;

            setLayout(new GridBagLayout());
            GridBagConstraints gbc1 = new GridBagConstraints();
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.weightx = 1.0;
            gbc1.gridx = 0;
            gbc2.gridx = 1;
            gbc1.gridy = 0;
            gbc2.gridy = 0;
            add(new JLabel("Summary"), gbc1);
            JEditorPane summary = new JEditorPane();
            summary.setContentType("text/html");
            summary.setText("<html><head></head><body><b>" + issue.getSummary() + "</body></html>");
            summary.setEditable(false);
            summary.setOpaque(false);
            add(summary, gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Description"), gbc1);
            JEditorPane description = new JEditorPane();
            description.setContentType("text/html");
            description.setText("<html><head></head><body>" + issue.getDescription() + "</body></html>");
            description.setEditable(false);
            description.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        BrowserUtil.launchBrowser(e.getURL().toString());
                    }
                }
            });
            description.setOpaque(false);
            add(description, gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Status"), gbc1);
            add(new JLabel(issue.getStatus()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Reporter"), gbc1);
            add(new JLabel(issue.getReporter()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Assignee"), gbc1);
            add(new JLabel(issue.getAssignee()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Resolution"), gbc1);
            add(new JLabel(issue.getResolution()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Created"), gbc1);
            add(new JLabel(issue.getCreated()), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            add(new JLabel("Updated"), gbc1);
            add(new JLabel(issue.getUpdated()), gbc2);

        }
    }

    private class JIRAFileEditor implements FileEditor {

		private final JIRAServerFacade facade;
		private final JIRAServer server;

		private JPanel mainPanel;
		private JLabel labelIssue;
		private JIRAIssue issue;

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
			setupUI();
		}

		private void setupUI() {
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			JTextArea warning = new JTextArea(
					"Warning: this is panel work in progress\n"
					+ "Not finished yet, don't post bugs against this feature!");
			warning.setForeground(Color.RED);
			warning.setEditable(false);
			warning.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			mainPanel.add(warning, gbc);
			gbc.gridy++;
			labelIssue = new JLabel(issue.getKey());
            labelIssue.setHorizontalAlignment(SwingConstants.CENTER);
            labelIssue.setForeground(Color.BLUE);
            mainPanel.add(labelIssue, gbc);
            gbc.gridy++;
            mainPanel.add(new SummaryPanel(issue), gbc);
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.gridy++;
			mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
			gbc.gridy++;
			CommentsPanel cp = new CommentsPanel();
			try {
				List<JIRAComment> comments = facade.getComments(server.getServer(), issue);
				for (JIRAComment c : comments) {
					gbc.gridy++;
					cp.addComment(c, server);
				}
			} catch (JIRAException e) {
				// todo: fixme
				e.printStackTrace();
			}
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1;
			mainPanel.add(cp, gbc);
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
		}
	}
}