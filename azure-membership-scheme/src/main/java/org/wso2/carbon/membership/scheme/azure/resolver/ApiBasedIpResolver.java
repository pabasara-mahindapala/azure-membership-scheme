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
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.internal.json.JsonValue;
import org.apache.axis2.description.Parameter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.Utils;
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
 * class responsible for resolving ips based on Azure REST API.
 */
public class ApiBasedIpResolver extends AddressResolver {

    private static final Log log = LogFactory.getLog(ApiBasedIpResolver.class);

    public ApiBasedIpResolver(final Map<String, Parameter> parameters) {

        super(parameters);
    }

    @Override
    public Set<String> resolveAddresses() throws AzureMembershipSchemeException {

        Set<String> ipAddresses;
        AzureApiEndpoint apiEndpoint = new AzureHttpsApiEndpoint(getParameters());

        String usePublicIPAddresses =
                Utils.getParameterValueOrNull(Constants.PARAMETER_NAME_USE_PUBLIC_IP_ADDRESSES, getParameters());

        try {
            if (StringUtils.isEmpty(usePublicIPAddresses) || !Boolean.parseBoolean(usePublicIPAddresses)) {
                log.debug("Using private IP addresses");

                ipAddresses = parsePrivateIpResponse(connectAndRead(apiEndpoint, buildUrlForPrivateIpList()));
            } else {
                log.debug("Using public IP addresses");

                ipAddresses = parsePublicIpResponse(connectAndRead(apiEndpoint, buildUrlForPublicIpList()));
            }
        } finally {
            apiEndpoint.disconnect();
        }

        if (!ipAddresses.isEmpty()) {
            log.debug(String.format("Found %s IP addresses", ipAddresses.size()));
            return ipAddresses;
        } else {
            throw Utils.handleException(Constants.ErrorMessage.NO_IPS_FOUND, apiEndpoint.getEndpoint().toString());
        }
    }

    private String connectAndRead(AzureApiEndpoint endpoint, String urlForIpList)
            throws AzureMembershipSchemeException {

        try {
            endpoint.createConnection(new URL(urlForIpList));
            return endpoint.read();
        } catch (MalformedURLException e) {
            throw Utils.handleException(Constants.ErrorMessage.COULD_NOT_CREATE_URL, e);
        } catch (IOException e) {
            throw Utils.handleException(Constants.ErrorMessage.COULD_NOT_READ_API, e);
        }
    }

    private String buildUrlForPublicIpList() throws AzureMembershipSchemeException {

        return String.format("%s/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network"
                        + "/publicIPAddresses?api-version=%s",
                Utils.getParameterValueOrDefault(Constants.PARAMETER_NAME_API_ENDPOINT, Constants.DEFAULT_API_ENDPOINT,
                        getParameters()),
                Utils.getParameterValue(Constants.PARAMETER_NAME_SUBSCRIPTION_ID, getParameters()),
                Utils.getParameterValue(Constants.PARAMETER_NAME_RESOURCE_GROUP, getParameters()),
                Utils.getParameterValueOrDefault(Constants.PARAMETER_NAME_API_VERSION, Constants.DEFAULT_API_VERSION,
                        getParameters()));
    }

    private Set<String> parsePublicIpResponse(String response) {

        HashSet<String> ipAddresses = new HashSet<>();

        for (JsonValue item : Utils.toJsonArray(Json.parse(response).asObject().get(Constants.VALUE))) {
            String publicIp =
                    Utils.toJsonObject(item.asObject().get(Constants.PROPERTIES)).getString(Constants.IP_ADDRESS, null);
            if (StringUtils.isNotBlank(publicIp)) {
                ipAddresses.add(publicIp);
            }
        }

        return ipAddresses;
    }

    private String buildUrlForPrivateIpList() throws AzureMembershipSchemeException {

        return String.format("%s/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network"
                        + "/networkInterfaces?api-version=%s",
                Utils.getParameterValueOrDefault(Constants.PARAMETER_NAME_API_ENDPOINT, Constants.DEFAULT_API_ENDPOINT,
                        getParameters()),
                Utils.getParameterValue(Constants.PARAMETER_NAME_SUBSCRIPTION_ID, getParameters()),
                Utils.getParameterValue(Constants.PARAMETER_NAME_RESOURCE_GROUP, getParameters()),
                Utils.getParameterValueOrDefault(Constants.PARAMETER_NAME_API_VERSION, Constants.DEFAULT_API_VERSION,
                        getParameters()));
    }

    private Set<String> parsePrivateIpResponse(String response) {

        HashSet<String> ipAddresses = new HashSet<>();

        for (JsonValue item : Utils.toJsonArray(Json.parse(response).asObject().get(Constants.VALUE))) {
            JsonObject properties = item.asObject().get(Constants.PROPERTIES).asObject();
            if (properties.get(Constants.VIRTUAL_MACHINE) != null) {
                for (JsonValue ipConfiguration : Utils.toJsonArray(properties.get(Constants.IP_CONFIGURATIONS))) {
                    JsonObject ipProps = ipConfiguration.asObject().get(Constants.PROPERTIES).asObject();
                    String privateIp = ipProps.getString(Constants.PRIVATE_IP_ADDRESS, null);
                    ipAddresses.add(privateIp);
                }
            }
        }

        return ipAddresses;
    }
}
