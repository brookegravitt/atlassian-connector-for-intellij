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
package com.atlassian.theplugin.idea.ui.tree.comment;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.MultiLineUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.font.FontRenderContext;

public class CrucibleStatementOfObjectivesNode extends AtlassianTreeNode {
	private static final TreeCellRenderer MY_RENDERER = new MyRenderer();

	private String getText() {
		return text;
	}

	private final String text;

	public CrucibleStatementOfObjectivesNode(final String text, AtlassianClickAction action) {
		super(action);
		this.text = text;
	}

	protected CrucibleStatementOfObjectivesNode(final CrucibleStatementOfObjectivesNode other) {
		super(other.getAtlassianClickAction());
		text = other.text;
	}


	@Override
	public CrucibleStatementOfObjectivesNode getClone() {
		return new CrucibleStatementOfObjectivesNode(this);
	}


	public int compareTo(Object o) {
		if (o instanceof CrucibleStatementOfObjectivesNode) {
			return 0;
		}
		return -1;
	}

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	private static class MyRenderer implements TreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			CrucibleStatementOfObjectivesNode node = (CrucibleStatementOfObjectivesNode) value;
			// wseliga: I know that component should be cached here and created only once and reused between invocations
			// but we could live here without it especially comparing to the cost of rebuilding every thing onresize of
			// this tree (new UI is set)
			return new StatementOfObjectivesPanel(MultiLineUtil.getCurrentWidth(tree, row), node.getText());
		}
	}


	private static final class StatementOfObjectivesPanel extends JPanel {

		private static final Color BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);

		private JTextArea messageBody;

		private StatementOfObjectivesPanel(int width, String text) {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));

			CellConstraints cc = new CellConstraints();

			final JPanel header = new JPanel(new FormLayout("4dlu, pref, 4dlu", "2dlu, pref:grow, 2dlu"));
			header.add(getHeaderLabel(), cc.xy(2, 2));
			add(header, cc.xy(1, 1));

			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu", "2dlu, pref:grow, 2dlu"));
			messageBody = createMessageBody();
			final int height = MultiLineUtil
					.getHeight(text, width, messageBody.getFont(), new FontRenderContext(null, true, true));
			messageBody.setPreferredSize(new Dimension(0, height));
			messageBody.setText(text);
			body.add(messageBody, cc.xy(2, 2));
			body.setBackground(messageBody.getBackground());
			add(body, cc.xy(1, 2));

			setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
		}

		protected Component getHeaderLabel() {
			return new JLabel("<html><b>Statement of Objectives</b>");
		}


		protected JTextArea createMessageBody() {
			JTextArea result = new JTextArea();
			result.setLineWrap(true);
			result.setWrapStyleWord(true);
			return result;
		}

	}


}
