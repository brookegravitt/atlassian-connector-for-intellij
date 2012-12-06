package com.atlassian.theplugin.idea.ui;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class StackTracePanel extends JPanel {

	public StackTracePanel(String stack, Project project) {
        String fixedStack = stack.replaceAll("(\\(.+?\\.java:\\d+\\)) ?at", "$1\nat");

        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder builder = factory.createBuilder(project);
		ConsoleView console = builder.getConsole();
		console.print(fixedStack, ConsoleViewContentType.NORMAL_OUTPUT);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(console.getComponent(), gbc);
	}
}
