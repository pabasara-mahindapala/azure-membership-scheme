## Azure Membership Scheme

Azure membership scheme provides features for automatically discovering WSO2 Carbon server clusters on Azure.

### How It Works

Once a Carbon server starts it will query IP addresses in the given cluster via Azure API or Azure SDK using the given
Azure services. Thereafter Hazelcast network configuration will be updated with the above IP addresses. As a result the
Hazelcast instance will get connected to all the other members in the cluster. In addition, once a new member is added
to the cluster, all the other members will get connected to the new member.

### Installation

1. For Azure Membership Scheme to work, Hazelcast configuration should be made pluggable. This has to be enabled in the
   products in different ways. For WSO2 products that are based on Carbon
   4.2.0, [apply kernel patch0012](https://docs.wso2.com/display/Carbon420/Applying+a+Patch+to+the+Kernel). For Carbon
   4.4.1 based products
   apply [patch0005](http://product-dist.wso2.com/downloads/carbon/4.4.1/patch0005/WSO2-CARBON-PATCH-4.4.1-0005.zip).
   These patches include a modification in the Carbon Core component for allowing to add third party membership schemes.
   WSO2 products that are based on Carbon versions later than 4.4.1 do not need any patches to be applied (To determine
   the Carbon version of a particular product, please refer to
   the [WSO2 Release Matrix](http://wso2.com/products/carbon/release-matrix/)).

2. Copy following JAR files to the `repository/components/dropins` directory of the Carbon server:
   ```
      azure-membership-scheme-1.0.0.jar
   ```

The Azure membership scheme supports finding the IPs in two ways:

1. Using the Azure REST API
2. Using Azure Java SDK

#### Using the Azure API to Resolve IPs

The membership scheme queries the Azure API for the relevant IP addresses. This method will be used by default. To
configure the membership scheme to use the Azure API, do the following configuration changes:

1. Update `<carbon_home>/repository/conf/axis2/axis2.xml` with the following configuration:

   ```xml
   <clustering class="org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent"
                   enable="true">
       
           <parameter name="AvoidInitiation">true</parameter>
   
           <parameter name="membershipScheme">azure</parameter>
           <parameter name="domain">pub.store.am.wso2.domain</parameter>
   
           <parameter name="mcastPort">45564</parameter>
           <parameter name="mcastTTL">100</parameter>
           <parameter name="mcastTimeout">60</parameter>
   
           <parameter name="localMemberHost">172.17.0.2</parameter>
           <parameter name="localMemberPort">4000</parameter>
   
           <!--
           Properties specific to this member
           -->
           <parameter name="properties">
               <property name="backendServerURL" value="https://${hostName}:${httpsPort}/services/"/>
               <property name="mgtConsoleURL" value="https://${hostName}:${httpsPort}/"/>
               <property name="subDomain" value="worker"/>
           </parameter>
   
           <parameter name="membershipSchemeClassName">org.wso2.carbon.membership.scheme.azure.AzureMembershipScheme</parameter>
           <parameter name="AZURE_CLIENT_ID">{{client-id}}</parameter>
           <parameter name="AZURE_CLIENT_SECRET">{{client-secret}}</parameter>
           <parameter name="AZURE_TENANT">{{tenant}}</parameter>
           <parameter name="AZURE_SUBSCRIPTION_ID">{{subscription-id}}</parameter>
           <parameter name="AZURE_RESOURCE_GROUP">{{resource-group}}</parameter>
           <parameter name="USE_SDK">false</parameter>
           <parameter name="AZURE_API_ENDPOINT">https://management.azure.com</parameter>
           <parameter name="AZURE_API_VERSION">2021-03-01</parameter>
   
           <groupManagement enable="false">
               <applicationDomain name="wso2.apim.domain"
                                  description="APIM group"
                                  agent="org.wso2.carbon.core.clustering.hazelcast.HazelcastGroupManagementAgent"
                                  subDomain="worker"
                                  port="2222"/>
           </groupManagement>
       </clustering>

#### Clustering Parameters required to communicate with the Azure REST API

1. `AZURE_CLIENT_ID` - Azure Client ID obtained when registering the application, **
   ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application, **
   ex:** `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
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

1. Update `<carbon_home>/repository/conf/axis2/axis2.xml` with the following configuration:

   ```xml
   <clustering class="org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent"
                   enable="true">
       
           <parameter name="AvoidInitiation">true</parameter>
   
           <parameter name="membershipScheme">azure</parameter>
           <parameter name="domain">pub.store.am.wso2.domain</parameter>
   
           <parameter name="mcastPort">45564</parameter>
           <parameter name="mcastTTL">100</parameter>
           <parameter name="mcastTimeout">60</parameter>
   
           <parameter name="localMemberHost">172.17.0.2</parameter>
           <parameter name="localMemberPort">4000</parameter>
   
           <!--
           Properties specific to this member
           -->
           <parameter name="properties">
               <property name="backendServerURL" value="https://${hostName}:${httpsPort}/services/"/>
               <property name="mgtConsoleURL" value="https://${hostName}:${httpsPort}/"/>
               <property name="subDomain" value="worker"/>
           </parameter>
   
           <parameter name="membershipSchemeClassName">org.wso2.carbon.membership.scheme.azure.AzureMembershipScheme</parameter>
           <parameter name="AZURE_CLIENT_ID">{{client-id}}</parameter>
           <parameter name="AZURE_CLIENT_SECRET">{{client-secret}}</parameter>
           <parameter name="AZURE_TENANT">{{tenant}}</parameter>
           <parameter name="AZURE_SUBSCRIPTION_ID">{{subscription-id}}</parameter>
           <parameter name="AZURE_RESOURCE_GROUP">{{resource-group}}</parameter>
           <parameter name="USE_SDK">true</parameter>
   
           <groupManagement enable="false">
               <applicationDomain name="wso2.apim.domain"
                                  description="APIM group"
                                  agent="org.wso2.carbon.core.clustering.hazelcast.HazelcastGroupManagementAgent"
                                  subDomain="worker"
                                  port="2222"/>
           </groupManagement>
       </clustering>

#### Clustering Parameters required to use Azure SDK

1. `AZURE_CLIENT_ID` - Azure Client ID obtained when registering the application,
   **ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
2. `AZURE_CLIENT_SECRET` - Azure Client Secret generated for the application,
   **ex:** `NMubGVcDqkwwGnCs6fa01tqlkTisfUd4pBBYgcxxx=`
3. `AZURE_TENANT` - Azure Active Directory tenant name or tenant ID, **ex:** `default`
4. `AZURE_SUBSCRIPTION_ID` - Azure Subscription ID, **ex:** `53ba6f2b-6d52-4f5c-8ae0-7adc20808854`
5. `AZURE_RESOURCE_GROUP` - Azure Resource Group to discover IP addresses, **ex:** `wso2cluster`
6. `USE_SDK` - Configure the membership scheme to either use Azure REST API (default) or use the Azure SDK for IP
   resolution,
   **ex:** `true`. To use the Azure SDK, this value **must** be set to `true`.
