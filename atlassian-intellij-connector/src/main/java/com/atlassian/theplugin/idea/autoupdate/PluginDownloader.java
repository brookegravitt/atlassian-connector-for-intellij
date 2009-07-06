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

package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaActionScheduler;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 11:37:09
 * To change this template use File | Settings | File Templates.
 */

public class PluginDownloader {


	public static final String PLUGIN_ID_TOKEN = "PLUGIN_ID";
	public static final String VERSION_TOKEN = "BUILD";

	private static String pluginName = PluginUtil.getInstance().getName();

	private static final int TIMEOUT = 15000;
	private static final int EXTENTION_LENGHT = 3;
	private InfoServer.VersionInfo newVersion;
	private GeneralConfigurationBean updateConfiguration;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	private int timeout = TIMEOUT;
	private int readTimeout = TIMEOUT;

	public PluginDownloader(InfoServer.VersionInfo newVersion, GeneralConfigurationBean updateConfiguration) {
		this.newVersion = newVersion;
		this.updateConfiguration = updateConfiguration;
	}

	public void run() {
		try {
			final File tmpDownloadFile = downloadPluginFromServer(
					this.newVersion.getDownloadUrl(), new File(PathManager.getPluginsPath()));

			// add startup actions

			// todo lguminski/jjaroczynski to find a better way of getting plugin descriptor
			// theoritically openapi should provide a method so the plugin could get info on itself

			IdeaPluginDescriptor pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginUtil.getInstance().getPluginId()));
			/* todo lguminsk when you debug the plugin it appears in registry as attlassian-idea-plugin, but when
			 	you rinstall it notmally it appears as Atlassian. Thats why it is double checked here
			    */
			if (pluginDescr == null) {
				pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginUtil.getInstance().getName()));
			}
			
			if (pluginDescr == null) {
				IdeaActionScheduler.getInstance().invokeLater(new Runnable() {
					public void run() {
						// todo add project or parent to the below window
						Messages.showErrorDialog("Cannot retrieve plugin descriptor", "Error installing plugin");
					}
				});
				return;
			}
			addActions(pluginDescr, tmpDownloadFile);

			// restart IDEA
			promptShutdownAndShutdown();

		} catch (final IOException e) {
			PluginUtil.getLogger().warn(e.getMessage(), e);
			IdeaActionScheduler.getInstance().invokeLater(new Runnable() {
				public void run() {
					Messages.showErrorDialog(e.getMessage(), "Error downloading and installing plugin");
				}
			});

		}
	}

	private void promptShutdownAndShutdown() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				String title = "IDEA shutdown";
				String message =
						"Atlassian Connector for IntelliJ IDEA has been installed successfully.\n"
						+ "IntelliJ IDEA needs to be restarted to activate the plugin.\n"
						+ "Would you like to shutdown IntelliJ IDEA now?";
				// todo again add project or parent to the below window
				int answer = Messages.showYesNoDialog(
						message, title, Messages.getQuestionIcon());
				if (answer == DialogWrapper.OK_EXIT_CODE) {
					//ApplicationManagerEx.getApplicationEx().exit(true);
					ApplicationManager.getApplication().exit();
				}
			}
		});
	}

	// todo add info about licence and author
	File downloadPluginFromServer(String version, @NotNull final File destinationDir) throws IOException {
		File pluginArchiveFile = FileUtil.createTempFile("temp_" + pluginName + "_", "tmp");

		String pluginUrl = newVersion.getDownloadUrl()
				.replaceAll(PLUGIN_ID_TOKEN, URLEncoder.encode(pluginName, "UTF-8"))
				.replaceAll(VERSION_TOKEN, URLEncoder.encode(version, "UTF-8"));
		if (!pluginUrl.contains("?")) {
			pluginUrl += "?";
		}
		pluginUrl += "uid=" + URLEncoder.encode(Long.toString(updateConfiguration.getUid()), "UTF-8");

		PluginUtil.getLogger().info("Downloading plugin archive from: " + pluginUrl);

		//HttpConfigurable.getInstance().prepareURL(pluginUrl);
		URL url = new URL(pluginUrl);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(getTimeout());
		connection.setReadTimeout(getReadTimeout());
		connection.connect();

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = connection.getInputStream();
			outputStream = new FileOutputStream(pluginArchiveFile);
			StreamUtil.copyStreamContent(inputStream, outputStream);
		} catch (FileNotFoundException e) {
			PluginUtil.getLogger().warn("File not found " + pluginArchiveFile.getPath(), e);
			throw e;
		} catch (IOException e) {
			PluginUtil.getLogger().warn(e);
			throw e;
		} finally {

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					PluginUtil.getLogger().warn("Exception while closing input stream");
					// nothing we can do at this point
				}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException ioe) {
					PluginUtil.getLogger().warn("Exception while closing output stream");
					// nothing we can do at this point
				}
			}

			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
				PluginUtil.getLogger().info("Disconnecting HttpURLConnection");
			}
		}
		PluginUtil.getLogger().info("Downloaded file has [" + pluginArchiveFile.length() + "] bytes");
		String srcName = connection.getURL().toString();
		String ext = srcName.substring(srcName.lastIndexOf("."));
		if (ext.contains("?")) {
			ext = ext.substring(0, ext.indexOf("?"));
		}
		String newName = pluginArchiveFile.getName().substring(0, pluginArchiveFile.getName().length()
				- EXTENTION_LENGHT) + ext;
		File newFile = new File(destinationDir, newName);
		PluginUtil.getLogger().info("Renaming downloaded file from [" + pluginArchiveFile.getAbsolutePath()
				+ "] to [" + newFile + "]");

		if (!pluginArchiveFile.renameTo(newFile)) {
			try {
				FileUtil.copy(pluginArchiveFile, newFile);
			} catch (IOException e) {
				throw new IOException("Renaming file from [" + pluginArchiveFile.getAbsolutePath() + "] to ["
						+ newFile.getAbsolutePath() + "] failed.");
			} finally {
				if (!pluginArchiveFile.delete()) {
					LoggerImpl.getInstance().warn("Deleting file [" + pluginArchiveFile.getAbsolutePath() + "] failed.");
				}
			}
		}
		
		PluginUtil.getLogger().info("After renaming file has [" + newFile.length() + "] bytes");
		return newFile;
	}

	private void addActions(@NotNull final IdeaPluginDescriptor installedPlugin, File localArchiveFile) throws IOException {
		PluginUtil.getLogger().info("IdeaPluginDescriptor [" + installedPlugin.getPluginId() + "]");

		PluginId id = installedPlugin.getPluginId();

		if (PluginManager.isPluginInstalled(id)) {
			// store old plugins file
			File oldFile = installedPlugin.getPath();
			StartupActionScriptManager.ActionCommand deleteOld = new StartupActionScriptManager.DeleteCommand(oldFile);
			StartupActionScriptManager.addActionCommand(deleteOld);
			PluginUtil.getLogger().info("Queueing deletion of [" + oldFile.getPath() + "], exists [" + oldFile.exists() + "]");
		} else {
			// we shoud not be here
			PluginUtil.getLogger().warn("Install error. Cannot find plugin [" + installedPlugin.getName()
					+ "] with id [" + id.getIdString() + "]. Cannot delete old plugin version installing new version");
		}

		//noinspection HardCodedStringLiteral
		boolean isJarFile = localArchiveFile.getName().endsWith(".jar")
				|| localArchiveFile.getName().contains(".jar?");

		if (isJarFile) {
			// add command to copy file to the IDEA/plugins path
			String fileName = localArchiveFile.getName();
			File newFile = new File(PathManager.getPluginsPath() + File.separator + fileName);
			StartupActionScriptManager.ActionCommand copyPlugin =
					new StartupActionScriptManager.CopyCommand(localArchiveFile, newFile);
			StartupActionScriptManager.addActionCommand(copyPlugin);
			PluginUtil.getLogger().info("Queueing copying of jar [" + localArchiveFile.getAbsolutePath() + "] to ["
					+ newFile.getAbsolutePath() + "]");
		} else {
			// add command to unzip file to the IDEA/plugins path
			String unzipPath;
			if (ZipUtil.isZipContainsFolder(localArchiveFile)) {
				unzipPath = PathManager.getPluginsPath();
				PluginUtil.getLogger().info("Zip [" + localArchiveFile + "] contains a root folder");
			} else {
				String dirName = installedPlugin.getName();
				unzipPath = PathManager.getPluginsPath() + File.separator + dirName;
				PluginUtil.getLogger().info("Zip [" + localArchiveFile + "] does not contain a root folder");
			}

			File newFile = new File(unzipPath);
			StartupActionScriptManager.ActionCommand unzip =
					new StartupActionScriptManager.UnzipCommand(localArchiveFile, newFile);
			StartupActionScriptManager.addActionCommand(unzip);
			PluginUtil.getLogger().info("Queueing unzipping/copying of [" + localArchiveFile.getAbsolutePath() + "] to ["
					+ newFile.getAbsolutePath() + "]");
		}

		// add command to remove temp plugin file
		StartupActionScriptManager.ActionCommand deleteTemp =
				new StartupActionScriptManager.DeleteCommand(localArchiveFile);
		StartupActionScriptManager.addActionCommand(deleteTemp);
		PluginUtil.getLogger().info("Queueing deletion of [" + localArchiveFile.getAbsolutePath() + "]");
	}

	public int getTimeout() {
		return timeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}
}
