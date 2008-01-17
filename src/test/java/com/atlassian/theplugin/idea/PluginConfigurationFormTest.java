package com.atlassian.theplugin.idea;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.configuration.SubscribedPlanBean;

import java.beans.BeanInfo;

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

        PluginConfigurationBean inBean = new PluginConfigurationBean();
        inBean.getBambooConfigurationData().getServerData().setName("name");
        inBean.getBambooConfigurationData().getServerData().setPassword("password");
        inBean.getBambooConfigurationData().getServerData().setUrlString("url");
        inBean.getBambooConfigurationData().getServerData().setUsername("userName");

        pluginConfigurationForm.setData(inBean);

        PluginConfigurationBean outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);

        assertNotSame(inBean, outBean);
        assertEquals("name", outBean.getBambooConfigurationData().getServerData().getName());
        assertEquals("password", outBean.getBambooConfigurationData().getServerData().getPassword());
        assertEquals("url", outBean.getBambooConfigurationData().getServerData().getUrlString());
        assertEquals("userName", outBean.getBambooConfigurationData().getServerData().getUsername());
        assertEquals(0, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-1");}});

        pluginConfigurationForm.setData(inBean);
        
        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        assertEquals("name", outBean.getBambooConfigurationData().getServerData().getName());
        assertEquals("password", outBean.getBambooConfigurationData().getServerData().getPassword());
        assertEquals("url", outBean.getBambooConfigurationData().getServerData().getUrlString());
        assertEquals("userName", outBean.getBambooConfigurationData().getServerData().getUsername());
        assertEquals(1, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        
        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-2");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        assertEquals("name", outBean.getBambooConfigurationData().getServerData().getName());
        assertEquals("password", outBean.getBambooConfigurationData().getServerData().getPassword());
        assertEquals("url", outBean.getBambooConfigurationData().getServerData().getUrlString());
        assertEquals("userName", outBean.getBambooConfigurationData().getServerData().getUsername());
        assertEquals(2, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        assertEquals("Plan-2", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(1).getPlanId());

        /*  */
        inBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().add(new SubscribedPlanBean() {{setPlanId("Plan-3");}});

        pluginConfigurationForm.setData(inBean);

        outBean = new PluginConfigurationBean();
        pluginConfigurationForm.getData(outBean);
        assertEquals("name", outBean.getBambooConfigurationData().getServerData().getName());
        assertEquals("password", outBean.getBambooConfigurationData().getServerData().getPassword());
        assertEquals("url", outBean.getBambooConfigurationData().getServerData().getUrlString());
        assertEquals("userName", outBean.getBambooConfigurationData().getServerData().getUsername());
        assertEquals(3, outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().size());
        assertEquals("Plan-1", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(0).getPlanId());
        assertEquals("Plan-2", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(1).getPlanId());
        assertEquals("Plan-3", outBean.getBambooConfigurationData().getServerData().getSubscribedPlansData().get(2).getPlanId());

    }


    public static Test suite() {
        return new TestSuite(PluginConfigurationFormTest.class);
    }
}
