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

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.tree.BuildTree;
import com.atlassian.theplugin.idea.ui.PopupAwareMouseAdapter;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @autrhor pmaruszak
 * @date Aug 1, 2010
 */
public class BuildTreeEventHandler {
    private final BambooToolWindowPanel bambooPanel;
    private final BuildTree buildTree;
    private final BuildHistoryPanel buildHistoryPanel;

    BuildTreeEventHandler(BambooToolWindowPanel panel, BuildTree bTree, BuildHistoryPanel historyPanel) {
        this.bambooPanel = panel;
        this.buildTree = bTree;
        this.buildHistoryPanel = historyPanel;


        buildTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				final BambooBuildAdapter buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getKeyCode() == KeyEvent.VK_ENTER && buildDetailsInfo != null) {
					bambooPanel.openBuild(buildDetailsInfo);
				}
			}
        });

		buildTree.addMouseListener(new PopupAwareMouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				final BambooBuildAdapter buildDetailsInfo = buildTree.getSelectedBuild();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && buildDetailsInfo != null 
                        && ((e.getModifiers() & KeyEvent.CTRL_DOWN_MASK) == 0)) {
					bambooPanel.openBuild(buildDetailsInfo);                    
				}
			}

			@Override
			protected void onPopup(MouseEvent e) {
				int selRow = buildTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = buildTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && selPath != null) {
                  if (buildTree.getSelectedBuild() != null) {
					buildTree.setSelectionPath(selPath);
					final BambooBuildAdapter buildDetailsInfo = buildTree.getSelectedBuild();
					if (buildDetailsInfo != null) {
						bambooPanel.launchContextMenu(e);
					}
                  } else if (buildTree.getSelectedBuilds() != null) {
                        bambooPanel.launchContextMenuGorGroup(e);
                  }
            }
			}
		});


		buildTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent event) {
				final BambooBuildAdapter buildDetailsInfo = buildTree.getSelectedBuild();
				if (buildDetailsInfo != null) {
					buildHistoryPanel.showHistoryForBuild(buildDetailsInfo);
				} else {
					buildHistoryPanel.clearBuildHistory();
				}
			}
		});

    }
}
