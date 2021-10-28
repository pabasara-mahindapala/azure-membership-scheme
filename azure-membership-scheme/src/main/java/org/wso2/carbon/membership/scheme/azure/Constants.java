/*
* Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.membership.scheme.azure;

import java.util.Collections;
import java.util.Set;

public class Constants {
    public static final String RESOURCE_GROUP_NAME = "";
    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";
    public static final String TENANT = "";
    public static final String SUBSCRIPTION_ID = "";
    public static final String INSTANCE = "https://login.microsoftonline.com/%s/";
    public static final Set<String> MANAGEMENT_DEFAULT_SCOPE = Collections.singleton("https://management.azure.com/.default");
    public static final String USE_SDK = "";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_VERSION = "2021-03-01";
    public static final String AZURE_API_ENDPOINT = "https://management.azure.com";
}
