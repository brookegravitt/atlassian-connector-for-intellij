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
package com.atlassian.connector.intellij.crucible.content;

/**
 * User: mwent
 * Date: Mar 13, 2009
 * Time: 12:57:37 PM
 */
public class ReviewFileContentException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6060229636851982549L;

	public ReviewFileContentException() {
	}

	public ReviewFileContentException(final String message) {
		super(message);
	}

	public ReviewFileContentException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ReviewFileContentException(final Throwable cause) {
		super(cause);
	}
}
