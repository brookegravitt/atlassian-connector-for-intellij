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

package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.idea.ui.CertMessageDialog;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Based on: http://www.devx.com/tips/Tip/30077
 * Date: Jun 26, 2008
 * Time: 7:43:30 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PluginTrustManager implements X509TrustManager {
	private static Collection<String> alreadyRejectedCerts = new HashSet<String>();
	private static Collection<String> temporarilyAcceptedCerts =
			Collections.synchronizedCollection(new HashSet<String>());

	private Collection<String> aceptedCerts;

	private PluginConfiguration configuration;
	private X509TrustManager standardTrustManager;

	public PluginTrustManager(PluginConfiguration configuration) throws NoSuchAlgorithmException, KeyStoreException {
		this.configuration = configuration;
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init((KeyStore) null);
		TrustManager[] trustmanagers = factory.getTrustManagers();
		if (trustmanagers.length == 0) {
			throw new NoSuchAlgorithmException("no trust manager found");
		}

		//looking for a X509TrustManager instance
		for (int i = 0; i < trustmanagers.length; i++) {
			if (trustmanagers[i] instanceof X509TrustManager) {
				standardTrustManager = (X509TrustManager) trustmanagers[i];
				return;
			}
		}

	}

	//checkClientTrusted
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		standardTrustManager.checkClientTrusted(chain, authType);
	}


	private boolean isSelfSigned(X509Certificate certificate) {
		return certificate.getSubjectDN().equals(certificate.getIssuerDN());
	}

	//checkServerTrusted
	public void checkServerTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
		try {
			standardTrustManager.checkServerTrusted(chain, authType);
		} catch (final CertificateException
				e) {

			String strCert = chain[0].toString();
			if (alreadyRejectedCerts.contains(strCert)) {
				throw e;
			}

			if (checkChain(chain, configuration.getGeneralConfigurationData().getCerts())
					||
					checkChain(chain, temporarilyAcceptedCerts)) {
				return;
			}

			String message = e.getMessage();
			message = message.substring(message.lastIndexOf(":") + 1);
			if (isSelfSigned(chain[0])) {
				message = "Self-signed certificate";
			}
			try {
				chain[0].checkValidity();
			} catch (CertificateExpiredException e1) {
				message = "Certificate expired";
			} catch (CertificateNotYetValidException e1) {
				message = "Certificate not yet valid";
			}
			final String server =
					AbstractHttpSession.getServerNameFromUrl(AbstractHttpSession.getUrl());

			// check if it should be accepted
			final int[] accepted = new int[]{0}; // 0 rejected 1 accepted temporarily 2 - accepted perm.
			synchronized (PluginTrustManager.class) {
				try {
					final String message1 = message;
					EventQueue.invokeAndWait(new Runnable() {
						public void run() {
							CertMessageDialog dialog = new CertMessageDialog(server, message1, chain);
							dialog.show();
							if (dialog.isOK()) {
								if (dialog.isTemporarily()) {
									accepted[0] = 1;
									return;
								}
								accepted[0] = 2;
								return;
							}
						}
					});
				} catch (InterruptedException e1) {
					// swallow
				} catch (InvocationTargetException e1) {
					// swallow
				}
			}

			switch (accepted[0]) {
				case 1:
					temporarilyAcceptedCerts.add(strCert);
					break;
				case 2:
					synchronized (configuration) {
						// taken once again because something could change in the state
						aceptedCerts = configuration.getGeneralConfigurationData().getCerts();
						aceptedCerts.add(strCert);
						configuration.
								getGeneralConfigurationData().setCerts(aceptedCerts);
					}
					break;
				default:
					synchronized (alreadyRejectedCerts) {
						alreadyRejectedCerts.add(strCert);
					}
					throw e;
			}
		}
	}

	private boolean checkChain(X509Certificate[] chain, Collection<String> certs) {
		for (X509Certificate cert : chain) {
			if (certs.contains(cert.toString())) {
				return true;
			}
		}
		return false;
	}

	//getAcceptedIssuers
	public X509Certificate[] getAcceptedIssuers() {
		return standardTrustManager.getAcceptedIssuers();
	}

}
