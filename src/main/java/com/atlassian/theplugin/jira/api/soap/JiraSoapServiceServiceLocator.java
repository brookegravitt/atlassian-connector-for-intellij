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

/**
 * JiraSoapServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.jira.api.soap;

public class JiraSoapServiceServiceLocator extends org.apache.axis.client.Service implements com.atlassian.theplugin.jira.api.soap.JiraSoapServiceService {

    public JiraSoapServiceServiceLocator() {
    }


    public JiraSoapServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public JiraSoapServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for JirasoapserviceV2
    private java.lang.String JirasoapserviceV2_address = "http://jira.atlassian.com/rpc/soap/jirasoapservice-v2";

    public java.lang.String getJirasoapserviceV2Address() {
        return JirasoapserviceV2_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String JirasoapserviceV2WSDDServiceName = "jirasoapservice-v2";

    public java.lang.String getJirasoapserviceV2WSDDServiceName() {
        return JirasoapserviceV2WSDDServiceName;
    }

    public void setJirasoapserviceV2WSDDServiceName(java.lang.String name) {
        JirasoapserviceV2WSDDServiceName = name;
    }

    public com.atlassian.theplugin.jira.api.soap.JiraSoapService getJirasoapserviceV2() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(JirasoapserviceV2_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getJirasoapserviceV2(endpoint);
    }

    public com.atlassian.theplugin.jira.api.soap.JiraSoapService getJirasoapserviceV2(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.atlassian.theplugin.jira.api.soap.JirasoapserviceV2SoapBindingStub _stub = new com.atlassian.theplugin.jira.api.soap.JirasoapserviceV2SoapBindingStub(portAddress, this);
            _stub.setPortName(getJirasoapserviceV2WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setJirasoapserviceV2EndpointAddress(java.lang.String address) {
        JirasoapserviceV2_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.atlassian.theplugin.jira.api.soap.JiraSoapService.class.isAssignableFrom(serviceEndpointInterface)) {
                com.atlassian.theplugin.jira.api.soap.JirasoapserviceV2SoapBindingStub _stub = new com.atlassian.theplugin.jira.api.soap.JirasoapserviceV2SoapBindingStub(new java.net.URL(JirasoapserviceV2_address), this);
                _stub.setPortName(getJirasoapserviceV2WSDDServiceName());
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
        if ("jirasoapservice-v2".equals(inputPortName)) {
            return getJirasoapserviceV2();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "JiraSoapServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "jirasoapservice-v2"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("JirasoapserviceV2".equals(portName)) {
            setJirasoapserviceV2EndpointAddress(address);
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
