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
package com.atlassian.connector.intellij.configuration;

/**
	 * This is serialization friendly thing
 */
public class UserCfgBean {
	private String username;

	private String encodedPassword;

    public UserCfgBean() {
        this("", "");
    }

	public UserCfgBean(final String username, final String encodedPassword) {
		this.username = username;
		this.encodedPassword = encodedPassword;
	}

	public String getUsername() {
		return username;
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setEncodedPassword(final String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}
}
