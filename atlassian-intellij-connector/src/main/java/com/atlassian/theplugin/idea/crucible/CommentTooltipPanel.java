package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.action.crucible.comment.RemoveCommentConfirmation;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentPanel;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.ui.ShowHideButton;
import com.atlassian.theplugin.idea.ui.WhiteLabel;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.HyperlinkLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
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
public abstract class CommentTooltipPanel extends JPanel {
	private final ReviewAdapter review;
	private MyReviewListener listener;
	private final CrucibleFileInfo fileInfo;
    private Comment parent;
    private Comment rootComment;
	private final boolean useTextTwixie;

	private ScrollablePanel commentsPanel = new ScrollablePanel();
	private JScrollPane scroll = new JScrollPane();
	private JLabel statusLabel = new JLabel(" ");

	private List<CommentPanel> commentPanelList = new ArrayList<CommentPanel>();
	private static final int PANEL_WIDTH = 700;
	private static final int PANEL_HEIGHT = 300;
	private JBPopup popup;
	private AnActionEvent anActionEvent;
    private Comment commentTemplate;
    private Mode mode;

    public enum Mode {
        SHOW,
        EDIT,
        ADD
    }

    public static void showCommentTooltipPopup(AnActionEvent anActionEvent, CommentTooltipPanel lctp) {
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

	public CommentTooltipPanel(ReviewAdapter review, CrucibleFileInfo file, Comment comment, Comment parent) {
		this(review, file, comment, parent, Mode.SHOW);
	}

    public CommentTooltipPanel(ReviewAdapter review, CrucibleFileInfo file,
                               Comment comment, Comment parent, Mode mode) {
        this(review, file, comment, parent, mode, false);
    }

	public CommentTooltipPanel(final ReviewAdapter review, CrucibleFileInfo file,
                               Comment comment, Comment parent, Mode mode, boolean useTextTwixie) {
		super(new BorderLayout());

		this.fileInfo = file;
        this.parent = parent;
        this.mode = mode;

        this.rootComment = parent != null ? parent : (mode != Mode.ADD ? comment : null);
        commentTemplate = mode == Mode.ADD ? comment : null;

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
		JPanel wrap = new JPanel(new FormLayout("p:g", "p"));
		CellConstraints cc = new CellConstraints();
		wrap.add(statusLabel, cc.xy(1, 1));
		add(wrap, BorderLayout.SOUTH);

		final CommentPanel cmtPanel = new CommentPanel(review, rootComment, false,
                parent == null && mode != Mode.SHOW,
                parent == null && mode == Mode.ADD);
        CommentPanel selectedPanel = parent == null && mode != Mode.SHOW ? cmtPanel : null;
		commentsPanel.add(cmtPanel);
		commentPanelList.add(cmtPanel);
		java.util.List<Comment> replies = rootComment != null ? rootComment.getReplies() : null;
		if (replies != null) {
			for (Comment reply : replies) {
                boolean sel = mode != Mode.SHOW && reply.getPermId().equals(comment.getPermId());
				CommentPanel replyPanel = new CommentPanel(review, reply, true, sel, false);
                if (sel) {
                    selectedPanel = replyPanel;
                }
				commentsPanel.add(replyPanel);
				commentPanelList.add(replyPanel);
			}
		}
        if (parent != null && mode == Mode.ADD) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cmtPanel.addEmptyReplyPanelAndSetupButtons(review);
                }
            });
        }

        if (selectedPanel != null) {
            selectedPanel.setButtonsVisible(false);
        }

		review.addReviewListener(listener);
		addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				review.removeReviewListener(listener);
			}
		});
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
	}

    private void addCommentPanel(ReviewAdapter aReview, Comment comment) {
        CommentPanel cmt = new CommentPanel(aReview, comment, false, false, false);
        commentsPanel.add(cmt, 0);
        commentPanelList.add(0, cmt);
        rootComment = comment;
        validate();
        scrollUp();
    }

	private void addCommentReplyPanel(ReviewAdapter aReview, Comment reply, boolean panelForNewComment) {
		CommentPanel cmt = new CommentPanel(aReview, reply, true, reply == null, panelForNewComment);
		commentsPanel.add(cmt);
		commentPanelList.add(cmt);
		validate();
		scrollDown();
	}

	private void removeCommentPanel(CommentPanel panel) {
		commentsPanel.remove(panel);
		commentPanelList.remove(panel);
		validate();
	}

	protected ReviewAdapter getReview() {
		return review;
	}

    public CrucibleFileInfo getFileInfo() {
        return fileInfo;
    }

    private void scrollDown() {
		scroll.getVerticalScrollBar().setValue(ScrollablePanel.A_LOT);
	}

    private void scrollUp() {
		scroll.getVerticalScrollBar().setValue(0);
	}

	public void setParentPopup(JBPopup pop) {
		this.popup = pop;
	}

	public void resumeEditing(final CommentBean comment) {
		CommentPanel panel = null;
		for (CommentPanel commentPanel : commentPanelList) {
            if ((commentPanel.comment.getPermId() == null && comment.getPermId() == null)
                || (commentPanel.comment.getPermId().equals(comment.getPermId()))) {
				panel = commentPanel;
				break;
			}
		}
		if (panel != null) {
			setCommentEditable(comment, panel, true);
		}
	}

	public void resumeAdding(final CommentBean comment) {
		CommentPanel panel = null;
		for (CommentPanel commentPanel : commentPanelList) {
			if (commentPanel.comment == null || commentPanel.comment == commentTemplate) {
				panel = commentPanel;
				break;
			}
		}
		if (panel != null) {
			setCommentEditable(comment, panel, true);
		}
	}

	private final class CommentPanel extends JPanel {
		private ShowHideButton btnShowHide;

		private String lastCommentBody;
		private static final String EDIT = "Edit";
		private static final String APPLY = "Post";
		private static final String APPLY_DRAFT = "Post as Draft";
		private static final String PUBLISH = "Publish";
		private static final String DELETE = "Remove";

		private Comment comment;
        private boolean selectedPanel;
        private ReviewAdapter review;
        private boolean panelForNewComment;

        private HyperlinkLabel btnEdit;
		private HyperlinkLabel btnCancel;
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

		private static final int TWIXIE_1_POS = 1;
		private static final int TWIXIE_2_POS = 2;
		private static final int USER_POS = 3;
		private static final int HYPHEN_POS = 5;
		private static final int DATE_POS = 7;
		private static final int DRAFT_POS = 9;
		private static final int DEFECT_POS = 11;
		private static final int REPLY_POS = 12;
		private static final int EDIT_POS = 13;
		private static final int SAVE_DRAFT_POS = 14;
		private static final int CANCEL_POS = 15;
		private static final int PUBLISH_POS = 16;
		private static final int DELETE_POS = 17;
		private static final int WIDTH_INDENTED = 15;
		private static final int WIDTH_ALL = 16;
        private JLabel underConstruction;
        private boolean indent;

        private class HeaderListener extends MouseAdapter {
			public void mouseClicked(MouseEvent e) {
				btnShowHide.click();
			}
		}

		private CommentPanel(final ReviewAdapter review, final Comment comment,
                             boolean replyPanel, boolean selectedPanel, boolean panelForNewComment) {
			this.review = review;
            this.panelForNewComment = panelForNewComment;
            this.comment = comment != null ? comment : (replyPanel ? null : commentTemplate);
            this.selectedPanel = selectedPanel;
            setOpaque(true);
			setBackground(Color.WHITE);
            indent = comment == null || comment.isReply();

			setLayout(new FormLayout(
					"max(8dlu;p), max(8dlu;d), d, 2dlu, d, 2dlu, d, 2dlu, d, 2dlu, d, r:p:g, r:p, r:p, r:p, r:p, r:p",
					"p, p:g, p"
			));
			CellConstraints cc = new CellConstraints();

			commentBody = new JEditorPane();
			btnShowHide = new ShowHideButton(commentBody, this, useTextTwixie);
			HeaderListener headerListener = new HeaderListener();

			add(btnShowHide, cc.xy(indent ? TWIXIE_2_POS : TWIXIE_1_POS, 1));

			if (comment != null) {
                createCommentInfoComponents(comment);
			} else {
                underConstruction = new JLabel("Comment under construction");
				underConstruction.setFont(underConstruction.getFont().deriveFont(Font.ITALIC));
				add(underConstruction, cc.xy(3, 1));
			}

			if (comment != null && !comment.isReply()) {
				btnReply = new HyperlinkLabel("Reply");
				btnReply.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
                        addEmptyReplyPanelAndSetupButtons(review);
					}
				});
				btnReply.setOpaque(false);
				add(btnReply, cc.xy(REPLY_POS, 1));
			}
			if (comment == null || isOwner(comment)) {
				createCommentEditButtons(cc);
			}
			if (comment != null && isOwner(comment)) {
				if (comment.isDraft()) {
					createPublishButtons(cc);
				}
				createDeleteButton(cc);
			}

			createCommentBody(cc);

			if (this.comment != null && !this.comment.isReply()) {
				createDefectClassificationPanel(cc);
			}

			addMouseListener(headerListener);
			validate();
		}

        private void createCommentInfoComponents(Comment cmt) {
            CellConstraints cc = new CellConstraints();
            JLabel user = new JLabel(cmt.getAuthor().getDisplayName());
            user.setFont(user.getFont().deriveFont(Font.BOLD));
            user.setMinimumSize(new Dimension(0, 0));
            add(user, cc.xy(indent ? USER_POS : TWIXIE_2_POS, 1));

            final JLabel hyphen = new WhiteLabel();
            hyphen.setText("-");
            add(hyphen, cc.xy(HYPHEN_POS, 1));

            creationDate = new WhiteLabel();
            creationDate.setForeground(Color.GRAY);
            creationDate.setFont(creationDate.getFont().deriveFont(Font.ITALIC));
            creationDate.setMinimumSize(new Dimension(0, 0));

            setCommentDate();
            add(creationDate, cc.xy(DATE_POS, 1));

            if (cmt.isDraft()) {
                draftLabel = new WhiteLabel();
                draftLabel.setForeground(Color.GRAY);
                draftLabel.setText("(Draft)");
                draftLabel.setFont(draftLabel.getFont().deriveFont(Font.ITALIC));
                draftLabel.setMinimumSize(new Dimension(0, 0));
                add(draftLabel, cc.xy(DRAFT_POS, 1));
            }

            defectLabel = new WhiteLabel();
            defectLabel.setForeground(Color.RED);
            defectLabel.setMinimumSize(new Dimension(0, 0));
            add(defectLabel, cc.xy(DEFECT_POS, 1));
            updateDefectField();
        }

        private void addEmptyReplyPanelAndSetupButtons(ReviewAdapter rev) {
            setButtonsVisible(false);
            if (btnEdit != null) {
                btnEdit.setVisible(false);
            }
            addCommentReplyPanel(rev, null, true);
            setStatusText(" ", false);
        }

        private void updateDefectField() {
			defectLabel.setText("DEFECT: " + ReviewCommentPanel.getRankingString(review, comment));
			defectLabel.setVisible(comment.isDefectRaised());
		}

		private void createCommentBody(CellConstraints cc) {
			commentBody.setOpaque(true);
			commentBody.setContentType("text/plain");
			commentBody.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

            // PL-1407 - without setting a decent editor kit, lines wrap within word boundaries, which looks bad
            commentBody.setEditorKit(new StyledEditorKit());

			commentBody.setText(comment != null ? comment.getMessage() : "");
			commentBody.setRequestFocusEnabled(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setCommentBodyEditable(CommentPanel.this,
                            comment == null || (selectedPanel && mode != Mode.SHOW));
				}
			});
			add(commentBody, cc.xyw(indent ? USER_POS : TWIXIE_2_POS, TWIXIE_2_POS, indent ? WIDTH_INDENTED : WIDTH_ALL));
		}

		private void createDefectClassificationPanel(CellConstraints cc) {
			boxIsDefect = new JCheckBox("Mark as Defect");
            boxIsDefect.setOpaque(false);
			boxIsDefect.setSelected(comment.isDefectRaised());

			defectClassificationPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			defectClassificationPanel.add(boxIsDefect, gbc);
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			JPanel filler = new JPanel();
			filler.setOpaque(false);
			defectClassificationPanel.add(filler, gbc);
			gbc.gridx++;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;

			JPanel combosPanel = new JPanel(new FlowLayout());
			combosPanel.setOpaque(false);
			List<CustomFieldDef> metrics = review.getMetricDefinitions();
			final CrucibleReviewMetricsCombos combos = new CrucibleReviewMetricsCombos(
					comment.getCustomFields(), metrics, combosPanel);
			combos.showMetricCombos(comment.isDefectRaised());

			defectClassificationPanel.setOpaque(false);
			defectClassificationPanel.add(combosPanel, gbc);

			add(defectClassificationPanel, cc.xyw(TWIXIE_2_POS, 2 + 1, WIDTH_ALL));

			boxIsDefect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					combos.showMetricCombos(boxIsDefect.isSelected());
				}
			});

			defectClassificationPanel.setVisible(false);
		}

		private boolean isOwner(Comment cmt) {
			return review.getServerData().getUserName().equals(cmt.getAuthor().getUserName());
		}

		private void createPublishButtons(CellConstraints cc) {
			btnPublish = new HyperlinkLabel(PUBLISH);
			btnPublish.setOpaque(false);
			btnPublish.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					publishComment(comment);
				}
			});
			add(btnPublish, cc.xy(PUBLISH_POS, 1));
		}

		private void createDeleteButton(CellConstraints cc) {
			btnDelete = new HyperlinkLabel(DELETE);
			btnDelete.setOpaque(false);
			btnDelete.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					popup.cancel();
					final boolean agreed = RemoveCommentConfirmation.userAgreed(null);
					showCommentTooltipPopup(anActionEvent, CommentTooltipPanel.this);
					if (agreed) {
						removeComment(comment);
					}
				}
			});
			add(btnDelete, cc.xy(DELETE_POS, 1));
		}

		private void createCommentEditButtons(@NotNull CellConstraints cc) {
			btnEdit = new HyperlinkLabel(comment != null && !selectedPanel ? EDIT : APPLY);
			btnCancel = new HyperlinkLabel("Cancel");
			btnEdit.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (commentBody.isEditable()) {
						if (!validateText(commentBody.getText())) {
							return;
						}
						commentBody.setBackground(Color.GRAY);
						commentBody.setEnabled(false);
						btnEdit.setVisible(false);
                        btnCancel.setVisible(false);
						if (comment != null) {
							((CommentBean) comment).setDraft(false);
						}
						addOrUpdateCommentForReview(CommentPanel.this, comment, commentBody.getText(),
								comment != null && comment.isDraft(),
                                boxIsDefect != null && boxIsDefect.isSelected());
						btnEdit.setHyperlinkText(EDIT);
						if (btnSaveDraft != null) {
							btnSaveDraft.setVisible(false);
						}
					} else {
						setStatusText(" ", false);
						setButtonsVisible(false);
						btnEdit.setHyperlinkText(APPLY);
                        btnCancel.setVisible(true);
						if (btnSaveDraft != null && (panelForNewComment || comment.isDraft())) {
							btnSaveDraft.setVisible(true);
						}
						btnShowHide.setState(true);
						lastCommentBody = commentBody.getText();
					}
					setCommentBodyEditable(CommentPanel.this, !commentBody.isEditable());
				}
			});
			btnEdit.setOpaque(false);
			add(btnEdit, cc.xy(EDIT_POS, 1));
			if (panelForNewComment || comment.isDraft()) {
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
				add(btnSaveDraft, cc.xy(SAVE_DRAFT_POS, 1));
				btnSaveDraft.setVisible(comment == null || comment == commentTemplate);
			}
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
					if (!panelForNewComment) {
						setCommentBodyEditable(CommentPanel.this, false);
					} else {
						removeCommentPanel(CommentPanel.this);
					}
					setStatusText(" ", false);
				}
			});
			btnCancel.setOpaque(false);
			btnCancel.setVisible(panelForNewComment || selectedPanel);
			add(btnCancel, cc.xy(CANCEL_POS, 1));
		}

		public Comment getComment() {
			return comment;
		}

		public void setComment(Comment cmt) {
            if (cmt != null) {
                if (underConstruction != null) {
                    remove(underConstruction);
                    createCommentInfoComponents(cmt);
                }
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

	private void setCommentBodyEditable(CommentTooltipPanel.CommentPanel commentPanel, boolean editable) {
		JEditorPane pane = commentPanel.commentBody;
		pane.setEditable(editable);
		if (editable) {
			pane.requestFocusInWindow();
			pane.getCaret().setVisible(editable);
			pane.setCaretPosition(0);
			pane.selectAll();
		}
		pane.setBackground(editable ? CommentHighlighter.VERSIONED_COMMENT_BACKGROUND_COLOR : Color.WHITE);

		if (commentPanel.defectClassificationPanel != null) {
			commentPanel.defectClassificationPanel.setVisible(editable);
			commentPanel.defectClassificationPanel.validate();
		}
	}

	private void setCommentEditable(final CommentBean comment, CommentTooltipPanel.CommentPanel commentPanel,
			boolean editable) {
		commentPanel.commentBody.setEnabled(editable);
		commentPanel.btnEdit.setHyperlinkText(CommentPanel.APPLY);
		commentPanel.btnEdit.setVisible(editable);
		commentPanel.btnCancel.setVisible(editable);
		if (commentPanel.btnSaveDraft != null) {
			commentPanel.btnSaveDraft.setVisible(comment.isDraft());
		}
		setCommentBodyEditable(commentPanel, editable);
	}

	public void setAllButtonsVisible() {
        if (commentPanelList.size() > 0) {
		    commentPanelList.get(0).setButtonsVisible(true);
        }
	}

	public void setStatusText(String txt, boolean error) {
        statusLabel.setOpaque(error);
        if (error) {
            statusLabel.setBackground(Constants.FAIL_COLOR);
        }
		statusLabel.setText(txt);
		statusLabel.setPreferredSize(new Dimension(0, statusLabel.getHeight()));
	}

	/**
	 * @param panel   - panel that this invocation came from
	 * @param comment - null to create new reply
	 * @param text	- new comment body
	 * @param draft   - is the comment a draft?
	 * @param defect  - is this comment a defect?
	 */
	private void addOrUpdateCommentForReview(CommentPanel panel, Comment comment,
			String text, boolean draft, boolean defect) {

		if (panel.panelForNewComment) {
			//removeCommentPanel(panel);
			setCommentBodyEditable(panel, false);

            if (parent != null) {
                setStatusText("Adding new reply...", false);
                addNewReply(rootComment, text, draft);
            } else {
                setStatusText("Adding new comment...", false);
                ((CommentBean) commentTemplate).setMessage(text);
                updateDefectFields(commentTemplate, defect);
                addNewComment(commentTemplate, draft);
            }
		} else {
            setStatusText("Updating comment...", false);
            if (parent == null) {
                updateDefectFields(comment, defect);
            }
            updateComment(comment, text);
		}
	}

    private void updateDefectFields(Comment comment, boolean defect) {
        CommentBean bean = (CommentBean) comment;
        bean.setDefectRaised(defect);
        if (!defect) {
            bean.getCustomFields().clear();
        }
    }

    private static boolean validateText(String text) {
		return !(text == null || text.length() == 0);
	}

	protected abstract void addNewComment(Comment comment, boolean draft);

    protected abstract void addNewReply(Comment parentComment, String text, boolean draft);

	protected abstract void updateComment(Comment comment, String text);

	protected abstract void removeComment(Comment comment);

	protected abstract void publishComment(Comment comment);

	private class MyReviewListener extends CrucibleReviewListenerAdapter {

        @Override
        public void createdOrEditedGeneralCommentReply(ReviewAdapter rev, GeneralComment parentComment,
                                                       GeneralComment comment) {
            createdOrEditedCommentReply(rev, null, parentComment, comment);
        }

        public void createdOrEditedVersionedCommentReply(final ReviewAdapter rev, final PermId file,
				final VersionedComment parentComment, final VersionedComment comment) {
            createdOrEditedCommentReply(rev, file, parentComment, comment);
        }

        private void createdOrEditedCommentReply(final ReviewAdapter rev,
            final PermId file,
            final Comment parentComment,
            final Comment comment) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}
                    if (file != null && fileInfo != null) {
					    if (!file.equals(fileInfo.getPermId())) {
						    return;
					    }
                    }
					if (!isTheSameComment(rootComment, parentComment)) {
						return;
					}

					for (CommentPanel panel : commentPanelList) {
						Comment reply = panel.getComment();
						if (isTheSameComment(reply, comment)) {
							setStatusText("Comment reply updated", false);
							setAllButtonsVisible();
							panel.setComment(comment);
							return;
						}
					}
                    removeUnderConstructionPanel(comment);
					setStatusText("Comment reply added", false);
					setAllButtonsVisible();
					addCommentReplyPanel(review, comment, false);
				}
			});
		}

        private void removeUnderConstructionPanel(Comment comment) {
            CommentPanel underConstructionPanel = null;
            for (CommentPanel panel : commentPanelList) {
                if (panel.panelForNewComment) {
                    underConstructionPanel = panel;
                    break;
                }
            }
            if (underConstructionPanel != null) {
                if (underConstructionPanel.commentBody.getText().trim().equals(comment.getMessage().trim())) {
                    removeCommentPanel(underConstructionPanel);
                }
            }
        }

        @Override
        public void createdOrEditedGeneralComment(ReviewAdapter rev, GeneralComment comment) {
            createdOrEditedComment(rev, null, comment);
        }

        public void createdOrEditedVersionedComment(final ReviewAdapter rev, final PermId file,
                                                    final VersionedComment comment) {
            createdOrEditedComment(rev, file, comment);
        }

        private void createdOrEditedComment(final ReviewAdapter rev, final PermId file, final Comment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

                    if (file != null && fileInfo != null) {
					    if (!file.equals(fileInfo.getPermId())) {
						    return;
					    }
                    }

					if (!comment.isReply() && commentPanelList.size() > 0) {
                        if (mode == Mode.ADD) {
                            CommentPanel panel = commentPanelList.get(0);
                            if (comment.getMessage().equals(panel.getComment().getMessage())) {
                                setStatusText("Comment aded", false);
                                parent = comment;
                                removeUnderConstructionPanel(comment);
                                addCommentPanel(rev, comment);
                                setAllButtonsVisible();
                            }
                        } else {
                            if (!isTheSameComment(rootComment, comment)) {
                                return;
                            }

                            CommentPanel panel = commentPanelList.get(0);
                            setStatusText("Comment updated", false);
                            setAllButtonsVisible();
                            panel.setComment(comment);
                        }
					} else {
                        createdOrEditedCommentReply(rev, file, rootComment, comment);
					}
				}
			});
		}

		public void removedComment(final ReviewAdapter rev, final Comment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!comment.isReply()) {
						if (isTheSameComment(rootComment, comment)) {
							popup.cancel();
						}
						return;
					}
					for (CommentPanel panel : commentPanelList) {
						Comment reply = panel.getComment();
						if (isTheSameComment(reply, comment)) {
							setStatusText("Comment reply removed", false);
							removeCommentPanel(panel);
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
						Comment cmt = panel.getComment();
						if (isTheSameComment(cmt, comment)) {
							setStatusText("Comment published", false);
							((VersionedCommentBean) cmt).setDraft(false);
							panel.setComment(cmt);
							return;
						}
					}

				}
			});
		}

		private boolean isTheSameComment(Comment lhs, Comment rhs) {
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
