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

import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.PathUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;


/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jan 24, 2008
 * Time: 11:50:18 AM
 */
public final class PluginUtil {

	private static String baseDir = PathUtil.getJarPathForClass(PluginUtil.class);

	private static Document doc;
	public static final String STABLE_VERSION_INFO_URL =
			"http://update.atlassian.com/atlassian-idea-plugin/latestStableVersion.xml";
	public static final String LATEST_VERSION_INFO_URL =
			"http://update.atlassian.com/atlassian-idea-plugin/latestPossibleVersion.xml";
	private static PluginUtil instance;

	static {
        doc = setDoc();
    }

    ///CLOVER:OFF
	private PluginUtil() {
	}
	///CLOVER:ON

	public static Logger getLogger() {
//		if (logger == null) {
//			if (!LogManager.getCurrentLoggers().hasMoreElements()) {
//				DOMConfigurator.configure(PluginUtil.class.getResource("/properties/log4j.xml"));
//			}
		return LoggerImpl.getInstance();
	}

	public static PluginUtil getInstance() {
		if (instance == null) {
			instance = new PluginUtil();
		}

		return instance;
	}

	public String getName() {
		return getConfigValue("/idea-plugin/name");
	}

	public String getVersion() {
		return getConfigValue("/idea-plugin/version");
	}

	public String getVendor() {
		return getConfigValue("/idea-plugin/vendor");
	}

	public String getPluginId() {
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
			PluginUtil.getLogger().error("Error accessing plugin.xml file.");
			throw new UnsupportedOperationException(e);
		} catch (JDOMException e) {
			PluginUtil.getLogger().error("Error accessing plugin.xml file.");
			throw new UnsupportedOperationException(e);
		}

		return doc;
	}

	private String getConfigValue(String path) {
		String result = null;
		try {
			XPath xpath = XPath.newInstance(path);
			Element element = (Element) xpath.selectSingleNode(doc);
			if (element != null) {
				result = element.getValue();
			}
		} catch (JDOMException e) {
			PluginUtil.getLogger().error("Error while retrieving plugin name.");
		}
		return result;
	}

    private static final int FALLBACK_DATE_WIDTH = 180;

    private static int dateWidth = -1;

    public static int getTimeWidth(JLabel label) {
        if (dateWidth == -1) {
            try {
                DateFormat dfi = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US);
                String t = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(dfi.parse("22 Dec 2008 11:22:22"));
                final JLabel jLabel = new JLabel(t);
                jLabel.setFont(label.getFont());
                dateWidth = jLabel.getPreferredSize().width;
            } catch (ParseException e) {
                dateWidth = FALLBACK_DATE_WIDTH;
            }
        }
        return dateWidth;
    }

	@NotNull
	public static final String PRODUCT_NAME = "Atlassian Connector for IntelliJ IDEA";

    public static void removeChangeList(Project project, LocalChangeList currentChangeList) {
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        IdeaVersionFacade ivf = IdeaVersionFacade.getInstance();

        if (changeListManager.getChangeLists().size() <= 1) {
            return;
        }

        if (currentChangeList.isDefault()) {
            for (LocalChangeList list : changeListManager.getChangeLists()) {
                if (!ivf.getChangeListId(list).equals(ivf.getChangeListId(currentChangeList))) {
                    //switch to first as default
                    changeListManager.setDefaultChangeList(list);
                    break;
                }
            }

        }
        changeListManager.removeChangeList(currentChangeList);
    }

      public static void activateDefaultChangeList(ChangeListManager changeListManager) {
            List<LocalChangeList> chLists = changeListManager.getChangeLists();
            for (LocalChangeList chl : chLists) {
                if ("default".equalsIgnoreCase(chl.getName())) {
                    changeListManager.setDefaultChangeList(chl);
                    return;
                }
            }
        }


}

