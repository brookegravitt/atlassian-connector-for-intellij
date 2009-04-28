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
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public final class IdeaUiMultiTaskExecutor {
	private int semaphore;
	private Component component;
	private List<ErrorObject> errors = new ArrayList<ErrorObject>();

	private IdeaUiMultiTaskExecutor() {
	}

	public static void execute(final UiTask uiTask) {
		getExecutor().executeTask(uiTask);
	}

	public static void execute(final List<UiTask> tasks, final Component c) {
		getExecutor().executeTasks(tasks, c);
	}

	private static IdeaUiMultiTaskExecutor getExecutor() {
		return new IdeaUiMultiTaskExecutor();
	}

	private void executeTask(final UiTask uiTask) {
		semaphore = 1;
		component = uiTask.getComponent();
		runTask(uiTask);
	}

	private void executeTasks(final List<UiTask> tasks, final Component c) {
		semaphore = tasks.size();
		component = c;
		for (UiTask uiTask : tasks) {
			runTask(uiTask);
		}
	}

	private void runTask(final UiTask uiTask) {
		final ModalityState modalityState = ModalityState.stateForComponent(uiTask.getComponent());

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				try {
					uiTask.run();
				} catch (final Exception e) {
					reportError(e, uiTask, modalityState);
					stopTask();
					return;
				}

				reportSuccess(uiTask, modalityState);
				stopTask();
			}
		});
	}

	private synchronized void stopTask() {
		semaphore--;

		if (semaphore < 0) {
			PluginUtil.getLogger().warn("Wrong IdeaUiTaskExecutor implementation! Semaphore value is less than zero.");
			semaphore = 0;
		}

		if (semaphore == 0) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (component != null && component.isShowing()) {
						if (errors.size() == 1) {
							DialogWithDetails
									.showExceptionDialog(component, errors.get(0).getMessage(), errors.get(0).getException());
						} else if (errors.size() > 1) {
							DialogWithDetails.showExceptionDialog(component, errors);
						}
					}
					clearErrors();
				}
			});
		}
	}

	private synchronized void reportSuccess(final UiTask uiTask, final ModalityState modalityState) {

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				try {
					uiTask.onSuccess();
				} catch (final Exception e) {
					ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
						public void run() {
							LoggerImpl.getInstance().warn(e);
							addError(new ErrorObject("Error while " + uiTask.getLastAction(), e));
						}
					});
				}
			}
		}, modalityState);
	}

	private synchronized void reportError(final Exception e, final UiTask uiTask, final ModalityState modalityState) {

		LoggerImpl.getInstance().warn(e);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				uiTask.onError();
			}
		}, modalityState);

		addError(new ErrorObject("Error while " + uiTask.getLastAction(), e));
	}

	private void addError(ErrorObject error) {
		errors.add(error);
	}

	private void clearErrors() {
		errors.clear();
	}

	public static class ErrorObject {
		private String message;
		private Exception exception;

		public ErrorObject(final String message, final Exception exception) {
			this.message = message;
			this.exception = exception;
		}

		public Exception getException() {
			return exception;
		}

		public String getMessage() {
			return message;
		}
	}
}
