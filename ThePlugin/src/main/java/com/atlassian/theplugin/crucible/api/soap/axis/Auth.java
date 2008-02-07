/**
 * Auth.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.crucible.api.soap.axis;

public interface Auth extends javax.xml.rpc.Service {
    public java.lang.String getAuthPortAddress();

    public RpcAuthServiceName getAuthPort() throws javax.xml.rpc.ServiceException;

    public RpcAuthServiceName getAuthPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
