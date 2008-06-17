package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ui.CollapsibleTable;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
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
	private JPanel toolBarPanel;
	private ListTableModel listTableModel;
	private JPanel dataPanelsHolder;
	private JScrollPane tablePane;
	protected ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	protected TableColumnProvider tableColumnProvider = new CommentColumnProvider();
	private CrucibleServerFacade crucibleServerFacade;
	public static final Logger LOGGER = PluginUtil.getLogger();


	private ReviewCommentsPanel() {
		super();
		IdeaHelper.getCurrentReviewActionEventBroker().registerListener(this);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		setBackground(UIUtil.getTreeTextBackground());

		toolBarPanel = new JPanel(new BorderLayout());
//        ActionManager actionManager = ActionManager.getInstance();
//        ActionGroup toolbar = (ActionGroup) actionManager.getAction(getToolbarActionGroup());
//        ActionToolbar actionToolbar = actionManager.createActionToolbar(
//                "atlassian.toolwindow.serverToolBar", toolbar, true);
//        toolBarPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
		add(toolBarPanel, BorderLayout.NORTH);

//		editorPane = new ToolWindowBambooContent();
//		editorPane.setEditorKit(new ClasspathHTMLEditorKit());
//		JScrollPane pane = setupPane(editorPane, wrapBody(getInitialMessage()));
//		editorPane.setMinimumSize(ED_PANE_MINE_SIZE);
//		add(pane, BorderLayout.SOUTH);
//
		TableColumnInfo[] columns = new TableColumnInfo[0];

		listTableModel = new ListTableModel(columns);
		listTableModel.setSortable(true);
		TableView table = new TableView(listTableModel);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().setColumnMargin(0);

		table.setMinRowHeight(20);
		table.setAutoResizeMode(TableView.AUTO_RESIZE_OFF);


		dataPanelsHolder = new JPanel();
		dataPanelsHolder.setLayout(new VerticalFlowLayout());
		dataPanelsHolder.setBackground(UIUtil.getTreeTextBackground());
		tablePane = new JScrollPane(dataPanelsHolder,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePane.setWheelScrollingEnabled(true);

		add(tablePane, BorderLayout.CENTER);

		progressAnimation.configure(this, tablePane, BorderLayout.CENTER);
	}

	public static ReviewCommentsPanel getInstance() {
		if (instance == null) {
			instance = new ReviewCommentsPanel();
		}
		return instance;
	}


	public void focusOnReview(ReviewDataInfoAdapter reviewItem) {
		dataPanelsHolder.removeAll();
		try {
			progressAnimation.startProgressAnimation();
			final List<GeneralComment> generalComments = crucibleServerFacade.getGeneralComments(reviewItem.getServer(),
					reviewItem.getPermaId());
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					for (GeneralComment generalComment : generalComments) {
						CollapsibleTable table = addCollapsiblePanel(generalComment.getMessage());
						dataPanelsHolder.add(table);
					}
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

	private CollapsibleTable addCollapsiblePanel(String title) {
		CollapsibleTable table = new CollapsibleTable(
				tableColumnProvider,
				null,
				title,
				"atlassian.toolwindow.serverToolBar",
				"ThePlugin.CrucibleReviewToolBar",
				"Context menu",
				null);
		table.addItemSelectedListener(new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});
		return table;
	}

	private static class CommentColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new ComentAuthorColumn(),
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
