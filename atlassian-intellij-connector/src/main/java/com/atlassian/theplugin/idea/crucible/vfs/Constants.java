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

package com.atlassian.theplugin.idea.crucible.vfs;

public final class Constants {
    private Constants() {
        // this is a utility class
    }
    public static final String CRUCIBLE_BOGUS_PROTOCOL = "cruciblefile";
	public static final String CRUCIBLE_BOGUS_SCHEMA = CRUCIBLE_BOGUS_PROTOCOL + "://";
	public static final String CRUCIBLE_ROOT = "cruciblefile";
}
