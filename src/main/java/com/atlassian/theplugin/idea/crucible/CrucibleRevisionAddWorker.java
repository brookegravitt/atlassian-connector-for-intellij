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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.PermId;
import com.intellij.openapi.vcs.changes.ChangeList;


public class CrucibleRevisionAddWorker implements Runnable {
	private final CrucibleServerFacade crucibleServerFacade;
    private PermId permId;
    private ChangeList[] changes;
    private String revision;
    final CrucibleHelperForm helperForm;

    public CrucibleRevisionAddWorker(CrucibleServerFacade crucibleServerFacade, PermId permId, ChangeList[] changes) {
		this.crucibleServerFacade = crucibleServerFacade;
        this.permId = permId;
        this.changes = changes;
        helperForm = new CrucibleHelperForm(crucibleServerFacade, permId, changes);
    }

    public CrucibleRevisionAddWorker(CrucibleServerFacade crucibleServerFacade, String revision) {
		this.crucibleServerFacade = crucibleServerFacade;
        this.revision = revision;
        helperForm = new CrucibleHelperForm(crucibleServerFacade, revision);
    }

    public void run() {
        helperForm.show();
    }
}