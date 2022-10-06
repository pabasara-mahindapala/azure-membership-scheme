/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.membership.scheme.azure;

import com.hazelcast.internal.json.JsonArray;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.json.JsonValue;
import org.apache.axis2.description.Parameter;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.util.Map;

/**
 * Util functions for azure membership scheme.
 */
public class Utils {

    /**
     * Handle Exception and throw AzureMembershipSchemeException.
     *
     * @param errorMessage Error Message
     * @param data         Data
     * @return AzureMembershipSchemeException
     */
    public static AzureMembershipSchemeException handleException(Constants.ErrorMessage errorMessage, String data) {

        String description;
        if (StringUtils.isNotBlank(data)) {
            description = String.format(errorMessage.getDescription(), data);
        } else {
            description = errorMessage.getDescription();
        }
        return new AzureMembershipSchemeException(errorMessage.getMessage(), description);
    }

    /**
     * Handle Exception and throw AzureMembershipSchemeException.
     *
     * @param errorMessage Error Message
     * @return AzureMembershipSchemeException
     */
    public static AzureMembershipSchemeException handleException(Constants.ErrorMessage errorMessage) {

        return new AzureMembershipSchemeException(errorMessage.getMessage(), errorMessage.getDescription());
    }

    /**
     * Handle Exception and throw AzureMembershipSchemeException.
     *
     * @param errorMessage Error Message
     * @param cause        Throwable cause
     * @return AzureMembershipSchemeException
     */
    public static AzureMembershipSchemeException handleException(Constants.ErrorMessage errorMessage, Throwable cause) {

        String description = errorMessage.getDescription();
        return new AzureMembershipSchemeException(errorMessage.getMessage(), description, cause);
    }

    /**
     * @param parameterName Name of the parameter
     * @param defaultValue  Default value
     * @param parameters    Map of parameters
     * @return Value of the parameter, or the default value
     * @throws AzureMembershipSchemeException if the default value is null
     */
    public static String getParameterValueOrDefault(String parameterName, String defaultValue,
                                                    Map<String, Parameter> parameters)
            throws AzureMembershipSchemeException {

        Parameter azureServicesParam = parameters.get(parameterName);
        if (azureServicesParam == null) {
            if (defaultValue == null) {
                throw Utils.handleException(Constants.ErrorMessage.PARAMETER_NOT_FOUND, parameterName);
            } else {
                return defaultValue;
            }
        }
        return String.valueOf(azureServicesParam.getValue());
    }

    /**
     * @param parameterName Name of the parameter
     * @param parameters    Map of parameters
     * @return Value of the parameter
     * @throws AzureMembershipSchemeException if the parameter is not found
     */
    public static String getParameterValue(String parameterName, Map<String, Parameter> parameters)
            throws AzureMembershipSchemeException {

        Parameter azureServicesParam = parameters.get(parameterName);
        if (azureServicesParam == null) {
            throw Utils.handleException(Constants.ErrorMessage.PARAMETER_NOT_FOUND, parameterName);
        }
        return String.valueOf(azureServicesParam.getValue());
    }

    /**
     * @param parameterName Name of the parameter
     * @param parameters    Map of parameters
     * @return Value of the parameter, or null if the parameter is not found
     */
    public static String getParameterValueOrNull(String parameterName, Map<String, Parameter> parameters) {

        Parameter azureServicesParam = parameters.get(parameterName);
        if (azureServicesParam == null) {
            return null;
        }
        return String.valueOf(azureServicesParam.getValue());
    }

    /**
     * Check a String is Not Null or Empty After Trim.
     *
     * @param s
     * @return True or false
     */
    public static boolean isNotNullOrEmptyAfterTrim(String s) {

        return s != null && !s.trim().isEmpty();
    }

    /**
     * Convert JsonValue to JsonArray.
     *
     * @param jsonValue JsonValue
     * @return JsonArray
     */
    public static JsonArray toJsonArray(JsonValue jsonValue) {

        if (jsonValue == null || jsonValue.isNull()) {
            return new JsonArray();
        } else {
            return jsonValue.asArray();
        }
    }

    /**
     * Convert JsonValue to JsonObject.
     *
     * @param jsonValue JsonValue
     * @return JsonObject
     */
    public static JsonObject toJsonObject(JsonValue jsonValue) {

        if (jsonValue == null || jsonValue.isNull()) {
            return new JsonObject();
        } else {
            return jsonValue.asObject();
        }
    }

}
