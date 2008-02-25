package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.UIActionScheduler;

import java.awt.*;

public class IdeaActionScheduler implements UIActionScheduler {
	private static UIActionScheduler instance = new IdeaActionScheduler();

	public void invokeLater(Runnable action) {
		EventQueue.invokeLater(action);
	}

	public static UIActionScheduler getInstance() {
		return instance;
	}
}
