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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jacek Jaroczynski
 */
public class ColoredTextFieldListener implements DocumentListener {
	private JTextField field;
	private String regexp;
	private boolean emptyOk;

	public ColoredTextFieldListener(final JTextField textField, final String regexp, final boolean emptyOk) {
		this.field = textField;
		this.regexp = regexp;
		this.emptyOk = emptyOk;
	}

	public JTextField getField() {
		return field;
	}

	public void insertUpdate(final DocumentEvent e) {
		stateChanged();
	}

	public void removeUpdate(final DocumentEvent e) {
		stateChanged();
	}

	public void changedUpdate(final DocumentEvent e) {
		stateChanged();
	}

	/**
	 * Makes the color of input text red in case of wrong syntax
	 *
	 * @return true if there is no syntaxt error
	 */
	public boolean stateChanged() {

		boolean correct = isCorrect(field.getText());

		Color c = correct ? Color.BLACK : Color.RED;
		field.setForeground(c);

		return correct;
	}

	private boolean isCorrect(final String text) {

		if (emptyOk && (text == null || text.length() == 0)) {
			return true;
		}

		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(text);
		return m.matches() && text.length() > 0;
	}
}
