package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.util.InfoServer;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.io.ZipUtil;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-20
 * Time: 11:37:09
 * To change this template use File | Settings | File Templates.
 */

public class PluginDownloader implements Runnable {

	private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);

	public static final String PLUGIN_ID_TOKEN = "PLUGIN_ID";
	public static final String VERSION_TOKEN = "BUILD";

	private static String pluginName = PluginInfoUtil.getName();

	private static final int TIMEOUT = 15000;
	private static final int EXTENTION_LENGHT = 3;
	private InfoServer.VersionInfo newVersion;

	public PluginDownloader(InfoServer.VersionInfo newVersion) {
		this.newVersion = newVersion;
	}

	public void run() {
		try {
			File localArchiveFile = downloadPluginFromServer(this.newVersion.getDownloadUrl());

			// add startup actions

			// todo lguminski/jjaroczynski to find a better way of getting plugin descriptor
			// theoritically openapi should provide a method so the plugin could get info on itself

			IdeaPluginDescriptor pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginInfoUtil.getPluginId()));
			/* todo lguminsk when you debug the plugin it appears in registry as attlassian-idea-plugin, but when
			 	you rinstall it notmally it appears as Atlassian. Thats why it is double checked here
			    */
			if (pluginDescr == null) {
				pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginInfoUtil.getName()));
			}
			addActions(pluginDescr, localArchiveFile);

			// restart IDEA
			promptShutdownAndShutdown();

		} catch (IOException e) {
			LOGGER.error("Error registering action in IDEA", e);
		} catch (VersionServiceException e) {
			LOGGER.error("Error registering action in IDEA", e);
		}
	}

	private void promptShutdownAndShutdown() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				String title = "IDEA shutdown";
				String message = "Plugin has been installed successfully. Do you want to restart IDEA to activate the plugin?";

				int answer = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
						message, title, JOptionPane.YES_NO_OPTION);

//						Messages.showDialog(PluginUpdateNotifierBundle.message("progress.shutdown.dialog.text"),
//                        PluginUpdateNotifierBundle.message("progress.shutdown.dialog.title"),
//                        new String[]{PluginUpdateNotifierBundle.message("progress.shutdown.dialog.button.shutdown.text"),
//                                PluginUpdateNotifierBundle.message("progress.shutdown.dialog.button.cancel.text")},
//                        0, Messages.getQuestionIcon());

				if (answer == JOptionPane.YES_OPTION) {
					//ApplicationManagerEx.getApplicationEx().exit(true);
					ApplicationManager.getApplication().exit();
				}
			}
		});
	}

	// todo add info about licence and author
	private File downloadPluginFromServer(String version) throws IOException {
		File pluginArchiveFile = FileUtil.createTempFile("temp_" + pluginName + "_", "tmp");


		String pluginUrl = null;
		try {
			pluginUrl = newVersion.getDownloadUrl()
				.replaceAll(PLUGIN_ID_TOKEN, pluginName)
					.replaceAll(VERSION_TOKEN, version);
		} catch (VersionServiceException e) {
			LOGGER.error("Error retrieving url for new version of the plugin.");
		}

		LOGGER.info("Downloading plugin archive from: " + pluginUrl);

		//HttpConfigurable.getInstance().prepareURL(pluginUrl);
		URL url = new URL(pluginUrl);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);
		connection.connect();

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = connection.getInputStream();
			outputStream = new FileOutputStream(pluginArchiveFile);
			StreamUtil.copyStreamContent(inputStream, outputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
					// nothing we can do at this point
				}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException ioe) {
					// nothing we can do at this point
				}
			}

			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).disconnect();
			}
		}
		String srcName = connection.getURL().toString();
		String ext = srcName.substring(srcName.lastIndexOf("."));
		String newName = pluginArchiveFile.getPath().substring(0, pluginArchiveFile.getPath().length()
				- EXTENTION_LENGHT) + ext;
		File newFile = new File(newName);
		if (!pluginArchiveFile.renameTo(new File(newName))) {
			pluginArchiveFile.delete();
			throw new IOException("Renaming received file from \"" + srcName + "\" to \"" + newName + "\" failed.");
		}
		return newFile;
	}

	private void addActions(IdeaPluginDescriptor installedPlugin, File localArchiveFile) throws IOException {

		PluginId id = installedPlugin.getPluginId();

		if (PluginManager.isPluginInstalled(id)) {
			// store old plugins file
			File oldFile = installedPlugin.getPath();
			StartupActionScriptManager.ActionCommand deleteOld = new StartupActionScriptManager.DeleteCommand(oldFile);
			StartupActionScriptManager.addActionCommand(deleteOld);
		}

		//noinspection HardCodedStringLiteral
		boolean isJarFile = localArchiveFile.getName().endsWith(".jar");

		if (isJarFile) {
			// add command to copy file to the IDEA/plugins path
			String fileName = localArchiveFile.getName();
			File newFile = new File(PathManager.getPluginsPath() + File.separator + fileName);
			StartupActionScriptManager.ActionCommand copyPlugin =
					new StartupActionScriptManager.CopyCommand(localArchiveFile, newFile);
			StartupActionScriptManager.addActionCommand(copyPlugin);
		} else {
			// add command to unzip file to the IDEA/plugins path
			String unzipPath;
			if (ZipUtil.isZipContainsFolder(localArchiveFile)) {
				unzipPath = PathManager.getPluginsPath();
			} else {
				String dirName = installedPlugin.getName();
				unzipPath = PathManager.getPluginsPath() + File.separator + dirName;
			}

			File newFile = new File(unzipPath);
			StartupActionScriptManager.ActionCommand unzip =
					new StartupActionScriptManager.UnzipCommand(localArchiveFile, newFile);
			StartupActionScriptManager.addActionCommand(unzip);
		}

		// add command to remove temp plugin file
		StartupActionScriptManager.ActionCommand deleteTemp = new StartupActionScriptManager.DeleteCommand(localArchiveFile);
		StartupActionScriptManager.addActionCommand(deleteTemp);
	}

}
