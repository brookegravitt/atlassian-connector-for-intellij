package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.*;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.idea.config.serverconfig.BambooServerConfigForm;
import com.atlassian.theplugin.idea.bamboo.BambooStatusIcon;
import com.intellij.openapi.util.IconLoader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * BambooServerConfigForm Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/17/2008</pre>
 */
public class PluginConfigurationFormTest extends TestCase {

	BambooServerConfigForm pluginConfigurationForm;
	//statuses as strings returned by bamboo Rest API
	public static String BUILD_SUCCESSFUL = "Successful";
	public static String BUILD_FAILED = "Failed";

	protected void setUp() throws Exception {
		super.setUp();
		pluginConfigurationForm = new BambooServerConfigForm();
	}

	/*    public void testDummyFail(){
			fail();

		}
	 */
	public void testSetGetData() throws Exception {
		assertNotNull(pluginConfigurationForm.getRootComponent());

		ServerBean inServerBean = createServerBean();
		ServerBean outServerBean = null;

		pluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean)pluginConfigurationForm.getData();

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

		pluginConfigurationForm.setData(inServerBean);
		outServerBean = (ServerBean) pluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		assertEquals(1, outServerBean.getSubscribedPlansData().size());
		assertEquals("Plan-1", outServerBean.getSubscribedPlansData().get(0).getPlanId());

		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-2");
			}
		});

		pluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServerBean);
		assertEquals(2, outServerBean.getSubscribedPlansData().size());
		checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2" });
		/*  */
		inServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-3");
			}
		});

		pluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServerBean);

		assertEquals(3, outServerBean.getSubscribedPlansData().size());
		checkSubscribedPlans(outServerBean, new String[]{ "Plan-1", "Plan-2", "Plan-3" });

		/*  */
		inServerBean.getSubscribedPlansData().clear();

		pluginConfigurationForm.setData(inServerBean);


		outServerBean = (ServerBean)pluginConfigurationForm.getData();

		checkServerBean(outServerBean);

		assertEquals(0, outServerBean.getSubscribedPlansData().size());

	}

	@SuppressWarnings({ "RedundantStringConstructorCall" })
	public void testIsModified() throws Exception {
		ServerBean inServerBean = createServerBean();

		pluginConfigurationForm.setData(inServerBean);

		ServerBean outServerBean = createServerBean();

		assertFalse(pluginConfigurationForm.isModified());

		/* with arraylist set */

	
		outServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId("Plan-1");
			}
		});

		pluginConfigurationForm.setData(outServerBean);
		assertFalse(pluginConfigurationForm.isModified());

		/* equals vs == */

		outServerBean.setName(new String("name"));
		outServerBean.setPasswordString(new String("password"), true);
		outServerBean.setUrlString(new String("url"));
		outServerBean.setUsername(new String("userName"));
		outServerBean.setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
		outServerBean.getSubscribedPlansData().add(new SubscribedPlanBean() {
			{
				setPlanId(new String("Plan-1"));
			}
		});


		pluginConfigurationForm.setData(outServerBean);
		assertFalse(pluginConfigurationForm.isModified());

		PluginConfigurationFormHelper formHelper = new PluginConfigurationFormHelper(pluginConfigurationForm);

		formHelper.serverName.setText(outServerBean.getName() + "-chg");
		assertTrue(pluginConfigurationForm.isModified());
		formHelper.serverName.setText(outServerBean.getName());

		formHelper.serverUrl.setText(outServerBean.getUrlString() + "-chg");
		assertTrue(pluginConfigurationForm.isModified());
		formHelper.serverUrl.setText(outServerBean.getUrlString());

		formHelper.username.setText(outServerBean.getUsername() + "-chg");
		assertTrue(pluginConfigurationForm.isModified());
		formHelper.username.setText(outServerBean.getUsername());


		formHelper.password.setText(outServerBean.getName() + "-chg");
		assertTrue(pluginConfigurationForm.isModified());
		formHelper.password.setText(outServerBean.getPasswordString());


		formHelper.buildPlansTextArea.setText("-chg");
		assertTrue(pluginConfigurationForm.isModified());				
	}

	public void testFieldSetting() throws Exception {
		pluginConfigurationForm.setData(new ServerBean());

		ServerBean outServer = (ServerBean)pluginConfigurationForm.getData();
		assertEquals("", outServer.getName());
		assertEquals("", outServer.getUrlString());
		assertEquals("", outServer.getUsername());
		assertEquals("", outServer.getPasswordString());
		assertEquals(0, outServer.getSubscribedPlansData().size());

		PluginConfigurationFormHelper helper = new PluginConfigurationFormHelper(pluginConfigurationForm);

		helper.serverName.setText("name");
		helper.password.setText("password");
		helper.serverUrl.setText("url");
		helper.username.setText("userName");

		outServer = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" ");
		outServer = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" \n");
		outServer = (ServerBean) pluginConfigurationForm.getData();
		checkServerBean(outServer);
		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText(" \n\r\r\r\n \n \r \t");
		outServer = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServer);

		assertEquals(0, outServer.getSubscribedPlansData().size());

		/*  */
		helper.buildPlansTextArea.setText("Plan-1");
		outServer = (ServerBean)pluginConfigurationForm.getData();
		checkServerBean(outServer);
		checkSubscribedPlans(outServer, new String[]{ "Plan-1" });

		/*  */
		helper.buildPlansTextArea.setText(" Plan-1 \n");
		outServer =  (ServerBean) pluginConfigurationForm.getData();
		checkServerBean(outServer);
		checkSubscribedPlans(outServer, new String[]{ "Plan-1" });

		/*  */
		helper.buildPlansTextArea.setText(" Plan-1 \nPlan-2   Plan-3\tPlan-4\n\rPlan-5\r\nPlan-6");
		outServer = (ServerBean) pluginConfigurationForm.getData();
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
		outServer.setUsername("userName");


		return outServer;
	}

	private static void checkServerBean(ServerBean outServer) throws ServerPasswordNotProvidedException {

		assertEquals("name", outServer.getName());
		assertEquals("password", outServer.getPasswordString());
		assertEquals("url", outServer.getUrlString());
		assertEquals("userName", outServer.getUsername());
	}

	public static Test suite() {
		return new TestSuite(PluginConfigurationFormTest.class);
	}

	// @todo restore test
	public void StatusListenerAlgorithm() {


		BambooStatusIconHelper statusIcon = new BambooStatusIconHelper();
		Collection<BambooBuild> buildStatuses = new ArrayList<BambooBuild>();

		//add crap as build status
		BambooBuild bambooBuild = new BambooBuildInfo("projectName", "buildNameSuccess", "buildKey", BUILD_SUCCESSFUL, "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");


		buildStatuses.add(bambooBuild);
		BambooStatusListener statusListener = new HtmlBambooStatusListener(statusIcon);

		statusListener.updateBuildStatuses(buildStatuses);
		assertEquals(BuildStatus.BUILD_SUCCEED, statusIcon.getBuildStatus());

		bambooBuild = new BambooBuildInfo("projectName", "buildNameCrap", "buildKey", "CRAPSTATUS", "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertEquals(BuildStatus.UNKNOWN, statusIcon.getBuildStatus());

		bambooBuild = new BambooBuildInfo("projectName", "buildNameFailed", "buildKey", BuildStatus.BUILD_FAILED.toString(), "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertEquals(BuildStatus.BUILD_FAILED, statusIcon.getBuildStatus());

		bambooBuild = new BambooBuildInfo("projectName", "buildNameFailed2", "buildKey", BuildStatus.BUILD_FAILED.toString(), "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertEquals(BuildStatus.BUILD_FAILED, statusIcon.getBuildStatus());

		assertEquals("<html><body>" +
				"<table>" +
				"<tr><td>buildKey</td><td><font color=\"green\">success</font></td></tr>" +
				"<tr><td>buildKey</td><td><font color=\"ltgray\">null</font></td></tr>" +
				"<tr><td>buildKey</td><td><font color=\"red\">build failed</font></td></tr>" +
				"<tr><td>buildKey</td><td><font color=\"red\">build failed</font></td></tr>" +
				"</table>" +
				"</body></html>", statusIcon.getFullInfo());
	}


	// @todo restore test
	public void BambooStatusIcon() {
		BambooStatusIcon statusIcon = new BambooStatusIcon(null);
		Collection<BambooBuild> buildStatuses = new ArrayList<BambooBuild>();

		//add crap as build status
		BambooBuild bambooBuild = new BambooBuildInfo("projectName", "buildNameSuccess", "buildKey", BUILD_SUCCESSFUL, "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");

		buildStatuses.add(bambooBuild);
		BambooStatusListener statusListener = new HtmlBambooStatusListener(statusIcon);

		statusListener.updateBuildStatuses(buildStatuses);
		assertTrue(statusIcon.getIcon().equals(IconLoader.getIcon("/icons/green-16.png")));

		bambooBuild = new BambooBuildInfo("projectName", "buildNameCrap", "buildKey", "CRAPSTATUS", "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertTrue(statusIcon.getIcon().equals(IconLoader.getIcon("/icons/grey-16.png")));

		bambooBuild = new BambooBuildInfo("projectName", "buildNameFailed", "buildKey", BuildStatus.BUILD_FAILED.toString(), "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertTrue(statusIcon.getIcon().equals(IconLoader.getIcon("/icons/red-16.png")));

		bambooBuild = new BambooBuildInfo("projectName", "buildNameFailed2", "buildKey", BuildStatus.BUILD_FAILED.toString(), "buildNumber", "buildReason",
				"buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");
		buildStatuses.add(bambooBuild);
		statusListener.updateBuildStatuses(buildStatuses);
		assertTrue(statusIcon.getIcon().equals(IconLoader.getIcon("/icons/red-16.png")));

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

	private class BambooStatusIconHelper implements BambooStatusDisplay {
		private BuildStatus status;
		private String fullInfo;

		public void updateBambooStatus(BuildStatus status, String fullInfo) {

			this.status = status;
			this.fullInfo = fullInfo;
		}

		public String getFullInfo() {
			return fullInfo;
		}

		public BuildStatus getBuildStatus() {
			return status;
		}

	}
}
