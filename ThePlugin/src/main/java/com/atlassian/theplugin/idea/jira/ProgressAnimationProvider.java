package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.ui.AnimatedProgressIcon;

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
	 * Sets component which is hidden when ProgressIcon starts and restored when ProgressIcon is stops
	 * Parameters shouldn't be null
	 * @param rootComponent parent of the replaceComponent
	 * @param replaceComponent component to hide (to replace by animated progress icon)
	 * @param constraint constraind used to add replaceComponent
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
				parentComponent.requestFocus();
			} else {

				progressIcon.suspend();
				parentComponent.remove(progressIcon);
				if (replacedComponent != null) {
					parentComponent.add(replacedComponent, addConstraint);
				}
				parentComponent.repaint();
				parentComponent.validate();
				parentComponent.requestFocus();
			}

		}
	}
}
