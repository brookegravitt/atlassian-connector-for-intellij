package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.idea.config.serverconfig.GenericServerConfigForm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Feb 20, 2008
 * Time: 2:22:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericServerConfigurationFormTest extends TestCase {

	GenericServerConfigForm genericServerConfigurationForm;



	protected void setUp() throws Exception {
		super.setUp();
		genericServerConfigurationForm = new GenericServerConfigForm(null);
	}

	public void testGenericSetGetData() throws Exception {
		assertNotNull(genericServerConfigurationForm.getRootComponent());

		ServerBean inServerBean = createServerBean();
		ServerBean outServerBean = null;

		genericServerConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean) genericServerConfigurationForm.getData();

		// form use cloned instance
		assertNotSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
	}

	@SuppressWarnings({ "RedundantStringConstructorCall" })
	public void testBambooFormIsModified() throws Exception {
		ServerBean inServerBean = createServerBean();

		genericServerConfigurationForm.setData(inServerBean);

		ServerBean outServerBean = createServerBean();

		assertFalse(genericServerConfigurationForm.isModified());


		/* equals vs == */

		outServerBean.setName(new String("name"));
		outServerBean.setPasswordString(new String("password"), true);
		outServerBean.setUrlString(new String("url"));
		outServerBean.setUserName(new String("userName"));


		PluginConfigurationFormHelper formHelper = new PluginConfigurationFormHelper(genericServerConfigurationForm);

		formHelper.serverName.setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.serverName.setText(outServerBean.getName());

		formHelper.serverUrl.setText(outServerBean.getUrlString() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.serverUrl.setText(outServerBean.getUrlString());

		formHelper.username.setText(outServerBean.getUserName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.username.setText(outServerBean.getUserName());


		formHelper.password.setText(outServerBean.getName() + "-chg");
		assertTrue(genericServerConfigurationForm.isModified());
		formHelper.password.setText(outServerBean.getPasswordString());
	}

	public void testBambooFormFieldSetting() throws Exception {
		genericServerConfigurationForm.setData(new ServerBean());

		ServerBean outServer = (ServerBean) genericServerConfigurationForm.getData();
		assertEquals("", outServer.getName());
		assertEquals("", outServer.getUrlString());
		assertEquals("", outServer.getUserName());
		assertEquals("", outServer.getPasswordString());
		assertEquals(0, outServer.getSubscribedPlansData().size());

		PluginConfigurationFormHelper helper = new PluginConfigurationFormHelper(genericServerConfigurationForm);

		helper.serverName.setText("name");
		helper.password.setText("password");
		helper.serverUrl.setText("url");
		helper.username.setText("userName");

		outServer = (ServerBean) genericServerConfigurationForm.getData();
		checkServerBean(outServer);
	}


	private static ServerBean createServerBean() {

		ServerBean outServer = new ServerBean();
		outServer.setName("name");
		outServer.setPasswordString("password", true);
		outServer.setUrlString("url");
		outServer.setUserName("userName");


		return outServer;
	}

	private static void checkServerBean(ServerBean outServer) throws ServerPasswordNotProvidedException {

		assertEquals("name", outServer.getName());
		assertEquals("password", outServer.getPasswordString());
		assertEquals("url", outServer.getUrlString());
		assertEquals("userName", outServer.getUserName());
	}

	public static Test suite() {
		return new TestSuite(GenericServerConfigurationFormTest.class);
	}




	private class PluginConfigurationFormHelper {
		public JTextField serverName;
		public JTextField serverUrl;
		public JTextField username;
		public JPasswordField password;
		public JButton testConnection;
		public JCheckBox chkPasswordRemember;
		public JCheckBox cbEnabled;

		public PluginConfigurationFormHelper(GenericServerConfigForm pluginConfigurationForm) throws Exception {
			for (Field f : getClass().getFields()) {
				String name = f.getName();
				Field original = pluginConfigurationForm.getClass().getDeclaredField(name);
				original.setAccessible(true);

				f.set(this, original.get(pluginConfigurationForm));				
			}
		}
	}
}
