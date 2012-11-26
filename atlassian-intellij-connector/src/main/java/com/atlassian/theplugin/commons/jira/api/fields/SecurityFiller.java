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
package com.atlassian.theplugin.commons.jira.api.fields;


import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class SecurityFiller implements FieldFiller {
	public List<String> getFieldValues(final String field, final JIRAIssue detailedIssue) {
		if (detailedIssue.getSecurityLevel() == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		result.add("" + detailedIssue.getSecurityLevel().getId());
		return result;
	}

    @Override
    public FieldInput generateJrJcFieldValue(JIRAIssue issue, JIRAActionField field, JSONObject fieldMetadata) throws JSONException {
        List<String> values = field.getValues();
        if (values == null || values.size() == 0) {
            return new FieldInput(field.getFieldId(), null);
        }
        return new FieldInput(field.getFieldId(), new ComplexIssueInputFieldValue(ImmutableMap.of("id", (Object) values.get(0))));
    }
}
