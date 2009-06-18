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

package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;


public class ViewFisheyeChangesetAction extends AbstractFisheyeAction {

	@Override
	public void actionPerformed(AnActionEvent event) {
		String rev = ChangeListUtil.getRevision(event);
		ServerData cfg = getFishEyeServerCfg(event);
		String repository = getFishEyeRepository(event);
		if (cfg != null && repository != null) {
			String url = cfg.getUrl() + "/changelog/" + repository + "/?cs=" + rev;
			BrowserUtil.launchBrowser(url);
		}
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		if (event.getPresentation().isVisible()) {
			event.getPresentation().setEnabled(ChangeListUtil.getRevision(event) != null);
		}
	}
}

