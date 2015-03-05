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
package com.atlassian.theplugin.idea.config;

public class GenericComboBoxItemWrapper<T> {
	protected T wrapped;

	public GenericComboBoxItemWrapper(final T wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GenericComboBoxItemWrapper)) {
			return false;
		}

		final GenericComboBoxItemWrapper<?> that = (GenericComboBoxItemWrapper<?>) o;

		if (wrapped != null ? !wrapped.equals(that.wrapped) : that.wrapped != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (wrapped != null ? wrapped.hashCode() : 0);
	}

	@Override
	public String toString() {
		if (wrapped != null) {
			return wrapped.toString();
		} else {
			return "None";
		}
	}

	public T getWrapped() {
		return wrapped;
	}
}
