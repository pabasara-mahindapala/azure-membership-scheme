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

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.compute.ComputeManager;

/**
 * Azure compute manager
 */
public class ComputeManagerHelper {

    public static ComputeManager getComputeManager() {
        AzureProfile profile = new AzureProfile(Constants.TENANT, Constants.SUBSCRIPTION_ID, AzureEnvironment.AZURE);

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(Constants.CLIENT_ID)
                .clientSecret(Constants.CLIENT_SECRET)
                .tenantId(Constants.TENANT)
                .build();

        return ComputeManager.authenticate(credential, profile);
    }

}
