package com.atlassian.theplugin.idea;

import com.intellij.util.PathUtil;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

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
	public static final String VERSION_INFO_URL = "http://docs.atlassian.com/atlassian-idea-plugin/latestVersion.xml";

	///CLOVER:OFF
	private PluginInfoUtil() {
		super();
	}
	///CLOVER:ON

	public static String getName() {
		return getConfigValue("/idea-plugin/name");
	}

	public static String getVersion() {
		return getConfigValue("/idea-plugin/version");
	}

	public static String getVendor() {
		return getConfigValue("/idea-plugin/vendor");
	}

	public static String getPluginId() {
		return getConfigValue("/idea-plugin/plugin-id");
	}

	private static Document setDoc() {
		File base = new File(baseDir);
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		try {
			if (base.isDirectory()) {
				File file = new File(base.getAbsolutePath(), "../META-INF/plugin.xml");
				// magic: we try to find plugin.xml, which is not so simple
				// beacuase structure of project and structure of the package
				// made by maven are different
				boolean giveUp = false;
				while (true) {
					try {
						doc = builder.build(file);
					} catch (FileNotFoundException e) {
						if (giveUp) {
							throw e;
						}
						giveUp = true;
						file = new File(base.getAbsolutePath(), "META-INF/plugin.xml");
						continue;
					}
					break;
				}
			} else {
				ZipFile zip = new ZipFile(base);
				InputStream in = zip.getInputStream(zip.getEntry("META-INF/plugin.xml"));
				doc = builder.build(in);
				in.close();
			}
		} catch (IOException e) {
			LOGGER.warn("Error accessing plugin.xml file.");
			throw new UnsupportedOperationException(e);
		} catch (JDOMException e) {
			LOGGER.warn("Error accessing plugin.xml file.");
			throw new UnsupportedOperationException(e);
		}

		return doc;
	}

	private static String getConfigValue(String path) {
		String result = null;
		try {
			XPath xpath = XPath.newInstance(path);
			Element element = (Element) xpath.selectSingleNode(doc);
			if (element != null) {
				result = element.getValue();
			}
		} catch (JDOMException e) {
			LOGGER.warn("Error while retrieving plugin name.");
		}
		return result;
	}
}

