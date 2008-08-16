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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.ConnectionWrapper;
import com.atlassian.theplugin.LoginDataProvided;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.idea.GeneralConfigForm;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 12, 2008
 * Time: 5:04:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewVersionButtonListener implements ActionListener {
    private ConnectionWrapper checkerThread;
    private static final long CHECK_CANCEL_INTERVAL = 500; //milis
    private GeneralConfigForm generalConfigForm;
    private InfoServer.VersionInfo newVersion;
    private GeneralConfigurationBean updateConfig = new GeneralConfigurationBean();

    public NewVersionButtonListener(GeneralConfigForm generalConfigForm) {
        this.generalConfigForm = generalConfigForm;
    }

    public void actionPerformed(ActionEvent event) {
        updateConfig.setAnonymousFeedbackEnabled(generalConfigForm.getIsAnonymousFeedbackEnabled());
        updateConfig.setAutoUpdateEnabled(true);    // check now button always checks for new version
        updateConfig.setCheckUnstableVersionsEnabled(generalConfigForm.getCheckNewVersionAll().isSelected());
        updateConfig.setUid(IdeaHelper.getAppComponent().getState().getGeneralConfigurationData().getUid());

        ProgressManager.getInstance().run(new UpdateModalTask());
    }

    private class UpdateServerConnection extends Connector {
		protected UpdateServerConnection() {
		}

		@Override
		public void connect(LoginDataProvided loginDataProvided) throws ThePluginException {
            NewVersionChecker.getInstance(IdeaHelper.getPluginConfiguration()).doRun(new UpdateActionHandler() {
                public void doAction(InfoServer.VersionInfo versionInfo, boolean showConfigPath) throws ThePluginException {
                    newVersion = versionInfo;
                }
            }, false, updateConfig);
        }
    }

    private class UpdateModalTask extends Task.Modal {
        public UpdateModalTask() {
            super(null, "Checking available updates", true);
        }

        @Override
		public void run(ProgressIndicator indicator) {
            newVersion = null;
            setCancelText("Stop");
            indicator.setText("Connecting...");
            indicator.setFraction(0);
            indicator.setIndeterminate(true);
            checkerThread = new ConnectionWrapper(new UpdateServerConnection(), null, "atlassian-idea-plugin New version checker");
            checkerThread.start();
            while (checkerThread.getConnectionState() == ConnectionWrapper.ConnectionState.NOT_FINISHED) {
                try {
                    if (indicator.isCanceled()) {
                        checkerThread.setInterrupted();
                        //t.interrupt();
                        break;
                    } else {
                        Thread.sleep(CHECK_CANCEL_INTERVAL);
                    }
                } catch (InterruptedException e) {
                    PluginUtil.getLogger().info(e.getMessage());
                }
            }

            switch (checkerThread.getConnectionState()) {
                case FAILED:
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            showMessageDialog(checkerThread.getErrorMessage(),
                                    "Error occured when contacting update server", Messages.getErrorIcon());
                        }
                    });
                    break;
                case INTERUPTED:
                    PluginUtil.getLogger().debug("Cancel was pressed during the upgrade process");
                    break;
                case NOT_FINISHED:
                    break;
                case SUCCEEDED:
                    if (newVersion != null) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    new NewVersionConfirmHandler(null, updateConfig).doAction(newVersion, false);
                                } catch (ThePluginException e) {
                                    showMessageDialog(e.getMessage(),
                                            "Error retrieving new version", Messages.getErrorIcon());
                                }
                            }
                        });
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                showMessageDialog("You have the latest version (" + PluginUtil.getInstance().getVersion() + ")",
                                        "Version checked", Messages.getInformationIcon());
                            }
                        });
                    }
                    break;
                default:
                    PluginUtil.getLogger().info("Unexpected thread state: "
                            + checkerThread.getConnectionState().toString());
            }
        }
    }
}
