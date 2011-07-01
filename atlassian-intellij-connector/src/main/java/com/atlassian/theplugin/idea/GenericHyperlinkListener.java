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

import com.intellij.ide.BrowserUtil;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A generic HyperlinkListener which catches plugin configuration URLs and opens the settings panel.
 * In all other cases, a browser is opened with the URL clicked.
*/
public class GenericHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
            BrowserUtil.launchBrowser(e.getURL().toString());
        }
	}
}
