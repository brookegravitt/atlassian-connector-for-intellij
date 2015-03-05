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

package com.atlassian.theplugin.idea;


import com.atlassian.theplugin.commons.util.LoggerImpl;

import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 17, 2008
 * Time: 4:44:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultLoggerImpl extends LoggerImpl {

	private static final String[] LOG_LEVEL_STR = {
				"ERROR",
				"WARN",
				"INFO",
				"VERBOSE",
				"DEBUG"
		};

	public void log(int level, String msg, Throwable t) {
        if (canIgnore(level)) {
            return;
        }

        final PrintStream stream;
        final String decoratedMsg;
        if (level >= LOG_INFO) {
            // for INFO or below level msgs, print the msg only, nothing else
            decoratedMsg = msg;
            //stream = System.out;
			stream = System.err;
		} else {
            // since we are obfuscating, mName shouldn't be printed.
            decoratedMsg = LOG_LEVEL_STR[level] + ": " + msg;
            stream = System.err;
        }
        stream.print(decoratedMsg);

        if (t != null && (isVerbose() || isDebug())) {
            System.err.println(msg + " : " + t.getMessage());
            t.printStackTrace();
        } else {
            stream.println();
        }
    }
}
