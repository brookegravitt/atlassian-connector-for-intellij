package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.labels.BoldLabel;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Based on: http://www.devx.com/tips/Tip/30077
 * Date: Jun 26, 2008
 * Time: 7:43:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginTrustManager implements X509TrustManager {
	private static Collection<String> alreadyRejectedCerts = new HashSet<String>();


	X509TrustManager X509TM = null;		   //default X.509 TrustManager
	TrustManagerFactory ClientTMF = null;   //SunX509 factory from SunJSSE provider
	KeyStore ClientKS = null;							   //keystore SSLCert - just an example

	TrustManager[] ClientTMs = null;				 //all the TrustManagers from SunX509 factory

	private static PluginTrustManager instance;
	private PluginConfiguration configuration;
	private Collection<String> certs;

	private PluginTrustManager(PluginConfiguration configuration) {
		this.configuration = configuration;
		//get an KeyStore object of type JKS (default type)
		try {
			ClientKS = KeyStore.getInstance("JKS");
		} catch (java.security.KeyStoreException e) {
		}

//		InputStream inStream = new FileInputStream("fileName-of-cert");
//		CertificateFactory cf = CertificateFactory.getInstance("X.509");
//		X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);

		//TrustManagerFactory of SunJSSE
		try {
			ClientTMF = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("5: " + e.getMessage());
		} catch (java.security.NoSuchProviderException e) {
		}

		//call init method for ClientTMF
		try {
			ClientTMF.init(ClientKS);
		} catch (java.security.KeyStoreException e) {
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
			X509TM.checkClientTrusted(chain, authType);
		} catch (java.security.cert.CertificateException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	//checkServerTrusted
	public void checkServerTrusted(final X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			X509TM.checkServerTrusted(chain, authType);
		} catch (CertificateException
				e) {

			String strCert = chain[0].toString();
			if (alreadyRejectedCerts.contains(strCert)) {
				throw e;
			}

			certs = configuration.getGeneralConfigurationData().getCerts();
			if (certs.contains(strCert)) {
				return; // already accepted
			}

			final boolean[] accepted = new boolean[]{false};
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						CertMessageDialog dialog = new CertMessageDialog("");
						for (X509Certificate cert : chain) {
							dialog.addCert(cert.toString());
						}
						dialog.show();
						if (dialog.isOK()) {
							accepted[0] = true;
						}
					}
				});
			} catch (InterruptedException e1) {
				// swallow
			} catch (InvocationTargetException e1) {
				// swallow
			}

			if (accepted[0] == true) {
				certs.add(strCert);

				ConfigurationFactory.getConfiguration().
						getGeneralConfigurationData().setCerts(certs);
			} else {
				synchronized (alreadyRejectedCerts) {
					alreadyRejectedCerts.add(strCert);
				}
				throw e;
			}
		}
	}

	//getAcceptedIssuers
	public X509Certificate[] getAcceptedIssuers() {
		return X509TM.getAcceptedIssuers();
	}

	public synchronized static PluginTrustManager getInstance(PluginConfiguration cofiguration) {
		return new PluginTrustManager(cofiguration);
	}

	private static class CertMessageDialog extends DialogWrapper {
		private String server;

		private class ScrollablePanel extends JPanel implements Scrollable {
			private static final int A_LOT = 100000;

			// cheating obviously but this seems to do the right thing, so whatever :)
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(1, A_LOT);
			}

			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 1;
			}

			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 1;
			}

			public boolean getScrollableTracksViewportWidth() {
				return true;
			}

			public boolean getScrollableTracksViewportHeight() {
				return false;
			}
		}

		private ScrollablePanel certInfo = new ScrollablePanel();

		protected CertMessageDialog(String server) {
			super(false);
			this.server = server;
			setTitle("Security Alert");
			setModal(true);

			init();
		}

		@Nullable
		protected JComponent createCenterPanel() {
			JPanel panel = new JPanel(new GridBagLayout());
			JScrollPane scroll = new JScrollPane();
			JLabel titleLabel = new BoldLabel("Server " + server + " uses an invalid security certificate.");

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			panel.add(titleLabel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			certInfo.setLayout(new VerticalFlowLayout());
			scroll.setViewportView(certInfo);
			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			panel.add(scroll, gbc);
			panel.setPreferredSize(new Dimension(800, 800));

			gbc.gridy = 2;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			JLabel bottomLabel = new BoldLabel("Do you want to add an exception and connect?");
			panel.add(bottomLabel, gbc);

			return panel;
		}

		public void addCert(String cert) {
			certInfo.add(new JLabel("<html><pre>" + cert));
		}

		protected String getOkActionName() {
			return "Accept";
		}

		protected String getCancelActionName() {
			return "Cancel";
		}

		protected boolean isToBeShown() {
			return false;  //To change body of implemented methods use File | Settings | File Templates.
		}

		protected void setToBeShown(boolean b, boolean b1) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		protected boolean shouldSaveOptionsOnCancel() {
			return false;  //To change body of implemented methods use File | Settings | File Templates.
		}
	}
}
