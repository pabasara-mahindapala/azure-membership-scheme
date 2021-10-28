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

package org.wso2.carbon.membership.scheme.azure.resolver;

import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonArray;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.json.JsonValue;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.api.AzureApiEndpoint;
import org.wso2.carbon.membership.scheme.azure.api.AzureHttpsApiEndpoint;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * class responsible for resolving ips based on Azure REST API
 */
public class ApiBasedIpResolver extends AddressResolver {

    private static final Log log = LogFactory.getLog(ApiBasedIpResolver.class);

    public ApiBasedIpResolver(final Map<String, Parameter> parameters) throws AzureMembershipSchemeException {
        super(parameters);
    }

    @Override
    public Set<String> resolveAddresses() throws AzureMembershipSchemeException {
        URL apiEndpointUrl;
        try {
            apiEndpointUrl = new URL(urlForIpList());
        } catch (MalformedURLException e) {
            throw new AzureMembershipSchemeException("Could not create endpoint URL", e);
        }
        AzureApiEndpoint apiEndpoint = new AzureHttpsApiEndpoint(apiEndpointUrl);

        Set<String> publicIps;

        try {
            publicIps = parsePublicIpResponse(connectAndRead(apiEndpoint));
        } finally {
            apiEndpoint.disconnect();
        }

        if (!publicIps.isEmpty()) {
            return publicIps;
        } else {
            throw new AzureMembershipSchemeException("No IPs found at " + apiEndpointUrl.toString());
        }
    }

    private String connectAndRead(AzureApiEndpoint endpoint) throws AzureMembershipSchemeException {
        try {
            endpoint.createConnection();
        } catch (IOException e) {
            throw new AzureMembershipSchemeException("Could not connect to Azure API", e);
        }

        try {
            return endpoint.read();
        } catch (IOException e) {
            throw new AzureMembershipSchemeException("Could not read from Azure API", e);
        }
    }

    private String urlForIpList() {
        return String.format("%s/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network"
                + "/publicIPAddresses?api-version=%s", Constants.AZURE_API_ENDPOINT, Constants.SUBSCRIPTION_ID, Constants.RESOURCE_GROUP_NAME, Constants.API_VERSION);
    }

    private Set<String> parsePublicIpResponse(String response) {
        HashSet<String> ipAddresses = new HashSet<>();

        for (JsonValue item : toJsonArray(Json.parse(response).asObject().get("value"))) {
            String ip = toJsonObject(item.asObject().get("properties")).getString("ipAddress", null);
            if (!isNullOrEmptyAfterTrim(ip)) {
                ipAddresses.add(ip);
            }
        }

        return ipAddresses;
    }

    private JsonArray toJsonArray(JsonValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull()) {
            return new JsonArray();
        } else {
            return jsonValue.asArray();
        }
    }

    private JsonObject toJsonObject(JsonValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull()) {
            return new JsonObject();
        } else {
            return jsonValue.asObject();
        }
    }

    private boolean isNullOrEmptyAfterTrim(String s) {
        return s == null || s.trim().isEmpty();
    }
}