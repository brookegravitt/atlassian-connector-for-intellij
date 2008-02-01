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
		bambooContent = new ToolWindowBambooContent();
		bambooContent.setText("Waiting for Bamboo build statuses.");

		add(bambooContent, BorderLayout.NORTH);
	}

	public ToolWindowBambooContent getBambooContent() {
		return bambooContent;
	}
}
