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

package org.wso2.carbon.membership.scheme.azure.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.AzureAuthenticator;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AzureHttpsApiEndpoint extends AzureApiEndpoint {

    private static final Log log = LogFactory.getLog(AzureHttpsApiEndpoint.class);

    public AzureHttpsApiEndpoint(URL url) {
        super(url);
    }

    @Override
    public void createConnection() throws IOException, AzureMembershipSchemeException {
        log.debug("Connecting to Azure API server...");
        connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty(Constants.AUTHORIZATION_HEADER, "Bearer " + getAccessToken());
        log.debug("Connected successfully");
    }

    @Override
    public void disconnect() {
        log.debug("Disconnecting from Azure API server...");
        connection.disconnect();
        log.debug("Disconnected successfully");
    }

    private String getAccessToken() throws AzureMembershipSchemeException {
        try {
            return AzureAuthenticator.acquireToken().accessToken();
        } catch (MalformedURLException e) {
            throw new AzureMembershipSchemeException("Failed to acquire azure access token", e);
        }
    }
}