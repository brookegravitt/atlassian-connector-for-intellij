package com.atlassian.theplugin.idea;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;
import com.atlassian.theplugin.configuration.ServerBean;

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

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
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        assertEquals("Plan-2", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(1).getPlanId());

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-3");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        checkBasicBean(outBean);
        assertEquals(3, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        assertEquals("Plan-2", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(1).getPlanId());
        assertEquals("Plan-3", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(2).getPlanId());

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().clear();

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

        checkBasicBean(outBean);
        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

    }

    public void testIsModified() throws Exception {
        PluginConfigurationBean inBean = createBasicBean();

        pluginConfigurationForm.setData(inBean);

        PluginConfigurationBean outBean = new PluginConfigurationBean();

        outBean = createBasicBean();

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
}
