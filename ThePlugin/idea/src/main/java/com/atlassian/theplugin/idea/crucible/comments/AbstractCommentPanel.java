package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.idea.ui.AtlassianTableViewWithToolbar;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 7:31:59 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCommentPanel extends JPanel implements CrucibleReviewActionListener {
	private ListTableModel commentTableModel;
	private JPanel dataPanelsHolder;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
    private static final int MIN_ROW_HEIGHT = 20;

    public AtlassianTableViewWithToolbar getCommentsTable() {
		return commentsTable;
	}

	public AtlassianTableViewWithToolbar getCommentReplyTable() {
		return replyCommentsTable;
	}

	private AtlassianTableViewWithToolbar commentsTable;
	private AtlassianTableViewWithToolbar replyCommentsTable;
	private ListTableModel commentReplyTableModel;

	public AbstractCommentPanel() {
		super(new BorderLayout());
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		getCommentTableModel().setSortable(true);
		commentsTable = createCommentsTable(getCommentTableModel(), getCommentTableColumnProvider(),
				getCommentToolbarActionGroup(), getCommentSelectedListener(), getCommentToolbarPlace());

		getCommentReplyTableModel().setSortable(true);
		replyCommentsTable = createCommentsTable(getCommentReplyTableModel(), getCommentReplyTableColumnProvider(),
				getCommentReplyToolbarActionGroup(),
				getCommentReplySelectedListener(), getCommentReplyToolbarPlace());

		dataPanelsHolder = new JPanel();
		dataPanelsHolder.setLayout(new BorderLayout());
		dataPanelsHolder.setBackground(UIUtil.getTreeTextBackground());
//		dataPanelsHolder.add(replyCommentsTable, BorderLayout.CENTER, JLayeredPane.DEFAULT_LAYER);
		dataPanelsHolder.add(commentsTable, BorderLayout.CENTER);
		add(dataPanelsHolder, BorderLayout.CENTER);
		progressAnimation.configure(this, dataPanelsHolder, BorderLayout.CENTER);
	}

	protected abstract String getCommentToolbarPlace();

	protected abstract String getCommentReplyToolbarPlace();

	protected abstract TableColumnProvider getCommentTableColumnProvider();

	protected abstract TableColumnProvider getCommentReplyTableColumnProvider();

	protected abstract TableItemSelectedListener getCommentReplySelectedListener();

	protected abstract TableItemSelectedListener getCommentSelectedListener();

	protected abstract String getCommentToolbarActionGroup();

	protected abstract String getCommentReplyToolbarActionGroup();

	protected ProgressAnimationProvider getProgressAnimation() {
		return progressAnimation;
	}

	protected ListTableModel getCommentTableModel() {
		return commentTableModel;
	}

	protected void setCommentTableModel(ListTableModel commentTableModel) {
		this.commentTableModel = commentTableModel;
	}

	protected void setCommentReplyTableModel(ListTableModel commentReplyTableModel) {
		this.commentReplyTableModel = commentReplyTableModel;
	}

	protected ListTableModel getCommentReplyTableModel() {
		return commentReplyTableModel;
	}

	protected AtlassianTableViewWithToolbar createCommentsTable(ListTableModel listTableModel,
            TableColumnProvider columnProvider, String toolbarName, TableItemSelectedListener tableItemSelectedListener,
            String toolbarPlace) {

        AtlassianTableViewWithToolbar table = new AtlassianTableViewWithToolbar(columnProvider, listTableModel, null,
                toolbarPlace, toolbarName, "Context menu", "ThePlugin.Crucible.ReviewPopupMenu");
        table.setBorder(BorderFactory.createEmptyBorder());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().setColumnMargin(0);

		table.setMinRowHeight(MIN_ROW_HEIGHT);
		table.getTable().addItemSelectedListener(tableItemSelectedListener);
		return table;
	}

	public void switchToComments() {
		dataPanelsHolder.removeAll();
		dataPanelsHolder.add(commentsTable, BorderLayout.CENTER);
		dataPanelsHolder.invalidate();
		dataPanelsHolder.revalidate();
		dataPanelsHolder.repaint();
	}

	protected void switchToCommentReplies() {
		dataPanelsHolder.removeAll();
		dataPanelsHolder.add(replyCommentsTable, BorderLayout.CENTER);
		dataPanelsHolder.invalidate();
		dataPanelsHolder.revalidate();
		dataPanelsHolder.repaint();
	}


}
