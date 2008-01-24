package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * BambooConfigurationBean for a single Bamboo server.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:51:08 AM
 */
public class ServerBean implements Server{
    private String name;
    private String urlString;
    //private char[] encryptedPassword = new char[0];
    private String encryptedPassword;
    private String username;
    private Boolean shouldPasswordBeStored;
    private String password;


    private List<SubscribedPlanBean> subscribedPlans = new ArrayList<SubscribedPlanBean>();

    private Boolean isConfigInitialized = false;

    public ServerBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

//    public char[] getEncryptedPassword() {
//        if(encryptedPassword == null) {
//            encryptedPassword = new char[0];
//        }
//        return encryptedPassword;
//    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }


//    public void setEncryptedPassword(char[] encryptedPassword) {
//        if (encryptedPassword == null) {
//            this.encryptedPassword = new char[0];
//        } else {
//            this.encryptedPassword = encryptedPassword;
//        }
//    }

    private String decode(String str2decode) {
        return str2decode;
    }


    private String encode(String str2encode) {
        return str2encode;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        password = decode(encryptedPassword);
        this.encryptedPassword = encryptedPassword;
        isConfigInitialized = true;
    }

    public Boolean getShouldPasswordBeStored() {
        return shouldPasswordBeStored;
    }

    public Boolean getIsConfigInitialized() {
        return isConfigInitialized;
    }

    @Transient
    public void setIsConfigInitialized(Boolean isConfigInitialized) {
        this.isConfigInitialized = isConfigInitialized;
    }

    public void setShouldPasswordBeStored(Boolean shouldPasswordBeStored) {
        this.shouldPasswordBeStored = shouldPasswordBeStored;
    }

    @Transient
    public Collection<? extends SubscribedPlan> getSubscribedPlans() {
        return subscribedPlans;
    }

    @Transient
    public String getPasswordString() throws ServerPasswordNotProvidedException {
        if (!isConfigInitialized) {
            throw new ServerPasswordNotProvidedException("User password for \""+ name +"\" server not provided.");
        }
        return password;
    }

    @Transient
    public void setPasswordString(String password, Boolean shouldBeStoredPermanently) {
        this.shouldPasswordBeStored = shouldBeStoredPermanently;
        this.password = password;
        isConfigInitialized = true;
//        if (shouldBeStoredPermanently && encryptedPassword != null) {
//            this.encryptedPassword = new char[encryptedPassword.length()];
//            encryptedPassword.getChars(0, encryptedPassword.length(), this.encryptedPassword, 0);
//        }
        if (shouldBeStoredPermanently) {
            this.encryptedPassword = encode(password);
        } else {
            this.encryptedPassword = null;
        }


    }

    public List<SubscribedPlanBean> getSubscribedPlansData() {
        return subscribedPlans;
    }

    public void setSubscribedPlansData(List<SubscribedPlanBean> subscribedPlansData) {
        this.subscribedPlans = subscribedPlansData;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
    }
}