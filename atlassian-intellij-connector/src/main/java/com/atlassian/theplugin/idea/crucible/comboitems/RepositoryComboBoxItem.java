package com.atlassian.theplugin.idea.crucible.comboitems;

import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import org.jetbrains.annotations.NotNull;

public class RepositoryComboBoxItem {

	private final Repository repo;

	public RepositoryComboBoxItem(@NotNull final Repository repo) {
		this.repo = repo;
	}

	@Override
	public String toString() {
		return repo.getName();
	}

	public Repository getRepository() {
		return repo;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final RepositoryComboBoxItem that = (RepositoryComboBoxItem) o;

		//noinspection RedundantIfStatement
		if (!repo.equals(that.repo)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return repo.hashCode();
	}
}
