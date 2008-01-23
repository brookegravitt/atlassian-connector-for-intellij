package com.atlassian.theplugin.bamboo.api;

import javax.net.ssl.*;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

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
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (KeyManagementException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            socketFactory = sc.getSocketFactory();
        }
        return socketFactory;
    }

    private static HostnameVerifier getHostnameVerifier() {
        if (hostnameVerifier == null) {
            hostnameVerifier = new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };
        }
        return hostnameVerifier;
    }
}
