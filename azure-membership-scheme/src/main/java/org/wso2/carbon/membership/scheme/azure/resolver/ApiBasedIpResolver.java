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

package org.wso2.carbon.membership.scheme.azure.resolver;

import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonValue;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.Utils;
import org.wso2.carbon.membership.scheme.azure.api.AzureApiEndpoint;
import org.wso2.carbon.membership.scheme.azure.api.AzureHttpsApiEndpoint;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * class responsible for resolving ips based on Azure REST API.
 */
public class ApiBasedIpResolver extends AddressResolver {

    private static final Log log = LogFactory.getLog(ApiBasedIpResolver.class);

    public ApiBasedIpResolver(final Map<String, Parameter> parameters) {

        super(parameters);
    }

    @Override
    public Set<String> resolveAddresses() throws AzureMembershipSchemeException {

        AzureApiEndpoint apiEndpoint = new AzureHttpsApiEndpoint(getParameters());

        Set<String> publicIps;

        try {
            publicIps = parsePublicIpResponse(connectAndRead(apiEndpoint));
        } finally {
            apiEndpoint.disconnect();
        }

        if (!publicIps.isEmpty()) {
            log.debug(String.format("Found %s IP addresses", publicIps.size()));
            return publicIps;
        } else {
            throw Utils.handleException(Constants.ErrorMessage.NO_IPS_FOUND, apiEndpoint.getEndpoint().toString());
        }
    }

    private String connectAndRead(AzureApiEndpoint endpoint) throws AzureMembershipSchemeException {

        endpoint.createConnection();

        try {
            return endpoint.read();
        } catch (IOException e) {
            throw Utils.handleException(Constants.ErrorMessage.COULD_NOT_READ_API, null, e);
        }
    }

    private Set<String> parsePublicIpResponse(String response) {

        HashSet<String> ipAddresses = new HashSet<>();

        for (JsonValue item : Utils.toJsonArray(Json.parse(response).asObject().get("value"))) {
            String ip = Utils.toJsonObject(item.asObject().get("properties")).getString("ipAddress", null);
            if (Utils.isNotNullOrEmptyAfterTrim(ip)) {
                ipAddresses.add(ip);
            }
        }

        return ipAddresses;
    }
}
