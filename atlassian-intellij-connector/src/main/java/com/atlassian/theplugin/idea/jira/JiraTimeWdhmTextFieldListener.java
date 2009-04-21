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
package com.atlassian.theplugin.idea.jira;

import javax.swing.*;

/**
 * @author Jacek Jaroczynski
 */
public class JiraTimeWdhmTextFieldListener extends ColoredTextFieldListener {
	private static final String REGEX = "^\\s*(\\d+w)?\\s*(\\d+d)?\\s*(\\d+h)?\\s*(\\d+m)?\\s*$";

	public JiraTimeWdhmTextFieldListener(final JTextField textField, boolean isEmptyOk) {
		super(textField, REGEX, isEmptyOk);
	}
}
