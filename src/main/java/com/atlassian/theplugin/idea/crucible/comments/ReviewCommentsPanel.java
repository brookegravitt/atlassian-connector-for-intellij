package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ui.*;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
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
	private ListTableModel listTableModel;
	private JPanel dataPanelsHolder;
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	protected TableColumnProvider tableColumnProvider = new CommentColumnProvider();
	private CrucibleServerFacade crucibleServerFacade;
	public static final Logger LOGGER = PluginUtil.getLogger();
	private AtlassianTableViewWithToolbar table;


	private ReviewCommentsPanel() {
		super(new BorderLayout());
		IdeaHelper.getCurrentReviewActionEventBroker().registerListener(this);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		listTableModel = new ListTableModel(tableColumnProvider.makeColumnInfo());
		listTableModel.setSortable(true);
		table = new AtlassianTableViewWithToolbar(listTableModel, null,
				"atlassian.toolwindow.serverToolBar",
				"ThePlugin.CrucibleReviewToolBar",
				"Context menu",
				null);
		table.prepareColumns(tableColumnProvider);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().setColumnMargin(0);

		table.setMinRowHeight(20);
		//.setAutoResizeMode(TableView.AUTO_RESIZE_OFF);


		dataPanelsHolder = new JPanel();
		dataPanelsHolder.setLayout(new BorderLayout());
		dataPanelsHolder.setBackground(UIUtil.getTreeTextBackground());
//		tablePane = new JScrollPane(dataPanelsHolder,
//				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		tablePane.setWheelScrollingEnabled(true);
//
//		add(tablePane, BorderLayout.CENTER);
//

//		dataPanelsHolder.add(table, BorderLayout.CENTER);
//		add(dataPanelsHolder, BorderLayout.CENTER);
		add(table, BorderLayout.CENTER);
		progressAnimation.configure(this, dataPanelsHolder, BorderLayout.CENTER);
	}

	public static ReviewCommentsPanel getInstance() {
		if (instance == null) {
			instance = new ReviewCommentsPanel();
		}
		return instance;
	}


	public void focusOnReview(ReviewDataInfoAdapter reviewItem) {
		try {
			progressAnimation.startProgressAnimation();
			final List<GeneralComment> generalComments = crucibleServerFacade.getGeneralComments(reviewItem.getServer(),
					reviewItem.getPermaId());
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					listTableModel.setItems(generalComments);
					listTableModel.fireTableDataChanged();
					table.getTable().revalidate();
					table.getTable().setEnabled(true);
					table.getTable().setForeground(UIUtil.getActiveTextColor());
				}
			});
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
}
