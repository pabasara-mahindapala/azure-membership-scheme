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

import org.apache.axis2.description.Parameter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * class responsible for resolving ips based on Azure REST API
 */
public class ApiBasedIpResolver extends AddressResolver {

    private static final Log log = LogFactory.getLog(ApiBasedIpResolver.class);

    public ApiBasedIpResolver(final Map<String, Parameter> parameters) throws AzureMembershipSchemeException {
        super(parameters);
        initialize();
    }

    private void initialize() throws AzureMembershipSchemeException {
        throw new NotImplementedException();
    }

    @Override
    public Set<String> resolveAddresses() throws AzureMembershipSchemeException {
        throw new NotImplementedException();
    }
}