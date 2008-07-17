package com.atlassian.theplugin.util;

import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleCommentPanel;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.DefaultFormBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 3:33:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentPanelBuilder {
	public static JPanel createAPanel(Comment comment) {
		JPanel panel = new JPanel();
		JPanel panel2 = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
		panel2.add(new JLabel("a"));
		panel2.add(new JLabel("b"));
		panel2.add(new JLabel("c"));
		panel.add(new JLabel(comment.getMessage()));
		panel.add(panel2);
		panel.add(new JLabel("z"));
		panel.setPreferredSize(new Dimension(200, 200));
		panel.setBackground(Color.white);
		return panel;  //To change body of created methods use File | Settings | File Templates.
	}

	public static JPanel createEditPanelOfGeneralComment(ReviewData review, GeneralComment comment) {
		return createAPanel(comment);
	}

	public static JPanel createViewPanelOfGeneralComment(ReviewData review, GeneralComment comment) {
		return createAPanel(comment);
	}

	public static JPanel createEditPanelOfVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		return createAPanel(comment);
	}

	public static JPanel createViewPanelOfVersionedComment(ReviewData review, CrucibleFileInfo file, VersionedComment comment) {
		return createAPanel(comment);
	}
}
