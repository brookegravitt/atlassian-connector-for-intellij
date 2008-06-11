package com.atlassian.theplugin.commons.crucible.api.model;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jun 10, 2008
 * Time: 12:48:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CustomField {
    CustomFieldValueType getType();

    int getConfigVersion();

    String getFieldScope();

    Object getValue();

    String getHrValue();
}
