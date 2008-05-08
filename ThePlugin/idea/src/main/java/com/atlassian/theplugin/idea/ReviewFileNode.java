package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.crucible.api.ReviewItemData;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class ReviewFileNode extends PsiFileNode {
	ReviewItemData reviewItem;

	protected ReviewFileNode(ReviewItemData reviewItem, Project project, PsiFile o, ViewSettings viewSettings) {
		super(project, o, viewSettings);
		this.reviewItem = reviewItem;
	}

	public void update(PresentationData presentationData) {
		super.update(presentationData);
		presentationData.setPresentableText(presentationData.getPresentableText() + " (rev " + reviewItem.getFromRevision() + " -> " + reviewItem.getToRevision() + ")" );
        presentationData.setAttributesKey(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
	}

}