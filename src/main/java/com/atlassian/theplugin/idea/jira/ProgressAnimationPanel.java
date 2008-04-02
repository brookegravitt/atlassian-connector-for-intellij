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
public class ProgressAnimationPanel extends JPanel {

	private AnimatedProgressIcon progressIcon = new AnimatedProgressIcon("Progress Indicator");
	private JComponent replacedComponent;
	private Object addConstraint;
	private JComponent rootComponent;

	/**
	 * Sets component which is hidden when ProgressIcon starts and restored when ProgressIcon is stops
	 * Parameters shouldn't be null
	 * @param rootComponent parent of the replacedComponent
	 * @param replacedComponent component to hide (to replace by animated progress icon)
	 * @param addConstraint constraind used to add replacedComponent
	 */
	public void setReplacedComponent(JComponent rootComponent, JComponent replacedComponent, Object addConstraint) {
		this.rootComponent = rootComponent;
		this.replacedComponent = replacedComponent;
		this.addConstraint = addConstraint;
	}

	protected void startProgressAnimation() {
		EventQueue.invokeLater(new ProgressAnimation(true));
	}

	protected void stopProgressAnimation() {
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
					rootComponent.remove(replacedComponent);
				}
				rootComponent.add(progressIcon, addConstraint);
				progressIcon.resume();
				rootComponent.repaint();
				rootComponent.validate();
				rootComponent.requestFocus();
			} else {

				progressIcon.suspend();
				rootComponent.remove(progressIcon);
				if (replacedComponent != null) {
					rootComponent.add(replacedComponent, addConstraint);
				}
				rootComponent.repaint();
				rootComponent.validate();
				rootComponent.requestFocus();
			}

		}
	}
}
