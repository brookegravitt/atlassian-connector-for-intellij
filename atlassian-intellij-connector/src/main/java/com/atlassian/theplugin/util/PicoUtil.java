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

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.crucible.model.ReviewListModelBuilderImpl;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import org.picocontainer.MutablePicoContainer;

@Deprecated
public final class PicoUtil {
	///CLOVER:OFF
	private PicoUtil() {
	}
	///CLOVER:ON

	private static final Class<?>[] GLOBAL_COMPONENTS = {
			IdeaActionScheduler.class,
			NewVersionChecker.class
	};

	private static final Class<?>[] PROJECT_COMPONENTS = {
			ReviewListModelBuilderImpl.class,
			CrucibleStatusChecker.class
	};


	public static void populateGlobalPicoContainer(MutablePicoContainer pico) {
		populate(pico, GLOBAL_COMPONENTS);
	}

	public static void populateProjectPicoContainer(MutablePicoContainer pico) {
		populate(pico, PROJECT_COMPONENTS);
	}

	private static void populate(MutablePicoContainer pico, Class<?>[] projectComponents) {
		for (Class<?> clazz : projectComponents) {
			pico.registerComponentImplementation(clazz);
		}
	}

}
