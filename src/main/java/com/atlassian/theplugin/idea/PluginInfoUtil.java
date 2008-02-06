package com.atlassian.theplugin.idea;

import com.intellij.util.PathUtil;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jan 24, 2008
 * Time: 11:50:18 AM
 */
public final class PluginInfoUtil {
    private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

    private static String baseDir = PathUtil.getJarPathForClass(PluginInfoUtil.class);

    private static Document doc = setDoc();

	private PluginInfoUtil() {
		super();
	}

	public static String getName() {
		// TODO lguminski: to make the application reading plugin.xml settings
		return getConfigValue("/idea-plugin/name");
    }

    public static String getVersion() {
        return getConfigValue("/idea-plugin/version");
    }

    public static String getVendor() {
        return getConfigValue("/idea-plugin/vendor");
    }

    private static Document setDoc() {
        Document doc = null;
		InputStream is = PluginInfoUtil.class.getResourceAsStream("/META-INF/plugin.xml");
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		try {
			doc = builder.build(is);
		} catch (IOException e) {
			 LOGGER.error("Error accessing plugin.xml file.");
		} catch (JDOMException e) {
			 LOGGER.error("Error accessing plugin.xml file.");
		 }
         return doc;
    }

    private static String getConfigValue(String path) {
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

