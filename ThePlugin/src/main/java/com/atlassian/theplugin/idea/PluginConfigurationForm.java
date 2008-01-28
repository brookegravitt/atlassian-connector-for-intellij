package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooServerFactory;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Plugin configuration form.
 */
public class PluginConfigurationForm {
    private JPanel rootComponent;
    private JTextField serverName;
	private JTextField serverUrl;
	private JTextField username;
	private JPasswordField password;
	private JButton testConnection;
	private JTextArea buildPlansTextArea;
	private HyperlinkLabel openJiraHyperlinkLabel;
	private JCheckBox chkPasswordRemember;

	public PluginConfigurationForm() {

		testConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					BambooServerFactory.getBambooServerFacade().testServerConnection(serverUrl.getText(),
							username.getText(), String.valueOf(password.getPassword()));
					showMessageDialog("Connected successfully", "Connection OK", Messages.getInformationIcon());
				} catch (BambooLoginException e1) {
					showMessageDialog(e1.getMessage(), "Connection Error", Messages.getErrorIcon());
					//throw e1;
				}
			}
		});
	}

	public void setData(PluginConfigurationBean data) {
		for (ServerBean server: data.getBambooConfigurationData().getServersData()){
			serverName.setText(server.getName());
			serverUrl.setText(server.getUrlString());
			username.setText(server.getUsername());
			this.chkPasswordRemember.setSelected(server.getShouldPasswordBeStored());
			try {
				password.setText(server.getPasswordString());
			} catch (ServerPasswordNotProvidedException serverPasswordNotProvidedException) {
				// swallow - password does not have to be initialized always
			}

			buildPlansTextArea.setText(subscribedPlansToString(server.getSubscribedPlansData()));
		}
	}

	public void getData(PluginConfigurationBean data) {
	ServerBean serverBean = null;

		data.getBambooConfigurationData().getServersData().clear();

		//@todo loop here to add all bamboo servers from gui

		//for (){
		    serverBean = new ServerBean();
			serverBean.setName(serverName.getText());
			serverBean.setUrlString(serverUrl.getText());
			serverBean.setUsername(username.getText());
			serverBean.setPasswordString(String.valueOf(password.getPassword()), chkPasswordRemember.isSelected());

			serverBean.setSubscribedPlansData(subscribedPlansFromString(buildPlansTextArea.getText()));
			data.getBambooConfigurationData().getServersData().add(serverBean);
		//}
	}

	static String subscribedPlansToString(Collection<SubscribedPlanBean> plans) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SubscribedPlanBean plan : plans) {
			if (!first) {
				sb.append(' ');
			} else {
				first = false;
			}
			sb.append(plan.getPlanId());
		}

		return sb.toString();
	}

	static List<SubscribedPlanBean> subscribedPlansFromString(String planList) {
		List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();

		for (String planId : planList.split("\\s+")) {
			if (planId.length() == 0) {
				continue;
			}
			SubscribedPlanBean spb = new SubscribedPlanBean();
			spb.setPlanId(planId);
			plans.add(spb);
		}

		return plans;
	}

public boolean isModified(PluginConfigurationBean data) {
		 boolean isModified = false;
		 ServerBean server = null;
		 //@todo synchronize with gui
		 if (data.getBambooConfigurationData().getServersData().size() == 0) {
			 return true;

		 } else {
			 server = data.getBambooConfigurationData().getServersData().iterator().next();
		 }
			 if (chkPasswordRemember.isSelected() != server.getShouldPasswordBeStored())
				 return true;
			 if (serverName.getText() != null ? !serverName.getText().equals(server.getName()) : server.getName() != null)
				 return true;
			 if (serverUrl.getText() != null ? !serverUrl.getText().equals(server.getUrlString()) : server.getUrlString() != null)
				 return true;
			 if (username.getText() != null ? !username.getText().equals(server.getUsername()) : server.getUsername() != null)
				 return true;
			 if (String.valueOf(password.getPassword()) != null) {
				 while (true) {
					 try {
						 if (String.valueOf(password.getPassword()).equals(server.getPasswordString()))
							 break;
					 } catch (ServerPasswordNotProvidedException serverPasswordNotProvidedException) {
						 // swallow
					 }
					 return true;
				 }
			 }
			 if (null != buildPlansTextArea.getText() ? !buildPlansTextArea.getText().equals(subscribedPlansToString(server.getSubscribedPlansData())) :
					 server.getSubscribedPlansData() != null)
				 return true;


		 return isModified;
	 }


	public JComponent getRootComponent() {
		return rootComponent;
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
		openJiraHyperlinkLabel = new HyperlinkLabel("Report a bug/issue/request.");
		openJiraHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser("https://studio.atlassian.com/browse/PL");
			}
		});


	}
}
