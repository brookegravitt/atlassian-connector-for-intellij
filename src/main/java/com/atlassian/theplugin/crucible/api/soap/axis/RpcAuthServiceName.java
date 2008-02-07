/**
 * RpcAuthServiceName.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.crucible.api.soap.axis;

public interface RpcAuthServiceName extends java.rmi.Remote {
    public java.lang.String login(java.lang.String arg0, java.lang.String arg1) throws java.rmi.RemoteException;
    public void logout(java.lang.String arg0) throws java.rmi.RemoteException;
}
