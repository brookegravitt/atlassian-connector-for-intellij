package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.crucible.api.ReviewItemData;

public class ReviewFileNode /*extends PsiFileNode*/ {
	ReviewItemData reviewItem;
/*
	protected ReviewFileNode(ReviewItemData reviewItem, Project project, PsiFile o, ViewSettings viewSettings) {
		super(project, o, viewSettings);
		this.reviewItem = reviewItem;
	}

	public void update(PresentationData presentationData) {
		super.update(presentationData);
		presentationData.setPresentableText(presentationData.getPresentableText() + " (rev " + reviewItem.getFromRevision() + " -> " + reviewItem.getToRevision() + ")" );
        presentationData.setAttributesKey(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
	}
*/
}