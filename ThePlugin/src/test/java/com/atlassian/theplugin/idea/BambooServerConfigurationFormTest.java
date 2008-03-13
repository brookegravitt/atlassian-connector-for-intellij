package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.config.serverconfig.BambooServerConfigForm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
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


		Server outServerBean = bambooPluginConfigurationForm.getData();

		// form use cloned instance
		assertNotSame(inServerBean, outServerBean);
		checkServerBean(outServerBean);
		assertEquals(0, outServerBean.getSubscribedPlans().size());

		/*  */

		inServerBean.getSubscribedPlans().add(new SubscribedPlanBean() {
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
		inServerBean.getSubscribedPlans().add(new SubscribedPlanBean() {
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
		inServerBean.getSubscribedPlans().add(new SubscribedPlanBean() {
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
		inServerBean.getSubscribedPlans().clear();

		bambooPluginConfigurationForm.setData(inServerBean);


		outServerBean = bambooPluginConfigurationForm.getData();

		checkServerBean(outServerBean);

		assertEquals(0, outServerBean.getSubscribedPlans().size());

	}


	private static void checkSubscribedPlans(ServerBean server, String[] ids) {
		assertEquals(ids.length, server.getSubscribedPlans().size());

		Iterator<SubscribedPlan> i = server.getSubscribedPlans().iterator();
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

	private static void checkServerBean(Server outServer) throws ServerPasswordNotProvidedException {

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
