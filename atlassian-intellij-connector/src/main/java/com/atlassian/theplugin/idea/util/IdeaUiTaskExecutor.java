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
package com.atlassian.theplugin.idea.util;

import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

public class IdeaUiTaskExecutor implements UiTaskExecutor {


	public void execute(final UiTask uiTask) {
		final ModalityState modalityState = ModalityState.stateForComponent(uiTask.getComponent());

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				try {
					uiTask.run();
				} catch (final Exception e) {
					LoggerImpl.getInstance().warn(e);
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						public void run() {
							uiTask.onError();
							if (uiTask.getComponent() != null && uiTask.getComponent().isShowing()) {
								DialogWithDetails.showExceptionDialog(uiTask.getComponent(),
										"Error while " + uiTask.getLastAction(), e);
							}
						}
					}, modalityState);
					return;
				}

				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						try {
							uiTask.onSuccess();
						} catch (Exception e) {
							LoggerImpl.getInstance().warn(e);
							if (uiTask.getComponent() != null && uiTask.getComponent().isShowing()) {
								DialogWithDetails.showExceptionDialog(uiTask.getComponent(),
										"Error while " + uiTask.getLastAction(), e);
							}
						}
					}
				}, modalityState);
			}
		});
	}
}
