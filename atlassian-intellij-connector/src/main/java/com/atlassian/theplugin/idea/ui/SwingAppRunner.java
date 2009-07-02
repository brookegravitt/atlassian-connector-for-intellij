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
package com.atlassian.theplugin.idea.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class SwingAppRunner {
	private static final int WIDTH = 500;
	private static final int HEIGHT = 180;

	private SwingAppRunner() {
	}

	public static void run(@NotNull JComponent component) {
		run(component, "Test Application", WIDTH, HEIGHT);
	}

	public static void run(@NotNull JComponent component, @NotNull String titile, final int width, final int height) {
		JFrame frame = new JFrame("Test Application");
		//Finish setting up the frame, and show it.
		frame.setSize(width, height);
		frame.getContentPane().add(component, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
//		frame.pack();
		frame.setVisible(true);

	}

}
