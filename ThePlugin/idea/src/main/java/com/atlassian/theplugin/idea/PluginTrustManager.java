package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.KeyStore;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 25, 2008
 * Time: 5:17:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginTrustManager implements X509TrustManager {

	X509TrustManager X509TM = null;		  //default X.509 TrustManager
	TrustManagerFactory ClientTMF = null;	//SunX509 factory from SunJSSE provider
	KeyStore ClientKS = null;				//keystore SSLCert - just an example

	TrustManager[] ClientTMs = null;		 //all the TrustManagers from SunX509 factory

	char[] ClientKeystorePassword = "Varonmykey".toCharArray();//SSLCert access password
	private static PluginTrustManager instance;
	private PluginConfiguration configuration;

	private PluginTrustManager(PluginConfiguration configuration) {
		this.configuration = configuration;
		//get an KeyStore object of type JKS (default type)
		try {
			ClientKS = KeyStore.getInstance("JKS");
		} catch (java.security.KeyStoreException e) {
			System.out.println("1: " + e.getMessage());
		}

		//loading SSLCert keystore
		try {
			ClientKS.load(new FileInputStream("SSLKeystore"), ClientKeystorePassword);
		} catch (java.io.IOException e) {
			System.out.println("2: " + e.getMessage());
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("3: " + e.getMessage());
		} catch (java.security.cert.CertificateException e) {
			System.out.println("4: " + e.getMessage());
		}

		//TrustManagerFactory of SunJSSE
		try {
			ClientTMF = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("5: " + e.getMessage());
		} catch (java.security.NoSuchProviderException e) {
			System.out.println("6: " + e.getMessage());
		}

		//call init method for ClientTMF
		try {
			ClientTMF.init(ClientKS);
		} catch (java.security.KeyStoreException e) {
			System.out.println("7: " + e.getMessage());
		}

		//get all the TrustManagers
		ClientTMs = ClientTMF.getTrustManagers();

		//looking for a X509TrustManager instance
		for (int i = 0; i < ClientTMs.length; i++) {
			if (ClientTMs[i] instanceof X509TrustManager) {
				System.out.println("X509TrustManager certificate found...");
				X509TM = (X509TrustManager) ClientTMs[i];
				return;
			}
		}
	}

	//checkClientTrusted
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			System.out.println("Verify-client...");
			X509TM.checkClientTrusted(chain, authType);
		} catch (CertificateException e) {
			System.out.println("I:  " + e.getMessage());
			//X509TrustManagerDialog valid = new X509TrustManagerDialog();
		}
	}

	//checkServerTrusted
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			System.out.println("Verify-server...");

			//ask the user what to do ?
			X509TM.checkServerTrusted(chain, authType);
		} catch (CertificateException
				e) {
			System.out.println("II:  " + e.getMessage());

			//ask the user what to do ?
			//X509TrustManagerDialog valid = new X509TrustManagerDialog();
		}
	}

	//getAcceptedIssuers
	public X509Certificate[] getAcceptedIssuers() {
		return X509TM.getAcceptedIssuers();
	}

	public synchronized static PluginTrustManager getInstance(PluginConfiguration configuration) {
		if (instance == null) {
			instance = new PluginTrustManager(configuration);
		}
		return instance;
	}

}
