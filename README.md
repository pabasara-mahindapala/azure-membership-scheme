## Azure Membership Scheme

Azure membership scheme provides features for automatically discovering WSO2 Carbon server clusters on Azure.

### How It Works

Once a Carbon server starts it will query the IP addresses in the given cluster via Azure API or Azure SDK using the given Azure services. The name of the resource group where the virtual machines are assigned should be provided. Thereafter, Hazelcast network configuration will be updated with the above IP addresses. As a result, the Hazelcast instance will get connected to all the other members in the cluster. In addition, once a new member is added to the cluster, all other members will get connected to the new member.

Azure Membership Scheme is compatible with WSO2 products that are based on Carbon 4.6.1 and onwards. (To determine the Carbon version of a particular product, please refer to the [WSO2 Release Matrix](http://wso2.com/products/carbon/release-matrix/)).

Following two approaches can be used for discovering Azure IP addresses.

1. Using the Azure REST API


   An access token will be acquired from Azure using the provided client credentials and using
   the access token, Azure REST API is called. IP addresses of the virtual machines are parsed from the response and
   provided to the Hazelcast network configuration.

3. Using the Azure SDK


   Azure NetworkManager class from the Java SDK is authenticated with the provided client
   credentials, and it is used to query IP addresses of the virtual machines from the resource group. Then the IP
   addresses will be provided to the Hazelcast network configuration.

### Installation

1. Run the following Maven command from the `azure-membership-scheme` directory.
   ```
      mvn clean install
   ```

2. Copy following JAR file to the `<carbon_home>/repository/components/dropins` directory of the Carbon server:
   ```
      azure-membership-scheme-1.0.0.jar
   ```

3. Since Azure SDK is using the Java service loader, and it is not OSGI compatible, we have
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

The Azure membership scheme supports finding the IPs in two ways:

1. Using the Azure REST API
2. Using Azure Java SDK

#### Using the Azure API to Resolve IPs

The membership scheme queries the Azure API for the relevant IP addresses. This method will be used by default. To
configure the membership scheme to use the Azure API, do the following configuration changes:

1. Update `<carbon_home>/repository/conf/deployment.toml` with the following configuration:

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
   USE_SDK = "false"
   AZURE_API_ENDPOINT = "https://management.azure.com"
   AZURE_API_VERSION = "2021-03-01"
   ```

#### Clustering Parameters required to communicate with the Azure REST API

1. `AZURE_CLIENT_ID` - Azure Client ID obtained when registering the application, **ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application, **ex:** `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
3. `AZURE_TENANT` - Azure Active Directory tenant name or tenant ID, **ex:** `default`
4. `AZURE_SUBSCRIPTION_ID` - Azure Subscription ID, **ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
5. `AZURE_RESOURCE_GROUP` - Azure Resource Group to discover IP addresses, **ex:** `wso2cluster`
6. `USE_SDK` - Configure the membership scheme to either use Azure REST API (default) or use the Azure SDK for IP
   resolution,
   **ex:** `false`. To use the Azure REST API, this value **must** be set to `false`.
7. `AZURE_API_ENDPOINT` - Azure Resource Manager API Endpoint, **ex:** `https://management.azure.com`
8. `AZURE_API_VERSION` - Azure API Version, **ex:** `2021-03-01`

#### Using Azure SDK to Resolve IPs

Azure SDK is used by the membership scheme to find the relevant IP addresses. To configure the membership scheme to use
the Azure SDK, do the following configuration changes:

1. Update `<carbon_home>/repository/conf/deployment.toml` with the following configuration:

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

#### Clustering Parameters required to use Azure SDK

1. `AZURE_CLIENT_ID` - Azure Client ID obtained when registering the application,
   **ex:** `53ba6f2b-6d52-4f6c-8ae0-7adc213808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application,
   **ex:** `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
3. `AZURE_TENANT` - Azure Active Directory tenant name or tenant ID, **ex:** `default`
4. `AZURE_SUBSCRIPTION_ID` - Azure Subscription ID, **ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
5. `AZURE_RESOURCE_GROUP` - Azure Resource Group to discover IP addresses, **ex:** `wso2cluster`
6. `USE_SDK` - Configure the membership scheme to either use Azure REST API (default) or use the Azure SDK for IP
   resolution,
   **ex:** `true`. To use the Azure SDK, this value **must** be set to `true`.
