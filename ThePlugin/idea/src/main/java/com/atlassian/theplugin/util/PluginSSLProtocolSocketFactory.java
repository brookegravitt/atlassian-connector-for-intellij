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
/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 19, 2008
 */
package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.thirdparty.apache.EasySSLProtocolSocketFactory;

import javax.net.ssl.X509TrustManager;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.net.Socket;
import java.util.Hashtable;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.axis.components.net.SocketFactoryFactory;
import org.apache.axis.components.net.SecureSocketFactory;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.AxisProperties;

public class PluginSSLProtocolSocketFactory extends EasySSLProtocolSocketFactory implements SecureSocketFactory {
	private X509TrustManager trustManager;

	public PluginSSLProtocolSocketFactory(Hashtable attributes) {
		this();		
	}

	public PluginSSLProtocolSocketFactory() {
		try {
			PluginConfiguration config = ConfigurationFactory.getConfiguration();
			trustManager = new PluginTrustManager(config);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (KeyStoreException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		//To change body of created methods use File | Settings | File Templates.
	}

	@Override
	protected X509TrustManager getTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
		return trustManager;
	}

	public static void initializeSocketFactory() {
		Protocol.registerProtocol("https", new Protocol(
				"https", (ProtocolSocketFactory) new PluginSSLProtocolSocketFactory(),
				EasySSLProtocolSocketFactory.SSL_PORT));
		try {
			Class.forName(SocketFactoryFactory.class.getCanonicalName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		AxisProperties.setClassDefault(SecureSocketFactory.class,
				PluginSSLProtocolSocketFactory.class.getCanonicalName());
	}

	public Socket create(final String host, final int port, final StringBuffer otherHeaders, final BooleanHolder useFullURL)
			throws Exception {
		int sslPort = port;
		if (port == port) {
			sslPort = EasySSLProtocolSocketFactory.SSL_PORT;
		}
		return createSocket(host, sslPort);
	}
}
