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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.ui.SwingAppRunner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;


public final class TwoPanePanelTestUi {
	private TwoPanePanelTestUi() {
	}

	public static void main(String[] args) {
		SwingAppRunner.run(new ThreePanePanel() {
			{
				init();
//				setStatusInfoMessage("Very long message<br/>THIS IS VERY LOOOOOOOOOOOOOOOOOONG line<br/>Another line<br/>", true);
			}

			@Override
			protected JTree getRightTree() {
				return new JTree();
			}

			@Override
			protected JComponent getLeftToolBar() {
				final JButton button = new JButton("my toolbar");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						if (new Random().nextBoolean()) {
							if (new Random().nextBoolean()) {
								setStatusMessage("All OK");
							} else {
								setStatusMessage("Very looooooooooooooooooong message");
							}
						} else {
							setErrorMessage("Last lineWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW"
											+ "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWXYZWWWWWWWWWWWWWWWWWWWWWWWWWWWW"
											+ "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWAAAAAAAAAMMMMWWWWWWZZZ! error");
						}
					}
				});
				return button;
			}

			@Override
			protected JComponent getLeftPanel() {
				return new JLabel(
						"<html>Very long message<br/>THIS IS VERY LOOOOOOOOOOOOOOOOOONG line<br/>Another line<br/>");
			}

            protected JComponent getRightMostPanel() {
                return new JPanel();
            }

            protected JComponent getRightMostToolBar() {
                return new JPanel();
            }
        });
	}

}
