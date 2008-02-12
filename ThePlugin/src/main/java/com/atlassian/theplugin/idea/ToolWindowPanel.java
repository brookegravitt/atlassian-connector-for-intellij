package com.atlassian.theplugin.idea;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-01
 * Time: 11:39:50
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowPanel extends JPanel {
	private ToolWindowBambooContent bambooContent;

	ToolWindowPanel() {
		super(new BorderLayout());

		// create and add bamboo content
		// add scroll facility to the html area
		bambooContent = new ToolWindowBambooContent();
		bambooContent.setText(
			"<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">"
			+ "Waiting for Bamboo build statuses."
			+ "</div>");

		JScrollPane scrollPane = new JScrollPane(bambooContent,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);

		add(scrollPane, BorderLayout.CENTER);
	}

	public ToolWindowBambooContent getBambooContent() {
		return bambooContent;
	}
}
