package com.atlassian.theplugin.idea;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jan 24, 2008
 * Time: 11:50:18 AM
 * taken from  Alexey Efimov http://intellij.net/forums/thread.jspa?messageID=796438&
 */
public final class PluginInfo {

  /**
   * Logger
   */
  private static final Category logger = Logger.getInstance(PluginStatusBarToolTip.class);

  /**
   * Plugin name
   */
  public static String NAME;

  /**
   * Plugin version
   */
  public static String VERSION;

  /**
   * Plugin vendor
   */
  public static String VENDOR;

  /**
   * Home of plugin
   */
  public static VirtualFile HOME;

  /**
   * Internal name, usual the same as last package name of this class
   */
  public static String INTERNAL_NAME;

  /**
   * Method return current plugin JDOM element of root XML element.
   *
   * @return Element "idea-plugin"
   * @throws java.io.IOException
   * @throws org.jdom.JDOMException
   */
  private static Element getPluginElement() throws IOException, JDOMException {
//    ClassLoader classLoader = PluginInfo.class.getClassLoader();
//    logger.info(classLoader.toString());
//    URL url = classLoader.getResource("META-INF/plugin.xml");

    if (HOME == null || INTERNAL_NAME == null) {
      throw new IllegalStateException();
    }

    String jarFileName = INTERNAL_NAME + ".jar";
    VirtualFile jar = HOME.findFileByRelativePath("lib/" + jarFileName);
    if (jar == null) {
      throw new FileNotFoundException(jarFileName);
    }

    String descriptorPath = "META-INF/plugin.xml";
    JarFile jarFile = new JarFile(jar.getPresentableUrl());
    ZipEntry pluginDescriptor = jarFile.getJarEntry(descriptorPath);
    if (pluginDescriptor == null) {
      throw new FileNotFoundException(descriptorPath);
    }

    SAXBuilder builder = new SAXBuilder(false);
    Document doc = builder.build(jarFile.getInputStream(pluginDescriptor));
    return doc.getRootElement();
  }

  static {
    try {
      // Internal name
      String[] names = PluginInfo.class.getName().split("\\.");
      INTERNAL_NAME = names.length > 1 ? names[names.length - 2] : null;

      // Plugin home
      String pluginHomePath = (PathManager.getPluginsPath() + File.separatorChar + INTERNAL_NAME).replace(File.separatorChar, '/');
      HOME = LocalFileSystem.getInstance().findFileByPath(pluginHomePath);

      Element pluginElement = getPluginElement();
      // Filling
      NAME = pluginElement.getChildText("name");
      VERSION = pluginElement.getChildText("version");
      VENDOR = pluginElement.getChildText("vendor");

    } catch (Exception e) {
      logger.error(e);
    }

  }

}

