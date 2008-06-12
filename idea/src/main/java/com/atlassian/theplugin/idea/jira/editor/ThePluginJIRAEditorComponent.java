package com.atlassian.theplugin.idea.jira.editor;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.Key;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.idea.IdeaHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jdom.Element;

import java.util.List;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.awt.*;

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
			mainPanel.setOpaque(true);
			mainPanel.setBackground(Color.WHITE);
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 0;
			labelIssue = new JLabel(issue.getKey());
			mainPanel.add(labelIssue, gbc);
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.gridy++;
			mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
			gbc.gridy++;
			mainPanel.add(new JLabel("Comments:"), gbc);
			gbc.gridy++;
			mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
			gbc.gridy++;
			try {
				List<JIRAComment> comments = facade.getComments(server.getServer(), issue);
				for (JIRAComment c : comments) {
					gbc.gridy++;
					JPanel p = new JPanel();
					p.setLayout(new GridLayout(2, 1));
					p.setBackground(Color.WHITE);
					p.add(new JLabel(c.getAuthor()));
					JTextArea l = new JTextArea(c.getBody());
					l.setLineWrap(true);
					l.setEditable(false);
					p.add(l);
					mainPanel.add(p, gbc);
				}
			} catch (JIRAException e) {
				// todo: fixme
				e.printStackTrace();
			}
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