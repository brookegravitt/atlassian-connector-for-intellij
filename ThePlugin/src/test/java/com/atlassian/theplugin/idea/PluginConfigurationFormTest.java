package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.bamboo.BambooStatusListenerImpl;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
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
 * PluginConfigurationForm Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/17/2008</pre>
 */
public class PluginConfigurationFormTest extends TestCase {

    PluginConfigurationForm pluginConfigurationForm;
    //statuses as strings returned by bamboo Rest API
    public static String BUILD_SUCCESSFUL = "Successful";
    public static String BUILD_FAILED = "Failed";

    protected void setUp() throws Exception {
        super.setUp();
        pluginConfigurationForm = new PluginConfigurationForm();
    }

/*    public void testDummyFail(){
        fail();

    }
 */
    public void testSetGetData() throws Exception {
        assertNotNull(pluginConfigurationForm.getRootComponent());

        PluginConfigurationBean inBean = createBasicBean();

        pluginConfigurationForm.setData(inBean);

        PluginConfigurationBean outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

        assertNotSame(inBean, outBean);
        checkBasicBean(outBean);
		ServerBean outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(0, outServer.getSubscribedPlansData().size());

        /*  */
		ServerBean inServer = inBean.getBambooConfigurationData().getServersData().iterator().next();
		inServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        pluginConfigurationForm.setData(inBean);
        //@todo fix this test after GUI redesign
        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(1, outServer.getSubscribedPlansData().size());
        assertEquals("Plan-1", outServer.getSubscribedPlansData().get(0).getPlanId());

        /*  */
        inServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-2");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(2, outServer.getSubscribedPlansData().size());
        checkSubscribedPlans(outBean, new String[]{"Plan-1", "Plan-2"});
        /*  */
        inServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-3");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(3, outServer.getSubscribedPlansData().size());
        checkSubscribedPlans(outBean, new String[]{"Plan-1", "Plan-2", "Plan-3"});

        /*  */
        inServer.getSubscribedPlansData().clear();

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(0, outServer.getSubscribedPlansData().size());

    }

    @SuppressWarnings({"RedundantStringConstructorCall"})
    public void testIsModified() throws Exception {
        PluginConfigurationBean inBean = createBasicBean();

        pluginConfigurationForm.setData(inBean);

        PluginConfigurationBean outBean = createBasicBean();

        assertFalse(pluginConfigurationForm.isModified(outBean));

        /* with arraylist set */
		ServerBean inServer = inBean.getBambooConfigurationData().getServersData().iterator().next();
		inServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        pluginConfigurationForm.setData(inBean);
        assertTrue(pluginConfigurationForm.isModified(outBean));
		ServerBean outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
        outServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        assertFalse(pluginConfigurationForm.isModified(outBean));

        /* equals vs == */

        outServer.setName(new String("name"));
        outServer.setPasswordString(new String("password"), true);
        outServer.setUrlString(new String("url"));
        outServer.setUsername(new String("userName"));
        outServer.setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
        outServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId(new String("Plan-1"));}});


        assertFalse(pluginConfigurationForm.isModified(outBean));


        for (Field f : outServer.getClass().getDeclaredFields()) {
            if (f.getType().equals(String.class)) {
                testForChangedString(outBean, f.getName());
            }
        }

        outServer.getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId(new String("Plan-2"));}});
        assertTrue(pluginConfigurationForm.isModified(outBean));

    }

    public void testFieldSetting() throws Exception {
        PluginConfigurationBean outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

		assertEquals(1, outBean.getBambooConfigurationData().getServersData().size());

		ServerBean outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
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

        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
        assertEquals(0, outServer.getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" ");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
        assertEquals(0, outServer.getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" \n");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
		outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
        assertEquals(0, outServer.getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" \n\r\r\r\n \n \r \t");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);

        assertEquals(0, outServer.getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText("Plan-1");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        checkSubscribedPlans(outBean, new String[]{"Plan-1"});

        /*  */
        helper.buildPlansTextArea.setText(" Plan-1 \n");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        checkSubscribedPlans(outBean, new String[]{"Plan-1"});

        /*  */
        helper.buildPlansTextArea.setText(" Plan-1 \nPlan-2   Plan-3\tPlan-4\n\rPlan-5\r\nPlan-6");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        checkSubscribedPlans(outBean, new String[]{"Plan-1", "Plan-2", "Plan-3", "Plan-4", "Plan-5", "Plan-6"});

    }

    private static void checkSubscribedPlans(PluginConfigurationBean config, String[] ids) {
		ServerBean server = config.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals(ids.length, server.getSubscribedPlansData().size());

        Iterator<SubscribedPlanBean> i = server.getSubscribedPlansData().iterator();
        for (String id : ids) {
            assertEquals(id, i.next().getPlanId());
        }

    }

    private void testForChangedString(PluginConfigurationBean outBean, String property) throws Exception {
        System.out.println("Testing field " + property);

        ServerBean serverBean = outBean.getBambooConfigurationData().getServersData().iterator().next();
        Field f = serverBean.getClass().getDeclaredField(property);
        if (f != null) {
            f.setAccessible(true);
            String prev = (String) f.get(serverBean);
            if(property.equals("encryptedPassword")) {
                serverBean.setPasswordString(prev + "-chg", true);
            } else {
                f.set(serverBean, prev + "-chg");
            }
            assertTrue(pluginConfigurationForm.isModified(outBean));
            f.set(serverBean, prev);
        }
    }

    private static PluginConfigurationBean createBasicBean() {
        PluginConfigurationBean outBean = new PluginConfigurationBean();
		ServerBean outServer = new ServerBean();
		outServer.setName("name");
        outServer.setPasswordString("password", true);
        outServer.setUrlString("url");
        outServer.setUsername("userName");

		outBean.getBambooConfigurationData().getServersData().add(outServer);
        return outBean;
    }

    private static void checkBasicBean(PluginConfigurationBean outBean) throws ServerPasswordNotProvidedException {
		ServerBean outServer = outBean.getBambooConfigurationData().getServersData().iterator().next();
		assertEquals("name", outServer.getName());
        assertEquals("password", outServer.getPasswordString());
        assertEquals("url", outServer.getUrlString());
        assertEquals("userName", outServer.getUsername());
    }

    public static Test suite() {
        return new TestSuite(PluginConfigurationFormTest.class);
    }

	// @todo restore test
	public void StatusListenerAlgorithm(){


        BambooStatusIconHelper statusIcon = new BambooStatusIconHelper();
        Collection<BambooBuild> buildStatuses = new ArrayList<BambooBuild>();

        //add crap as build status
        BambooBuild  bambooBuild = new BambooBuildInfo("projectName", "buildNameSuccess", "buildKey", BUILD_SUCCESSFUL, "buildNumber", "buildReason",
              "buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");



        buildStatuses.add(bambooBuild);
        BambooStatusListenerImpl statusListener = new BambooStatusListenerImpl(statusIcon);

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
	public void BambooStatusIcon(){
        BambooStatusIcon statusIcon = new BambooStatusIcon();
        Collection<BambooBuild> buildStatuses = new ArrayList<BambooBuild>();

        //add crap as build status
        BambooBuild  bambooBuild = new BambooBuildInfo("projectName", "buildNameSuccess", "buildKey", BUILD_SUCCESSFUL, "buildNumber", "buildReason",
              "buildRelativeBuildDate", "buildDurationDescription", "buildTestSummary");

        buildStatuses.add(bambooBuild);
        BambooStatusListenerImpl statusListener = new BambooStatusListenerImpl(statusIcon);

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

        public PluginConfigurationFormHelper(PluginConfigurationForm pluginConfigurationForm) throws Exception {
            for (Field f : getClass().getFields()) {
                String name = f.getName();
                Field original = pluginConfigurationForm.getClass().getDeclaredField(name);
                original.setAccessible(true);

                f.set(this, original.get(pluginConfigurationForm));
                System.out.println("Copied field " + original.getName());

            }
        }
    }

    private class BambooStatusIconHelper extends BambooStatusIcon {
        private BuildStatus status;
        private String fullInfo;

        public void updateBambooStatus(BuildStatus status, String fullInfo) {

                this.status = status;
                this.fullInfo = fullInfo;
        }

        public String getFullInfo(){
            return fullInfo;
        }
        public BuildStatus getBuildStatus(){
            return status;
        }

    }
}
