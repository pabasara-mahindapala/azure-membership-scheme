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

package org.wso2.carbon.membership.scheme.azure.api;

import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.compiler.core.common.SuppressFBWarnings;
import org.wso2.carbon.membership.scheme.azure.AzureAuthenticator;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.Utils;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Https Endpoint to call Azure API.
 */
public class AzureHttpsApiEndpoint extends AzureApiEndpoint {

    private URL endpoint;
    private static final Log log = LogFactory.getLog(AzureHttpsApiEndpoint.class);

    public AzureHttpsApiEndpoint(final Map<String, Parameter> parameters) {

        super(parameters);
    }

    @Override
    @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "URL is built with constants.")
    public void createConnection(URL url) throws AzureMembershipSchemeException {

        log.debug("Connecting to Azure API server");
        try {
            connection = (HttpsURLConnection) url.openConnection();
            this.endpoint = url;
        } catch (IOException e) {
            throw Utils.handleException(Constants.ErrorMessage.FAILED_TO_CONNECT, e);
        }
        connection.addRequestProperty(Constants.AUTHORIZATION_HEADER,
                String.format("%s %s", Constants.BEARER, getAccessToken()));
        log.debug("Connected to Azure API server successfully");
    }

    @Override
    public void disconnect() {

        log.debug("Disconnecting from Azure API server");
        if (connection != null) {
            connection.disconnect();
        }
        log.debug("Disconnected from Azure API server successfully");
    }

    private String getAccessToken() throws AzureMembershipSchemeException {

        AzureAuthenticator azureAuthenticator = AzureAuthenticator.getInstance(getParameters());
        return azureAuthenticator.acquireToken().accessToken();
    }

    public URL getEndpoint() {

        return endpoint;
    }
}
