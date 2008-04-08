/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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