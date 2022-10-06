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

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.NetworkInterfaceBase;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.AzureAuthenticator;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.Utils;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;
import org.wso2.carbon.utils.xml.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class responsible for resolving ips based on Azure SDK.
 */
public class SdkBasedIpResolver extends AddressResolver {

    private static final Log log = LogFactory.getLog(SdkBasedIpResolver.class);
    private NetworkManager networkManager;

    public SdkBasedIpResolver(final Map<String, Parameter> parameters) throws AzureMembershipSchemeException {

        super(parameters);
        initialize();
    }

    private void initialize() throws AzureMembershipSchemeException {

        AzureAuthenticator azureAuthenticator = AzureAuthenticator.getInstance(getParameters());

        try {
            AzureProfile profile = azureAuthenticator.getAzureProfile();
            TokenCredential credential = azureAuthenticator.getClientSecretCredential();
            networkManager = NetworkManager.authenticate(credential, profile);
        } catch (Exception e) {
            throw Utils.handleException(Constants.ErrorMessage.FAILED_TO_AUTHENTICATE_COMPUTEMANAGER, e);
        }
    }

    @Override
    public Set<String> resolveAddresses() throws AzureMembershipSchemeException {

        Set<String> ipAddresses;

        String usePublicIPAddresses =
                Utils.getParameterValueOrNull(Constants.PARAMETER_NAME_USE_PUBLIC_IP_ADDRESSES, getParameters());

        if (StringUtils.isEmpty(usePublicIPAddresses) || !Boolean.parseBoolean(usePublicIPAddresses)) {
            log.debug("Using private IP addresses");

            ipAddresses = networkManager.networkInterfaces().listByResourceGroup(
                            Utils.getParameterValue(Constants.PARAMETER_NAME_RESOURCE_GROUP, getParameters()))
                    .stream()
                    .map(NetworkInterfaceBase::primaryPrivateIP)
                    .filter(Utils::isNotNullOrEmptyAfterTrim)
                    .collect(Collectors.toSet());
        } else {
            log.debug("Using public IP addresses");

            ipAddresses = networkManager.publicIpAddresses().listByResourceGroup(
                            Utils.getParameterValue(Constants.PARAMETER_NAME_RESOURCE_GROUP, getParameters()))
                    .stream()
                    .map(PublicIpAddress::ipAddress)
                    .filter(Utils::isNotNullOrEmptyAfterTrim)
                    .collect(Collectors.toSet());
        }

        if (!ipAddresses.isEmpty()) {
            log.debug(String.format("Found %s IP addresses", ipAddresses.size()));
            return ipAddresses;
        } else {
            throw Utils.handleException(Constants.ErrorMessage.NO_IPS_FOUND);
        }
    }
}
