package com.atlassian.theplugin.idea.bamboo;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.content.Content;
import com.intellij.peer.PeerFactory;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.bamboo.TestDetails;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.HashMap;
import java.util.List;
import java.awt.*;

public class TestResultsToolWindow {
	private static final String TOOL_WINDOW_TITLE = "Bamboo Test Results";

	private static TestResultsToolWindow instance = new TestResultsToolWindow();

	private HashMap<String, TestDetailsPanel> panelMap = new HashMap<String, TestDetailsPanel>();

	private TestResultsToolWindow() {
	}

	public static TestResultsToolWindow getInstance() {
		return instance;
	}

	public void showTestResults(String buildKey, String buildNumber, List<TestDetails> tests) {
		TestDetailsPanel detailsPanel;
		String contentKey = buildKey + "-" + buildNumber;

		ToolWindowManager twm = ToolWindowManager.getInstance(IdeaHelper.getCurrentProject());
		ToolWindow testDetailsToolWindow = twm.getToolWindow(TOOL_WINDOW_TITLE);
		if (testDetailsToolWindow == null) {
			testDetailsToolWindow = twm.registerToolWindow(TOOL_WINDOW_TITLE, true, ToolWindowAnchor.BOTTOM);
			testDetailsToolWindow.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
		}

		Content content = testDetailsToolWindow.getContentManager().findContent(contentKey);

		if (content != null) {
			detailsPanel = panelMap.get(contentKey);
		} else {
			detailsPanel = new TestDetailsPanel(contentKey, tests);
			panelMap.remove(contentKey);
			panelMap.put(contentKey, detailsPanel);

			PeerFactory peerFactory = PeerFactory.getInstance();
			content = peerFactory.getContentFactory().createContent(detailsPanel, contentKey, true);
			content.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
			content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			testDetailsToolWindow.getContentManager().addContent(content);
		}
		testDetailsToolWindow.getContentManager().setSelectedContent(content);
		testDetailsToolWindow.show(null);
	}

	private class TestDetailsPanel extends JPanel {
		private static final float SPLIT_RATIO = 0.3f;

		private ConsoleView console;

		public TestDetailsPanel(String name, final List<TestDetails> tests) {
			super();
			if (tests.size() > 0) {

				Splitter split = new Splitter(false, SPLIT_RATIO);

				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;

				DefaultListModel lm = new DefaultListModel();
				for (TestDetails d : tests) {
					lm.addElement(d.getTestClassName() + "." + d.getTestMethodName());
				}
				final JList list = new JList(lm);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						print(tests.get(list.getSelectedIndex()).getErrors());
					}
				});
				JScrollPane sp = new JScrollPane(list);

				split.setFirstComponent(sp);

				TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
				TextConsoleBuilder builder = factory.createBuilder(IdeaHelper.getCurrentProject());
				console = builder.getConsole();

				split.setSecondComponent(console.getComponent());
				add(split, gbc);
			} else {
				add(new JLabel("No failed tests in build " + name));
			}
		}

		public void print(String txt) {
			console.clear();
			console.print(txt, ConsoleViewContentType.NORMAL_OUTPUT);
		}
	}
}
