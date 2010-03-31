package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: pstefaniak
 * Date: Mar 22, 2010
 */
public class EditableIssueField extends JPanel {
	private EditIssueFieldButton button;
	private JComponent component; //ususally label displaying value of that field
	private final EditIssueFieldHandler handler;

	public EditableIssueField(JComponent component, EditIssueFieldHandler handler) {
		this.handler = handler;
		button = new EditIssueFieldButton();
		this.component = component;
		this.component.setBackground(Color.WHITE);
		setBackground(Color.WHITE);
		button.setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder());
		rebuild();
	}

	private void rebuild() {
		JPanel groupingPanel = new JPanel(new GridBagLayout());
		groupingPanel.setBackground(getBackground());
		groupingPanel.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 0.0;
		gbc1.weighty = 0.0;
		gbc1.fill = GridBagConstraints.NONE;

		removeAll();
		if (component != null) {
			setBackground(component.getBackground());
			groupingPanel.add(component, gbc1);
		}

		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 0.0;
		gbc1.weighty = 0.0;
		gbc1.gridx++;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		gbc1.fill = GridBagConstraints.NONE;

		groupingPanel.add(button, gbc1);
		add(groupingPanel, gbc);

		IssueDetailsToolWindow.addFillerPanel(this, gbc, true);
	}


	public void setButtonVisible(boolean isVisible) {
		button.setVisible(isVisible);
	}

	private class EditIssueFieldButton extends JRadioButton {
		private final Icon editIcon = IconLoader.getIcon("/icons/edit.png");

		public EditIssueFieldButton() {
			super();
			setIcon(editIcon);
			this.setBackground(com.intellij.util.ui.UIUtil.getLabelBackground());
			this.setBorder(BorderFactory.createEmptyBorder());
			this.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					if (handler != null) {
						handler.handleClickedEditButton();
					}
				}
			});

			this.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				public void mouseExited(MouseEvent e) {
					setCursor(Cursor.getDefaultCursor());
				}
			});
		}
	}

	public interface EditIssueFieldHandler {
		void handleClickedEditButton();
	}
}

