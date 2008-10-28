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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.Messages;

public class IdeaUiTaskExecutor implements UiTaskExecutor {
	public void execute(final UiTask uiTask) {
		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				try {
					uiTask.run();
				} catch (Exception e) {
					LoggerImpl.getInstance().warn(e);
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						public void run() {
							uiTask.onError();
							if (uiTask.getComponent().isShowing()) {
								Messages.showErrorDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(),
										"Error");
							}
						}
					}, ModalityState.stateForComponent(uiTask.getComponent()));
					return;
				}
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					public void run() {
						try {
							uiTask.onSuccess();
						} catch (Exception e) {
							LoggerImpl.getInstance().warn(e);
							Messages.showErrorDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(),
									"Error");
						}
					}
				}, ModalityState.stateForComponent(uiTask.getComponent()));
			}
		});
	}
}
