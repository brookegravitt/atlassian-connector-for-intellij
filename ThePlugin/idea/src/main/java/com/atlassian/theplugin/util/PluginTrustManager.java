package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.idea.ui.CollapsiblePanel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.labels.BoldLabel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.*;

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
	private static Collection<String> temporarilyAcceptedCerts =
			Collections.synchronizedCollection(new HashSet<String>());

	private Collection<String> aceptedCerts;

	private PluginConfiguration configuration;
	private X509TrustManager standardTrustManager;

	private PluginTrustManager(PluginConfiguration configuration) throws NoSuchAlgorithmException, KeyStoreException {
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

	public synchronized static PluginTrustManager getInstance(PluginConfiguration cofiguration) throws NoSuchAlgorithmException, KeyStoreException {
		return new PluginTrustManager(cofiguration);
	}

	private static class CertMessageDialog extends DialogWrapper {
		private String server;

		private X509Certificate[] chain;

		InfoPanel generalInfo = new InfoPanel();
		private CollapsiblePanel infoPanel = new CollapsiblePanel(false, false, "General Information");


		private JLabel details
				= new JLabel();

		JButton accept = new JButton("Accept");

		JButton acceptTmp = new JButton("Accept temporarily");

		JButton cancel = new JButton("Cancel");

		private boolean temporarily = false;
		private String message;


		protected CertMessageDialog(String serverName, String message, X509Certificate[] chain) {
			super(false);
			this.server = serverName;
			this.message = message;
			this.chain = chain;
			setTitle("Security Alert");
			setModal(true);
			accept.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					CertMessageDialog.this.close(JOptionPane.OK_OPTION);
				}
			});
			acceptTmp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					temporarily = true;
					CertMessageDialog.this.close(JOptionPane.OK_OPTION);
				}
			});
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					temporarily = true;
					CertMessageDialog.this.close(JOptionPane.CANCEL_OPTION);
				}
			});

			init();
		}

		public boolean isTemporarily() {
			return temporarily;
		}


		@Override
		@Nullable
		protected JComponent createSouthPanel() {
			ButtonBarBuilder builder = new ButtonBarBuilder();
			builder.addRelatedGap();
			builder.addGlue();
			builder.addGriddedButtons(new JButton[]{accept, acceptTmp, cancel});
			return builder.getPanel();
		}

		private ScrollablePanel certInfo = new ScrollablePanel();

		private Map<String, String> fields = new LinkedHashMap<String, String>() {
			{
				put("CN", "Common Name");
				put("O", "Organisation");
				put("OU", "Organisational Unit");
				put("C", "Country code");
				put("S", "State");
				put("ST", "State ");
				put("L", "Location");
				put("E", "E-mail adress");
			}
		};

		private void initCertInfo() {
			X509Certificate cert = chain[0];
			certInfo.add(infoPanel);
			infoPanel.setContent(generalInfo);
			infoPanel.collapse();  // workaround
			infoPanel.expand();
			generalInfo.addSeparator("Issued To");
			buildDNPanel(cert.getSubjectDN().getName(), generalInfo);
			generalInfo.addSeparator("Issued By");
			buildDNPanel(cert.getIssuerDN().getName(), generalInfo);
			generalInfo.addSeparator("Validity");
			generalInfo.addRow("From", cert.getNotBefore().toString());
			generalInfo.addRow("To", cert.getNotAfter().toString());
			generalInfo.addSeparator("Fingerprints");
			try {
				generalInfo.addRow("SHA1 fingerprint", getCertificateFingerprint("SHA1", cert));
				generalInfo.addRow("MD5 fingerprint", getCertificateFingerprint("MD5", cert));
			} catch (Exception e) {
				//
			}

			CollapsiblePanel detailsPanel = new CollapsiblePanel(true, true, "Details");
			details.setText("<html><pre>" + cert.toString());
			detailsPanel.setContent(details);
			certInfo.add(detailsPanel);
		}

		private String getCertificateFingerprint(String s, X509Certificate certificate)
				throws Exception {
			MessageDigest digest = MessageDigest.getInstance(s);
			byte fing[] = digest.digest(certificate.getEncoded());
			return toHexString(fing);
		}


		private String toHexString(byte bytes[]) {
			StringBuffer stringBuffer = new StringBuffer();
			int i = bytes.length;
			for (int j = 0; j < i; j++) {
				byte2hex(bytes[j], stringBuffer);
				if (j < i - 1)
					stringBuffer.append(":");
			}
			return stringBuffer.toString();
		}

		private static final String[] HEX_DIGITS = {"0", "1", "2", "3", "4",
				"5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

		private void byte2hex(byte b, StringBuffer stringbuffer) {
			int nb = b & 0xFF;
			int i_1 = (nb >> 4) & 0xF;
			int i_2 = nb & 0xF;
			stringbuffer.append(HEX_DIGITS[i_1] + HEX_DIGITS[i_2]);
		}


		private void buildDNPanel(String strDN, InfoPanel panel) {
			Map<String, String> fields;
			fields = parse(strDN);
			for (Map.Entry<String, String> field : this.fields.entrySet()) {
				String key = field.getKey();
				if (fields.containsKey(key)) {
					panel.addRow(this.fields.get(key) + " (" + key + ")", fields.get(key));
				}
			}
		}

		private Map<String, String> parse(String name) {
			Map<String, String> result = new HashMap<String, String>();
			String[] fields = name.split(",");
			for (String field : fields) {
				String[] parts = field.trim().split("=");
				result.put(parts[0], parts[1]);
			}
			return result;
		}


		@Nullable
		protected JComponent createCenterPanel() {
			JPanel panel = new JPanel(new GridBagLayout());
			JScrollPane scroll = new JScrollPane();


			JPanel msgPanel = new JPanel(new BorderLayout(20, 0));
			JLabel titleLabel = new BoldLabel("Server " + server
					+ " uses an invalid security certificate.");
			msgPanel.add(titleLabel, BorderLayout.NORTH);
			JPanel info = new JPanel();
			JLabel msgLabel = new BoldLabel(message);
			msgLabel.setForeground(Color.RED);
			info.add(msgLabel);
//			info.setBackground(Color.GRAY);
			msgPanel.add(info, BorderLayout.CENTER);
			JLabel qLabel = new BoldLabel("Do you want to accept it anyway?");
			msgPanel.add(qLabel, BorderLayout.SOUTH);
			msgPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			panel.add(msgPanel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			certInfo.setLayout(new VerticalFlowLayout());
			scroll.setViewportView(certInfo);
			scroll.getViewport().setOpaque(false);
			scroll.setOpaque(false);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			panel.add(scroll, gbc);
			panel.setPreferredSize(new Dimension(800, 500));

			gbc.gridy = 4;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			initCertInfo();
			return panel;
		}

		protected boolean shouldSaveOptionsOnCancel() {
			return false;  //To change body of implemented methods use File | Settings | File Templates.
		}

		private class InfoPanel extends JPanel {

			private JPanel panel = new JPanel();

			private DefaultFormBuilder builder;

			private InfoPanel() {
				super();
//				super(true, collapsed, title);
//				setContent(panel);
//
//				setCollapsed(!collapsed); // workaround
//				setCollapsed(collapsed);
				FormLayout layout = new FormLayout("right:max(100dlu;pref), 4dlu, pref:grow", "");
				builder = new DefaultFormBuilder(layout, this);
				builder.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			}

			void addSeparator(String title) {
				builder.appendSeparator(title);
			}

			void addRow(String caption, String value) {
				builder.append(new BoldLabel(caption));
				builder.append(value);
				builder.nextLine();
			}

		}

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

	}

}
