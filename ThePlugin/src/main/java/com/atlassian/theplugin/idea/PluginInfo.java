package com.atlassian.theplugin.idea;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.PathUtil;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jan 24, 2008
 * Time: 11:50:18 AM
 * based on Martin Fuhrer's code (http://www.intellij.net/forums/message.jspa?messageID=5112288#5112295)
 */
public final class PluginInfo {
    private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

    private String baseDir = PathUtil.getJarPathForClass(PluginInfo.class);

    private Document doc = setDoc();

	private PluginInfo() {
		super();
	}

	public static String getName() {
		// TODO lguminski: to make the application reading plugin.xml settings
		return "The Plugin";
		// return getConfigValue("/idea-plugin/name");
    }

    public String getVersion() {
        return getConfigValue("/idea-plugin/version");
    }

    public String getVendor() {
        return getConfigValue("/idea-plugin/vendor");
    }

    private Document setDoc() {
        Document doc = null;
        File base = new File(baseDir);
        if (base.isDirectory()) {
            File file = new File(base.getAbsolutePath(), "META-INF/plugin.xml");
            try {
                doc = JDOMUtil.loadDocument(file);
            } catch (IOException e) {
                LOGGER.error("Error accessing plugin.xml file.");
            } catch (JDOMException e) {
                LOGGER.error("Error accessing plugin.xml file.");
            }
        } else {
            ZipFile zip = null;
            try {
                zip = new ZipFile(base);
                InputStream in = zip.getInputStream(zip.getEntry("META-INF/plugin.xml"));
                doc = JDOMUtil.loadDocument(in);
                in.close();
            } catch (IOException e) {
                LOGGER.error("Error accessing plugin.xml file.");
            } catch (JDOMException e) {
                LOGGER.error("Error accessing plugin.xml file.");
            }
        }
        return doc;
    }

    private String getConfigValue(String path) {
        String result = null;
        XPath xpath = null;
        try {
            xpath = XPath.newInstance(path);
            Element element = (Element) xpath.selectSingleNode(doc);
            if (element != null) {
                result = element.getValue();
            }
        } catch (JDOMException e) {
            LOGGER.error("Error while retrieving plugin name.");
        }
        return result;
    }

}

