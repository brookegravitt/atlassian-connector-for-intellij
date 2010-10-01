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

package com.atlassian.theplugin.exception;

import com.atlassian.theplugin.commons.exception.ThePluginException;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Feb 19, 2008
 * Time: 11:48:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class VersionServiceException extends ThePluginException {
	public VersionServiceException(String s, Throwable e) {
		super(s, e);
	}

	public VersionServiceException(String s) {
		super(s);
	}
}
