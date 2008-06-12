package com.atlassian.theplugin.idea.crucible;

import com.intellij.openapi.util.IconLoader;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 12, 2008
 * Time: 1:28:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleTreeRenderer extends DefaultTreeCellRenderer {

	private final static String MODIFIED_FILE_STR = "(mod)";
	private final static String NEW_FILE_STR = "(new)";
	private final static String DELETED_FILE_STR = "(del)";
	private final static String UNKNOWN_FILE_STR = "(???)";


	private static Icon crucibleServersIcon;
	private static Icon crucibleServerEnabledIcon;
	private static Icon crucibleServerDisabledIcon;
	private static Icon crucibleSelectNoneIcon;
	private static Icon crucibleSelectAllIcon;
	private static final int DEFAULT_COMMENT_MESSAGE_LENGTH = 20;

	static {

		crucibleServersIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerEnabledIcon = IconLoader.getIcon("/icons/crucible-blue-16.png");
		crucibleServerDisabledIcon = IconLoader.getIcon("/icons/crucible-grey-16.png");
		crucibleSelectNoneIcon = IconLoader.getIcon("/icons/select_none.gif");
		crucibleSelectAllIcon = IconLoader.getIcon("/icons/select_all.gif");

	}


	public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		StringBuffer labelText = new StringBuffer() ;

		if (value instanceof ReviewItemDataNode) {
			ReviewItem item =  ((ReviewItemDataNode)value).getReviewItem();

			if (item.getFromPath().length() > 0 && item.getToPath().length() == 0){
				//new file
				labelText.append(getFileNameFromPath(item.getFromPath()));
				labelText.append(NEW_FILE_STR);

			} else if (item.getFromPath().length() > 0 && item.getToPath().length() > 0){

				labelText.append(getFileNameFromPath(item.getToPath()));
				labelText.append(" ").append(MODIFIED_FILE_STR);

			} else if (item.getFromPath().length() == 0 && item.getToPath().length() > 0){
				labelText.append(item.getToPath());
				labelText.append(" ").append(DELETED_FILE_STR);

			} else {
				labelText.append((item.getFromPath().length() > 0 ? getFileNameFromPath(item.getFromPath()) : getFileNameFromPath(item.getToPath())));
				labelText.append(" ").append(UNKNOWN_FILE_STR);
			}

			label.setText(labelText.toString());
			label.setIcon(crucibleSelectNoneIcon);

			}
		if (value instanceof CrucibleTreeRootNode) {
			ReviewDataInfoAdapter adapter = ((CrucibleTreeRootNode) value).getReviewDataInfoAdapter();
			if (adapter != null) {


				labelText.append(adapter.getName());
			}
			label.setText(labelText.toString());
			label.setIcon(crucibleServerEnabledIcon);
		}

		if (value instanceof CommentNode){
			GeneralComment generalComment = ((CommentNode) value).getGeneralComment();
			if (generalComment != null) {
				labelText.append(generalComment.getMessage().substring(0, Math.min(generalComment.getMessage().length(),DEFAULT_COMMENT_MESSAGE_LENGTH)));
				if (generalComment.getMessage().length() > DEFAULT_COMMENT_MESSAGE_LENGTH) {
					labelText.append("...");
				}
				labelText.append(" (").append(generalComment.getUser()).append(")");
			}
			label.setIcon(crucibleSelectNoneIcon);
		}
		

		return label;
	}

	private String getFileNameFromPath(String filePath){
		
		return filePath.substring(filePath.lastIndexOf("/") + 1);
	};

}

