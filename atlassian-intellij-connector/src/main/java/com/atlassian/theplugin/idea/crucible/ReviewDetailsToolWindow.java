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

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.MultiTabToolWindow;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.editor.ChangeViewer;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.idea.ui.BoldLabel;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import com.atlassian.theplugin.util.Htmlizer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Date;

/**
 * User: pmaruszak
 */
public class ReviewDetailsToolWindow extends MultiTabToolWindow implements DataProvider {
	private static final String TOOL_WINDOW_TITLE = "Reviews - Crucible";
	private ReviewAdapter reviewAdapter;
	private final Project project;
	private final ThePluginProjectComponent pluginProjectComponent;
	private PluginConfiguration pluginConfiguration;
	private WorkspaceConfigurationBean workspaceConfiguration;

	private ReviewPanel contentPanel;
	private ReviewContentParameters contentParams;


	protected ReviewDetailsToolWindow(@NotNull final Project project,
			@NotNull final ThePluginProjectComponent pluginProjectComponent,
			@NotNull final PluginConfiguration pluginConfiguration,
			@NotNull final WorkspaceConfigurationBean workspaceConfiguration) {
		super(true);
		this.project = project;
		this.pluginProjectComponent = pluginProjectComponent;
		this.pluginConfiguration = pluginConfiguration;
		this.workspaceConfiguration = workspaceConfiguration;
	}

	@Override
	protected String getContentKey(ContentParameters params) {
		ReviewContentParameters cParams = (ReviewContentParameters) params;
		ReviewAdapter ra = cParams != null ? cParams.reviewAdapter : null;
		String key = "";

		if (ra != null) {
			key = ra.getServerData().getServerId() + ra.getPermId().getId();
		}
		return key;
	}

	@Override
	protected ContentPanel createContentPanel(ContentParameters params) {
		pluginConfiguration.getGeneralConfigurationData().bumpCounter("r");
		contentPanel = new ReviewPanel((ReviewContentParameters) params);
		return contentPanel;
	}

	public void closeToolWindow(AnActionEvent event) {
		super.closeToolWindow(TOOL_WINDOW_TITLE, event);
		CommentHighlighter.removeCommentsInEditors(project);
		ChangeViewer.removeHighlightersInEditors(project);
		this.contentParams.reviewAdapter.clearContentCache();
	}

	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(Constants.REVIEW)) {
			return reviewAdapter;
		}
		return null;
	}

	/**
	 * Select 'file and comments' tab and required comment but only if panel contains provided review
	 *
	 * @param review
	 * @param file
	 * @param comment
	 */
	public void selectVersionedComment(final ReviewAdapter review, final CrucibleFileInfo file, final Comment comment) {
		if (contentParams.reviewAdapter.equals(review)) {
			contentPanel.selectVersionedComment(file, comment);
		}
	}

	public void selectGeneralComment(final ReviewAdapter review, final Comment comment) {
		if (contentParams.reviewAdapter.equals(review)) {
			contentPanel.selectGeneralComment(comment);
		}
	}

	public void selectFile(final ReviewAdapter review, final CrucibleFileInfo file) {
		if (contentParams.reviewAdapter.equals(review)) {
			contentPanel.selectFile(file);
		}
	}

	private final class ReviewContentParameters implements MultiTabToolWindow.ContentParameters {
		private final ReviewAdapter reviewAdapter;
		private boolean refreshDetails;

		private ReviewContentParameters(ReviewAdapter reviewAdapter, final boolean refreshDetails) {
			this.reviewAdapter = reviewAdapter;
			this.refreshDetails = refreshDetails;
		}
	}

	public void showReview(ReviewAdapter adapter, boolean refreshDetails) {
		this.reviewAdapter = adapter;
		if (workspaceConfiguration.getCrucibleConfiguration() != null) {
			workspaceConfiguration.getCrucibleConfiguration()
					.getCrucibleFilters().getRecenltyOpenFilter().addRecentlyOpenReview(adapter);
		}
		contentParams = new ReviewContentParameters(adapter, refreshDetails);
		showToolWindow(project, contentParams, TOOL_WINDOW_TITLE,
				Constants.CRUCIBLE_REVIEW_PANEL_ICON, Constants.CRUCIBLE_REVIEW_TAB_ICON);
	}

	public AtlassianTreeWithToolbar getAtlassianTreeWithToolbar() {
		if (contentParams != null && getContentPanel(getContentKey(contentParams)) != null) {
			return ((ReviewPanel) getContentPanel(getContentKey(contentParams)))
					.commentsPanel.getReviewItemTreePanel().getAtlassianTreeWithToolbar();
		}
		return null;
	}

	public void switchFilter() {
		if (contentParams != null && getContentPanel(getContentKey(contentParams)) != null) {
			((ReviewPanel) getContentPanel(getContentKey(contentParams))).commentsPanel.getReviewItemTreePanel().switchFilter();
		}
	}

	private final class ReviewPanel extends MultiTabToolWindow.ContentPanel implements CrucibleReviewListener {
		private final ReviewContentParameters params;
		private DetailsPanel detailsPanel;
		private SummaryPanel summaryPanel;
		private CommentsPanel commentsPanel;
		private static final String TAB_DETAILS = "Details";
		private static final String TAB_FILES_AND_COMMENTS = "Files and Comments";
		private JTabbedPane tabs;

		private ReviewPanel(ReviewContentParameters params) {
			this.params = params;

			tabs = new JTabbedPane();
			detailsPanel = new DetailsPanel(params.reviewAdapter);

			tabs.addTab(TAB_DETAILS, detailsPanel);
			commentsPanel = new CommentsPanel(params.refreshDetails);
			tabs.addTab(TAB_FILES_AND_COMMENTS, commentsPanel);

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

			if (params.reviewAdapter != null) {
				params.reviewAdapter.addReviewListener(this);
			}
			refresh();
		}

		public void refresh() {

		}

		@Override
		public String getTitle() {
			return params.reviewAdapter.getPermId().getId();
		}

		@Override
		public void unregister() {
			if (params != null) {
				params.reviewAdapter.removeReviewListener(this);
			}

			commentsPanel.unregisterListeners();
		}

		public String getKey() {
			if (params != null && params.reviewAdapter != null) {
				ReviewAdapter ra = params.reviewAdapter;
				return ra.getPermId().getId();
			}
			return "";
		}

		public ContentParameters getContentParameters() {
			return params;
		}

		public CommentsPanel getCommentsPanel() {
			return commentsPanel;
		}

		public void createdOrEditedVersionedCommentReply(final ReviewAdapter review, final PermId file,
				final VersionedComment parentComment,
				final
				VersionedComment comment) {
		}

		public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final GeneralComment parentComment,
				final GeneralComment comment) {
		}

		public void createdOrEditedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
		}

		public void createdOrEditedVersionedComment(final ReviewAdapter review, final PermId file,
				final VersionedComment comment) {
		}

		public void removedComment(final ReviewAdapter review, final Comment comment) {
		}

		public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
		}

		public void publishedVersionedComment(final ReviewAdapter review, final PermId filePermId,
				final VersionedComment comment) {
		}

		public void reviewChanged(final ReviewAdapter review, final java.util.List<CrucibleNotification> notifications) {
			summaryPanel.refresh();
			detailsPanel.refresh();
		}

		public void selectVersionedComment(final CrucibleFileInfo file, final Comment comment) {
			// select tab
			tabs.setSelectedComponent(commentsPanel);

			// select comment
			commentsPanel.selectVersionedComment(file, comment);
		}

		public void selectGeneralComment(final Comment comment) {
			// select tab
			tabs.setSelectedComponent(commentsPanel);

			// select comment
			commentsPanel.selectGeneralComment(comment);
		}

		public void selectFile(final CrucibleFileInfo file) {
			// select tab
			tabs.setSelectedComponent(commentsPanel);

			// select comment
			commentsPanel.selectFile(file);
		}

		private final class CommentsPanel extends JPanel {
			private ReviewItemTreePanel reviewItemTreePanel;
			private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();

			private CommentsPanel(final boolean retrieveDetails) {
				super(new BorderLayout());
				setBackground(UIUtil.getTreeTextBackground());
				reviewItemTreePanel = new ReviewItemTreePanel(project,
						CrucibleFilteredModelProvider.Filter.FILES_ALL,
						pluginProjectComponent);
				reviewItemTreePanel.getProgressAnimation().configure(reviewItemTreePanel,
						reviewItemTreePanel, BorderLayout.CENTER);
				add(reviewItemTreePanel, BorderLayout.CENTER);

				registerListeners();
				progressAnimation.configure(this, reviewItemTreePanel, BorderLayout.CENTER);
				progressAnimation.startProgressAnimation();

				Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving Crucible Data", false) {
					@Override
					public void run(@NotNull final ProgressIndicator indicator) {
						reviewItemTreePanel.showReview(params.reviewAdapter, retrieveDetails);
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

			public void selectVersionedComment(final CrucibleFileInfo file, final Comment comment) {
				reviewItemTreePanel.selectVersionedComment(file, comment);
			}

			public void selectGeneralComment(final Comment comment) {
				reviewItemTreePanel.selectGeneralComment(comment);
			}

			public void selectFile(final CrucibleFileInfo file) {
				reviewItemTreePanel.selectFile(file);
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
				} else if (dataId.equals(Constants.REVIEW) && contentPanel != null && params != null) {
					return params.reviewAdapter;
				}
				return null;
			}
		}

	}

	public ReviewAdapter getReview() {
		return reviewAdapter;
	}
}

// kalamon: I absolutely bloody hate hate hate package-scope non-inner classes

// piggy-backed on the main class of the file. To the author of this class - you suck.
class DetailsPanel extends JPanel {
	private JScrollPane scroll;

	private final ReviewAdapter ra;
	private static final int MAX_DISPLAYED_LINK_LENGTH = 80;

	public DetailsPanel(final ReviewAdapter reviewAdapter) {
		this.ra = reviewAdapter;
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		final JPanel panel = createBody();
		scroll.setViewportView(panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		add(scroll, gbc);
	}

	private JPanel createBody() {
		final JPanel body = new JPanel();

		body.setLayout(new GridBagLayout());
		body.setOpaque(true);
		body.setBackground(Color.WHITE);

		GridBagConstraints gbc1 = new GridBagConstraints();
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc1.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.insets = new Insets(Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.weightx = 1.0;
		gbc1.gridx = 0;
		gbc2.gridx = gbc1.gridx + 1;
		gbc1.gridy = 0;
		gbc2.gridy = 0;

		body.add(new BoldLabel("Statement of Objectives"), gbc1);

		final JEditorPane statementOfObjectives = new JEditorPane() {

		};
		statementOfObjectives.setEditable(false);
		statementOfObjectives.setOpaque(true);
		statementOfObjectives.setBackground(Color.WHITE);
		statementOfObjectives.setContentType("text/html");
		statementOfObjectives.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		Htmlizer htmlizer = new Htmlizer(MAX_DISPLAYED_LINK_LENGTH);
		String sooText = htmlizer.htmlizeHyperlinks(ra.getDescription());
		sooText = htmlizer.replaceWhitespace(sooText);
		statementOfObjectives.setText("<html><body>" + sooText + "</body></html>");
		statementOfObjectives.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					BrowserUtil.launchBrowser(e.getURL().toString());
				}
			}
		});

		statementOfObjectives.setBorder(null);
		body.add(statementOfObjectives, gbc2);
		scroll.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
////				statementOfObjectives.doLayout();
//				System.out.println(statementOfObjectives.getX());
//				int preferredWidth = e.getComponent().getWidth() - statementOfObjectives.getX();
//				System.out.println(preferredWidth);
//				statementOfObjectives.setSize(preferredWidth, 100);
//				final int prefHeight = statementOfObjectives.getPreferredSize().height;
//				System.out.println("H" + prefHeight);
//				statementOfObjectives.setPreferredSize(new Dimension(preferredWidth , prefHeight));
//				statementOfObjectives.setSize(preferredWidth, prefHeight);
//				body.validate();
////				invalidate();
//
//
				body.setPreferredSize(null);

				body.setPreferredSize(new Dimension(scroll.getViewport().getWidth(),
						body.getPreferredSize().height));

				body.validate();
			}
		});

		gbc1.gridy++;
		gbc2.gridy++;
		gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		gbc2.insets = new Insets(0, Constants.DIALOG_MARGIN,
				Constants.DIALOG_MARGIN / 2, Constants.DIALOG_MARGIN);
		body.add(new BoldLabel("State"), gbc1);
		body.add(new JLabel(ra.getState().getDisplayName()), gbc2);
		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("Open"), gbc1);

		body.add(new JLabel(DateUtil.getRelativeBuildTime(ra.getCreateDate())), gbc2);

		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("Author"), gbc1);
		body.add(new JLabel(ra.getCreator().getDisplayName()), gbc2);

		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("Moderator"), gbc1);
		body.add(new JLabel(ra.getModerator().getDisplayName()), gbc2);

		gbc1.gridy++;
		gbc2.gridy++;
		body.add(new BoldLabel("Reviewers"), gbc1);

		JPanel reviewers = new JPanel();
		VerticalFlowLayout layout = new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false);
		layout.setHgap(0);
		layout.setVgap(0);
		Container container = new Container();
		layout.layoutContainer(container);

		reviewers.setLayout(layout);

		Icon reviewCompletedIcon = IconLoader.getIcon("/icons/icn_complete.gif");
		try {
			for (Reviewer reviewer : ra.getReviewers()) {
				JLabel label = new JLabel(reviewer.getDisplayName(),
						reviewer.isCompleted() ? reviewCompletedIcon : null,
						SwingConstants.LEFT);
				label.setOpaque(true);
				label.setBackground(Color.WHITE);
				label.setHorizontalTextPosition(SwingUtilities.LEFT);
				label.setHorizontalAlignment(SwingUtilities.LEFT);
				reviewers.add(label);
			}

			body.add(reviewers, gbc2);
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			//do not care
		}

		gbc1.gridy++;
		gbc1.weighty = 1.0;
		gbc1.fill = GridBagConstraints.VERTICAL;
		JPanel filler = new JPanel();
		filler.setOpaque(true);
		filler.setBackground(Color.WHITE);
		body.add(filler, gbc1);

		return body;
	}

	public static void main(String[] args) {
		ServerData cruc = new ServerData("my crucible server", new ServerIdImpl(), "", "", "");
		ReviewBean review = new ReviewBean("myreviewbean");
		ReviewAdapter reviewAdapter = new ReviewAdapter(review, cruc);
		review.setDescription("My description dfjlslj ldfsjalkfsdjlkj sld"
				+ "jldfjal jfdlkjafl jldfsjalfj ldsj fldjsf; ljWojciech Seliga fjdsalkfjs df\nA new line above\nand then some"
				+ "very very very long lllllllllllllloooong string.");
		review.setName("My review name");
		review.setState(State.REVIEW);
		review.setCreateDate(new Date());
		final ReviewerBean author = new ReviewerBean();
		author.setUserName("wseliga");
		author.setDisplayName("Wojciech Seliga");
		review.setAuthor(author);
		review.setCreator(author);
		review.setModerator(author);

		SwingAppRunner.run(new DetailsPanel(reviewAdapter));
	}

	public void createdOrEditedVersionedCommentReply(final ReviewAdapter review, final PermId file,
			final VersionedComment parentComment,
			final
			VersionedComment comment) {
	}

	public void createdOrEditedGeneralCommentReply(final ReviewAdapter review, final GeneralComment parentComment,
			final GeneralComment comment) {
	}

	public void createdOrEditedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void createdOrEditedVersionedComment(final ReviewAdapter review, final PermId file, final VersionedComment comment) {
	}

	public void removedComment(final ReviewAdapter review, final Comment comment) {
	}

	public void publishedGeneralComment(final ReviewAdapter review, final GeneralComment comment) {
	}

	public void publishedVersionedComment(final ReviewAdapter review, final PermId filePermId, final VersionedComment comment) {
	}

	public void refresh() {
		final JPanel panel = createBody();
		//scroll.setViewport(null);
		scroll.setViewportView(panel);
		scroll.validate();
		validate();
	}
}
