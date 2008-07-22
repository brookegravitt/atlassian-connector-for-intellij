package com.atlassian.theplugin.idea.crucible.tree;
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

import com.atlassian.theplugin.commons.crucible.*;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.file.FileTreeModelBuilder;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 11, 2008
 * Time: 10:56:46 AM
 * To change this template use File | Settings | File Templates.
 */
public final class ReviewItemTreePanel extends JPanel {


	//	ProjectView.
	private JTree reviewFilesTree = null;
	private CrucibleReviewActionListener listener = new MyReviewActionListener();
	private static final int WIDTH = 150;
	private static final int HEIGHT = 250;
	private static ReviewItemTreePanel instance;
	private static CrucibleServerFacade crucibleServerFacade;

	public static final Logger LOGGER = PluginUtil.getLogger();

	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private JLabel statusLabel;

	private ReviewItemTreePanel(ProjectConfigurationBean projectConfigurationBean) {
		initLayout();
		IdeaHelper.getReviewActionEventBroker().registerListener(listener);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
	}


	public static ReviewItemTreePanel getInstance(ProjectConfigurationBean projectConfigurationBean) {
		if (instance == null) {
			instance = new ReviewItemTreePanel(projectConfigurationBean);
		}
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		add(new JLabel("File list"), BorderLayout.NORTH);
		add(new JScrollPane(getReviewItemTree()), BorderLayout.CENTER);
		statusLabel = new JLabel();
		statusLabel.setBackground(UIUtil.getTreeTextBackground());
		add(statusLabel, BorderLayout.SOUTH);
	}

	private JTree getReviewItemTree() {
		if (reviewFilesTree == null) {
			reviewFilesTree = new AtlassianTree();
			reviewFilesTree.setRootVisible(false);
		}
		return reviewFilesTree;
	}

	public void setEnabled(boolean b) {
		super.setEnabled(b);
		getReviewItemTree().setEnabled(b);
	}


	public ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	private class MyReviewActionListener extends CrucibleReviewActionListener {

		public void showReview(final ReviewData reviewItem) {
			progressAnimation.startProgressAnimation();
			try {
				AtlassianTreeModel model = null;
				try {
					model = FileTreeModelBuilder.buildTreeModelFromCrucibleChangeSet(reviewItem);
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
//					((ReviewData) reviewItem).setFiles(
//							crucibleServerFacade.getFiles(reviewItem.getServer(), reviewItem.getPermId()));
				}
				final AtlassianTreeModel model1 = model;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						StringBuffer buffer = new StringBuffer();
						buffer.append("<html>");
						buffer.append("<body>");
						buffer.append(reviewItem.getCreator().getDisplayName());
						buffer.append(" ");
						buffer.append("<font size=-1 color=");
						buffer.append(CrucibleConstants.CRUCIBLE_AUTH_COLOR);
						buffer.append(">AUTH</font>");
						buffer.append(" ");
						if (!reviewItem.getCreator().equals(reviewItem.getModerator())) {
							buffer.append(reviewItem.getModerator().getDisplayName());
						}
						buffer.append(" ");
						buffer.append("<font size=-1 color=");
						buffer.append(CrucibleConstants.CRUCIBLE_MOD_COLOR);
						buffer.append(">MOD</font>");
						int i = 0;
						List<Reviewer> reviewers = null;
						try {
							reviewers = reviewItem.getReviewers();
							if (reviewers != null) {
								buffer.append("<br>");
								for (Reviewer reviewer : reviewers) {
									if (i > 0) {
										buffer.append(", ");
									}
									buffer.append(reviewer.getDisplayName());
									i++;
								}
							}
						} catch (ValueNotYetInitialized valueNotYetInitialized) {
							//ignore
						}
						buffer.append("</body>");
						buffer.append("</html>");
						statusLabel.setText(buffer.toString());

						reviewFilesTree.setModel(model1);
						reviewFilesTree.setRootVisible(true);
					}
				});
			} finally {
				progressAnimation.stopProgressAnimation();
			}
		}

	}
}
