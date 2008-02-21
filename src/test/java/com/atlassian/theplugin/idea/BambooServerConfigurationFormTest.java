package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.idea.config.serverconfig.BambooServerConfigForm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * BambooServerConfigForm Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/17/2008</pre>
 */
public class BambooServerConfigurationFormTest extends TestCase {

	BambooServerConfigForm bambooPluginConfigurationForm;
	//statuses as strings returned by bamboo Rest API
	public static String BUILD_SUCCESSFUL = "Successful";
	public static String BUILD_FAILED = "Failed";

	protected void setUp() throws Exception {
		super.setUp();
		bambooPluginConfigurationForm = new BambooServerConfigForm();
	}

	/*    public void testDummyFail(){
			fail();

		}
	 */
	public void testBambooSetGetData() throws Exception {
		assertNotNull(bambooPluginConfigurationForm.getRootComponent());

		ServerBean inServerBean = createServerBean();
		ServerBean outServerBean = null;

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean) bambooPluginConfigurationForm.getData();

		// form use cloned instance
		assertNotSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
		assertEquals(0, outServerBean.getSubscribedPlansData().size());

		/*  */

		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-1");
			}
		});

		bambooPluginConfigurationForm.setData(inServerBean);
		outServerBean = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		assertEquals(1, outServerBean.getSubscribedPlansData().size());
		assertEquals("Plan-1", outServerBean.getSubscribedPlansData().get(0).getPlanId());

		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-2");
			}
		});

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		assertEquals(2, outServerBean.getSubscribedPlansData().size());
		checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2" });
		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-3");
			}
		});

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);

		assertEquals(3, outServerBean.getSubscribedPlansData().size());
		checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2", "Plan-3" });

		/*  */
		inServerBean.getSubscribedPlansData().clear();

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean) bambooPluginConfigurationForm.getData();

		checkServerBean(outServerBean);

		assertEquals(0, outServerBean.getSubscribedPlansData().size());

	}

	@SuppressWarnings({ "RedundantStringConstructorCall" })
	public void testBambooFormIsModified() throws Exception {
		ServerBean inServerBean = createServerBean();

		bambooPluginConfigurationForm.setData(inServerBean);

		ServerBean outServerBean = createServerBean();

		assertFalse(bambooPluginConfigurationForm.isModified());

		/* with arraylist set */

	
		outServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-1");
			}
		});

		bambooPluginConfigurationForm.setData(outServerBean);
		assertFalse(bambooPluginConfigurationForm.isModified());

		/* equals vs == */

		outServerBean.setName(new String("name"));
		outServerBean.setPasswordString(new String("password"), true);
		outServerBean.setUrlString(new String("url"));
		outServerBean.setUserName(new String("userName"));
		outServerBean.setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
		outServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId(new String("Plan-1"));
			}
		});


		bambooPluginConfigurationForm.setData(outServerBean);
		assertFalse(bambooPluginConfigurationForm.isModified());

		PluginConfigurationFormHelper formHelper = new PluginConfigurationFormHelper(bambooPluginConfigurationForm);

		formHelper.serverName.setText(outServerBean.getName() + "-chg");
		assertTrue(bambooPluginConfigurationForm.isModified());
		formHelper.serverName.setText(outServerBean.getName());

		formHelper.serverUrl.setText(outServerBean.getUrlString() + "-chg");
		assertTrue(bambooPluginConfigurationForm.isModified());
		formHelper.serverUrl.setText(outServerBean.getUrlString());

		formHelper.username.setText(outServerBean.getUserName() + "-chg");
		assertTrue(bambooPluginConfigurationForm.isModified());
		formHelper.username.setText(outServerBean.getUserName());


		formHelper.password.setText(outServerBean.getName() + "-chg");
		assertTrue(bambooPluginConfigurationForm.isModified());
		formHelper.password.setText(outServerBean.getPasswordString());


		formHelper.buildPlansTextArea.setText("-chg");
		assertTrue(bambooPluginConfigurationForm.isModified());
	}

	public void testBambooFormFieldSetting() throws Exception {
		bambooPluginConfigurationForm.setData(new ServerBean());

		ServerBean outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		assertEquals("", outServer.getName());
		assertEquals("", outServer.getUrlString());
		assertEquals("", outServer.getUserName());
		assertEquals("", outServer.getPasswordString());
		assertEquals(0, outServer.getSubscribedPlansData().size());

		PluginConfigurationFormHelper helper = new PluginConfigurationFormHelper(bambooPluginConfigurationForm);

		helper.serverName.setText("name");
		helper.password.setText("password");
		helper.serverUrl.setText("url");
		helper.username.setText("userName");

		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" ");
		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" \n");
		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" \n\r\r\r\n \n \r \t");
		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);

		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText("Plan-1");
		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		checkSubscribedPlans(outServer, new String[]{ "Plan-1" });

		/*  */
		helper.buildPlansTextArea.setText(" Plan-1 \n");
		outServer =  (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		checkSubscribedPlans(outServer, new String[]{ "Plan-1" });

		/*  */
		helper.buildPlansTextArea.setText(" Plan-1 \nPlan-2   Plan-3\tPlan-4\n\rPlan-5\r\nPlan-6");
		outServer = (ServerBean) bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		checkSubscribedPlans(outServer, new String[]{ "Plan-1", "Plan-2", "Plan-3", "Plan-4", "Plan-5", "Plan-6" });

	}

	private static void checkSubscribedPlans(ServerBean server, String[] ids) {
		assertEquals(ids.length, server.getSubscribedPlansData().size());

		Iterator<SubscribedPlanBean> i = server.getSubscribedPlansData().iterator();
		for (String id : ids) {
			assertEquals(id, i.next().getPlanId());
		}

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
		return new TestSuite(BambooServerConfigurationFormTest.class);
	}




	private class PluginConfigurationFormHelper {
		public JPanel rootComponent;
		public JTextField serverName;
		public JTextField serverUrl;
		public JTextField username;
		public JPasswordField password;
		public JButton testConnection;
		public JTextArea buildPlansTextArea;

		public PluginConfigurationFormHelper(BambooServerConfigForm pluginConfigurationForm) throws Exception {
			for (Field f : getClass().getFields()) {
				String name = f.getName();
				Field original = pluginConfigurationForm.getClass().getDeclaredField(name);
				original.setAccessible(true);

				f.set(this, original.get(pluginConfigurationForm));
				System.out.println("Copied field " + original.getName());

			}
		}
	}


}
