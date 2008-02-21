package com.atlassian.theplugin.idea;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.io.ZipUtil;
import com.atlassian.theplugin.exception.ThePluginException;
import org.apache.log4j.Category;
import org.jetbrains.annotations.NonNls;

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

	public static final Category LOG = Category.getInstance(PluginDownloader.class);

	public static final String PLUGIN_ID_TOKEN = "PLUGIN_ID";
	public static final String VERSION_TOKEN = "BUILD";

	@NonNls
	public static String PLUGIN_DOWNLOAD_URL =
			"http://plugins.intellij.net/pluginManager/?action=download&id="
					+ PLUGIN_ID_TOKEN
					+ "&build="
					+ VERSION_TOKEN; // non final due to the need of changing it in test case

	private static String pluginName;
	private String pluginLatestVersion;
	private static final int TIMEOUT = 15000;

	public PluginDownloader(String version) {
		pluginLatestVersion = version;
		pluginName = PluginInfoUtil.getName();
	}

	public void run() {
		try {
			File localArchiveFile = downloadPluginFromServer(this.pluginLatestVersion);

			// add startup actions

			// todo lguminski/jjaroczynski to find a better way of getting plugin descriptor
			// theoritically openapi should provide a method so the plugin could get info on itself

			addActions(PluginManager.getPlugin(PluginId.getId(PluginInfoUtil.getPluginId())), localArchiveFile);

			// restart IDEA
			promptShutdownAndShutdown();

		} catch (IOException e) {
			LOG.error("Error registering action in IDEA", e);
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


		String pluginUrl = PLUGIN_DOWNLOAD_URL
				.replaceAll(PLUGIN_ID_TOKEN, pluginName)
				.replaceAll(VERSION_TOKEN, version);

		LOG.info("Downloading plugin archive from: " + pluginUrl);

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
		String newName = pluginArchiveFile.getPath().substring(0, pluginArchiveFile.getPath().length() - 3) + ext;
		File newFile = new File(newName);
		if(pluginArchiveFile.renameTo(new File(newName)) == false) {
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
			StartupActionScriptManager.ActionCommand copyPlugin = new StartupActionScriptManager.CopyCommand(localArchiveFile, newFile);
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
			StartupActionScriptManager.ActionCommand unzip = new StartupActionScriptManager.UnzipCommand(localArchiveFile, newFile);
			StartupActionScriptManager.addActionCommand(unzip);
		}

		// add command to remove temp plugin file
		StartupActionScriptManager.ActionCommand deleteTemp = new StartupActionScriptManager.DeleteCommand(localArchiveFile);
		StartupActionScriptManager.addActionCommand(deleteTemp);
	}

}
