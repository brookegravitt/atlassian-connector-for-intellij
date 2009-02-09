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

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.thirdparty.apache.EasySSLProtocolSocketFactory;
import org.apache.axis.AxisProperties;
import org.apache.axis.components.net.*;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.StringUtils;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

public class PluginSSLProtocolSocketFactory extends EasySSLProtocolSocketFactory implements SecureSocketFactory {
	private X509TrustManager trustManager;
	private static final int DEFAULT_PROXY_PORT = 80;

	public PluginSSLProtocolSocketFactory(Hashtable attributes) {
		this();
	}

	public PluginSSLProtocolSocketFactory() {
		try {
			PluginConfiguration config = ConfigurationFactory.getConfiguration();
			trustManager = new PluginTrustManager(config.getGeneralConfigurationData());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
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
			e.printStackTrace();
		}
		AxisProperties.setClassDefault(SecureSocketFactory.class,
				PluginSSLProtocolSocketFactory.class.getCanonicalName());
	}

	/**
	 * Copied from AXIS source code,
	 * original @author Davanum Srinivas (dims@yahoo.com)
	 * THIS CODE STILL HAS DEPENDENCIES ON sun.* and com.sun.*
	 */
	public Socket create(final String host, final int port, final StringBuffer otherHeaders, final BooleanHolder useFullURL)
			throws Exception {

		int sslPort = port;
		if (port == -1) {
			sslPort = EasySSLProtocolSocketFactory.SSL_PORT;
		}

		TransportClientProperties tcp = TransportClientPropertiesFactory.create("https");

		//boolean hostInNonProxyList = super.isHostInNonPxyList(host, tcp.getNonProxyHosts());

		Socket sslSocket;
		if (tcp.getProxyHost().length() == 0) { // || hostInNonProxyList) {
			// direct SSL connection
			sslSocket = super.createSocket(host, sslPort);
		} else {
			int tunnelPort = (tcp.getProxyPort().length() != 0)
					? Integer.parseInt(tcp.getProxyPort())
					: DEFAULT_PROXY_PORT;
			if (tunnelPort < 0) {
				tunnelPort = DEFAULT_PROXY_PORT;
			}

			// Create the regular socket connection to the proxy
			Socket tunnel = new Socket(tcp.getProxyHost(), tunnelPort);

			// The tunnel handshake method (condensed and made reflexive)
			OutputStream tunnelOutputStream = tunnel.getOutputStream();
			PrintWriter out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(tunnelOutputStream)));

			// More secure version... engage later?
			// PasswordAuthentication pa =
			// Authenticator.requestPasswordAuthentication(
			// InetAddress.getByName(tunnelHost),
			// tunnelPort, "SOCK", "Proxy","HTTP");
			// if(pa == null){
			// printDebug("No Authenticator set.");
			// }else{
			// printDebug("Using Authenticator.");
			// tunnelUser = pa.getUserName();
			// tunnelPassword = new String(pa.getPassword());
			// }
			out.print("CONNECT " + host + ":" + sslPort + " HTTP/1.0\r\n"
					+ "User-Agent: AxisClient");
			if (tcp.getProxyUser().length() != 0
					&& tcp.getProxyPassword().length() != 0) {

				// add basic authentication header for the proxy
				String encodedPassword = XMLUtils.base64encode((tcp.getProxyUser()
						+ ":"
						+ tcp.getProxyPassword()).getBytes());

				out.print("\nProxy-Authorization: Basic " + encodedPassword);
			}
			out.print("\nContent-Length: 0");
			out.print("\nPragma: no-cache");
			out.print("\r\n\r\n");
			out.flush();
			InputStream tunnelInputStream = tunnel.getInputStream();

			PluginUtil.getLogger().debug(Messages.getMessage("isNull00", "tunnelInputStream",
					"" + (tunnelInputStream
							== null)));
			String replyStr = "";

			// Make sure to read all the response from the proxy to prevent SSL negotiation failure
			// Response message terminated by two sequential newlines
			int newlinesSeen = 0;
			boolean headerDone = false;	/* Done on first newline */

			while (newlinesSeen < 2) {
				int i = tunnelInputStream.read();

				if (i < 0) {
					throw new IOException("Unexpected EOF from proxy");
				}
				if (i == '\n') {
					headerDone = true;
					++newlinesSeen;
				} else if (i != '\r') {
					newlinesSeen = 0;
					if (!headerDone) {
						replyStr += String.valueOf((char) i);
					}
				}
			}
			if (StringUtils.startsWithIgnoreWhitespaces("HTTP/1.0 200", replyStr)
					&& StringUtils.startsWithIgnoreWhitespaces("HTTP/1.1 200", replyStr)) {
				throw new IOException(Messages.getMessage("cantTunnel00",
						new String[]{
								tcp.getProxyHost(),
								"" + tunnelPort,
								replyStr}));
			}

			// End of condensed reflective tunnel handshake method
			sslSocket = super.createSocket(tunnel, host, port, true);

			PluginUtil.getLogger().debug(Messages.getMessage("setupTunnel00",
					tcp.getProxyHost(),
					"" + tunnelPort));

		}

		((SSLSocket) sslSocket).startHandshake();
		PluginUtil.getLogger().debug(Messages.getMessage("createdSSL00"));
		return sslSocket;
	}


}
