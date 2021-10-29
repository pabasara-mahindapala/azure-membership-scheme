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

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.AzureAuthenticator;
import org.wso2.carbon.membership.scheme.azure.Constants;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class responsible for resolving ips based on Azure SDK
 */
public class SdkBasedIpResolver extends AddressResolver {
    private static final Log log = LogFactory.getLog(SdkBasedIpResolver.class);
    private ComputeManager computeManager;

    public SdkBasedIpResolver(final Map<String, Parameter> parameters) throws AzureMembershipSchemeException {
        super(parameters);
        initialize();
    }

    private void initialize() throws AzureMembershipSchemeException {
        try {
            AzureProfile profile = AzureAuthenticator.getAzureProfile();
            TokenCredential credential = AzureAuthenticator.getClientSecretCredential();
            computeManager = ComputeManager.authenticate(credential, profile);
        } catch (Exception e) {
            throw new AzureMembershipSchemeException("Failed to authenticate azure ComputeManager", e);
        }
    }

    @Override
    public Set<String> resolveAddresses() {
        Iterable<VirtualMachine> virtualMachines = computeManager.virtualMachines().listByResourceGroup(Constants.RESOURCE_GROUP_NAME);
        HashSet<String> ipAddresses = new HashSet<>();

        for (VirtualMachine virtualMachine : virtualMachines) {
            // skip any deallocated vms
            if (!PowerState.RUNNING.equals(virtualMachine.powerState())) {
                continue;
            }

            NetworkInterface networkInterface = virtualMachine.getPrimaryNetworkInterface();
            for (NicIpConfiguration ipConfiguration : networkInterface.ipConfigurations().values()) {
                String publicIP = ipConfiguration.getPublicIpAddress().ipAddress();
                ipAddresses.add(publicIP);
            }
        }

        return ipAddresses;
    }
}