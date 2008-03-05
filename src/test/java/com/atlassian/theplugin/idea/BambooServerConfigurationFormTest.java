package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.idea.config.serverconfig.BambooServerConfigForm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
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

	private BambooServerConfigForm bambooPluginConfigurationForm;
	//statuses as strings returned by bamboo Rest API
	public static final String BUILD_SUCCESSFUL = "Successful";
	public static final String BUILD_FAILED = "Failed";

	protected void setUp() throws Exception {
		super.setUp();
		bambooPluginConfigurationForm = new BambooServerConfigForm(new BambooServerFacadeImpl());
	}

	/*    public void testDummyFail(){
			fail();

		}
	 */
	public void testBambooSetGetData() throws Exception {
		assertNotNull(bambooPluginConfigurationForm.getRootComponent());

		ServerBean inServerBean = createServerBean();

		bambooPluginConfigurationForm.setData(inServerBean);


		ServerBean outServerBean = bambooPluginConfigurationForm.getData();

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
		outServerBean = bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		//@todo enable again		
		//assertEquals(1, outServerBean.getSubscribedPlansData().size());
		//assertEquals("Plan-1", outServerBean.getSubscribedPlansData().get(0).getPlanId());

		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-2");
			}
		});

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		//assertEquals(2, outServerBean.getSubscribedPlansData().size());
		//checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2" });
		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-3");
			}
		});

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getData();
		checkServerBean(outServerBean);

		//assertEquals(3, outServerBean.getSubscribedPlansData().size());
		//checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2", "Plan-3" });

		/*  */
		inServerBean.getSubscribedPlansData().clear();

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getData();

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
	}

	public void testBambooFormFieldSetting() throws Exception {
		bambooPluginConfigurationForm.setData(new ServerBean());

		ServerBean outServer = bambooPluginConfigurationForm.getData();
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

		outServer = bambooPluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());
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
		assertEquals("http://url", outServer.getUrlString());
		assertEquals("userName", outServer.getUserName());
	}

	public static Test suite() {
		return new TestSuite(BambooServerConfigurationFormTest.class);
	}




	private class PluginConfigurationFormHelper extends PrivateFieldMapper {
		public JPanel rootComponent;
		public JTextField serverName;
		public JTextField serverUrl;
		public JTextField username;
		public JPasswordField password;
		public JButton testConnection;

		public PluginConfigurationFormHelper(BambooServerConfigForm pluginConfigurationForm) throws Exception {
			super(pluginConfigurationForm);
		}
	}
}
