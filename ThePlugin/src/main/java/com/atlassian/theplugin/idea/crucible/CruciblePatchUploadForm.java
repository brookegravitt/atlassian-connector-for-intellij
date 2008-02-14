package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.crucible.CrucibleServerFactory;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CruciblePatchUploadForm extends DialogWrapper {
	private JTextArea patchPreview;
	private JPanel rootComponent;
	private JTextField titleText;
	private JComboBox crucibleServersComboBox;
	private JTextField projectKeyText;
	private JTextArea statementArea;
	private JCheckBox openBrowserToCompleteCheckBox;

	protected CruciblePatchUploadForm(String commitMessage) {
		super(false);
		$$$setupUI$$$();
		init();
		fillInCrucibleServers();
		statementArea.setText(commitMessage);
		getOKAction().putValue(Action.NAME, "Create review...");
	}

	public JComponent getPreferredFocusedComponent() {
		return titleText;
	}

	private void fillInCrucibleServers() {
		CrucibleConfigurationBean crucibleConfigurationBean =
				(CrucibleConfigurationBean) ConfigurationFactory.getConfiguration().getProductServers(ServerType.CRUCIBLE_SERVER);

		crucibleServersComboBox.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
				if (o instanceof ServerBean) {
					ServerBean serverBean = (ServerBean) o;
					return new JLabel(serverBean.getName());
				}
				return null;
			}
		});
		for (Server server : crucibleConfigurationBean.getServers()) {
			crucibleServersComboBox.addItem(server);
		}

	}


	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setPatchPreview(String preview) {
		patchPreview.setText(preview);
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	protected void doOKAction() {
		ReviewData reviewData = new ReviewData();
		ServerBean serverBean = (ServerBean) crucibleServersComboBox.getSelectedItem();
		if (serverBean != null) {
			reviewData.setAuthor(serverBean.getUserName());
			reviewData.setCreator(serverBean.getUserName());
			reviewData.setDescription(statementArea.getText());
			reviewData.setName(titleText.getText());
			reviewData.setProjectKey(projectKeyText.getText());

			try {
				ReviewData draftReviewData = CrucibleServerFactory.getCrucibleServerFacade().createReviewFromPatch(serverBean, reviewData, patchPreview.getText());
				if (openBrowserToCompleteCheckBox.isSelected()) {
					BrowserUtil.launchBrowser(serverBean.getUrlString() + "/cru/" + draftReviewData.getPermaId().getId());
				}
				super.doOKAction();
			} catch (CrucibleException e) {
				showMessageDialog(e.getMessage(), "Error creating review: " + serverBean.getUrlString(), Messages.getErrorIcon());
			}


		}
	}


//	private boolean isValid() {
//
//		if (crucibleServersComboBox.getSelectedItem() == null ||
//				patchPreview.getText().length() == 0 ||
//				titleText.getText().length() == 0 ||
//				projectKeyText.getText().length() == 0 ||
//				statementArea.getText().length() == 0) {
//			return false;
//		}
//
//		return true;
//
//	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}


	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootComponent = new JPanel();
		rootComponent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setMinimumSize(new Dimension(760, 505));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(7, 4, new Insets(1, 1, 1, 1), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		crucibleServersComboBox = new JComboBox();
		panel1.add(crucibleServersComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		titleText = new JTextField();
		panel1.add(titleText, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		projectKeyText = new JTextField();
		projectKeyText.setText("CR");
		projectKeyText.setToolTipText("Project Key the same as in Crucible added manually here");
		panel1.add(projectKeyText, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Server:");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setInheritsPopupMenu(false);
		label2.setText("Project Key:");
		panel1.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Title:");
		panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Statement of Objectives:");
		panel1.add(label4, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		patchPreview = new JTextArea();
		patchPreview.setEditable(false);
		patchPreview.setEnabled(true);
		patchPreview.setFont(new Font("Monospaced", patchPreview.getFont().getStyle(), patchPreview.getFont().getSize()));
		patchPreview.setLineWrap(true);
		patchPreview.setRows(5);
		patchPreview.setText("");
		scrollPane1.setViewportView(patchPreview);
		final JScrollPane scrollPane2 = new JScrollPane();
		panel1.add(scrollPane2, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		statementArea = new JTextArea();
		statementArea.setLineWrap(true);
		statementArea.setRows(5);
		scrollPane2.setViewportView(statementArea);
		final JLabel label5 = new JLabel();
		label5.setText("Patch:");
		panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		openBrowserToCompleteCheckBox = new JCheckBox();
		openBrowserToCompleteCheckBox.setSelected(true);
		openBrowserToCompleteCheckBox.setText("Open browser to complete review creation");
		panel1.add(openBrowserToCompleteCheckBox, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(crucibleServersComboBox);
		label2.setLabelFor(projectKeyText);
		label3.setLabelFor(titleText);
		label4.setLabelFor(statementArea);
		label5.setLabelFor(patchPreview);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
