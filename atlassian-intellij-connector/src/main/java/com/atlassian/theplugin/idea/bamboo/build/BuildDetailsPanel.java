package com.atlassian.theplugin.idea.bamboo.build;

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Jan 7, 2009
 * Time: 1:33:07 PM
 */
public class BuildDetailsPanel extends JPanel {
	private final BambooBuildAdapterIdea build;

	public BuildDetailsPanel(BambooBuildAdapterIdea build) {
		this.build = build;

		setLayout(new GridBagLayout());
	}
}
