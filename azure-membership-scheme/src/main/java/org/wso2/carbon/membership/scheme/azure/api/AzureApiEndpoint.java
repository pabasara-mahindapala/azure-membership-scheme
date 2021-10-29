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

package org.wso2.carbon.membership.scheme.azure.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.azure.exceptions.AzureMembershipSchemeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public abstract class AzureApiEndpoint {

    private static final Log log = LogFactory.getLog(AzureApiEndpoint.class);

    URL url;
    HttpURLConnection connection;

    AzureApiEndpoint(URL url) {
        this.url = url;
    }

    public abstract void createConnection() throws IOException, AzureMembershipSchemeException;

    public abstract void disconnect();

    public String read() throws IOException {
        InputStream stream;
        stream = connection.getInputStream();

        if (stream == null) {
            return null;
        }

        Scanner scanner = new Scanner(stream, "UTF-8");
        scanner.useDelimiter("\\Z");
        return scanner.next();
    }
}