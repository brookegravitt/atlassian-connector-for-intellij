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
package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.util.Connector;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.LoginDataProvided;

public class ProductConnector extends Connector {
	private final ProductServerFacade facade;

	public ProductConnector(final ProductServerFacade facade) {
		this.facade = facade;
	}

	@Override
	public void connect(LoginDataProvided loginDataProvided) throws ThePluginException {
		//validate();
		try {
			facade.testServerConnection(loginDataProvided.getServerUrl(), loginDataProvided.getUserName(),
					loginDataProvided.getPassword());
		} catch (RemoteApiException e) {
			throw new ThePluginException(e.getMessage(), e);
		}
	}
}
