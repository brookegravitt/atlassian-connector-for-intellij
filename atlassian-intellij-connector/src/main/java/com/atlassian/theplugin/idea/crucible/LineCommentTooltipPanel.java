package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.action.crucible.comment.RemoveCommentConfirmation;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentPanel;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.ui.ShowHideButton;
import com.atlassian.theplugin.idea.ui.WhiteLabel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.HyperlinkLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Mar 5, 2009
 * Time: 10:51:23 AM
 */
public abstract class LineCommentTooltipPanel extends JPanel {
	private final ReviewAdapter review;
	private MyReviewListener listener;
	private final Project project;
	private final CrucibleFileInfo fileInfo;
	private final VersionedComment thisLineComment;
	private final boolean useTextTwixie;

	private ScrollablePanel commentsPanel = new ScrollablePanel();
	private JScrollPane scroll = new JScrollPane();
	private JLabel statusLabel = new JLabel(" ");

	private List<CommentPanel> commentPanelList = new ArrayList<CommentPanel>();
	private static final int PANEL_WIDTH = 700;
	private static final int PANEL_HEIGHT = 300;
	private JBPopup popup;
	private AnActionEvent anActionEvent;

	public static void showCommentTooltipPopup(AnActionEvent anActionEvent, LineCommentTooltipPanel lctp) {
		JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(lctp, lctp)
				.setRequestFocus(true)
				.setCancelOnClickOutside(true)
				.setCancelOnOtherWindowOpen(false)
				.setMovable(true)
				.setTitle("Comment")
				.setResizable(true)
				.setCancelKeyEnabled(true)
				.createPopup();
		lctp.setParentPopup(popup);
		lctp.setEvent(anActionEvent);
		popup.showInBestPositionFor(anActionEvent.getDataContext());
	}

	private void setEvent(AnActionEvent e) {
		this.anActionEvent = e;
	}

	public LineCommentTooltipPanel(Project project, ReviewAdapter review,
								   CrucibleFileInfo file, VersionedComment thisLineComment) {
		this(project, review, file, thisLineComment, false);
	}

	public LineCommentTooltipPanel(Project project, final ReviewAdapter review, CrucibleFileInfo file,
								   VersionedComment thisLineComment, boolean useTextTwixie) {
		super(new BorderLayout());
		this.project = project;
		this.fileInfo = file;
		this.thisLineComment = thisLineComment;
		this.useTextTwixie = useTextTwixie;
		listener = new MyReviewListener();
		this.review = review;

		commentsPanel.setLayout(new VerticalFlowLayout());
		commentsPanel.setOpaque(true);
		commentsPanel.setBackground(Color.WHITE);
		scroll.setViewportView(commentsPanel);
		scroll.getViewport().setOpaque(true);
		scroll.getViewport().setBackground(Color.WHITE);
		scroll.setOpaque(true);
		scroll.setBackground(Color.WHITE);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());

		add(scroll, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		CommentPanel cmtPanel = new CommentPanel(thisLineComment);
		commentsPanel.add(cmtPanel);
		commentPanelList.add(cmtPanel);
		java.util.List<VersionedComment> replies = thisLineComment.getReplies();
		if (replies != null) {
			for (VersionedComment reply : replies) {
				CommentPanel replyPanel = new CommentPanel(reply);
				commentsPanel.add(replyPanel);
				commentPanelList.add(replyPanel);
			}
		}
		review.addReviewListener(listener);
		addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				review.removeReviewListener(listener);
			}
		});
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
	}

	private void addCommentReplyPanel(VersionedComment reply) {
		CommentPanel cmt = new CommentPanel(reply);
		commentsPanel.add(cmt);
		commentPanelList.add(cmt);
		validate();
		scrollDown();
	}

	private void removeCommentReplyPanel(CommentPanel panel) {
		commentsPanel.remove(panel);
		commentPanelList.remove(panel);
		validate();
	}

	protected ReviewAdapter getReview() {
		return review;
	}

	private void scrollDown() {
		scroll.getVerticalScrollBar().setValue(ScrollablePanel.A_LOT);
	}

	public void setParentPopup(JBPopup pop) {
		this.popup = pop;
	}

	private final class CommentPanel extends JPanel {
		private ShowHideButton btnShowHide;
		private static final int REPLY_PADDING = Constants.DIALOG_MARGIN * 2;

		private String lastCommentBody;
		private static final String EDIT = "Edit";
		private static final String APPLY = "Post";
		private static final String APPLY_DRAFT = "Post as Draft";
		private static final String PUBLISH = "Publish";
		private static final String DELETE = "Remove";

		private VersionedComment comment;
		private HyperlinkLabel btnEdit;
		private HyperlinkLabel btnReply;
		private JLabel creationDate;
		private JEditorPane commentBody;
		private HyperlinkLabel btnDelete;
		private HyperlinkLabel btnPublish;
		private HyperlinkLabel btnSaveDraft;
		private JLabel draftLabel;
		private JPanel defectClassificationPanel;
		private JCheckBox boxIsDefect;
		private JLabel defectLabel;

		private class HeaderListener extends MouseAdapter {
			public void mouseClicked(MouseEvent e) {
				btnShowHide.click();
			}
		}

		private CommentPanel(final VersionedComment comment) {
			this.comment = comment;
			setOpaque(true);
			setBackground(Color.WHITE);
			int pad = comment == null || comment.isReply() ? REPLY_PADDING : 0;

			setLayout(new GridBagLayout());
			GridBagConstraints gbc;

			commentBody = new JEditorPane();
			btnShowHide = new ShowHideButton(commentBody, this, useTextTwixie);
			HeaderListener headerListener = new HeaderListener();

			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;

			gbc.insets = new Insets(0, pad + Constants.DIALOG_MARGIN / 2, 0, 0);
			add(btnShowHide, gbc);

			if (comment != null) {
				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, 0);
				JLabel user = new JLabel(comment.getAuthor().getDisplayName());
				user.setFont(user.getFont().deriveFont(Font.BOLD));
				add(user, gbc);

				final JLabel hyphen = new WhiteLabel();
				hyphen.setText("-");
				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, Constants.DIALOG_MARGIN / 2);
				add(hyphen, gbc);

				creationDate = new WhiteLabel();
				creationDate.setForeground(Color.GRAY);
				creationDate.setFont(creationDate.getFont().deriveFont(Font.ITALIC));

				setCommentDate();
				gbc.gridx++;
				gbc.insets = new Insets(0, 0, 0, 0);
				add(creationDate, gbc);

				if (comment.isDraft()) {
					draftLabel = new WhiteLabel();
					draftLabel.setForeground(Color.GRAY);
					draftLabel.setText("(Draft)");
					draftLabel.setFont(draftLabel.getFont().deriveFont(Font.ITALIC));
					gbc.gridx++;
					gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, 0);
					add(draftLabel, gbc);

				}

				defectLabel = new WhiteLabel();
				defectLabel.setForeground(Color.RED);
				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, 0);
				add(defectLabel, gbc);
				updateDefectField();
			} else {
				gbc.gridx++;
				gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, 0);
				JLabel underContruction = new JLabel("Comment under construction");
				underContruction.setFont(underContruction.getFont().deriveFont(Font.ITALIC));
				add(underContruction, gbc);
			}

			// filler
			gbc.gridx++;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			JPanel filler = new JPanel();
			filler.setBackground(Color.WHITE);
			filler.setOpaque(true);
			gbc.insets = new Insets(0, 0, 0, 0);
			add(filler, gbc);

			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			if (comment != null && !comment.isReply()) {
				gbc.gridx++;
				btnReply = new HyperlinkLabel("Reply");
				btnReply.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						setButtonsVisible(false);
						if (btnEdit != null) {
							btnEdit.setVisible(false);
						}
						addCommentReplyPanel(null);
					}
				});
				btnReply.setOpaque(false);
				add(btnReply, gbc);
			}
			if (comment == null || isOwner(comment)) {
				createCommentEditButtons(gbc);
			}
			if (comment != null && isOwner(comment)) {
				if (comment.isDraft()) {
					createPublishButtons(gbc);
				}
				createDeleteButton(gbc);
			}

			gbc.insets = new Insets(0, pad, 0, 0);

			createCommentBody(gbc);

			if (comment != null && !comment.isReply()) {
				createDefectClassificationPanel(gbc);
			}

			addMouseListener(headerListener);
		}

		private void updateDefectField() {
			defectLabel.setText("DEFECT: " + ReviewCommentPanel.getRankingString(comment));
			defectLabel.setVisible(comment.isDefectRaised());
		}

		private void createCommentBody(GridBagConstraints gbc) {
			int gridwidth = gbc.gridx + 1;

			commentBody.setOpaque(true);
			commentBody.setContentType("text/plain");
			commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			commentBody.setText(comment != null ? comment.getMessage() : "");
			commentBody.setRequestFocusEnabled(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setCommentBodyEditable(CommentPanel.this, comment == null);
				}
			});
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = gridwidth;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			add(commentBody, gbc);
		}

		private void createDefectClassificationPanel(GridBagConstraints gbc) {
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.LINE_START;
			boxIsDefect = new JCheckBox("Mark as Defect");
			boxIsDefect.setSelected(comment.isDefectRaised());

			defectClassificationPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridx = 0;
			gbc2.gridy = 0;
			defectClassificationPanel.add(boxIsDefect, gbc2);
			gbc2.gridx++;
			gbc2.weightx = 1.0;
			gbc2.fill = GridBagConstraints.HORIZONTAL;
			JPanel filler = new JPanel();
			filler.setOpaque(false);
			defectClassificationPanel.add(filler, gbc2);
			gbc2.gridx++;
			gbc2.weightx = 0.0;
			gbc2.fill = GridBagConstraints.NONE;

			JPanel combosPanel = new JPanel(new FlowLayout());
			combosPanel.setOpaque(false);
			List<CustomFieldDef> metrics = CrucibleHelper.getMetricsForReview(project, review);
			final CrucibleReviewMetricsCombos combos = new CrucibleReviewMetricsCombos(
					review, (CommentBean) comment, metrics, combosPanel);
			combos.showMetricCombos(comment.isDefectRaised());

			defectClassificationPanel.setOpaque(false);
			defectClassificationPanel.add(combosPanel, gbc2);
			add(defectClassificationPanel, gbc);

			boxIsDefect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					combos.showMetricCombos(boxIsDefect.isSelected());
				}
			});

			defectClassificationPanel.setVisible(false);
		}

		private boolean isOwner(VersionedComment cmt) {
			return review.getServer().getUsername().equals(cmt.getAuthor().getUserName());
		}

		private void createPublishButtons(GridBagConstraints gbc) {
			gbc.gridx++;
			btnPublish = new HyperlinkLabel(PUBLISH);
			btnPublish.setOpaque(false);
			btnPublish.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					publishComment(comment);
				}
			});
			add(btnPublish, gbc);
		}

		private void createDeleteButton(GridBagConstraints gbc) {
			gbc.gridx++;
			btnDelete = new HyperlinkLabel(DELETE);
			btnDelete.setOpaque(false);
			btnDelete.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					popup.cancel();
					final boolean agreed = RemoveCommentConfirmation.userAgreed(null);
					showCommentTooltipPopup(anActionEvent, LineCommentTooltipPanel.this);
					if (agreed) {
						removeComment(comment);
					}
				}
			});
			add(btnDelete, gbc);
		}

		private void createCommentEditButtons(@NotNull GridBagConstraints gbc) {
			gbc.gridx++;
			btnEdit = new HyperlinkLabel(comment != null ? EDIT : APPLY);
			final HyperlinkLabel btnCancel = new HyperlinkLabel("Cancel");
			btnEdit.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (commentBody.isEditable()) {
						if (!validateText(commentBody.getText())) {
							return;
						}
						commentBody.setBackground(Color.GRAY);
						commentBody.setEnabled(false);
						btnEdit.setVisible(false);
						if (comment != null) {
							((VersionedCommentBean) comment).setDraft(false);
						}
						addOrUpdateCommentForReview(CommentPanel.this, comment, commentBody.getText(),
								comment != null && comment.isDraft(), boxIsDefect != null && boxIsDefect.isSelected());
						btnEdit.setHyperlinkText(EDIT);
						if (btnSaveDraft != null) {
							btnSaveDraft.setVisible(false);
						}
					} else {
						setStatusText(" ");
						setButtonsVisible(false);
						btnEdit.setHyperlinkText(APPLY);
						if (btnSaveDraft != null && (comment == null || comment.isDraft())) {
							btnSaveDraft.setVisible(true);
						}
						btnShowHide.setState(true);
						lastCommentBody = commentBody.getText();
					}
					btnCancel.setVisible(!commentBody.isEditable());
					setCommentBodyEditable(CommentPanel.this, !commentBody.isEditable());
				}
			});
			btnEdit.setOpaque(false);
			add(btnEdit, gbc);
			gbc.gridx++;
			if (comment == null || comment.isDraft()) {
				btnSaveDraft = new HyperlinkLabel(APPLY_DRAFT);
				btnSaveDraft.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (commentBody.isEditable()) {
							if (!validateText(commentBody.getText())) {
								return;
							}
							commentBody.setBackground(Color.GRAY);
							commentBody.setEnabled(false);
							btnSaveDraft.setVisible(false);
							addOrUpdateCommentForReview(CommentPanel.this, comment, commentBody.getText(), true,
									boxIsDefect != null && boxIsDefect.isSelected());
							btnCancel.setVisible(false);
							setCommentBodyEditable(CommentPanel.this, false);
							btnEdit.setHyperlinkText(EDIT);
						}
					}
				});
				btnSaveDraft.setOpaque(false);
				add(btnSaveDraft, gbc);
				btnSaveDraft.setVisible(comment == null);
			}
			gbc.gridx++;
			btnCancel.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					btnCancel.setVisible(false);
					btnEdit.setHyperlinkText(EDIT);

					if (btnSaveDraft != null) {
						btnSaveDraft.setVisible(false);
					}
					setButtonsVisible(true);

					if (lastCommentBody != null) {
						commentBody.setText(lastCommentBody);
					}
					if (comment != null) {
						setCommentBodyEditable(CommentPanel.this, false);
					} else {
						removeCommentReplyPanel(CommentPanel.this);
					}
				}
			});
			btnCancel.setOpaque(false);
			btnCancel.setVisible(comment == null);
			add(btnCancel, gbc);
		}

		public VersionedComment getComment() {
			return comment;
		}

		public void setComment(VersionedComment cmt) {
			this.comment = cmt;
			setCommentText();
			setCommentDate();
			updateDefectField();
			
			commentBody.setBackground(Color.WHITE);
			commentBody.setEnabled(true);
			btnEdit.setEnabled(true);

			if (draftLabel != null) {
				draftLabel.setVisible(comment.isDraft());
			}
			if (btnPublish != null) {
				btnPublish.setVisible(comment.isDraft());
			}
		}

		public HyperlinkLabel getBtnEdit() {
			return btnEdit;
		}

		public HyperlinkLabel getBtnReply() {
			return btnReply;
		}

		public HyperlinkLabel getBtnDelete() {
			return btnDelete;
		}

		public HyperlinkLabel getBtnPublish() {
			return btnPublish;
		}

		public HyperlinkLabel getBtnSaveDraft() {
			return btnSaveDraft;
		}

		public ShowHideButton getShowHideButton() {
			return btnShowHide;
		}

		private void setCommentText() {
			commentBody.setText(comment.getMessage());
		}

		private void setButtonsVisible(boolean visible) {
			for (CommentPanel panel : commentPanelList) {
				if (panel != this || visible) {
					JComponent edit = panel.getBtnEdit();
					if (edit != null) {
						edit.setVisible(visible);
					}
				}
				JComponent reply = panel.getBtnReply();
				if (reply != null) {
					reply.setVisible(visible);
				}
				JComponent delete = panel.getBtnDelete();
				if (delete != null) {
					delete.setVisible(visible);
				}
				JComponent publish = panel.getBtnPublish();
				if (publish != null) {
					publish.setVisible(visible && panel.comment.isDraft());
				}
			}
		}

		private void setCommentDate() {
			creationDate.setText(CommentDateUtil.getDateText(comment.getCreateDate()));
		}
	}

	private static void setCommentBodyEditable(LineCommentTooltipPanel.CommentPanel commentPanel, boolean editable) {
		JEditorPane pane = commentPanel.commentBody;
		pane.setEditable(editable);
		if (editable) {
			pane.requestFocusInWindow();
			pane.getCaret().setVisible(editable);
			pane.setCaretPosition(0);
			pane.selectAll();
		}
		pane.setBorder(BorderFactory.createEmptyBorder(0, 2 * Constants.DIALOG_MARGIN, 0, 0));
		pane.setBackground(editable ? CommentHighlighter.VERSIONED_COMMENT_BACKGROUND_COLOR : Color.WHITE);

		if (commentPanel.defectClassificationPanel != null) {
			commentPanel.defectClassificationPanel.setVisible(editable);
			commentPanel.defectClassificationPanel.validate();
		}
	}

	public void setAllButtonsVisible() {
		commentPanelList.get(0).setButtonsVisible(true);
	}

	public void setStatusText(String txt) {
		statusLabel.setText(txt);
	}

	/**
	 *
	 * @param panel - panel that this invocation came from
	 * @param comment - null to create new reply
	 * @param text - new comment body
	 * @param draft - is the comment a draft?
	 */
	private void addOrUpdateCommentForReview(CommentPanel panel, VersionedComment comment,
											 String text, boolean draft, boolean defect) {

		if (comment == null) {
			removeCommentReplyPanel(panel);
			setStatusText("Adding new reply...");
			addNewReply(thisLineComment, text, draft);
		} else {
			setStatusText("Updating comment...");
			((CommentBean) comment).setDefectRaised(defect);
			updateComment(comment, text);
		}
	}

	private static boolean validateText(String text) {
		return !(text == null || text.length() == 0);
	}

	protected abstract void addNewReply(VersionedComment parent, String text, boolean draft);
	protected abstract void updateComment(VersionedComment comment, String text);
	protected abstract void removeComment(VersionedComment comment);
	protected abstract void publishComment(VersionedComment comment);

	private class MyReviewListener extends CrucibleReviewListenerAdapter {

		public void createdOrEditedVersionedCommentReply(final ReviewAdapter rev,
														 final PermId file,
														 final VersionedComment parentComment,
														 final VersionedComment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!file.equals(fileInfo.getPermId())) {
						return;
					}

					if (!isTheSameComment(thisLineComment, parentComment)) {
						return;
					}

					for (CommentPanel panel : commentPanelList) {
						VersionedComment reply = panel.getComment();
						if (isTheSameComment(reply, comment)) {
							setStatusText("Comment reply updated");
							setAllButtonsVisible();
							panel.setComment(comment);
							return;
						}
					}
					setStatusText("Comment reply added");
					setAllButtonsVisible();
					addCommentReplyPanel(comment);
				}
			});
		}

		public void createdOrEditedVersionedComment(final ReviewAdapter rev, final PermId file,
													final VersionedComment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!file.equals(fileInfo.getPermId())) {
						return;
					}

					if (!comment.isReply()) {
						if (!isTheSameComment(thisLineComment, comment)) {
							return;
						}

						CommentPanel panel = commentPanelList.get(0);
						setStatusText("Comment updated");
						setAllButtonsVisible();
						panel.setComment(comment);
					} else {
						createdOrEditedVersionedCommentReply(rev, file, thisLineComment, comment);
					}
				}
			});
		}

		public void removedComment(final ReviewAdapter rev, final Comment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!(comment instanceof VersionedComment)) {
						return;
					}

					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!comment.isReply()) {
						if (isTheSameComment(thisLineComment, (VersionedComment) comment)) {
							popup.cancel();
						}
						return;
					}
					for (CommentPanel panel : commentPanelList) {
						VersionedComment reply = panel.getComment();
						if (isTheSameComment(reply, (VersionedComment) comment)) {
							setStatusText("Comment reply removed");
							removeCommentReplyPanel(panel);
							return;
						}
					}
				}
			});
		}

		public void publishedVersionedComment(final ReviewAdapter rev,
											  final PermId file, final VersionedComment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!file.equals(fileInfo.getPermId())) {
						return;
					}

					for (CommentPanel panel : commentPanelList) {
						VersionedComment cmt = panel.getComment();
						if (isTheSameComment(cmt, comment)) {
							setStatusText("Comment published");
							((VersionedCommentBean) cmt).setDraft(false);
							panel.setComment(cmt);
							return;
						}
					}

				}
			});
		}

		private boolean isTheSameComment(VersionedComment lhs, VersionedComment rhs) {
			if (lhs == null && rhs == null) {
				return true;
			}
			if (lhs == null || rhs == null) {
				return false;
			}
			PermId lhsId = lhs.getPermId();
			PermId rhsId = rhs.getPermId();

			return lhsId == null && rhsId == null || !(lhsId == null || rhsId == null) && lhsId.equals(rhsId);
		}
	}
}
