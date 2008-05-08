package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.crucible.api.ReviewItemData;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.RowIcon;

public class ReviewTreeNode extends ClassTreeNode {
	ReviewItemData reviewItem;
                                       
	protected ReviewTreeNode(ReviewItemData reviewItem, Project project, PsiClass o, ViewSettings viewSettings) {
		super(project, o, viewSettings);
		this.reviewItem = reviewItem;
	}

	public void update(PresentationData presentationData) {
		super.update(presentationData);
		ItemPresentation i = this.getValue().getPresentation();     
        presentationData.setPresentableText(i.getPresentableText() + " (rev " + reviewItem.getFromRevision() + " -> " + reviewItem.getToRevision() + ")" );
        presentationData.setIcons(i.getIcon(true));
		presentationData.setAttributesKey(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
	}

}
