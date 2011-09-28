/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-02
 * Time: 13:35:13
 * To change this template use File | Settings | File Templates.
 */
public class ProgressAnimationProvider extends JPanel {

	private AnimatedProgressIcon progressIcon = new AnimatedProgressIcon("Progress Indicator");
	private JComponent replacedComponent;
	private Object addConstraint;
	private JComponent parentComponent;

	/**
	 * @param rootComponent place where to put animation (parent of the replaceComponent). CANNOT BE NULL
	 * @param replaceComponent component to hide (to replace by animated progress icon).
	 * Can be null if rootComponent doesn't contain child
	 * @param constraint constraint used to add animation icon (and replaceComponent). 
	 */
	public void configure(JComponent rootComponent, JComponent replaceComponent, Object constraint) {
		this.parentComponent = rootComponent;
		this.replacedComponent = replaceComponent;
		this.addConstraint = constraint;
	}

	public void startProgressAnimation() {
		EventQueue.invokeLater(new ProgressAnimation(true));
	}

	public void stopProgressAnimation() {
		EventQueue.invokeLater(new ProgressAnimation(false));
	}

	private class ProgressAnimation implements Runnable {
		private boolean start;

		public ProgressAnimation(boolean start) {
			this.start = start;
		}

		public void run() {
			if (start) {
				if (replacedComponent != null) {
					parentComponent.remove(replacedComponent);
				}
				parentComponent.add(progressIcon, addConstraint);
				progressIcon.resume();
				parentComponent.repaint();
				parentComponent.validate();
//				parentComponent.requestFocus();
			} else {

				progressIcon.suspend();
				parentComponent.remove(progressIcon);
				if (replacedComponent != null) {
					parentComponent.add(replacedComponent, addConstraint);
				}
				parentComponent.repaint();
				parentComponent.validate();
//				parentComponent.requestFocus();
			}

		}
	}

	private static class AnimatedProgressIcon extends AsyncProcessIcon {
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
            IdeaVersionFacade.getInstance().initAsyncProcessIcon(this, icons, passiveIcon, CYCLE_LENGTH, CYCLE_GAP, -1);
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
}
