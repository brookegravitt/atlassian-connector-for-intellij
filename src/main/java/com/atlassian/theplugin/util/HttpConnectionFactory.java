package com.atlassian.theplugin.util;

import javax.net.ssl.*;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-23
 * Time: 11:56:09
 * To change this template use File | Settings | File Templates.
 */
public class HttpConnectionFactory {
    private static SSLSocketFactory socketFactory;
    private static HostnameVerifier hostnameVerifier;

    public static URLConnection getConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        return getConnection(url);
    }

    public static URLConnection getConnection(URL url) throws IOException {
        URLConnection c = url.openConnection();
        if (c instanceof HttpsURLConnection) {
            HttpsURLConnection cs = (HttpsURLConnection) c;
            cs.setSSLSocketFactory(getSSLSocketFactory());
            cs.setHostnameVerifier(getHostnameVerifier());
        }
        return c;
    }

    private static SSLSocketFactory getSSLSocketFactory() {
        if (socketFactory == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new EasyTrustManager()
            };
            SSLContext sc;
            try {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
            socketFactory = sc.getSocketFactory();
        }
        return socketFactory;
    }

    private static HostnameVerifier getHostnameVerifier() {
        if (hostnameVerifier == null) {
            hostnameVerifier = new EasyHostnameVerifier();
        }
        return hostnameVerifier;
    }
}
