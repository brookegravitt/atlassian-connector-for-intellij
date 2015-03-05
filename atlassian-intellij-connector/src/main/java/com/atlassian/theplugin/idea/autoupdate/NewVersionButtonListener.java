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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.idea.GeneralConfigForm;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.progress.ProgressManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Lukasz Guminski
 */
public class NewVersionButtonListener implements ActionListener {


	private GeneralConfigForm generalConfigForm;
	private GeneralConfigurationBean updateConfig = new GeneralConfigurationBean();

	public NewVersionButtonListener(GeneralConfigForm generalConfigForm) {
		this.generalConfigForm = generalConfigForm;
	}

	public void actionPerformed(ActionEvent event) {
		updateConfig.setAnonymousEnhancedFeedbackEnabled(generalConfigForm.getIsAnonymousFeedbackEnabled());
		updateConfig.setAutoUpdateEnabled(true);	// check now button always checks for new version
		updateConfig.setCheckUnstableVersionsEnabled(generalConfigForm.getCheckNewVersionAll().isSelected());
		updateConfig.setUid(IdeaHelper.getAppComponent().getConfiguration().getState().getGeneralConfigurationData().getUid());

		ProgressManager.getInstance().run(new
				NewVersionCheckModalTask(generalConfigForm.getRootComponent(), updateConfig, false));
	}


}
