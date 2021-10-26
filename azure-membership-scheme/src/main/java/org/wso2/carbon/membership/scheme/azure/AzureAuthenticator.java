package org.wso2.carbon.membership.scheme.azure;


import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.ClientCredentialParameters;

import java.net.MalformedURLException;

/**
 * Authenticate and retrieve azure access token
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-daemon-acquire-token">
 * Acquire a token</a>
 */
public class AzureAuthenticator {
    private static IAuthenticationResult acquireToken() throws MalformedURLException {
        String authority = String.format(Constants.INSTANCE, Constants.TENANT);

        IClientCredential credential = ClientCredentialFactory.createFromSecret(Constants.CLIENT_SECRET);

        ConfidentialClientApplication cca =
                ConfidentialClientApplication
                        .builder(Constants.CLIENT_ID, credential)
                        .authority(authority)
                        .build();

        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(Constants.MANAGEMENT_DEFAULT_SCOPE)
                        .build();

        return cca.acquireToken(parameters).join();
    }
}
