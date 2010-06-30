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
package com.atlassian.connector.intellij.remoteapi;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;

/**
 * @autrhor pmaruszak
 * @date Jun 28, 2010
 */
public abstract class IntelliJHttpSessionCallback extends HttpSessionCallbackImpl {
    abstract void disposeClient(ConnectionCfg server);
}
