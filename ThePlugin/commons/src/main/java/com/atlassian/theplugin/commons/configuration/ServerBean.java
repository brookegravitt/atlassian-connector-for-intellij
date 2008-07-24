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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.thirdparty.base64.Base64;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bean representing a single server.
 */
public class ServerBean implements Server {

	private long uid = 0;
	private String name = "";
	private String urlString = "";
	//private char[] encryptedPassword = new char[0];
	private String encryptedPassword = "";
	private String username = "";
	private Boolean shouldPasswordBeStored = false;
	private String password = "";
	private Boolean enabled = true;
	private Boolean useFavourite = false;
	private boolean isBamboo2 = false;

	private Collection<SubscribedPlan> subscribedPlans = new ArrayList<SubscribedPlan>();

	private transient Boolean isConfigInitialized = false;
	private static final double ID_DISCRIMINATOR = 1000d;

	public ServerBean() {

		uid = System.currentTimeMillis() + (long) (Math.random() * ID_DISCRIMINATOR);
	}

	public ServerBean(Server cfg) {
		this.setName(cfg.getName());
		this.setUid(cfg.getUid());
		this.setUserName(cfg.getUserName());
		this.transientSetPasswordString(cfg.transientGetPasswordString(), cfg.getShouldPasswordBeStored());
		this.setUrlString(cfg.getUrlString());
		this.setEnabled(cfg.getEnabled());
		this.setUseFavourite(cfg.getUseFavourite());
		this.transientSetIsConfigInitialized(cfg.getIsConfigInitialized());

		for (SubscribedPlan plan : cfg.transientGetSubscribedPlans()) {
			SubscribedPlan newPlan = new SubscribedPlanBean(plan);
			subscribedPlans.add(newPlan);
		}
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized String getUrlString() {
		return urlString;
	}

	public synchronized void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	public synchronized String getUserName() {
		return username;
	}

	public synchronized void setUserName(String anUsername) {
		this.username = anUsername;
	}

	public synchronized Boolean getEnabled() {
		return this.enabled;
	}

	public synchronized void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public synchronized Boolean getUseFavourite() {
		return this.useFavourite;
	}

	public synchronized void setUseFavourite(Boolean useFavourite) {
		this.useFavourite = useFavourite;
	}

//    public char[] getEncryptedPassword() {
//        if(encryptedPassword == null) {
//            encryptedPassword = new char[0];
//        }
//        return encryptedPassword;
//    }


	private synchronized String decode(String str2decode) {
		try {

			byte[] passwordBytes = Base64.decode(str2decode);

			/*if passwordBytes is null means that we tried to decode password with
						 * not supported characters or just password hasn't been encoded yet
						 * in this situation clear password*/
			if (passwordBytes == null) {
				shouldPasswordBeStored = false;
				return "";
			}

			return new String(passwordBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		}

	}

	private synchronized String encode(String str2encode) {
		try {
			return Base64.encodeBytes(str2encode.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		}
	}

	/**
	 * This one should be used by persistence logic ONLY.
	 * <p/>
	 * From the code you must use {@link #transientSetPasswordString(String, Boolean)},
	 * unless you really know what you are doing.
	 *
	 * @param encryptedPassword encrypted (encoded actually) version of the password
	 */
	public void setEncryptedPassword(String encryptedPassword) {
		password = decode(encryptedPassword);
		this.encryptedPassword = encryptedPassword;
		//if (password.length() > 0) {
			isConfigInitialized = true;
//		} else {
//			isConfigInitialized = false;
//		}
	}

	/**
	 * This one should be used by persistence logic ONLY.
	 *
	 * @return encoded version of the password.
	 */
	public String getEncryptedPassword() {
		return encryptedPassword;
	}


	public synchronized Boolean getShouldPasswordBeStored() {
		return shouldPasswordBeStored;
	}

	public synchronized Boolean getIsConfigInitialized() {
		return isConfigInitialized;
	}

	//@Transient
	public synchronized void transientSetIsConfigInitialized(Boolean isInitialized) {
		this.isConfigInitialized = isInitialized;
	}

	public synchronized void setShouldPasswordBeStored(Boolean shouldPasswordBeStored) {
		this.shouldPasswordBeStored = shouldPasswordBeStored;
	}

	//@Transient
	public synchronized Collection<SubscribedPlan> transientGetSubscribedPlans() {
		return subscribedPlans;
	}

	//@Transient
	public synchronized void transientSetSubscribedPlans(Collection<SubscribedPlan> plans) {
		this.subscribedPlans = plans;
	}


	public synchronized List<SubscribedPlanBean> getSubscribedPlansData() {
		List<SubscribedPlanBean> beanList = new ArrayList<SubscribedPlanBean>();
		for (SubscribedPlan subscribedPlan : subscribedPlans) {
			beanList.add(new SubscribedPlanBean(subscribedPlan));
		}
		return beanList;
	}

	public synchronized void setSubscribedPlansData(List<SubscribedPlan> subscribedPlansData) {
		this.subscribedPlans = subscribedPlansData;
	}

	//@Transient
	public synchronized String transientGetPasswordString() {
		return password;
	}

	//@Transient
	public synchronized void transientSetPasswordString(String aPassword, Boolean shouldBeStoredPermanently) {
		this.shouldPasswordBeStored = shouldBeStoredPermanently;
		this.password = aPassword;
		//if (password.length() > 0) {
			isConfigInitialized = true;
//		} else {
//			isConfigInitialized = false;
//		}
//        if (shouldBeStoredPermanently && encryptedPassword != null) {
//            this.encryptedPassword = new char[encryptedPassword.length()];
//            encryptedPassword.getChars(0, encryptedPassword.length(), this.encryptedPassword, 0);
//        }
		if (shouldBeStoredPermanently) {
			this.encryptedPassword = encode(aPassword);
		} else {
			this.encryptedPassword = "";
		}


	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public boolean isBamboo2() {
		return isBamboo2;
	}

	public void setIsBamboo2(boolean bamboo2) {
		isBamboo2 = bamboo2;
	}

	private static final int ONE_EFF = 31;
	private static final int TWO_ZERO = 32;

	public int hashCode() {
		int result;
		result = (int) (uid ^ (uid >>> TWO_ZERO));
		result = ONE_EFF * result + (name != null ? name.hashCode() : 0);
		result = ONE_EFF * result + (urlString != null ? urlString.hashCode() : 0);
		result = ONE_EFF * result + (encryptedPassword != null ? encryptedPassword.hashCode() : 0);
		result = ONE_EFF * result + (username != null ? username.hashCode() : 0);
		result = ONE_EFF * result + (shouldPasswordBeStored != null ? shouldPasswordBeStored.hashCode() : 0);
		result = ONE_EFF * result + (password != null ? password.hashCode() : 0);
		result = ONE_EFF * result + (subscribedPlans != null ? subscribedPlans.hashCode() : 0);
		result = ONE_EFF * result + (isConfigInitialized != null ? isConfigInitialized.hashCode() : 0);
		return result;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !(o instanceof Server)) {
			return false;
		}

		Server that = (Server) o;

		if (uid != that.getUid()) {
			return false;
		}
//		if (encryptedPassword != null ? !encryptedPassword.equals(that.encryptedPassword) : that.encryptedPassword != null) {
//			return false;
//		}
		if (isConfigInitialized != null
				? !isConfigInitialized.equals(that.getIsConfigInitialized()) : that.getIsConfigInitialized() != null) {
			return false;
		}
		if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
			return false;
		}
		if (password != null ? !password.equals(that.transientGetPasswordString())  
				: that.transientGetPasswordString() != null) {
			return false;
		}
		if (shouldPasswordBeStored != null
				? !shouldPasswordBeStored.equals(that.getShouldPasswordBeStored())
				: that.getShouldPasswordBeStored() != null) {
			return false;
		}
		if (enabled != null ? !enabled.equals(that.getEnabled()) : that.getEnabled() != null) {
			return false;
		}
		if (useFavourite != null ? !useFavourite.equals(that.getUseFavourite()) : that.getUseFavourite() != null) {
			return false;
		}
		if (subscribedPlans != null ? !subscribedPlans.equals(that.transientGetSubscribedPlans())
                : that.transientGetSubscribedPlans() != null) {
			return false;
		}
		if (urlString != null ? !urlString.equals(that.getUrlString()) : that.getUrlString() != null) {
			return false;
		}
		if (username != null ? !username.equals(that.getUserName()) : that.getUserName() != null) {
			return false;
		}
		// runtime value - NOT changed configuration
/*
		if (isBamboo2 != that.isBamboo2()) {
			return false;
		}
*/
		return true;
	}
}