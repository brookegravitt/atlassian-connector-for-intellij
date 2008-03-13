package com.atlassian.theplugin.jira.api;

import junit.framework.TestCase;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcHandler;

import java.util.Vector;

/**
 * Uses a simple XML-RPC server locally. See http://ws.apache.org/xmlrpc/xmlrpc2/server.html for details.
 */
public class JIRAXmlRpcClientTest extends TestCase {
    private WebServer webserver;

    protected void setUp() throws Exception {
        webserver = new WebServer(2095);
        webserver.start();
    }

    protected void tearDown() throws Exception
    {
        webserver.shutdown();
    }

    public void testLogin() throws Exception
    {
        webserver.addHandler ("jira1", new XmlRpcHandler() {
            public Object execute(String method, Vector params) throws Exception
            {
                if (method.equals("jira1.login"))
                {
                    if (params.get(0).equals("validusername") && params.get(1).equals("validpassword"))
                    {
                        return "acceptableToken";
                    }
                    else
                    {
                        return "";
                    }
                }

                throw new UnsupportedOperationException("Bad XML RPC method");
            }
        });

        JIRAXmlRpcClient client = new JIRAXmlRpcClient("http://localhost:2095");
        assertTrue(client.login("validusername", "validpassword"));
        assertEquals("acceptableToken", client.getToken());
        assertFalse(client.login("badusername", "badpassword"));
        assertEquals("", client.getToken());
    }
}