package com.atlassian.theplugin.idea.config;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-05
 * Time: 11:42:01
 * To change this template use File | Settings | File Templates.
 */
public class FooterPanel extends JPanel {
	private JLabel versionLabel;
	private HyperlinkLabel openJiraHyperlinkLabel;

	public FooterPanel() {
		initLayout();
    }

	private void initLayout() {
		String versionName = "unknown";

		BorderLayout gb = new BorderLayout();
		setLayout(gb);

		InputStream is = getClass().getResourceAsStream("/META-INF/plugin.xml");
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		try {
			Document doc = builder.build(is);
			XPath xpath = XPath.newInstance("/idea-plugin/version");
			@SuppressWarnings("unchecked")
			Element element = (Element) xpath.selectSingleNode(doc);
			if (element != null) {
				versionName = element.getText();
			}
		} catch (JDOMException e) {
			versionName = "unknown - /META-INF/plugin.xml file is corrupt";
		} catch (IOException e) {
			versionName = "unknown - can't read /META-INF/plugien.xml";
		}


		versionLabel = new JLabel(versionName);

		openJiraHyperlinkLabel = new HyperlinkLabel("Report a bug/issue/request.");
		openJiraHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser("https://studio.atlassian.com/browse/PL");
			}
		});
		add(versionLabel, BorderLayout.WEST);
		add(openJiraHyperlinkLabel, BorderLayout.EAST);
	}
}
