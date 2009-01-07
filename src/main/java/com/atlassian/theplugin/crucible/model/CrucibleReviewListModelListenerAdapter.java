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
package com.atlassian.theplugin.crucible.model;

/**
 * @author Jacek Jaroczynski
 */
public class CrucibleReviewListModelListenerAdapter implements CrucibleReviewListModelListener {
	public void reviewAdded(UpdateContext updateContext) {
	}

	public void reviewRemoved(UpdateContext updateContext) {
	}

	public void reviewChanged(UpdateContext updateContext) {
	}

	public void modelChanged(UpdateContext updateContext) {
	}

	public void reviewListUpdateStarted(UpdateContext updateContext) {
	}

	public void reviewListUpdateFinished(UpdateContext updateContext) {
	}

	public void reviewListUpdateError(final UpdateContext updateContext, final Exception exception) {
	}
}
