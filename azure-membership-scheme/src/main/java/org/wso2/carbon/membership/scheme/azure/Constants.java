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

import java.util.Collections;
import java.util.Set;

/**
 * Constant values for azure membership scheme.
 */
public class Constants {

    public static final String INSTANCE = "https://login.microsoftonline.com/%s/";
    public static final Set<String> MANAGEMENT_DEFAULT_SCOPE =
            Collections.singleton("https://management.azure.com/.default");
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String DEFAULT_API_ENDPOINT = "https://management.azure.com";
    public static final String DEFAULT_API_VERSION = "2021-03-01";

    public static final String PARAMETER_NAME_CLIENT_ID = "AZURE_CLIENT_ID";
    public static final String PARAMETER_NAME_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    public static final String PARAMETER_NAME_TENANT = "AZURE_TENANT";
    public static final String PARAMETER_NAME_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";
    public static final String PARAMETER_NAME_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";
    public static final String PARAMETER_NAME_USE_SDK = "USE_SDK";
    public static final String PARAMETER_NAME_API_VERSION = "AZURE_API_VERSION";
    public static final String PARAMETER_NAME_API_ENDPOINT = "AZURE_API_ENDPOINT";

    /**
     * Azure membership scheme error codes.
     */
    public enum ErrorMessage {

        COULD_NOT_BUILD_CCA("Could not build ConfidentialClientApplication.",
                "Could not build ConfidentialClientApplication."),
        FAILED_TO_CONNECT("Failed to open connection.", "Failed to open connection."),
        NO_MEMBERS_FOUND("No members found.",
                "No members found, unable to initialize the Azure membership scheme."),
        PARAMETER_NOT_FOUND("Parameter not found.", "'%s' parameter not found."),
        NO_IPS_FOUND("No IPs found", "No IPs found at '%s'"),
        COULD_NOT_CREATE_URL("Could not create endpoint URL", "Could not create endpoint URL"),
        COULD_NOT_READ_API("Could not read from Azure API", "Could not read from Azure API"),
        FAILED_TO_AUTHENTICATE_COMPUTEMANAGER("Failed to authenticate azure ComputeManager",
                "Failed to authenticate azure ComputeManager");

        private final String message;
        private final String description;

        ErrorMessage(String message, String description) {

            this.message = message;
            this.description = description;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }
}
