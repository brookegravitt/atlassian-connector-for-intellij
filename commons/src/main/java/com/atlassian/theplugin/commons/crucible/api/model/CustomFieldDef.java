package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jun 11, 2008
 * Time: 12:53:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CustomFieldDef {
    CustomFieldValueType getType();

    int getConfigVersion();

    String getFieldScope();

    String getName();

    String getLabel();

    CustomFieldValue getDefaultValue();

    List<CustomFieldValue> getValues();
}
