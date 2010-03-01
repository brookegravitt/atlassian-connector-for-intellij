package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.commons.crucible.api.model.ReviewModelUtil;
import com.atlassian.connector.intellij.crucible.CrucibleReviewListenerAdapter;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.crucible.comment.RemoveCommentConfirmation;
import com.atlassian.theplugin.idea.crucible.ui.ReviewCommentPanel;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.ui.ShowHideButton;
import com.atlassian.theplugin.idea.ui.WhiteLabel;
import com.atlassian.theplugin.util.Htmlizer;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.ui.EmptyIcon;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
    private Comment rootComment;

	private ScrollablePanel commentsPanel = new ScrollablePanel();
	private JScrollPane scroll = new JScrollPane();
	private JLabel statusLabel = new JLabel(" ");

	private List<CommentPanel> commentPanelList = new ArrayList<CommentPanel>();
	private static final int PANEL_WIDTH = 700;
	private static final int PANEL_HEIGHT = 300;
	private JBPopup popup;
    private Comment commentTemplate;
    private Mode mode;
    private Project project;

    public static final String JBPOPUP_PARENT_COMPONENT = "JBPOPUP_PARENT_COMPONENT";

    private Component popupOwner;
    private static final int MAX_HREF_LINK_LENGTH = 40;
    private static final Color EDITABLE_BACKGROUND_COLOR = new Color(0xd0, 0xd0, 0xd0);
    private boolean doneWithThisPanel;
    private Window topFrame;

    public enum Mode {
        SHOW,
        EDIT,
        ADD
    }

    public static void showCommentTooltipPopup(
            final AnActionEvent event, final CommentTooltipPanel lctp, final Component owner) {
        showCommentTooltipPopup(event, null, lctp, owner, null);
    }

    private static void showCommentTooltipPopup(final Project project, final CommentTooltipPanel lctp,
                                               final Component owner, Point location) {
        showCommentTooltipPopup(null, project, lctp, owner, location);
    }

    private static void showCommentTooltipPopup(final AnActionEvent event, final Project project, 
                                               final CommentTooltipPanel lctp,
                                               final Component owner, Point location) {

        if (event == null && project == null) {
            return;
        }
        final Project proj = project != null ? project : IdeaHelper.getCurrentProject(event);
		JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(lctp, lctp)
				.setRequestFocus(true)
				.setCancelOnClickOutside(false)
				.setCancelOnOtherWindowOpen(false)
                .setTitleIcon(new ActiveIcon(new EmptyIcon(1)))
                .setCancelButton(new IconButton("Close", IconLoader.getIcon("/actions/cross.png")))
				.setMovable(true)
				.setTitle("Comment")
				.setResizable(true)
                .setCancelCallback(new Computable<Boolean>() {
                    public Boolean compute() {
                        if (lctp.doneWithThisPanel) {
                            return true;
                        }
                        // PL-1908 - IDEA 9 apparently changes the way toplevel windows behave
                        if (isPopupWindowActive(lctp)) {
                            return true;
                        }
                        Window topIdeaFrame = lctp.getTopFrame();
                        if (topIdeaFrame != null) {
                            return topIdeaFrame.isActive();
                        }
                        return WindowManager.getInstance().getFrame(proj).isActive();
                    }
                })
				.setCancelKeyEnabled(true)
				.createPopup();

		lctp.setParentPopup(popup);
        if (event != null) {
            Component parentComponent = (Component) event.getPresentation().getClientProperty(JBPOPUP_PARENT_COMPONENT);
            lctp.setPopupOwner(parentComponent);
        }
        if (owner != null && location != null) {
            popup.showInScreenCoordinates(owner, location);
        } else if (event != null) {
            // PL-1775 - fixing the problem with not being able to close the popup
            // in diff view turned out to be pretty sickly difficult
            // - getting to the toplevel window of the diff view is _NOT_ trivial
            DataContext dataContext = event.getDataContext();
            Editor editor = DataKeys.EDITOR.getData(dataContext);
            if (editor != null) {
                JComponent c = editor.getComponent();
                lctp.setTopFrame(getTopFrameFor(c));
            } else {
                lctp.setTopFrame(getTopFrameFor(lctp.popupOwner));
            }
            popup.showInBestPositionFor(dataContext);
        }
	}

    private static boolean isPopupWindowActive(CommentTooltipPanel lctp) {
        Component component = lctp;
        while (component  != null && !(component instanceof Window)) {
            component = component.getParent();
        }
        return component != null && ((Window) component).isActive();
    }

    private void setTopFrame(Window topFrame) {
        this.topFrame = topFrame;
    }

    public Window getTopFrame() {
        return topFrame;
    }

    private static Window getTopFrameFor(Component component) {
        if (component == null) {
            return null;
        }
        Component parent = null;
        do {
            component = component.getParent();
            if (component != null && component instanceof Window) {
                parent = component;
                break;
            }
        } while (component != null);
        return (Window) parent;
    }

    public Project getProject() {
        return project;
    }

    public Component getPopupOwner() {
        return popupOwner;
    }

    public void setPopupOwner(Component popupOwner) {
        this.popupOwner = popupOwner;
    }

	public CommentTooltipPanel(AnActionEvent event, ReviewAdapter review, CrucibleFileInfo file,
                               Comment comment, Comment parent) {
		this(event, review, file, comment, parent, Mode.SHOW);
	}

	public CommentTooltipPanel(AnActionEvent event, final ReviewAdapter review, CrucibleFileInfo file,
                               Comment comment, Comment parent, final Mode mode) {
		super(new BorderLayout());

        project = IdeaHelper.getCurrentProject(event);
		this.fileInfo = file;
        this.mode = mode;

		if (mode == Mode.ADD) {
			this.rootComment = parent;
		} else {
			this.rootComment = comment;
		}
        commentTemplate = mode == Mode.ADD ? comment : null;

		listener = new MyReviewListener();
		this.review = review;

        doneWithThisPanel = false;

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

        List<Comment> commentsToMarkRead = new ArrayList<Comment>();

        if (rootComment != null && rootComment.getReadState() == Comment.ReadState.UNREAD) {
            commentsToMarkRead.add(rootComment);
        }

		final CommentPanel cmtPanel = new CommentPanel(review, rootComment, false,
                parent == null && mode != Mode.SHOW,
                parent == null && mode == Mode.ADD, null, mode);
        CommentPanel selectedPanel = parent == null && mode != Mode.SHOW ? cmtPanel : null;
		commentsPanel.add(cmtPanel);
		commentPanelList.add(cmtPanel);

		java.util.LinkedList<Comment> queue = new LinkedList<Comment>();
		if (rootComment != null && mode != Mode.ADD && mode != Mode.EDIT) {
			queue.addAll(rootComment.getReplies());
		}

		while (!queue.isEmpty()) {
			Comment currentComment = queue.removeFirst();
			queue.addAll(0, currentComment.getReplies());

			boolean sel = mode != Mode.SHOW && currentComment.getPermId().equals(comment.getPermId());
			CommentPanel replyPanel = new CommentPanel(review, currentComment, true, sel, false,
					currentComment.getParentComment(), mode);
			if (sel) {
				selectedPanel = replyPanel;
			}

			if (currentComment.getReadState() == Comment.ReadState.UNREAD) {
				commentsToMarkRead.add(currentComment);
			}
			commentsPanel.add(replyPanel);
			commentPanelList.add(replyPanel);
		}

        markCommentsRead(commentsToMarkRead);

        if (parent != null && mode == Mode.ADD) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cmtPanel.addEmptyReplyPanelAndSetupButtons(review, mode);
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

    private void closePopup() {
        doneWithThisPanel = true;
        popup.cancel();
    }

    private void addCommentPanel(ReviewAdapter aReview, Comment comment) {
        CommentPanel cmt = new CommentPanel(aReview, comment, false, false, false, null, Mode.SHOW);
        commentsPanel.add(cmt, 0);
        commentPanelList.add(0, cmt);
        rootComment = comment;
        validate();
        scrollUp();
    }

	private void addCommentReplyPanel(ReviewAdapter aReview, Comment reply, boolean panelForNewComment, Comment parentOfReply,
			final Mode commentMode) {
		CommentPanel cmt = new CommentPanel(aReview, reply, true, reply == null,
				panelForNewComment, parentOfReply, commentMode);

		if (parentOfReply != null) {
			for (CommentPanel panel : commentPanelList) {
				if (panel.getComment() == parentOfReply) {
					int index = 0;
					for (Component component : commentsPanel.getComponents()) {
						if (component == panel) {
							commentsPanel.add(cmt, index + 1);
							validate();
							if (index + 2 >= commentsPanel.getComponentCount()) {
								scrollDown();
							}
							return;
						}
						index++;
					}
				}
			}
			//something went wrong... parent component was not found, fallbacking:
			commentsPanel.add(cmt);
		} else {
			commentsPanel.add(cmt);
		}
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

	public void resumeEditing(final Comment comment) {
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

	public void resumeAdding(final Comment comment) {
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
        private static final String MARK_UNREAD = "Leave Unread";
        private static final String MARK_READ = "Mark Read";

		private Comment comment;
		private Comment parentComment = null;
        private boolean selectedPanel;
        private ReviewAdapter review;
        private boolean replyPanel;
        private boolean panelForNewComment;

        private HyperlinkLabel btnEdit;
		private HyperlinkLabel btnCancel;
		private HyperlinkLabel btnReply;
		private JLabel creationDate;
		private JEditorPane commentBody;
		private HyperlinkLabel btnDelete;
		private HyperlinkLabel btnPublish;
		private HyperlinkLabel btnSaveDraft;
        private HyperlinkLabel btnMarkRead;
        private HyperlinkLabel btnMarkUnread;
		private JLabel draftLabel;
		private JPanel defectClassificationPanel;
		private JCheckBox boxIsDefect;
		private JLabel defectLabel;

//		private static final int TWIXIE_1_POS = 1;
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
        private static final int MARK_READ_POS = 18;
        private static final int MARK_UNREAD_POS = 19;
		private static final int WIDTH_INDENTED = 15;
		private static final int WIDTH_ALL = 16;
        private JLabel underConstruction;
		private int indentation;
        private static final String POST_COMMENT_TOOLTIP_TEXT = "Post Comment (Alt-Enter)";
        private static final int SET_FOCUS_DELAY = 100;

        private class HeaderListener extends MouseAdapter {
			public void mouseClicked(MouseEvent e) {
				btnShowHide.click();
			}
		}

		// pstefaniak: a good candidate to start "The Mighty Series of Cleanups" :)
		private CommentPanel(final ReviewAdapter review, final Comment comment,
                             boolean replyPanel, boolean selectedPanel, boolean panelForNewComment,
							 final Comment parentComment, final Mode mode) {
			this.review = review;
            this.replyPanel = replyPanel;
            this.panelForNewComment = panelForNewComment;
            this.comment = comment != null ? comment : (replyPanel ? null : commentTemplate);
			this.parentComment = parentComment;
            this.selectedPanel = selectedPanel;
            setOpaque(true);
			setBackground(Color.WHITE);

			{
				if (mode == Mode.ADD || mode == Mode.EDIT) {
					indentation = 0;
				} else {
					indentation = -1;
					Comment tmpComment = comment != null ? comment : parentComment;
					while (tmpComment != null) {
						indentation++;
						tmpComment = tmpComment.getParentComment();
					}
					if (panelForNewComment) {
						indentation++;
					}
				}
			}

			String spanStr = Integer.toString(8 * indentation);
			setLayout(new FormLayout(
					"max(" + spanStr + "dlu;p), 8dlu, d, 2dlu, d, 2dlu, d, 2dlu, d, 2dlu, d,"
                            + " r:p:g, r:p, r:p, r:p, r:p, r:p, r:p, r:p",
					"p, p:g, p"
			));
			CellConstraints cc = new CellConstraints();

			commentBody = new JEditorPane();
            commentBody.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        BrowserUtil.launchBrowser(e.getURL().toString());
                    } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        commentBody.setToolTipText(e.getURL().toString());
                    } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        commentBody.setToolTipText(null);
                    }
                }
            });
            if (comment != null && comment.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                commentBody.setFont(commentBody.getFont().deriveFont(Font.BOLD));
            }
			btnShowHide = new ShowHideButton(commentBody, this);
			HeaderListener headerListener = new HeaderListener();

			add(btnShowHide, cc.xy(TWIXIE_2_POS, 1));

			if (comment != null) {
                createCommentInfoComponents(comment);
			} else {
                underConstruction = new JLabel("Comment under construction");
				underConstruction.setFont(underConstruction.getFont().deriveFont(Font.ITALIC));
				add(underConstruction, cc.xy(3, 1));
			}

			if (mode != Mode.EDIT) {
				if (comment != null) {
					btnReply = new HyperlinkLabel("Reply");
					btnReply.addHyperlinkListener(new HyperlinkListener() {
						public void hyperlinkUpdate(HyperlinkEvent e) {
							addEmptyReplyPanelAndSetupButtons(review, mode);
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
			} else {
				createCommentEditButtons(cc);				
			}

            if (comment != null && !isOwner(comment)) {
                createReadUnreadButtons(cc);
            }

			createCommentBody(cc);
			addAltEnterKeyListener();

			if (this.comment != null && !this.comment.isReply()) {
				createDefectClassificationPanel(cc);
			}

			addMouseListener(headerListener);
			validate();
		}

        private void addAltEnterKeyListener() {
			commentBody.addKeyListener(new KeyListener() {
				private int previousKey;

				public void keyTyped(final KeyEvent e) {
				}

				public void keyPressed(final KeyEvent e) {
					if ((e.getKeyCode() == KeyEvent.VK_ENTER && previousKey == KeyEvent.VK_ALT)) {
						postComment();
						setCommentPanelEditable(CommentPanel.this, !commentBody.isEditable());
					}
					previousKey = e.getKeyCode();
				}

				public void keyReleased(final KeyEvent e) {
				}
			});
		}

		private void createCommentInfoComponents(Comment cmt) {
            CellConstraints cc = new CellConstraints();
            JLabel user = new JLabel(cmt.getAuthor().getDisplayName());
            user.setFont(user.getFont().deriveFont(Font.BOLD));
            user.setMinimumSize(new Dimension(0, 0));
            add(user, cc.xy(USER_POS, 1));

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

        private void addEmptyReplyPanelAndSetupButtons(ReviewAdapter rev, final Mode commentMode) {
            setButtonsVisible(false);
            if (btnEdit != null) {
                btnEdit.setVisible(false);
            }
            addCommentReplyPanel(rev, null, true, comment, commentMode);
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

			commentBody.setRequestFocusEnabled(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					boolean editable = mode == Mode.EDIT || panelForNewComment;
					setCommentPanelEditable(CommentPanel.this, editable);

                    if (editable) {
                        Timer t = new Timer(SET_FOCUS_DELAY, new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                commentBody.requestFocusInWindow();
                            }
                        });
                        t.setRepeats(false);
                        t.start();
                    }                    
				}
			});
			add(commentBody, cc.xyw(USER_POS, TWIXIE_2_POS, WIDTH_INDENTED));
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
			return review.getServerData().getUsername().equals(cmt.getAuthor().getUsername());
		}

        public void updateReadUnreadButtonState(Comment cmt) {
            final Comment.ReadState state = cmt.getReadState();
            comment.setReadState(state);
            if (btnMarkUnread != null && btnMarkRead != null
                    && (state == Comment.ReadState.READ || state == Comment.ReadState.LEAVE_UNREAD)) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        btnMarkUnread.setVisible(state == Comment.ReadState.READ);
                        btnMarkRead.setVisible(state == Comment.ReadState.LEAVE_UNREAD);
                        if (state == Comment.ReadState.LEAVE_UNREAD) {
                            commentBody.setFont(commentBody.getFont().deriveFont(Font.BOLD));
                        } else {
                            commentBody.setFont(commentBody.getFont().deriveFont(Font.PLAIN));
                        }
                    }
                });
            }
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

        private void createReadUnreadButtons(CellConstraints cc) {
            final Comment.ReadState state = comment.getReadState();
            btnMarkUnread = new HyperlinkLabel(MARK_UNREAD);
            btnMarkRead = new HyperlinkLabel(MARK_READ);

            btnMarkRead.setOpaque(false);
            btnMarkUnread.setOpaque(false);
            btnMarkRead.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                    btnMarkRead.setVisible(false);
                    List<Comment> list = new ArrayList<Comment>();
                    list.add(comment);
                    markCommentsRead(list);
                }
            });
            btnMarkUnread.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                    btnMarkUnread.setVisible(false);
                    markCommentLeaveUnread(comment);
                }
            });
            add(btnMarkRead, cc.xy(MARK_READ_POS, 1));
            add(btnMarkUnread, cc.xy(MARK_UNREAD_POS, 1));
            btnMarkRead.setVisible(state == Comment.ReadState.LEAVE_UNREAD);
            btnMarkUnread.setVisible(state == Comment.ReadState.READ); 
        }

		private void createDeleteButton(CellConstraints cc) {
			btnDelete = new HyperlinkLabel(DELETE);
			btnDelete.setOpaque(false);
			btnDelete.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
                    
                    // !!! ACHTUNG !!! - hairy scary ugly ugly
                    // PL-1818 - in diff view getCOntent() throws NoSuchMethod exception
                    // for some weird-ass reason. But granted, this is scary-hairy
                    // if the widget hierarchy ever changes, this will blow up bad
                    Point location = btnDelete.getParent().getParent().getLocationOnScreen();
//                    Point location = popup.getContent().getLocationOnScreen();

                    closePopup();
					final boolean agreed = RemoveCommentConfirmation.userAgreed(null);
					showCommentTooltipPopup(project, CommentTooltipPanel.this, getPopupOwner(), location);
					if (agreed) {
						removeComment(comment);
					}
				}
			});
			add(btnDelete, cc.xy(DELETE_POS, 1));

			if (comment != null && !comment.getReplies().isEmpty()) {
				btnDelete.setVisible(false);
			}
		}

		private void startEditingComment() {
			setStatusText(" ", false);
			setButtonsVisible(false);
			btnEdit.setHyperlinkText(APPLY);
			btnEdit.setToolTipText(POST_COMMENT_TOOLTIP_TEXT);
			if (comment != null && comment.getParentComment() != null && comment.getParentComment().isDraft()) {
				btnEdit.setVisible(false);
			} else {
				btnEdit.setVisible(true);
			}
			btnCancel.setVisible(true);
			if (btnSaveDraft != null && (panelForNewComment || comment.isDraft())) {
				btnSaveDraft.setVisible(true);
			}
			btnShowHide.setState(true);
			lastCommentBody = commentBody.getText();
			setCommentPanelEditable(CommentPanel.this, true);
		}

		private void createCommentEditButtons(@NotNull CellConstraints cc) {
            boolean editOrApply = comment != null && !selectedPanel;
			btnEdit = new HyperlinkLabel(editOrApply ? EDIT : APPLY);
            btnEdit.setToolTipText(editOrApply ? null : POST_COMMENT_TOOLTIP_TEXT);

			if (parentComment != null && parentComment.isDraft()) {
				btnEdit.setVisible(editOrApply);
			}

			btnEdit.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (!(commentBody.isEditable() && postComment())) {
						startEditingComment();
					}
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
							boolean isDefect = boxIsDefect != null && boxIsDefect.isSelected();
							if (panelForNewComment) {
								addCommentForReview(CommentPanel.this, parentComment, commentBody.getText(), true, isDefect);
							} else {
								updateCommentForReview(CommentPanel.this.comment, commentBody.getText(), isDefect);
							}
							btnCancel.setVisible(false);
							setCommentPanelEditable(CommentPanel.this, false);
							btnEdit.setHyperlinkText(EDIT);
						}
					}
				});
				btnSaveDraft.setOpaque(false);
				add(btnSaveDraft, cc.xy(SAVE_DRAFT_POS, 1));
				btnSaveDraft.setVisible(comment == null || comment == commentTemplate);
			}

			btnCancel = new HyperlinkLabel("Cancel");
			btnCancel.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
                    cancelEditing(mode);
				}
			});
			btnCancel.setOpaque(false);
			btnCancel.setVisible(panelForNewComment || selectedPanel);
			add(btnCancel, cc.xy(CANCEL_POS, 1));

			if (mode == Mode.EDIT) {
				startEditingComment();
			}
		}

        private void cancelEditing(Mode mode) {
            btnCancel.setVisible(false);
            btnEdit.setHyperlinkText(EDIT);

            if (btnSaveDraft != null) {
                btnSaveDraft.setVisible(false);
            }
            setButtonsVisible(true);

            if (lastCommentBody != null) {
                setCommentPanelText(this, false, lastCommentBody);
            }
            if (!panelForNewComment) {
                setCommentPanelEditable(this, false);
            } else {
                removeCommentPanel(this);
			}
			if (mode == Mode.EDIT || mode == Mode.ADD) {
				closePopup();
            }
            setStatusText(" ", false);
        }

        private boolean postComment() {
			if (commentBody.isEditable()) {
				if (!validateText(commentBody.getText())) {
					return true;
				}
				commentBody.setBackground(Color.GRAY);
				commentBody.setEnabled(false);
				btnEdit.setVisible(false);
				btnCancel.setVisible(false);
				if (comment != null) {
					comment.setDraft(false);
				}
				if (panelForNewComment) {
					addCommentForReview(this, parentComment, commentBody.getText(),
							comment != null && comment.isDraft(),
							boxIsDefect != null && boxIsDefect.isSelected());
				} else {
					updateCommentForReview(comment, commentBody.getText(), boxIsDefect != null && boxIsDefect.isSelected());
				}
				btnEdit.setHyperlinkText(EDIT);
                btnEdit.setToolTipText(null);
				if (btnSaveDraft != null) {
					btnSaveDraft.setVisible(false);
				}
			}
			return false;
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
                setCommentPanelText(this, false, cmt.getMessage());
                setCommentDate();
                updateDefectField();
                cancelEditing(mode);

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

		public void setButtonsVisible(boolean visible) {
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
					if (panel.comment != null && panel.comment.getReplies().isEmpty() == false) {
						delete.setVisible(false);
					} else { 					
						delete.setVisible(visible);
					}
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

    private void setCommentPanelText(CommentTooltipPanel.CommentPanel commentPanel, boolean editable, String text) {
        JEditorPane pane = commentPanel.commentBody;
        pane.setContentType(editable ? "text/plain" : "text/html");
        String txt = text != null ? text : "";
        if (!editable) {
            Htmlizer lizer = new Htmlizer(MAX_HREF_LINK_LENGTH);
            txt = lizer.replaceBrackets(txt);
            txt = lizer.htmlizeHyperlinks(txt);
            txt = lizer.replaceWhitespace(txt);
        }
        pane.setText(txt);
    }

	private void setCommentPanelEditable(CommentTooltipPanel.CommentPanel commentPanel, boolean editable) {
		JEditorPane pane = commentPanel.commentBody;

		pane.setEditable(editable);
        setCommentPanelText(commentPanel, editable, commentPanel.comment != null ? commentPanel.comment.getMessage() : "");

		if (editable) {
			pane.requestFocusInWindow();
			pane.getCaret().setVisible(editable);
			pane.setCaretPosition(0);
			pane.selectAll();
		}
		pane.setBackground(editable ? EDITABLE_BACKGROUND_COLOR : Color.WHITE);

		if (commentPanel.defectClassificationPanel != null) {
			commentPanel.defectClassificationPanel.setVisible(editable);
			commentPanel.defectClassificationPanel.validate();
		}
	}

	private void setCommentEditable(final Comment comment, CommentTooltipPanel.CommentPanel commentPanel,
			boolean editable) {
		commentPanel.commentBody.setEnabled(editable);
		commentPanel.btnEdit.setHyperlinkText(CommentPanel.APPLY);
		if (comment != null && comment.getParentComment() != null && comment.getParentComment().isDraft()) {
			commentPanel.btnEdit.setVisible(false);
		} else {
			commentPanel.btnEdit.setVisible(editable);
		}
		commentPanel.btnCancel.setVisible(editable);
		if (commentPanel.btnSaveDraft != null) {
			commentPanel.btnSaveDraft.setVisible(comment != null && comment.isDraft());
		}
		setCommentPanelEditable(commentPanel, editable);
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
	 * @param comment - comment to update
	 * @param text	- new comment body
	 * @param defect  - is this comment a defect?
	 */
	private void updateCommentForReview(Comment comment,
			String text, boolean defect) {
		setStatusText("Updating comment...", false);
		updateDefectFields(comment, defect);
		updateComment(comment, text);
	}

	/**
	 * @param panel   - panel that this invocation came from
	 * @param parentComment - comment, that will be parent of newly created comment
	 * @param text	- new comment body
	 * @param draft   - is the comment a draft?
	 * @param defect  - is this comment a defect?
	 */
	private void addCommentForReview(CommentPanel panel, Comment parentComment,
			String text, boolean draft, boolean defect) {
		setCommentPanelEditable(panel, false);

		if (panel.replyPanel) {
			setStatusText("Adding new reply...", false);
			addNewReply(parentComment, text, draft);
		} else {
			setStatusText("Adding new comment...", false);
			((Comment) commentTemplate).setMessage(text);
			updateDefectFields(commentTemplate, defect);
			addNewComment(commentTemplate, draft);
		}
	}

    private void updateDefectFields(Comment comment, boolean defect) {
        comment.setDefectRaised(defect);
        if (!defect) {
            comment.getCustomFields().clear();
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

    protected abstract void markCommentsRead(Collection<Comment> comments);

    protected abstract void markCommentLeaveUnread(Comment comment);

	private class MyReviewListener extends CrucibleReviewListenerAdapter {

        public void createdOrEditedReply(final ReviewAdapter rev, final PermId file,
				final Comment parentComment, final Comment comment) {
            createdOrEditedCommentReply(rev, parentComment, comment);
        }

        private void createdOrEditedCommentReply(final ReviewAdapter rev, final Comment parentComment, final Comment comment) {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (regardsDifferentFile(comment)) {
						return;
					}

//					if (!isTheSameComment(rootComment, parentComment)) {
//						return;
//					}

					for (CommentPanel panel : commentPanelList) {
						Comment reply = panel.getComment();
						if (isTheSameComment(reply, comment)) {
							setStatusText("Comment reply updated", false);
							setAllButtonsVisible();
							panel.setComment(comment);
                            closePopup();
							return;
						}
					}
                    removeUnderConstructionPanel();
					setStatusText("Comment reply added", false);
					setAllButtonsVisible();
					addCommentReplyPanel(review, comment, false, parentComment, Mode.SHOW);
                    closePopup();
				}
			});
		}

        private void removeUnderConstructionPanel() {
            CommentPanel underConstructionPanel = null;
            for (CommentPanel panel : commentPanelList) {
                if (panel.panelForNewComment) {
                    underConstructionPanel = panel;
                    break;
                }
            }
            if (underConstructionPanel != null) {
                removeCommentPanel(underConstructionPanel);
            }
        }

        @Override
        public void commentReadStateChanged(ReviewAdapter r, Comment cmt) {
            for (CommentPanel panel : commentPanelList) {
                if (isTheSameComment(panel.comment, cmt)) {
//                if (panel.comment != null && panel.comment.getPermId().equals(cmt.getPermId())) {
                    panel.updateReadUnreadButtonState(cmt);
                }
            }
        }

        @Override
        public void createdOrEditedGeneralComment(ReviewAdapter rev, Comment comment) {
            createdOrEditedComment(rev, comment);
        }

        public void createdOrEditedVersionedComment(final ReviewAdapter rev, final PermId file,
                                                    final VersionedComment comment) {
            createdOrEditedComment(rev, comment);
        }

        private void createdOrEditedComment(final ReviewAdapter rev, final Comment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (regardsDifferentFile(comment)) {
						return;
					}


					if (!comment.isReply() && commentPanelList.size() > 0) {
                        if (mode == Mode.ADD) {
                            CommentPanel panel = commentPanelList.get(0);
                            if (comment.getMessage().equals(panel.getComment().getMessage())) {
                                setStatusText("Comment aded", false);
                                removeUnderConstructionPanel();
                                addCommentPanel(rev, comment);
                                setAllButtonsVisible();
                                mode = Mode.SHOW;
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
                        closePopup();
					} else {
                        createdOrEditedCommentReply(rev, rootComment, comment);
					}
				}
			});
		}

		private boolean regardsDifferentFile(Comment comment) {
			final VersionedComment versionedComment = ReviewModelUtil.getParentVersionedComment(comment);
			if (versionedComment != null && fileInfo != null) {
				if (!versionedComment.getCrucibleFileInfo().getPermId().equals(fileInfo.getPermId())) {
					return true;
				}
			}
			return false;
		}

		public void removedComment(final ReviewAdapter rev, final Comment comment) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!rev.getPermId().equals(review.getPermId())) {
						return;
					}

					if (!comment.isReply()) {
						if (isTheSameComment(rootComment, comment)) {
                            closePopup();
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

        @Override
        public void publishedGeneralComment(ReviewAdapter rev, Comment comment) {
            publishedComment(rev, null, comment);
        }

        @Override
        public void publishedVersionedComment(final ReviewAdapter rev,
				final PermId file, final VersionedComment comment) {
            publishedComment(rev, file, comment);
        }

        private void publishedComment(final ReviewAdapter rev, final PermId file, final Comment comment) {
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
					for (CommentPanel panel : commentPanelList) {
						Comment cmt = panel.getComment();
						if (isTheSameComment(cmt, comment)) {
							setStatusText("Comment published", false);
							cmt.setDraft(false);
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
