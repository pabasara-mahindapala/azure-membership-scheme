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

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;
import org.wso2.carbon.membership.scheme.azure.resolver.AddressResolver;
import org.wso2.carbon.membership.scheme.azure.resolver.ApiBasedIpResolver;
import org.wso2.carbon.membership.scheme.azure.resolver.SdkBasedIpResolver;
import org.wso2.carbon.utils.xml.StringUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Azure membership scheme provides carbon cluster discovery on azure.
 */
public class AzureMembershipScheme implements HazelcastMembershipScheme {

    private static final Log log = LogFactory.getLog(AzureMembershipScheme.class);

    private final Map<String, Parameter> parameters;
    private final NetworkConfig nwConfig;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastInstance primaryHazelcastInstance;
    private HazelcastCarbonClusterImpl carbonCluster;
    private AddressResolver ipResolver;

    public AzureMembershipScheme(Map<String, Parameter> parameters, String primaryDomain, Config config,
                                 HazelcastInstance primaryHazelcastInstance, List<ClusteringMessage> messageBuffer) {

        this.parameters = parameters;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    /**
     * Set Primary Hazelcast Instance.
     *
     * @param primaryHazelcastInstance
     */
    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {

        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    /**
     * Set Local Member.
     *
     * @param localMember
     */
    @Override
    public void setLocalMember(Member localMember) {

    }

    /**
     * Set Carbon Cluster.
     *
     * @param hazelcastCarbonCluster
     */
    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {

        this.carbonCluster = hazelcastCarbonCluster;
    }

    /**
     * Initiates the IP resolver.
     * Uses the SDK based IP resolver or the REST API based IP resolver.
     */
    private void initIpResolver() throws AzureMembershipSchemeException {

        String useSDK = Utils.getParameterValueOrNull(Constants.PARAMETER_NAME_USE_SDK, parameters);

        if (StringUtils.isEmpty(useSDK) || !Boolean.parseBoolean(useSDK)) {
            log.debug("Using API based ip resolving method");
            ipResolver = new ApiBasedIpResolver(parameters);
        } else {
            log.debug("Using SDK based ip resolving method");
            ipResolver = new SdkBasedIpResolver(parameters);
        }
    }

    /**
     * Get Azure IP Addresses from the IP resolver.
     *
     * @return Azure IP Addresses
     * @throws AzureMembershipSchemeException
     */
    private Set<String> getAzureIpAddresses() throws AzureMembershipSchemeException {

        return ipResolver.resolveAddresses();
    }

    /**
     * Initialize the Azure membership scheme.
     *
     * @throws ClusteringFault
     */
    @Override
    public void init() throws ClusteringFault {

        try {
            log.info("Initializing azure membership scheme");
            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            nwConfig.getJoin().getAwsConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            initIpResolver();
            Set<String> azureIPs = getAzureIpAddresses();
            // if no IPs are found, can't initialize clustering
            if (azureIPs.isEmpty()) {
                throw Utils.handleException(Constants.ErrorMessage.NO_MEMBERS_FOUND);
            }

            for (String azureIP : azureIPs) {
                if (!azureIP.equals(Inet4Address.getLocalHost().getHostAddress())) {
                    tcpIpConfig.addMember(azureIP);
                    log.info("Member added to cluster configuration: [vm-ip] " + azureIP);
                }
            }
            log.info("Azure membership scheme initialized successfully");
        } catch (UnknownHostException | AzureMembershipSchemeException e) {
            String errorMsg = "Azure membership initialization failed";
            log.error(errorMsg, e);
            throw new ClusteringFault(errorMsg, e);
        }
    }

    /**
     * Add the Azure Membership Listener.
     */
    @Override
    public void joinGroup() {

        primaryHazelcastInstance.getCluster().addMembershipListener(new AzureMembershipSchemeListener());
    }

    /**
     * Azure membership scheme listener.
     */
    private class AzureMembershipSchemeListener implements MembershipListener {

        /**
         * A member joined the cluster.
         *
         * @param membershipEvent
         */
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {

            Member member = membershipEvent.getMember();
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            List<String> memberList = tcpIpConfig.getMembers();
            if (!memberList.contains(member.getSocketAddress().getAddress().getHostAddress())) {
                tcpIpConfig.addMember(String.valueOf(member.getSocketAddress().getAddress().getHostAddress()));
            }

            // Send all cluster messages
            carbonCluster.memberAdded(member);
            log.info(String.format("Member joined: [UUID] %s, [Address] %s", member.getUuid(),
                    member.getSocketAddress().toString()));
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Current member list: %s", tcpIpConfig.getMembers()));
            }
        }

        /**
         * A member left the cluster.
         *
         * @param membershipEvent
         */
        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {

            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            String memberIp = member.getSocketAddress().getAddress().getHostAddress();
            try {
                if (tcpIpConfig.getMembers().contains(memberIp)) {
                    tcpIpConfig.getMembers()
                            .remove(String.valueOf(memberIp));
                    log.info(String.format("Member left: [UUID] %s, [Address] %s", member.getUuid(),
                            member.getSocketAddress().toString()));
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Current member list: %s", tcpIpConfig.getMembers()));
                    }
                }
            } catch (Exception e) {
                log.error(String.format("Could not remove member: %s", memberIp), e);
            }
        }

        /**
         * Member attribute changed.
         *
         * @param memberAttributeEvent
         */
        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Member attribute changed: [Key] %s, [Value] %s", memberAttributeEvent.getKey(),
                        memberAttributeEvent.getValue()));
            }
        }
    }
}
