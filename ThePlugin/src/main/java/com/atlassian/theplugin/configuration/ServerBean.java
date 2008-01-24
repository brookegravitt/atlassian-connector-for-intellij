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
    //private char[] password = new char[0];
    private String password;
    private String username;
    transient private Boolean shouldPasswordBeStored;
    private String tmpPassword;


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

//    public char[] getPassword() {
//        if(password == null) {
//            password = new char[0];
//        }
//        return password;
//    }

    public String getPassword() {
        return password;
    }


//    public void setPassword(char[] password) {
//        if (password == null) {
//            this.password = new char[0];
//        } else {
//            this.password = password;
//        }
//    }

    private String decode(String str2decode) {
        return str2decode;
    }


    private String encode(String str2encode) {
        return str2encode;
    }

    public void setPassword(String password) {
        tmpPassword = decode(password);
        this.password = password;
    }

    public Boolean getShouldPasswordBeStored() {
        return shouldPasswordBeStored;
    }

    public Boolean getIsConfigInitialized() {
        return isConfigInitialized;
    }

    @Transient
    public void setIsConfigInitialized(Boolean hasUSerSessionStarted) {
        this.isConfigInitialized = hasUSerSessionStarted;
    }

    public void setShouldPasswordBeStored(Boolean shouldPasswordBeStored) {
        this.shouldPasswordBeStored = shouldPasswordBeStored;
    }

    @Transient
    public Collection<? extends SubscribedPlan> getSubscribedPlans() {
        return subscribedPlans;
    }

    @Transient
    public String getPasswordString() throws ServerPasswordNotProvidedExeption {
        if (!isConfigInitialized) {
            throw new ServerPasswordNotProvidedExeption("User password for \""+ name +"\" server not provided.");
        }
        return tmpPassword;
    }

    @Transient
    public void setPasswordString(String password, Boolean shouldBeStoredPermanently) {
        this.shouldPasswordBeStored = shouldBeStoredPermanently;
        this.tmpPassword = password;
        isConfigInitialized = true;
//        if (shouldBeStoredPermanently && password != null) {
//            this.password = new char[password.length()];
//            password.getChars(0, password.length(), this.password, 0);
//        }
        if (shouldBeStoredPermanently) {
            this.password = encode(password);
        } else {
            this.password = null;
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