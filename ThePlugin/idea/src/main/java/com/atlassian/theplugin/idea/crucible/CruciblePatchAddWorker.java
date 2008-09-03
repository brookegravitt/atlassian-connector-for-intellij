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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;


public class CruciblePatchAddWorker implements Runnable {
	private final CrucibleHelperForm helperForm;

	public CruciblePatchAddWorker(CrucibleServerCfg cfg, CrucibleServerFacade crucibleServerFacade,
			Project project, ChangeList[] changes) {
		PatchProducer patchProducer = new PatchProducer(project, changes[0].getChanges());
		String patch = patchProducer.generateUnifiedDiff();
		helperForm = new CrucibleHelperForm(project, cfg, crucibleServerFacade, patch);
	}

	public void run() {
		helperForm.show();
	}
}