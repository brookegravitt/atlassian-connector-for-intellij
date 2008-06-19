package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ui.*;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralCommentReplyEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 6:57:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCommentsPanel extends JPanel implements CrucibleReviewActionListener {
	private static ReviewCommentsPanel instance;
	private ListTableModel commentTableModel;
	private JLayeredPane dataPanelsHolder;
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	protected TableColumnProvider tableColumnProvider = new CommentColumnProvider();
	private CrucibleServerFacade crucibleServerFacade;
	public static final Logger LOGGER = PluginUtil.getLogger();
	private AtlassianTableViewWithToolbar commentsTable;
	private AtlassianTableViewWithToolbar replyCommentsTable;
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private ReviewItem reviewItem;
	private ListTableModel commentReplyTableModel;
	private GeneralComment selectedComment;


	private ReviewCommentsPanel() {
		super(new BorderLayout());
		IdeaHelper.getCurrentReviewActionEventBroker().registerListener(this);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		commentTableModel = new ListTableModel(tableColumnProvider.makeColumnInfo());
		commentTableModel.setSortable(true);
	    commentsTable = createCommentsTable(commentTableModel, "ThePlugin.CrucibleGeneralCommentToolBar", new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				GeneralComment selectedComment = (GeneralComment) item;
				if (noClicks == 2) {
					IdeaHelper.getCurrentReviewActionEventBroker().trigger(
							new FocusOnGeneralCommentEvent(
									I_WANT_THIS_MESSAGE_BACK,
									reviewDataInfoAdapter,
									selectedComment
							)
					);
				}
			}
		});

		commentReplyTableModel = new ListTableModel(tableColumnProvider.makeColumnInfo());
		commentReplyTableModel.setSortable(true);
		replyCommentsTable = createCommentsTable(commentReplyTableModel, "ThePlugin.CrucibleGeneralCommentReplyToolBar", new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				GeneralComment selectedComment = (GeneralComment) item;
				if (noClicks == 2) {
					IdeaHelper.getCurrentReviewActionEventBroker().trigger(
							new FocusOnGeneralCommentReplyEvent(
									I_WANT_THIS_MESSAGE_BACK, 
									reviewDataInfoAdapter,
									selectedComment
							)
					);
				}
			}
		});

		dataPanelsHolder = new JLayeredPane();
		dataPanelsHolder.setLayout(new BorderLayout());
		dataPanelsHolder.setBackground(UIUtil.getTreeTextBackground());
//		dataPanelsHolder.add(replyCommentsTable, BorderLayout.CENTER, JLayeredPane.DEFAULT_LAYER);
		dataPanelsHolder.add(commentsTable, BorderLayout.CENTER, JLayeredPane.POPUP_LAYER);
		add(dataPanelsHolder, BorderLayout.CENTER);
		progressAnimation.configure(this, dataPanelsHolder, BorderLayout.CENTER);
	}

	private AtlassianTableViewWithToolbar createCommentsTable(ListTableModel listTableModel, String toolbarName, TableItemSelectedListener tableItemSelectedListener) {
		AtlassianTableViewWithToolbar table = new AtlassianTableViewWithToolbar(tableColumnProvider, listTableModel, null,
				"atlassian.toolwindow.serverToolBar",
				toolbarName,
				"Context menu",
				 "ThePlugin.Crucible.ReviewPopupMenu"
				);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().setColumnMargin(0);

		table.setMinRowHeight(20);
		table.getTable().addItemSelectedListener(tableItemSelectedListener);
		return table;
	}

	public static ReviewCommentsPanel getInstance() {
		if (instance == null) {
			instance = new ReviewCommentsPanel();
		}
		return instance;
	}


	public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		try {
			progressAnimation.startProgressAnimation();
			final List<GeneralComment> generalComments = crucibleServerFacade.getGeneralComments(reviewDataInfoAdapter.getServer(),
					reviewDataInfoAdapter.getPermaId());
			EventQueue.invokeLater(new CommentListChangedListener(reviewDataInfoAdapter, generalComments));
		} catch (RemoteApiException e) {
			LOGGER.warn("Error retrieving comments", e);
		} catch (ServerPasswordNotProvidedException e) {
			LOGGER.warn("Error retrieving comments", e);
		} finally {
			progressAnimation.stopProgressAnimation();
		}
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
	}

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		EventQueue.invokeLater(new CommentSelectedListener(reviewDataInfoAdapter, comment));
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void switchToComments() {
		dataPanelsHolder.removeAll();
		dataPanelsHolder.add(commentsTable, BorderLayout.CENTER);
	}

	private static class CommentColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new CommentCreateDateColumn(),
					new CommentAuthorColumn(),
					new CommentSummaryColumn(),
					new CommentStateColumn()
			};
		}

		public TableCellRenderer[] makeRendererInfo() {
			return new TableCellRenderer[]{
					null,
					null,
					null,
					null
			};
		}
	}

	private class CommentListChangedListener implements Runnable {
		private ReviewDataInfoAdapter reviewDataInfoAdapter;
		private final List<GeneralComment> generalComments;

		public CommentListChangedListener(ReviewDataInfoAdapter reviewDataInfoAdapter, List<GeneralComment> generalComments) {
			this.reviewDataInfoAdapter = reviewDataInfoAdapter;
			this.generalComments = generalComments;
		}

		public void run() {
			ReviewCommentsPanel.this.reviewDataInfoAdapter = reviewDataInfoAdapter; // adapter changed to new one
			commentTableModel.setItems(generalComments);
			commentTableModel.fireTableDataChanged();
			commentsTable.getTable().revalidate();
			commentsTable.getTable().setEnabled(true);
			commentsTable.getTable().setForeground(UIUtil.getActiveTextColor());
//			dataPanelsHolder.moveToFront(commentsTable);
			switchToComments();
		}
	}

	private class CommentSelectedListener implements Runnable {
		private ReviewDataInfoAdapter reviewDataInfoAdapter;
		private GeneralComment comment;

		public CommentSelectedListener(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
			this.reviewDataInfoAdapter = reviewDataInfoAdapter;
			this.comment = comment;
		}

		public void run() {
			ReviewCommentsPanel.this.selectedComment = comment; // adapter changed to the new one
			commentReplyTableModel.setItems(comment.getReplies());
			commentReplyTableModel.fireTableDataChanged();
			StringBuffer buffer = new StringBuffer();
			buffer.append("Replies to a comment \"");
			buffer.append(comment.toString());
			buffer.append("\" made by ");
			buffer.append(comment.getDisplayUser());
			buffer.append(" [");
			buffer.append(CommentCreateDateColumn.FORMATTER.format(comment.getCreateDate()));
			buffer.append("]");
			replyCommentsTable.setStatusText(buffer.toString());
			replyCommentsTable.getTable().revalidate();
			replyCommentsTable.getTable().setEnabled(true);
			replyCommentsTable.getTable().setForeground(UIUtil.getActiveTextColor());
//			dataPanelsHolder.moveToFront(replyCommentsTable);
			dataPanelsHolder.removeAll();
			dataPanelsHolder.add(replyCommentsTable, BorderLayout.CENTER);
		}
	}
}
