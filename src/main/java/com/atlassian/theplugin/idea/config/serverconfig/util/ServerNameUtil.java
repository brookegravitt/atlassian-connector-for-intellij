package com.atlassian.theplugin.idea.config.serverconfig.util;

import com.atlassian.theplugin.configuration.Server;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.MessageFormat;

/**
 * Utility class to provide new server names
 * User: mwent
 * Date: 2008-01-31
 * Time: 10:15:24
 * To change this template use File | Settings | File Templates.
 */
public final class ServerNameUtil {

    private ServerNameUtil() { }
    
    /**
	 * Suggest a name to be used as the default for a new regexp context.
	 *
	 * @return
	 */
	public static String suggestNewName(Collection existing) {
		return suggestName("Unnamed_{0}", existing);
	}

	/**
	 * Suggest a name to be used as the default for a copy of the specified
	 * regexp context.
	 *
	 * @param server
	 * @return
	 */
	public static String suggestCopyName(Server server, Collection existing) {
		return suggestName("Copy_{0}_of_" + server.getName(), existing);
	}

	public static String suggestName(String template, Collection existing) {
		String regexp = MessageFormat.format(template, new Object[]{ "(\\d+)" });
		Pattern pattern = Pattern.compile(regexp);

		long maxIndex = 0;

		for (Iterator iter = existing.iterator(); iter.hasNext();) {
			Server server = (Server) iter.next();
			Matcher m = pattern.matcher(server.getName());
			if (m.matches()) {
				long index = Long.parseLong(m.group(1));
				if (index > maxIndex) {
					maxIndex = index;
				}
			}
		}
		return MessageFormat.format(template, new Object[]{ Long.valueOf(maxIndex + 1) });
	}	
}
