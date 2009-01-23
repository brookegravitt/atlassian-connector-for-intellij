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
package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CrucibleCreatePreCommitReviewForm extends CrucibleReviewCreateForm {
	private final String patch;

	public CrucibleCreatePreCommitReviewForm(Project project, CrucibleServerFacade crucibleServerFacade, String commitMessage,
			@NotNull final String patch, @NotNull final CfgManager cfgManager) {
		super(project, crucibleServerFacade, commitMessage, cfgManager, "Create Pre-Commit Review");
		this.patch = patch;
		setTitle("Create Patch Review");
		JPanel patchContainer = new JPanel(new BorderLayout());

		JLabel patchLabel = new JLabel();
		patchLabel.setText("Patch:");
		patchContainer.add(patchLabel, BorderLayout.NORTH);
		JScrollPane patchPanel = new JScrollPane();
		JTextArea patchPreview = new JTextArea();
		patchPreview.setEditable(false);
		patchPreview.setEnabled(true);
		patchPreview.setFont(new Font("Monospaced", patchPreview.getFont().getStyle(), patchPreview.getFont().getSize()));
		patchPreview.setLineWrap(true);
		patchPreview.setText(patch);
		patchPanel.setViewportView(patchPreview);
		patchLabel.setLabelFor(patchPreview);
		patchContainer.add(patchPanel);
		setCustomComponent(patchContainer);
		pack();
	}

	@Override
	protected Review createReview(CrucibleServerCfg server, ReviewProvider reviewProvider) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		return  crucibleServerFacade.createReviewFromPatch(server, reviewProvider, patch);
	}
}
