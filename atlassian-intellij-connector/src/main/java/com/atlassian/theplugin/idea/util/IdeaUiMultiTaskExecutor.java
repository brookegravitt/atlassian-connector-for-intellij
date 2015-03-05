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
import java.util.concurrent.CountDownLatch;

/**
 * @author Jacek Jaroczynski
 */
public final class IdeaUiMultiTaskExecutor {
	private Component component;
	private List<ErrorObject> errors = new ArrayList<ErrorObject>();

	private CountDownLatch finishedLatch;

	private IdeaUiMultiTaskExecutor(final int numberOfThread) {
		finishedLatch = new CountDownLatch(numberOfThread);
	}

	public static void execute(final UiTask uiTask) {
		getExecutor(1).executeTask(uiTask);
	}

	public static void execute(final List<UiTask> tasks, final Component c) {
		getExecutor(tasks.size()).executeTasks(tasks, c);
	}

	private static IdeaUiMultiTaskExecutor getExecutor(int numberOfThread) {
		return new IdeaUiMultiTaskExecutor(numberOfThread);
	}

	private void executeTask(final UiTask uiTask) {
		component = uiTask.getComponent();
		runTask(uiTask);
		waitForThreadsAndShowErrors();
	}

	private void executeTasks(final List<UiTask> tasks, final Component c) {
		component = c;
		for (UiTask uiTask : tasks) {
			runTask(uiTask);
		}
		waitForThreadsAndShowErrors();
	}

	private void runTask(final UiTask uiTask) {
		final ModalityState modalityState = ModalityState.stateForComponent(uiTask.getComponent());

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				try {
					uiTask.run();
				} catch (final Exception e) {
					reportError(e, uiTask, modalityState);
					finishedLatch.countDown();
					return;
				}

				reportSuccess(uiTask, modalityState);
				finishedLatch.countDown();
			}
		});
	}

	// todo it should not be synchronized???
	private synchronized void waitForThreadsAndShowErrors() {

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				try {
					finishedLatch.await();
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							if (component != null && component.isShowing()) {
								if (errors.size() == 1) {
									DialogWithDetails.showExceptionDialog(component, errors.get(0).getMessage(),
											errors.get(0).getException());
								} else if (errors.size() > 1) {
									DialogWithDetails.showExceptionDialog(component, errors);
								}
							}
							clearErrors();
						}
					});

				} catch (InterruptedException e) {
					PluginUtil.getLogger().warn("InterruptedException caught when waiting for CountDownLatch", e);
				}
			}
		});
	}

	private synchronized void reportSuccess(final UiTask uiTask, final ModalityState modalityState) {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				uiTask.onSuccess();
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
		private Throwable exception;

		public ErrorObject(final String message, final Throwable exception) {
			this.message = message;
			this.exception = exception;
		}

		public Throwable getException() {
			return exception;
		}

		public String getMessage() {
			return message;
		}
	}
}
