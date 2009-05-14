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
package com.atlassian.theplugin.idea.bamboo;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractBuildListModelDecorator implements BuildListModel, BuildListModelListener {
	protected BuildListModel parent;

	private Collection<BuildListModelListener> listeners = new ArrayList<BuildListModelListener>();

	public AbstractBuildListModelDecorator(final BuildListModel buildModel) {
		this.parent = buildModel;
		parent.addListener(this);
	}

	public Collection<BambooBuildAdapterIdea> getBuilds() {
		return parent.getBuilds();
	}

	public void addListener(final BuildListModelListener listener) {
		listeners.add(listener);
	}

	protected void notifyDecoratorListeners() {
		for (BuildListModelListener listener : listeners) {
			listener.modelChanged();
		}
	}

	public void modelChanged() {
		for (BuildListModelListener listener : listeners) {
			listener.modelChanged();
		}
	}

	public void buildsChanged(@Nullable final Collection<String> additionalInfo,
			@Nullable final Collection<Pair<String, Throwable>> errors) {
		for (BuildListModelListener listener : listeners) {
			listener.buildsChanged(additionalInfo, errors);
		}
	}
}
