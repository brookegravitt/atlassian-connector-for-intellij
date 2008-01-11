package com.atlassian.theplugin.api.bamboo;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 11:54:01
 * To change this template use File | Settings | File Templates.
 */
public class RestApi {
    private String baseUrl;
    private String authToken;

    private RestApi(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public static RestApi login(String url, String name, String password) throws Exception {
        SAXBuilder builder = new SAXBuilder();

        URL loginUrl = null;
        try {
            loginUrl = new URL(url + "/api/rest/login.action?username=" + name + "&password=" + password + "&os_username=" + name + "&os_password=" + password);
        } catch (MalformedURLException e) {
            throw new BambooLoginException(e.getMessage(), e.getCause());
        }

        List elements = null;
        try {
            Document doc = builder.build(loginUrl);
            checkForErrors(doc);
            XPath xpath = XPath.newInstance("/response/auth");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    String authToken = e.getText();
                    return new RestApi(url, authToken);
                }
            }
        }catch(JDOMException e){
            throw new BambooLoginException(e.getMessage(), e);
        }catch(IOException e){
            throw new BambooLoginException(e.getMessage(), e);
        }
        return null;
    }

    private static void checkForErrors(Document doc) throws JDOMException, BambooLoginException {
        XPath xpath = XPath.newInstance("/errors/error");
        List elements = xpath.selectNodes(doc);
        if (elements != null && elements.size() > 0) {
            Element e = (Element) elements.iterator().next();
            throw new BambooLoginException(e.getText());
        }
    }
}
