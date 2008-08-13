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
package com.atlassian.theplugin.cfg;

import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.intellij.openapi.project.Project;

public final class CfgUtil {

	public static final ProjectId GLOBAL_PROJECT = new ProjectId();

	private CfgUtil() {
		// this is utility class
	}

	public static ProjectId getProjectId(Project project) {
		return GLOBAL_PROJECT;
		//return new ProjectId(project.getPresentableUrl());
	}
}
