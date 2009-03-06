package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentPanel;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.ui.ShowHideButton;
import com.atlassian.theplugin.idea.ui.WhiteLabel;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	private final CrucibleFileInfo fileInfo;
	private final VersionedComment thisLineComment;
	private final boolean useTextTwixie;

	private ScrollablePanel commentsPanel = new ScrollablePanel();
	private JScrollPane scroll = new JScrollPane();
	private JLabel statusLabel = new JLabel(" ");

	private List<CommentPanel> commentPanelList = new ArrayList<CommentPanel>();
	private static final int PANEL_WIDTH = 600;
	private static final int PANEL_HEIGHT = 300;

	public LineCommentTooltipPanel(ReviewAdapter review, CrucibleFileInfo file, VersionedComment thisLineComment) {
		this(review, file, thisLineComment, false);
	}

	public LineCommentTooltipPanel(final ReviewAdapter review, CrucibleFileInfo file,
								   VersionedComment thisLineComment, boolean useTextTwixie) {
		super(new BorderLayout());
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

	private final class CommentPanel extends JPanel {
		private ShowHideButton btnShowHide;
		private static final int REPLY_PADDING = Constants.DIALOG_MARGIN * 2;

		private String lastCommentBody;
		private static final String EDIT = "Edit";
		private static final String APPLY = "Apply";
		private VersionedComment comment;
		private HyperlinkLabel btnEdit;
		private HyperlinkLabel btnReply;
		private JLabel creationDate;
		private JEditorPane commentBody;

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

				if (comment.isDefectRaised()) {
					JLabel defect = new WhiteLabel();
					defect.setForeground(Color.RED);
					defect.setText("DEFECT: " + ReviewCommentPanel.getRankingString(comment));
					gbc.gridx++;
					gbc.insets = new Insets(0, Constants.DIALOG_MARGIN / 2, 0, 0);
					add(defect, gbc);
				}
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
			if (comment == null || review.getServer().getUsername().equals(comment.getAuthor().getUserName())) {
				gbc.gridx++;
				btnEdit = new HyperlinkLabel(comment != null ? EDIT : APPLY);
				final HyperlinkLabel btnCancel = new HyperlinkLabel("Cancel");
				btnEdit.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (commentBody.isEditable()) {
							commentBody.setBackground(Color.GRAY);
							commentBody.setEnabled(false);
							btnEdit.setVisible(false);
							addOrUpdateCommentForReview(CommentPanel.this, comment, commentBody.getText());
							btnEdit.setHyperlinkText(EDIT);
						} else {
							setButtonsVisible(false);
							btnEdit.setHyperlinkText(APPLY);
							btnShowHide.setState(true);
							lastCommentBody = commentBody.getText();
						}
						btnCancel.setVisible(!commentBody.isEditable());
						setCommentBodyEditable(commentBody, !commentBody.isEditable());
					}
				});
				btnEdit.setOpaque(false);
				add(btnEdit, gbc);
				gbc.gridx++;
				btnCancel.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						btnCancel.setVisible(false);
						btnEdit.setHyperlinkText(EDIT);

						setButtonsVisible(true);

						if (lastCommentBody != null) {
							commentBody.setText(lastCommentBody);
						}
						if (comment != null) {
							setCommentBodyEditable(commentBody, false);
						} else {
							removeCommentReplyPanel(CommentPanel.this);
						}
					}
				});
				btnCancel.setOpaque(false);
				btnCancel.setVisible(comment == null);
				add(btnCancel, gbc);
			}

			int gridwidth = gbc.gridx + 1;

			commentBody.setOpaque(true);
			commentBody.setContentType("text/plain");
			commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			commentBody.setText(comment != null ? comment.getMessage() : "");
			commentBody.setRequestFocusEnabled(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setCommentBodyEditable(commentBody, comment == null);
				}
			});
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = gridwidth;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(0, pad, 0, 0);
			add(commentBody, gbc);

			addMouseListener(headerListener);
		}

		public VersionedComment getComment() {
			return comment;
		}

		public void setComment(VersionedComment comment) {
			this.comment = comment;
			setCommentText();
			setCommentDate();
			commentBody.setBackground(Color.WHITE);
			commentBody.setEnabled(true);
			btnEdit.setEnabled(true);
		}

		public HyperlinkLabel getBtnEdit() {
			return btnEdit;
		}

		public HyperlinkLabel getBtnReply() {
			return btnReply;
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
			}
		}

		private void setCommentDate() {
			creationDate.setText(CommentDateUtil.getDateText(comment.getCreateDate()));
		}
	}

	private static void setCommentBodyEditable(JEditorPane pane, boolean editable) {
		pane.setEditable(editable);
		if (editable) {
			pane.requestFocusInWindow();
			pane.getCaret().setVisible(editable);
			pane.setCaretPosition(0);
			pane.selectAll();
		}
		pane.setBorder(BorderFactory.createEmptyBorder(0, 2 * Constants.DIALOG_MARGIN, 0, 0));
		pane.setBackground(editable ? CommentHighlighter.VERSIONED_COMMENT_BACKGROUND_COLOR : Color.WHITE);
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
	 */
	private void addOrUpdateCommentForReview(CommentPanel panel, VersionedComment comment, String text) {

		if (comment == null) {
			removeCommentReplyPanel(panel);
			setStatusText("Adding new reply...");
			addNewReply(thisLineComment, text);
		} else {
			setStatusText("Updating comment...");
			updateComment(comment, text);
		}
	}

	protected abstract void addNewReply(VersionedComment parent, String text);
	protected abstract void updateComment(VersionedComment comment, String text);

	private class MyReviewListener extends CrucibleReviewListenerAdapter {
		public void createdOrEditedVersionedCommentReply(final ReviewAdapter rev,
														 final PermId file,
														 final VersionedComment parentComment,
														 final VersionedComment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().getId().equals(review.getPermId().getId())) {
						return;
					}

					if (!file.getId().equals(fileInfo.getPermId().getId())) {
						return;
					}

					if (!parentComment.getPermId().getId().equals(thisLineComment.getPermId().getId())) {
						return;
					}

					for (CommentPanel panel : commentPanelList) {
						VersionedComment reply = panel.getComment();
						if (comment.getPermId().getId().equals(reply.getPermId().getId())) {
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
					if (!rev.getPermId().getId().equals(review.getPermId().getId())) {
						return;
					}

					if (!file.getId().equals(fileInfo.getPermId().getId())) {
						return;
					}

					if (!comment.isReply()) {
						if (!comment.getPermId().getId().equals(thisLineComment.getPermId().getId())) {
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
	}
}
