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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleStatusDisplay;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-31
 * Time: 17:36:10
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowCrucibleContent extends JEditorPane implements CrucibleStatusDisplay {

	public ToolWindowCrucibleContent() {
		setEditable(false);
        setContentType("text/html");
		addHyperlinkListener(new GenericHyperlinkListener());
	}

    public void updateCrucibleStatus(String htmlPage) {
		this.setText(htmlPage);
	}
}