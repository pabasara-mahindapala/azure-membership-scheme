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
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

/**
 * Endpoint to call Azure API.
 */
public abstract class AzureApiEndpoint {

    HttpURLConnection connection;

    private final Map<String, Parameter> parameters;

    AzureApiEndpoint(final Map<String, Parameter> parameters) {

        this.parameters = parameters;
    }

    /**
     * Connecting to Azure API server.
     *
     * @param url
     * @throws AzureMembershipSchemeException
     */
    public abstract void createConnection(URL url) throws AzureMembershipSchemeException;

    /**
     * Disconnecting from Azure API server.
     */
    public abstract void disconnect();

    /**
     * Get Endpoint.
     *
     * @return
     */
    public abstract URL getEndpoint();

    /**
     * Read from the endpoint.
     *
     * @return
     * @throws IOException
     */
    public String read() throws IOException {

        InputStream stream = null;
        try {
            if (connection != null) {
                stream = connection.getInputStream();
            }

            if (stream == null) {
                return null;
            }

            Scanner scanner = new Scanner(stream, Constants.CHARSET_NAME);
            scanner.useDelimiter("\\Z");
            return scanner.next();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Get the map of parameters.
     *
     * @return Map of parameters
     */
    public Map<String, Parameter> getParameters() {

        return parameters;
    }

}
