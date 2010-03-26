package com.atlassian.theplugin.idea.ui;

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
	private JComponent label;
	private final EditIssueFieldHandler handler;

	public EditableIssueField(JLabel label, EditIssueFieldHandler handler) {
		this.handler = handler;
		button = new EditIssueFieldButton();
		this.label = label;
		this.label.setBackground(Color.WHITE);
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
		if (label != null) {
			setBackground(label.getBackground());
			groupingPanel.add(label, gbc1);
		}
		gbc1.gridx++;
		groupingPanel.add(button, gbc1);
		add(groupingPanel, gbc);

		addFillerPanel(this, gbc, true);
	}

	private static void addFillerPanel(JPanel parent, GridBagConstraints gbc, boolean horizontal) {
		if (horizontal) {
			gbc.gridx++;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
		} else {
			gbc.gridy++;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.VERTICAL;
		}
		JPanel filler = new JPanel();
		filler.setBorder(BorderFactory.createEmptyBorder());
		filler.setOpaque(false);
			parent.add(filler, gbc);
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

