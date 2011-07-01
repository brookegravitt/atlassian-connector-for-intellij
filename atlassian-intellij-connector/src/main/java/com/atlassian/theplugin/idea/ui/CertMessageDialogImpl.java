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

package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.labels.BoldLabel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CertMessageDialogImpl extends DialogWrapper implements com.atlassian.theplugin.commons.ssl.CertMessageDialog {
		private String server;

		private X509Certificate[] chain;

		private InfoPanel generalInfo = new InfoPanel();
		private CollapsiblePanel infoPanel = new CollapsiblePanel(false, false, "General Information");


		private JLabel details = new JLabel();

		private JButton accept = new JButton("Accept");

		private JButton acceptTmp = new JButton("Accept temporarily");

		private JButton cancel = new JButton("Cancel");

		private boolean temporarily = false;
		private String message;
        private static final int MARGIN = 5;


    public CertMessageDialogImpl() {
        super(false);
    }

    public void show(String host, String msg, X509Certificate[] certificates) {
        this.server = host;
        this.message = msg;
        this.chain = certificates;

        setTitle("Security Alert");
        setModal(true);
        accept.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                close(JOptionPane.OK_OPTION);
            }
        });
        acceptTmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                temporarily = true;
                close(JOptionPane.OK_OPTION);
            }
        });
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                temporarily = true;
                close(JOptionPane.CANCEL_OPTION);
            }
        });

        init();
        show();
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

		private void initCertInfo(X509Certificate[] chains) {
			X509Certificate cert = chains[0];
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
			byte[] fing = digest.digest(certificate.getEncoded());
			return toHexString(fing);
		}


		private String toHexString(byte[] bytes) {
			StringBuffer stringBuffer = new StringBuffer();
			int i = bytes.length;
			for (int j = 0; j < i; j++) {
				byte2hex(bytes[j], stringBuffer);
				if (j < i - 1) {
					stringBuffer.append(":");
                }
			}
			return stringBuffer.toString();
		}

		private static final String[] HEX_DIGITS = {"0", "1", "2", "3", "4",
				"5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

		private void byte2hex(byte b, StringBuffer stringbuffer) {
			int nb = b & 0xFF;
			int i1 = (nb >> 4) & 0xF;
			int i2 = nb & 0xF;
			stringbuffer.append(HEX_DIGITS[i1] + HEX_DIGITS[i2]);
		}


		private void buildDNPanel(String strDN, InfoPanel panel) {
			Map<String, String> lfields = parse(strDN);
			for (Map.Entry<String, String> field : this.fields.entrySet()) {
				String key = field.getKey();
				if (lfields.containsKey(key)) {
					panel.addRow(this.fields.get(key) + " (" + key + ")", lfields.get(key));
				}
			}
		}

		private Map<String, String> parse(String name) {
			Map<String, String> result = new HashMap<String, String>();
			String[] lfields = name.split(",");
			for (String field : lfields) {
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
			msgPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));

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

			initCertInfo(chain);
			return panel;
		}

		protected boolean shouldSaveOptionsOnCancel() {
			return false;
		}

		private final class InfoPanel extends JPanel {

			private JPanel panel = new JPanel();

			private DefaultFormBuilder builder;

			private InfoPanel() {
				super();
//				super(true, collapsed, title);
//				setContent(panel);
//
//				setCollapsed(!collapsed); // workaround
//				setCollapsed(collapsed);
				FormLayout layout = new FormLayout("right:pref, 4dlu, pref:grow", "");
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
