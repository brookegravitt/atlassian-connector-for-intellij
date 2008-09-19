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
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

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


	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}



	private static class MyRenderer implements TreeCellRenderer {
		private static final StatementOfObjectivesPanel PANEL = new StatementOfObjectivesPanel();
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			CrucibleStatementOfObjectivesNode node = (CrucibleStatementOfObjectivesNode) value;
			PANEL.setText(node.getText());
			return PANEL;
		}
	}


	private static final class StatementOfObjectivesPanel extends JPanel {

		private static final Color BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);

		private JTextArea messageBody;

		public void setText(String text) {
			messageBody.setText(text);
		}

		private StatementOfObjectivesPanel() {
			super(new FormLayout("pref:grow",
					"pref, pref:grow"));

			CellConstraints cc = new CellConstraints();

			final JPanel header = new JPanel(new FormLayout("4dlu, pref, 4dlu", "2dlu, pref:grow, 2dlu"));
			header.add(getHeaderLabel(), cc.xy(2, 2));
			add(header, cc.xy(1, 1));

			JPanel body = new JPanel(new FormLayout("4dlu, pref:grow, 4dlu", "2dlu, pref:grow, 2dlu"));
			messageBody = createMessageBody();
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
