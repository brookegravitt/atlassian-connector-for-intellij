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

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ide.DataManager;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-11
 * Time: 16:39:01
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowConfigPanel extends JPanel {
	public ToolWindowConfigPanel() {

		super(new GridBagLayout());

		HyperlinkLabel link = new HyperlinkLabel("Configure Plugin");
		link.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {

				ShowSettingsUtil.getInstance().
						editConfigurable(IdeaHelper.getCurrentProject(DataManager.getInstance().getDataContext(ToolWindowConfigPanel.this)), IdeaHelper.getAppComponent());
			}
		});

		link.setIcon(IconLoader.getIcon("/general/ideOptions.png"));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		this.add(link, c);
	}
}
