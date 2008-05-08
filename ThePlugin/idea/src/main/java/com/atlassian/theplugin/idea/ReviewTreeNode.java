package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.crucible.api.ReviewItemData;

public class ReviewTreeNode /*extends ClassTreeNode*/ {
	ReviewItemData reviewItem;
/*
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
*/
}
