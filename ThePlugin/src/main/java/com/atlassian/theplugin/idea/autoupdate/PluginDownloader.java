package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.configuration.PluginConfiguration;
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

public class PluginDownloader { //implements Runnable {


	public static final String PLUGIN_ID_TOKEN = "PLUGIN_ID";
	public static final String VERSION_TOKEN = "BUILD";

	private static String pluginName = PluginUtil.getName();

	private static final int TIMEOUT = 15000;
	private static final int EXTENTION_LENGHT = 3;
	private InfoServer.VersionInfo newVersion;
	private PluginConfiguration pluginConfiguration;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	private int timeout = TIMEOUT;
	private int readTimeout = TIMEOUT;

	public PluginDownloader(InfoServer.VersionInfo newVersion, PluginConfiguration pluginConfiguration) {
		this.newVersion = newVersion;
		this.pluginConfiguration = pluginConfiguration;
	}

	public void run() {
		try {
			File localArchiveFile = downloadPluginFromServer(this.newVersion.getDownloadUrl());

			// add startup actions

			// todo lguminski/jjaroczynski to find a better way of getting plugin descriptor
			// theoritically openapi should provide a method so the plugin could get info on itself

			IdeaPluginDescriptor pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginUtil.getPluginId()));
			/* todo lguminsk when you debug the plugin it appears in registry as attlassian-idea-plugin, but when
			 	you rinstall it notmally it appears as Atlassian. Thats why it is double checked here
			    */
			if (pluginDescr == null) {
				pluginDescr = PluginManager.getPlugin(PluginId.getId(PluginUtil.getName()));
			}
			addActions(pluginDescr, localArchiveFile);

			// restart IDEA
			promptShutdownAndShutdown();

		} catch (IOException e) {
			PluginUtil.getLogger().info("Error registering action in IDEA", e);
		}
	}
	
	private void promptShutdownAndShutdown() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				String title = "IDEA shutdown";
				String message = "Plugin has been installed successfully. Do you want to restart IDEA to activate the plugin?";

				int answer = Messages.showYesNoDialog(
						message, title, Messages.getQuestionIcon());

//						Messages.showDialog(PluginUpdateNotifierBundle.message("progress.shutdown.dialog.text"),
//                        PluginUpdateNotifierBundle.message("progress.shutdown.dialog.title"),
//                        new String[]{PluginUpdateNotifierBundle.message("progress.shutdown.dialog.button.shutdown.text"),
//                                PluginUpdateNotifierBundle.message("progress.shutdown.dialog.button.cancel.text")},
//                        0, Messages.getQuestionIcon());

				if (answer == DialogWrapper.OK_EXIT_CODE) {
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
		pluginUrl = newVersion.getDownloadUrl()
				.replaceAll(PLUGIN_ID_TOKEN, URLEncoder.encode(pluginName, "UTF-8"))
				.replaceAll(VERSION_TOKEN, URLEncoder.encode(version, "UTF-8"));
		if (!pluginUrl.contains("?")) {
			pluginUrl += "?";
		}
		pluginUrl += "uid=" + URLEncoder.encode(Long.toString(pluginConfiguration.getUid()), "UTF-8");

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
		if (ext.contains("?")) {
			ext = ext.substring(0, ext.indexOf("?"));	
		}
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
		boolean isJarFile = localArchiveFile.getName().endsWith(".jar")
				|| localArchiveFile.getName().contains(".jar?");

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

	public int getTimeout() {
		return timeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}
}
