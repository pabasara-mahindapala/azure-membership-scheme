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

package org.wso2.carbon.membership.scheme.azure.exceptions;

/**
 * Azure membership scheme exception.
 */
public class AzureMembershipSchemeException extends Exception {

    private String description;

    public AzureMembershipSchemeException(String message) {

        super(message);
    }

    public AzureMembershipSchemeException(String message, Throwable cause) {

        super(message, cause);
    }

    public AzureMembershipSchemeException(String message, String description) {

        super(message);
        this.description = description;
    }

    public AzureMembershipSchemeException(String message, String description, Throwable cause) {

        super(message, cause);
        this.description = description;
    }

    /**
     * Get Description.
     *
     * @return Description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set Description.
     *
     * @param description
     */
    public void setDescription(String description) {

        this.description = description;
    }
}
