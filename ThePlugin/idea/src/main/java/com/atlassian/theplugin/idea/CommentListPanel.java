package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleCommentPanel;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewData;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 15, 2008
 * Time: 1:26:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentListPanel extends JPanel {
	private CrucibleReviewActionListener listener = new MyCrucibleReviewActionListener();
	private JScrollPane commentScroll;
	private ProgressAnimationProvider progressAnimation = new ProgressAnimationProvider();
	private JList commentList;
	private ListModel model = new DefaultListModel();

	public CommentListPanel() {
		super();
		IdeaHelper.getReviewActionEventBroker().registerListener(listener);
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(UIUtil.getTreeTextBackground());
		progressAnimation.configure(this, commentScroll, BorderLayout.CENTER);

		commentList = new JList(model);
		commentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		commentScroll = new JScrollPane(commentList);
		add(commentScroll, BorderLayout.CENTER);
	}

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {
		@Override
		public void focusOnGeneralComments(ReviewData changeSet) {
		}

		@Override
		public void showReview(ReviewData review) {
			List<GeneralComment> generalComments = new ArrayList<GeneralComment>();
			commentList.setCellRenderer(new MyTenderer(review));
			try {
				generalComments = review.getGeneralComments();
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// ignore
			}
			Vector<GeneralComment> vector = new Vector<GeneralComment>(generalComments);
			commentList.setListData(vector);
		}

		private class MyTenderer implements ListCellRenderer {
			private ReviewData review;

			public MyTenderer(ReviewData review) {
				this.review = review;
			}

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				APannel commentPanel = new APannel(review, (GeneralComment) value);
				panel.add(commentPanel);
				panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
				panel.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder")
						: new EmptyBorder(1, 1, 1, 1));
				commentPanel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
				return panel;
			}
		}

		private class APannel extends JPanel {
			private ReviewData review;
			private GeneralComment generalComment;

			public APannel(ReviewData review, GeneralComment generalComment) {
				super(new BorderLayout());
				this.review = review;
				this.generalComment = generalComment;
				add(createPanel(), BorderLayout.CENTER);
			}

			private JPanel createPanel() {
				FormLayout layout = new FormLayout("pref");
				DefaultFormBuilder builder = new DefaultFormBuilder(layout);
				builder.setDefaultDialogBorder();
				builder.setLeadingColumnOffset(1);
//				builder.appendSeparator(generalComment.getMessage());
				builder.append(new CrucibleCommentPanel(review, generalComment));
//				builder.append(new JLabel("b"));
//				builder.append(new JLabel("c"));
//				builder.append(new JLabel("d"));
				return builder.getPanel();
			}

		}
	}
}
