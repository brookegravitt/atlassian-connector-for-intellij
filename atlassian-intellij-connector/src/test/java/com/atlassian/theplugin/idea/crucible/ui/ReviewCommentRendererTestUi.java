/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.crucible.ui;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import com.atlassian.theplugin.idea.ui.tree.comment.GeneralCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.comment.VersionedCommentTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.atlassian.theplugin.util.ui.SimpleIconProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collections;

public final class ReviewCommentRendererTestUi {
	private ReviewCommentRendererTestUi() {
	}

	public static void main(String[] args) {
		ReviewCommentRenderer renderer = new ReviewCommentRenderer(new SimpleIconProvider());
		ServerData cruc = new ServerData("my crucible server", new ServerId().toString(), "", "", "");
		ReviewBean review = new ReviewBean("myreviewbean");
		ReviewAdapter reviewAdapter = new ReviewAdapter(review, cruc);
		VersionedVirtualFile vvf1 = new VersionedVirtualFile("mypath", "1.342");
		VersionedVirtualFile vvf2 = new VersionedVirtualFile("mypath", "1.567");
		CrucibleFileInfo crucibleFileInfo = new CrucibleFileInfoImpl(vvf1, vvf2, new PermIdBean("mypermid"));

		final VersionedCommentBean versionedCommentBean = new VersionedCommentBean();
		versionedCommentBean.setMessage("my beautiful message");
		versionedCommentBean.setDefectRaised(true);
		final ReviewerBean author = new ReviewerBean();
		author.setUserName("wseliga");
		author.setDisplayName("Wojciech Seliga");
		versionedCommentBean.setAuthor(author);

		final VersionedCommentBean versionedCommentBean2 = new VersionedCommentBean();
		versionedCommentBean2.setMessage(
				"my very very very beautiful but annoyingly very very long long long long long comment."
						+ "Let us check if it wraps correctly \nWe have also another line here"
						+ "\n\nThere is also an empty line above");
		versionedCommentBean2.setToEndLine(31);
		final ReviewerBean author2 = new ReviewerBean();
		author2.setUserName("mwent");
		author2.setDisplayName("Marek Went Long Lastname");
		versionedCommentBean2.setAuthor(author2);
		final CustomFieldBean customFieldBean = new CustomFieldBean();
		customFieldBean.setValue("Major");
		versionedCommentBean2.getCustomFields().put("Rank", customFieldBean);
		final CustomFieldBean customFieldBean2 = new CustomFieldBean();
		customFieldBean2.setValue("Missing");
		versionedCommentBean2.getCustomFields().put("Classification", customFieldBean2);
		versionedCommentBean2.setToStartLine(171);
		versionedCommentBean2.setToEndLine(0);
		versionedCommentBean2.setToLineInfo(true);
		versionedCommentBean2.setDraft(true);

		final VersionedCommentBean versionedCommentBean3 = new VersionedCommentBean();
		versionedCommentBean3.setMessage("Another comment. Let us see if it wraps corrent.\nAnd if empty lines work fine\n"
				+ "This statement sucks:\n\tif (false) {\n\t\t...\n\t}");
		versionedCommentBean3.setToStartLine(21);
		versionedCommentBean3.setToEndLine(131);
		versionedCommentBean3.setToLineInfo(true);
		final ReviewerBean author3 = new ReviewerBean();
		author3.setUserName("ewong");
		author3.setDisplayName("Edwin Wong");
		versionedCommentBean3.setAuthor(author3);
		versionedCommentBean3.setDefectRaised(true);


		crucibleFileInfo.addComment(versionedCommentBean);
		crucibleFileInfo.addComment(versionedCommentBean2);
		review.setFiles(Collections.singleton(crucibleFileInfo));
		final VersionedCommentTreeNode n1 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean,
				null);
		final VersionedCommentTreeNode n2 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean2,
				null);
		final VersionedCommentTreeNode n3 = new VersionedCommentTreeNode(reviewAdapter, crucibleFileInfo, versionedCommentBean3,
				null);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		GeneralCommentBean generalComment = new GeneralCommentBean();
		generalComment.setAuthor(author3);
		generalComment.setMessage("This is general comment for this review in two lines.\nShould be quite lengthy");
		GeneralCommentTreeNode generalCommentTreeNode = new GeneralCommentTreeNode(reviewAdapter, generalComment, null);
		root.add(generalCommentTreeNode);
		root.add(n1);
		root.add(n2);
		n2.setExpanded(true);
		DefaultMutableTreeNode nestedChild = new DefaultMutableTreeNode();
		DefaultMutableTreeNode nestedChild2 = new DefaultMutableTreeNode();
		nestedChild.add(nestedChild2);
		nestedChild2.add(n3);
		root.add(nestedChild);

		final JTree jtree = new JTree(root);
		jtree.setCellRenderer(renderer);

		TreeUISetup buildTreeUiSetup = new TreeUISetup(renderer);
		final JScrollPane parentScrollPane = new JScrollPane(jtree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		buildTreeUiSetup.initializeUI(jtree, parentScrollPane);

		new ReviewDetailsTreeMouseListener(jtree, renderer, buildTreeUiSetup);
		SwingAppRunner.run(parentScrollPane, ReviewCommentRenderer.class.getName(), 600, 200);
	}
}
