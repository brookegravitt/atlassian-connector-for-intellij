package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        pluginConfigurationForm.setData(inBean);
        
        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        assertEquals(1, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        
        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-2");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        assertEquals(2, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        checkSubscribedPlans(outBean, new String[]{"Plan-1", "Plan-2"});
        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-3");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        assertEquals(3, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        checkSubscribedPlans(outBean, new String[]{"Plan-1", "Plan-2", "Plan-3"});

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().clear();

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

        checkBasicBean(outBean);
        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

    }

    @SuppressWarnings({"RedundantStringConstructorCall"})
    public void testIsModified() throws Exception {
        PluginConfigurationBean inBean = createBasicBean();

        pluginConfigurationForm.setData(inBean);

        PluginConfigurationBean outBean = createBasicBean();

        assertFalse(pluginConfigurationForm.isModified(outBean));

        /* with arraylist set */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        pluginConfigurationForm.setData(inBean);
        assertTrue(pluginConfigurationForm.isModified(outBean));

        outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        assertFalse(pluginConfigurationForm.isModified(outBean));

        /* equals vs == */

        outBean.getBambooConfigurationData().getServerData().setName(new String("name"));
        outBean.getBambooConfigurationData().getServerData().setPassword(new String("password"));
        outBean.getBambooConfigurationData().getServerData().setUrlString(new String("url"));
        outBean.getBambooConfigurationData().getServerData().setUsername(new String("userName"));
        outBean.getBambooConfigurationData().getServerData().setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
        outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId(new String("Plan-1"));}});


        assertFalse(pluginConfigurationForm.isModified(outBean));


        for (Field f : outBean.getBambooConfigurationData().getServerData().getClass().getDeclaredFields()) {
            if (f.getType().equals(String.class)) {
                testForChangedString(outBean, f.getName());
            }
        }

        outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId(new String("Plan-2"));}});
        assertTrue(pluginConfigurationForm.isModified(outBean));

    }

    public void testFieldSetting() throws Exception {
        PluginConfigurationBean outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        ServerBean serverData = outBean.getBambooConfigurationData().getServerData();
        assertEquals("", serverData.getName());
        assertEquals("", serverData.getUrlString());
        assertEquals("", serverData.getUsername());
        assertEquals("", serverData.getPassword());
        assertEquals(0, serverData.getSubscribedPlansData().size());

        PluginConfigurationFormHelper helper = new PluginConfigurationFormHelper(pluginConfigurationForm);

        helper.serverName.setText("name");
        helper.password.setText("password");
        helper.serverUrl.setText("url");
        helper.username.setText("userName");

        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);

        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" ");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);

        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" \n");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);

        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        /*  */
        helper.buildPlansTextArea.setText(" \n\r\r\r\n \n \r \t");
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);

        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

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
        assertEquals(ids.length, config.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        Iterator<SubscribedPlanBean> i = config.getBambooConfigurationData().getServerData().getSubscribedPlansData().iterator();
        for (String id : ids) {
            assertEquals(id, i.next().getPlanId());
        }

    }

    private void testForChangedString(PluginConfigurationBean outBean, String property) throws Exception {
        System.out.println("Testing field " + property);
        
        ServerBean serverBean = outBean.getBambooConfigurationData().getServerData();
        Field f = serverBean.getClass().getDeclaredField(property);
        f.setAccessible(true);
        String prev = (String) f.get(serverBean);

        f.set(serverBean, prev + "-chg");
        assertTrue(pluginConfigurationForm.isModified(outBean));
        f.set(serverBean, prev);
    }

    private static PluginConfigurationBean createBasicBean() {
        PluginConfigurationBean outBean = new PluginConfigurationBean();

        outBean.getBambooConfigurationData().getServerData().setName("name");
        outBean.getBambooConfigurationData().getServerData().setPassword("password");
        outBean.getBambooConfigurationData().getServerData().setUrlString("url");
        outBean.getBambooConfigurationData().getServerData().setUsername("userName");

        return outBean;
    }

    private static void checkBasicBean(PluginConfigurationBean outBean) {
        assertEquals("name", outBean.getBambooConfigurationData().getServerData().getName());
        assertEquals("password", outBean.getBambooConfigurationData().getServerData().getPassword());
        assertEquals("url", outBean.getBambooConfigurationData().getServerData().getUrlString());
        assertEquals("userName", outBean.getBambooConfigurationData().getServerData().getUsername());
    }

    public static Test suite() {
        return new TestSuite(PluginConfigurationFormTest.class);
    }

    private class PluginConfigurationFormHelper {
        public JPanel rootComponent;
        public JTabbedPane tabbedPane1;
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
}
