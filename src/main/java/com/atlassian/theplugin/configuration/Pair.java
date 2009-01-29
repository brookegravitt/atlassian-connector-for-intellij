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
package com.atlassian.theplugin.configuration;

import java.util.ArrayList;

/**
 * @author Jacek Jaroczynski
 */
public class Pair {

	private String key = "";
	private ArrayList<Long> value = new ArrayList<Long>();

	private static final int HASH_INT = 31;

	public Pair() {
	}

	public Pair(final String key, final ArrayList<Long> value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public ArrayList<Long> getValue() {
		return value;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public void setValue(final ArrayList<Long> newValue) {
		this.value = newValue;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Pair pair = (Pair) o;

		if (key != null ? !key.equals(pair.key) : pair.key != null) {
			return false;
		}
		if (value != null ? !value.equals(pair.value) : pair.value != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (key != null ? key.hashCode() : 0);
		result = HASH_INT * result + (value != null ? value.hashCode() : 0);
		return result;
	}
}
