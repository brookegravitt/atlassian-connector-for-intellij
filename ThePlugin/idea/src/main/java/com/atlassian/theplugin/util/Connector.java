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

import com.atlassian.theplugin.commons.exception.ThePluginException;

/**
 * Created by IntelliJ IDEA.
* User: lguminski
* Date: Mar 17, 2008
* Time: 4:46:50 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class Connector {

	private String url;
	private String userName;
	private String password;

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public abstract void connect() throws ThePluginException;

	protected void validate() throws IllegalArgumentException {
		if (url == null) {
			throw new IllegalArgumentException("Url not provided.");
		}
		if (userName == null) {
			throw new IllegalArgumentException("Username not provided.");
		}
		if (password == null) {
			throw new IllegalArgumentException("Password not provided.");
		}
	}

	public String getUrl() {
		return url;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
}
