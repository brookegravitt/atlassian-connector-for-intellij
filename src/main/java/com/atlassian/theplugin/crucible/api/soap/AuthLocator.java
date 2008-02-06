/**
 * AuthLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.crucible.api.soap;

public class AuthLocator extends org.apache.axis.client.Service implements com.atlassian.theplugin.crucible.api.soap.Auth {

    public AuthLocator() {
    }


    public AuthLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AuthLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AuthPort
    private java.lang.String AuthPort_address = "http://lech.atlassian.pl:8060/service/auth";

    public java.lang.String getAuthPortAddress() {
        return AuthPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthPortWSDDServiceName = "AuthPort";

    public java.lang.String getAuthPortWSDDServiceName() {
        return AuthPortWSDDServiceName;
    }

    public void setAuthPortWSDDServiceName(java.lang.String name) {
        AuthPortWSDDServiceName = name;
    }

    public com.atlassian.theplugin.crucible.api.soap.RpcAuthServiceName getAuthPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AuthPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthPort(endpoint);
    }

    public com.atlassian.theplugin.crucible.api.soap.RpcAuthServiceName getAuthPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.atlassian.theplugin.crucible.api.soap.AuthSoapBindingStub _stub = new com.atlassian.theplugin.crucible.api.soap.AuthSoapBindingStub(portAddress, this);
            _stub.setPortName(getAuthPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthPortEndpointAddress(java.lang.String address) {
        AuthPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.atlassian.theplugin.crucible.api.soap.RpcAuthServiceName.class.isAssignableFrom(serviceEndpointInterface)) {
                com.atlassian.theplugin.crucible.api.soap.AuthSoapBindingStub _stub = new com.atlassian.theplugin.crucible.api.soap.AuthSoapBindingStub(new java.net.URL(AuthPort_address), this);
                _stub.setPortName(getAuthPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AuthPort".equals(inputPortName)) {
            return getAuthPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://rpc.spi.crucible.atlassian.com/", "Auth");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://rpc.spi.crucible.atlassian.com/", "AuthPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AuthPort".equals(portName)) {
            setAuthPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
