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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModelListener;
import com.atlassian.theplugin.crucible.model.UpdateContext;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.idea.ui.SingleTabToolWindow;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: pmaruszak
 */
public class CrucibleToolWindow extends SingleTabToolWindow {

	protected CrucibleToolWindow(@NotNull final Project project,
								 @NotNull final CrucibleReviewListModel reviewListModel) {
		super(project, reviewListModel);
	}

	protected String getContentKey(ContentParameters params) {
		ReviewContentParameters cParams = (ReviewContentParameters) params;
		ReviewAdapter ra = cParams != null ? cParams.reviewAdapter : null;
		String key = "";

		if (ra != null) {
			key = ra.getServer().getServerId() + ra.getPermId().getId();
		}
		return key;
	}

	protected ContentPanel createContentPanel(ContentParameters params) {
		return new ReviewPanel((ReviewContentParameters) params);
	}


	private final class ReviewContentParameters implements SingleTabToolWindow.ContentParameters {
		private final ReviewAdapter reviewAdapter;

		private ReviewContentParameters(ReviewAdapter reviewAdapter) {
			this.reviewAdapter = reviewAdapter;
		}
	}

	public void showReview(ReviewAdapter reviewAdapter) {
		showToolWindow(new ReviewContentParameters(reviewAdapter), "Review", Constants.CRUCIBLE_ICON);
	}

	public AtlassianTreeWithToolbar getAtlassianTreeWithToolbar() {
		if (getContentPanel() != null && getContentPanel().getContentParameters() != null) {
			return ((ReviewPanel) getContentPanel()).commentsPanel.getReviewItemTreePanel().getAtlassianTreeWithToolbar();
		}
		return null;
	}

	public void switchFilter() {
		if (getContentPanel() != null && getContentPanel().getContentParameters() != null) {
			((ReviewPanel) getContentPanel()).commentsPanel.getReviewItemTreePanel().switchFilter();
		}
	}

	private final class ReviewPanel extends SingleTabToolWindow.ContentPanel implements CrucibleReviewListModelListener {
		private final ReviewContentParameters params;
		private DetailsPanel detailsPanel;
		private SummaryPanel summaryPanel;
		private CommentsPanel commentsPanel;

		private ReviewPanel(ReviewContentParameters params) {
			this.params = params;

			JTabbedPane tabs = new JTabbedPane();
			detailsPanel = new DetailsPanel();

			tabs.addTab("Details", detailsPanel);
			commentsPanel = new CommentsPanel();
			tabs.addTab("Comments", commentsPanel);

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN, 0, 0);
			summaryPanel = new SummaryPanel();
			add(summaryPanel, gbc);
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(tabs, gbc);

			if (reviewListModel != null) {
				reviewListModel.addListener(this);
			}
			refresh();
		}

		public void refresh() {

		}

		public void unregister() {
			if (params != null) {
				reviewListModel.removeListener(this);
			}

			commentsPanel.unregisterListeners();
		}

		public String getKey() {
			if (params != null && params.reviewAdapter != null) {
				ReviewAdapter ra = params.reviewAdapter;
				return ra.getPermId().getId() + "(" + ra.getServer().getName() + ")";
			}
			return "";
		}

		public ContentParameters getContentParameters() {
			return params;
		}

		public CommentsPanel getCommentsPanel() {
			return commentsPanel;
		}

		public void reviewAdded(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewRemoved(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewChanged(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void modelChanged(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewListUpdateStarted(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewListUpdateFinished(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewChangedWithoutFiles(UpdateContext updateContext) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void reviewListUpdateError(UpdateContext updateContext, Exception exception) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		private class DetailsPanel extends JPanel {
			private JScrollPane scroll;

			public DetailsPanel() {
				setLayout(new GridBagLayout());

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.fill = GridBagConstraints.BOTH;

				scroll = new JScrollPane(createBody());
				scroll.setBorder(BorderFactory.createEmptyBorder());
				add(scroll, gbc);

				Border b = BorderFactory.createTitledBorder("Details");
				setBorder(b);
				Insets i = b.getBorderInsets(this);
				setMinimumSize(new Dimension(0, i.top + i.bottom));
			}

			private JPanel createBody() {
				JPanel body = new JPanel();

				body.setLayout(new GridBagLayout());

				GridBagConstraints gbc1 = new GridBagConstraints();
				GridBagConstraints gbc2 = new GridBagConstraints();
				gbc1.anchor = GridBagConstraints.FIRST_LINE_END;
				gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
						Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
				gbc2.fill = GridBagConstraints.HORIZONTAL;
				gbc2.weightx = 1.0;
				gbc1.gridx = 0;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;

				ReviewAdapter ra = params.reviewAdapter;
				body.add(new BoldLabel("Statement of Objectives"), gbc1);
				body.add(new JLabel(ra.getSummary()), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("State"), gbc1);
				body.add(new JLabel(ra.getState().getDisplayName()), gbc2);
				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Open since"), gbc1);
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)");
				DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

				body.add(new JLabel(ds.format(ra.getCreateDate())), gbc2);

				gbc1.gridx = 2;
				gbc2.gridx = gbc1.gridx + 1;
				gbc1.gridy = 0;
				gbc2.gridy = 0;
				body.add(new BoldLabel("Author"), gbc1);
				body.add(new JLabel(ra.getCreator().getDisplayName()), gbc2);

				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Moderator"), gbc1);
				body.add(new JLabel(ra.getModerator().getDisplayName()), gbc2);

				gbc1.gridy++;
				gbc2.gridy++;
				body.add(new BoldLabel("Reviewers"), gbc1);

				JPanel reviewers = new JPanel(new VerticalFlowLayout());
				Icon reviewCompletedIcon = IconLoader.getIcon("/actions/check.png");
				try {
					for (Reviewer reviewer : ra.getReviewers()) {
						reviewers.add(new JLabel(reviewer.getDisplayName(),
								reviewer.isCompleted() ? reviewCompletedIcon : null,
								SwingConstants.LEFT));
					}

					body.add(reviewers, gbc2);
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
					//do not care
				}


				return body;
			}

		}

		private final class CommentsPanel extends JPanel {
			private ReviewItemTreePanel reviewItemTreePanel;
			private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

			private CommentsPanel() {
				super(new BorderLayout());
				setBackground(UIUtil.getTreeTextBackground());
				reviewItemTreePanel = new ReviewItemTreePanel(project, CrucibleFilteredModelProvider.Filter.FILES_ALL);
				reviewItemTreePanel.getProgressAnimation().configure(reviewItemTreePanel,
						reviewItemTreePanel, BorderLayout.CENTER);
				add(reviewItemTreePanel, BorderLayout.CENTER);

				registerListeners();
				progressAnimation.configure(this, reviewItemTreePanel, BorderLayout.CENTER);
				progressAnimation.startProgressAnimation();

				Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving Crucible Data", false) {
					@Override
					public void run(@NotNull final ProgressIndicator indicator) {
						reviewItemTreePanel.showReview(params.reviewAdapter);
					}

					@Override
					public void onCancel() {
						progressAnimation.stopProgressAnimation();
					}

					@Override
					public void onSuccess() {
						progressAnimation.stopProgressAnimation();
					}
				};

				ProgressManager.getInstance().run(task);


			}

			private void registerListeners() {
				reviewItemTreePanel.startListeningForCredentialChanges(project, params.reviewAdapter);
				params.reviewAdapter.addReviewListener(reviewItemTreePanel.getReviewListener());
			}

			public void unregisterListeners() {
				reviewItemTreePanel.stopListeningForCredentialChanges();
				params.reviewAdapter.removeReviewListener(reviewItemTreePanel.getReviewListener());
			}

			public ReviewItemTreePanel getReviewItemTreePanel() {
				return reviewItemTreePanel;
			}
		}

		private class SummaryPanel extends JPanel implements DataProvider {

			private JEditorPane summary;

			public SummaryPanel() {
				setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();

				gbc.gridy = 0;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				summary = new JEditorPane();
				summary.setContentType("text/html");
				summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				refresh();
				summary.setEditable(false);
				summary.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							BrowserUtil.launchBrowser(e.getURL().toString());
						}
					}
				});

				summary.setFont(summary.getFont().deriveFont(Font.BOLD));
				summary.setOpaque(false);
				JPanel p = new JPanel();
				p.setLayout(new GridBagLayout());
				GridBagConstraints gbcp = new GridBagConstraints();
				gbcp.fill = GridBagConstraints.BOTH;
				gbcp.weightx = 1.0;
				gbcp.weighty = 1.0;
				gbcp.gridx = 0;
				gbcp.gridy = 0;
				p.add(summary, gbcp);
				add(p, gbc);

				gbc.gridy++;

				ActionManager manager = ActionManager.getInstance();
				if (manager != null) {
					ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.Reviews.SingleReview.ToolBar");
					ActionToolbar toolbar = manager.createActionToolbar(getContentKey(params), group, true);
					toolbar.setTargetComponent(this);
					JComponent comp = toolbar.getComponent();
					add(comp, gbc);
				}
			}

			public void refresh() {
				String txt = "<html><body><a href=\"" + params.reviewAdapter.getReviewUrl() + "\">"
						+ params.reviewAdapter.getPermId().getId() + "</a> "
						+ params.reviewAdapter.getName() + "</body></html>";
				summary.setText(txt);
			}

			@Nullable
			public Object getData(@NonNls String dataId) {
				if (dataId.equals(Constants.REVIEW_TOOL_WINDOW)) {
					return this;
				} else if (dataId.equals(Constants.REVIEW_WINDOW_ENABLED)) {
					return true;
				} else if (dataId.equals(Constants.REVIEW) && getContentPanel() != null
						&& getContentPanel().getContentParameters() != null) {
					return ((ReviewContentParameters) getContentPanel().getContentParameters()).reviewAdapter;
				}
				return null;
			}
		}

	}
}
