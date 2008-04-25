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
 * RemotePermissionMapping.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.jira.api.soap;

public class RemotePermissionMapping  implements java.io.Serializable {
    private com.atlassian.theplugin.jira.api.soap.RemotePermission permission;

    private com.atlassian.theplugin.jira.api.soap.RemoteEntity[] remoteEntities;

    public RemotePermissionMapping() {
    }

    public RemotePermissionMapping(
           com.atlassian.theplugin.jira.api.soap.RemotePermission permission,
           com.atlassian.theplugin.jira.api.soap.RemoteEntity[] remoteEntities) {
           this.permission = permission;
           this.remoteEntities = remoteEntities;
    }


    /**
     * Gets the permission value for this RemotePermissionMapping.
     * 
     * @return permission
     */
    public com.atlassian.theplugin.jira.api.soap.RemotePermission getPermission() {
        return permission;
    }


    /**
     * Sets the permission value for this RemotePermissionMapping.
     * 
     * @param permission
     */
    public void setPermission(com.atlassian.theplugin.jira.api.soap.RemotePermission permission) {
        this.permission = permission;
    }


    /**
     * Gets the remoteEntities value for this RemotePermissionMapping.
     * 
     * @return remoteEntities
     */
    public com.atlassian.theplugin.jira.api.soap.RemoteEntity[] getRemoteEntities() {
        return remoteEntities;
    }


    /**
     * Sets the remoteEntities value for this RemotePermissionMapping.
     * 
     * @param remoteEntities
     */
    public void setRemoteEntities(com.atlassian.theplugin.jira.api.soap.RemoteEntity[] remoteEntities) {
        this.remoteEntities = remoteEntities;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RemotePermissionMapping)) return false;
        RemotePermissionMapping other = (RemotePermissionMapping) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.permission==null && other.getPermission()==null) || 
             (this.permission!=null &&
              this.permission.equals(other.getPermission()))) &&
            ((this.remoteEntities==null && other.getRemoteEntities()==null) || 
             (this.remoteEntities!=null &&
              java.util.Arrays.equals(this.remoteEntities, other.getRemoteEntities())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getPermission() != null) {
            _hashCode += getPermission().hashCode();
        }
        if (getRemoteEntities() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRemoteEntities());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemoteEntities(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RemotePermissionMapping.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionMapping"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permission");
        elemField.setXmlName(new javax.xml.namespace.QName("", "permission"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermission"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remoteEntities");
        elemField.setXmlName(new javax.xml.namespace.QName("", "remoteEntities"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteEntity"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
