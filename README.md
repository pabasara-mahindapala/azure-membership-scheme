## Azure Membership Scheme

Azure membership scheme provides features for automatically discovering WSO2 Carbon server clusters on Azure (Read the blog [here](https://towardsdev.com/running-wso2-products-on-azure-with-azure-membership-scheme-3175113d8e10)).

### How It Works

Once a Carbon server starts it will query the IP addresses in the given cluster via Azure API or Azure SDK using the
given Azure services. The name of the resource group where the virtual machines are assigned should be provided.
Thereafter, Hazelcast network configuration will be updated with the above IP addresses. As a result, the Hazelcast
instance will get connected to all the other members in the cluster. In addition, once a new member is added to the
cluster, all other members will get connected to the new member.

Azure Membership Scheme is compatible with WSO2 products that are based on Carbon 4.6.1 and onwards. (To determine the
Carbon version of a particular product, please refer to
the [WSO2 Release Matrix](http://wso2.com/products/carbon/release-matrix/)).

Following two approaches can be used for discovering Azure IP addresses.

1. Using the Azure REST API

An access token will be acquired from Azure using the provided client credentials and using the access token, Azure REST
API is called. IP addresses of the virtual machines are parsed from the response and provided to the Hazelcast network
configuration.

3. Using the Azure SDK

Azure NetworkManager class from the Java SDK is authenticated with the provided client credentials, and it is used to
query IP addresses of the virtual machines from the resource group. Then the IP addresses will be provided to the
Hazelcast network configuration.

### Using the Azure API to Resolve IPs

1. Run the following command from the `azure-membership-scheme` directory.
   ```
   mvn clean install
   ```

2. Copy the following JAR file from `azure-membership-scheme/target` to the `<carbon_home>/repository/components/lib`
   directory of the Carbon server.
   ```
   azure-membership-scheme-1.0.0.jar
   ```

3. Copy the following dependencies from `azure-membership-scheme/target/dependencies` to the `<carbon_home>/repository/components/lib` directory of the Carbon server.
   ```
   azure-core-1.23.1.jar
   content-type-2.1.jar
   msal4j-1.11.0.jar
   oauth2-oidc-sdk-9.7.jar
   ```

4. Configure the membership scheme as shown in the `<carbon_home>/repository/conf/deployment.toml` file.

   ```
   [clustering]
   membership_scheme = "azure"
   local_member_host = "127.0.0.1"
   local_member_port = "4000"
      
   [clustering.properties]
   membershipSchemeClassName = "org.wso2.carbon.membership.scheme.azure.AzureMembershipScheme"
   AZURE_CLIENT_ID = "{{client-id}}"
   AZURE_CLIENT_SECRET = "{{client-secret}}"
   AZURE_TENANT = "{{tenant}}"
   AZURE_SUBSCRIPTION_ID = "{{subscription-id}}"
   AZURE_RESOURCE_GROUP = "{{resource-group}}"
   AZURE_API_ENDPOINT = "https://management.azure.com"
   AZURE_API_VERSION = "2021-03-01"
   ```

5. When the server is starting, logs related to the cluster initialization can be observed.

#### Clustering Parameters required to communicate with the Azure REST API

1. `AZURE_CLIENT_ID` - Azure Client ID obtained by registering a client application in the Azure Active Directory
   tenant. The client app needs to have the necessary permissions assigned to perform the action
   [Microsoft.Network/networkInterfaces/read](https://learn.microsoft.com/en-us/azure/role-based-access-control/resource-provider-operations#microsoftnetwork)
   ex: `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application,
   ex: `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
3. `AZURE_TENANT` - Azure Active Directory tenant name or tenant ID, ex: `default`
4. `AZURE_SUBSCRIPTION_ID` - Azure Subscription ID, ex: `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
5. `AZURE_RESOURCE_GROUP` - Azure Resource Group to discover IP addresses, ex: `wso2cluster`
6. `AZURE_API_ENDPOINT` - Azure Resource Manager API Endpoint, ex: `https://management.azure.com`
7. `AZURE_API_VERSION` - Azure API Version, ex: `2021-03-01`

### Using Azure SDK to Resolve IPs

1. Run the following command from the `azure-membership-scheme` directory.
   ```
   mvn clean install
   ```

2. Copy the following JAR file from `azure-membership-scheme/target` to the `<carbon_home>/repository/components/lib`
   directory of the Carbon server.
   ```
   azure-membership-scheme-1.0.0.jar
   ```

3. Since the Azure SDK is using the Java service loader, and it is not OSGI compatible, we have
   used [Apache Aries SPI Fly](https://aries.apache.org/documentation/modules/spi-fly.html) as a resolution. Run the
   [p2-maven-plugin](https://github.com/reficio/p2-maven-plugin) using the following command in
   the `azure-membership-scheme` directory in order to generate the required dependencies.
   ```
      mvn p2:site
   ```

   Then copy the following files from `azure-membership-scheme/target/repository/plugins` to
   the `<carbon_home>/repository/components/dropins` directory.
   ```
      com.azure.core_1.22.0.jar
      com.azure.core-http-okhttp_1.7.6.jar
   ```

   You should also configure Apache SPI Fly to be used with the dynamic weaving bundle as
   explained [here](https://aries.apache.org/documentation/modules/spi-fly.html#_usage_there_are_currently_two_ways_to_use_the_spi_fly_component)


4. Copy the following dependencies from `azure-membership-scheme/target/dependencies` to the `<carbon_home>/repository/components/lib` directory of the Carbon server.
   ```
   azure-core-management-1.4.3.jar
   azure-identity-1.4.2.jar
   azure-resourcemanager-network-2.10.0.jar
   azure-resourcemanager-resources-2.10.0.jar
   content-type-2.1.jar
   jackson-dataformat-xml-2.13.0.jar
   jackson-datatype-jsr310-2.13.0.jar
   kotlin-stdlib-1.4.10.jar
   msal4j-1.11.0.jar
   oauth2-oidc-sdk-9.7.jar
   okhttp-4.9.2.jar
   okio-2.8.0.jar
   reactive-streams-1.0.3.jar
   reactor-core-3.4.12.jar
   ```

5. Configure the membership scheme as shown in the `<carbon_home>/repository/conf/deployment.toml` file.

      ```
   [clustering]
   membership_scheme = "azure"
   local_member_host = "127.0.0.1"
   local_member_port = "4000"
   
   [clustering.properties]
   membershipSchemeClassName = "org.wso2.carbon.membership.scheme.azure.AzureMembershipScheme"
   AZURE_CLIENT_ID = "{{client-id}}"
   AZURE_CLIENT_SECRET = "{{client-secret}}"
   AZURE_TENANT = "{{tenant}}"
   AZURE_SUBSCRIPTION_ID = "{{subscription-id}}"
   AZURE_RESOURCE_GROUP = "{{resource-group}}"
   USE_SDK = "true"
   ```

6. When the server is starting, logs related to the cluster initialization can be observed.

#### Clustering Parameters required to use Azure SDK

1. `AZURE_CLIENT_ID` - Azure Client ID obtained by registering a client application in the Azure Active Directory
   tenant. The client app needs to have the necessary permissions assigned to perform the action
   [Microsoft.Network/networkInterfaces/read](https://learn.microsoft.com/en-us/azure/role-based-access-control/resource-provider-operations#microsoftnetwork)
   ex: `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application,
   ex: `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
3. `AZURE_TENANT` - Azure Active Directory tenant name or tenant ID, ex: `default`
4. `AZURE_SUBSCRIPTION_ID` - Azure Subscription ID, ex: `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
5. `AZURE_RESOURCE_GROUP` - Azure Resource Group to discover IP addresses, ex: `wso2cluster`
6. `USE_SDK` - Configure the membership scheme to either use Azure REST API (default) or use the Azure SDK for IP
   resolution, ex: `true`. To use the Azure SDK, this value **must** be set to `true`.
