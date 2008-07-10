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

package com.atlassian.theplugin.commons.configuration;

import com.atlassian.theplugin.commons.util.Version;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-15
 * Time: 10:11:25
 * To change this template use File | Settings | File Templates.
 */
public class GeneralConfigurationBean {

	private boolean autoUpdateEnabled = true;
	private Version rejectedUpgrade = Version.NULL_VERSION;
	private boolean checkUnstableVersionsEnabled = false;
	private Boolean anonymousFeedbackEnabled = null;
	private boolean useIdeaProxySettings = true;
	private Collection<String> certs = new HashSet<String>();
	
	private long uid = 0;

	private static final double ID_DISCRIMINATOR = 1e3d;

	public GeneralConfigurationBean() {

	}

	public GeneralConfigurationBean(GeneralConfigurationBean generalConfigurationData) {
		this.anonymousFeedbackEnabled = generalConfigurationData.getAnonymousFeedbackEnabled();
		this.rejectedUpgrade = generalConfigurationData.getRejectedUpgrade();
		this.checkUnstableVersionsEnabled = generalConfigurationData.isCheckUnstableVersionsEnabled();
		this.autoUpdateEnabled = generalConfigurationData.isAutoUpdateEnabled();
		this.uid = generalConfigurationData.getUid();
		this.useIdeaProxySettings = generalConfigurationData.getUseIdeaProxySettings();
		this.certs = generalConfigurationData.getCerts();
	}

	public long getUid() {
		if (anonymousFeedbackEnabled != null && anonymousFeedbackEnabled) {
			if (uid == 0) {
				// generate if there was no uid yet
				uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);
			}
		} else {
			return 0;
		}
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public boolean isAutoUpdateEnabled() {
		return autoUpdateEnabled;
	}

	public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
		this.autoUpdateEnabled = autoUpdateEnabled;
	}

	public Version getRejectedUpgrade() {
		return rejectedUpgrade;
	}

	public void setRejectedUpgrade(Version rejectedUpgrade) {
		this.rejectedUpgrade = rejectedUpgrade;
	}

	public void setCheckUnstableVersionsEnabled(boolean checkUnstableVersionsEnabled) {
		this.checkUnstableVersionsEnabled = checkUnstableVersionsEnabled;
	}

	public boolean isCheckUnstableVersionsEnabled() {
		return checkUnstableVersionsEnabled;
	}

	public Boolean getAnonymousFeedbackEnabled() {
		return anonymousFeedbackEnabled;
	}

	public void setAnonymousFeedbackEnabled(boolean isAnonymousFeedbackEnabled) {
		this.anonymousFeedbackEnabled = isAnonymousFeedbackEnabled;
	}

	public void setAnonymousFeedbackEnabled(Boolean isAnonymousFeedbackEnabled) {
		this.anonymousFeedbackEnabled = isAnonymousFeedbackEnabled;
	}

	public boolean getUseIdeaProxySettings() {
		return useIdeaProxySettings;
	}

	public Collection<String> getCerts() {
		return certs;
	}

	public void setCerts(Collection<String> certs) {
		this.certs = certs;
	}

	public void setUseIdeaProxySettings(boolean use) {
		useIdeaProxySettings = use;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		GeneralConfigurationBean that = (GeneralConfigurationBean) o;

		if (autoUpdateEnabled != that.autoUpdateEnabled) {
			return false;
		}
		if (checkUnstableVersionsEnabled != that.checkUnstableVersionsEnabled) {
			return false;
		}
		if (uid != that.uid) {
			return false;
		}
		if (anonymousFeedbackEnabled != null
				? !anonymousFeedbackEnabled.equals(that.anonymousFeedbackEnabled)
				: that.anonymousFeedbackEnabled != null) {
			return false;
		}
		if (rejectedUpgrade != null ? !rejectedUpgrade.equals(that.rejectedUpgrade) : that.rejectedUpgrade != null) {
			return false;
		}
		if (!certs.equals(that.certs)) {
			return false;
		}
		return true;
	}

	private static final int THIRTY_ONE = 31;
	private static final int THIRTY_TWO = 32;

	public int hashCode() {
		int result;
		result = (autoUpdateEnabled ? 1 : 0);
		result = THIRTY_ONE * result + (rejectedUpgrade != null ? rejectedUpgrade.hashCode() : 0);
		result = THIRTY_ONE * result + (checkUnstableVersionsEnabled ? 1 : 0);
		result = THIRTY_ONE * result + (anonymousFeedbackEnabled != null ? anonymousFeedbackEnabled.hashCode() : 0);
		result = THIRTY_ONE * result + (int) (uid ^ (uid >>> THIRTY_TWO));
		result = THIRTY_ONE * result + certs.hashCode();
		return result;
	}

}
