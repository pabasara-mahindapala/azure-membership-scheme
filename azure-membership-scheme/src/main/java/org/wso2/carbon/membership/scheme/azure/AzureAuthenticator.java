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

package org.wso2.carbon.membership.scheme.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.net.MalformedURLException;

/**
 * Authenticate azure SDK and REST API.
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-daemon-acquire-token">
 * Acquire a token</a>
 */
public class AzureAuthenticator {

    public static IAuthenticationResult acquireToken() throws AzureMembershipSchemeException {

        String authority = String.format(Constants.INSTANCE, Constants.TENANT);

        IClientCredential credential = ClientCredentialFactory.createFromSecret(Constants.CLIENT_SECRET);

        ConfidentialClientApplication cca;

        try {
            cca = ConfidentialClientApplication
                    .builder(Constants.CLIENT_ID, credential)
                    .authority(authority)
                    .build();
        } catch (MalformedURLException e) {
            throw new AzureMembershipSchemeException("Could not build ConfidentialClientApplication", e);
        }

        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(Constants.MANAGEMENT_DEFAULT_SCOPE)
                        .build();

        return cca.acquireToken(parameters).join();
    }

    public static TokenCredential getClientSecretCredential() {

        return new ClientSecretCredentialBuilder()
                .clientId(Constants.CLIENT_ID)
                .clientSecret(Constants.CLIENT_SECRET)
                .tenantId(Constants.TENANT)
                .build();
    }

    public static AzureProfile getAzureProfile() {

        return new AzureProfile(Constants.TENANT, Constants.SUBSCRIPTION_ID, AzureEnvironment.AZURE);
    }
}
