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
 * RemotePermissionScheme.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.jira.api.soap;

public class RemotePermissionScheme  extends com.atlassian.theplugin.jira.api.soap.RemoteScheme  implements java.io.Serializable {
    private com.atlassian.theplugin.jira.api.soap.RemotePermissionMapping[] permissionMappings;

    public RemotePermissionScheme() {
    }

    public RemotePermissionScheme(
           java.lang.String description,
           java.lang.Long id,
           java.lang.String name,
           java.lang.String type,
           com.atlassian.theplugin.jira.api.soap.RemotePermissionMapping[] permissionMappings) {
        super(
            description,
            id,
            name,
            type);
        this.permissionMappings = permissionMappings;
    }


    /**
     * Gets the permissionMappings value for this RemotePermissionScheme.
     * 
     * @return permissionMappings
     */
    public com.atlassian.theplugin.jira.api.soap.RemotePermissionMapping[] getPermissionMappings() {
        return permissionMappings;
    }


    /**
     * Sets the permissionMappings value for this RemotePermissionScheme.
     * 
     * @param permissionMappings
     */
    public void setPermissionMappings(com.atlassian.theplugin.jira.api.soap.RemotePermissionMapping[] permissionMappings) {
        this.permissionMappings = permissionMappings;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RemotePermissionScheme)) return false;
        RemotePermissionScheme other = (RemotePermissionScheme) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.permissionMappings==null && other.getPermissionMappings()==null) || 
             (this.permissionMappings!=null &&
              java.util.Arrays.equals(this.permissionMappings, other.getPermissionMappings())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getPermissionMappings() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPermissionMappings());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPermissionMappings(), i);
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
        new org.apache.axis.description.TypeDesc(RemotePermissionScheme.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("permissionMappings");
        elemField.setXmlName(new javax.xml.namespace.QName("", "permissionMappings"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionMapping"));
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
