package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-31
 * Time: 15:33:45
 * To change this template use File | Settings | File Templates.
 */
public class AnimatedProgressIcon extends AsyncProcessIcon {
	private Icon[] icons;
	private Icon passiveIcon;
	private static final int CYCLE_LENGTH = 640; // whole animation single cycle lenght
	private static final int CYCLE_GAP = 80; // break after every single cycle (best 'cycleLenght / number of frames')

	public AnimatedProgressIcon(@org.jetbrains.annotations.NonNls String name) {
		super(name);

		// comment that line if you want to use standard IDEA small progress circle
		initCustomLook();
	}

	private void initCustomLook() {
		loadIcons();
		init(icons, passiveIcon, CYCLE_LENGTH, CYCLE_GAP, -1);
	}

	private void loadIcons() {
		icons = new Icon[]{
			IconLoader.getIcon("/icons/progress/roller_1.png"),
			IconLoader.getIcon("/icons/progress/roller_2.png"),
			IconLoader.getIcon("/icons/progress/roller_3.png"),
			IconLoader.getIcon("/icons/progress/roller_4.png"),
			IconLoader.getIcon("/icons/progress/roller_5.png"),
			IconLoader.getIcon("/icons/progress/roller_6.png"),
			IconLoader.getIcon("/icons/progress/roller_7.png"),
			IconLoader.getIcon("/icons/progress/roller_8.png"),
		};

		passiveIcon = IconLoader.getIcon("/icons/progress/roller_0.png");

	}
}
