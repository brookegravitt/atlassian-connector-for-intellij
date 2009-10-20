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

import com.atlassian.theplugin.util.PicoUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.AreaInstance;
import com.intellij.openapi.extensions.AreaListener;
import com.intellij.openapi.extensions.AreaPicoContainer;
import com.intellij.openapi.extensions.Extensions;

public class PerProjectPicoInit implements AreaListener {

	public void areaCreated(String areaClass, AreaInstance areaInstance) {
		if (PluginManager.AREA_IDEA_PROJECT.equals(areaClass)) {
			AreaPicoContainer apc = Extensions.getArea(areaInstance).getPicoContainer();
			PicoUtil.populateProjectPicoContainer(apc);
		}
	}

	public void areaDisposing(String areaClass, AreaInstance areaInstance) {
	}
}
