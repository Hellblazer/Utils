/** (C) Copyright 2014 Chiral Behaviors, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hellblazer.utils.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author hhildebrand
 *
 */
public class ConfigurableSslRmiClientSocketFactory implements
        RMIClientSocketFactory, Serializable {

    private static final long      serialVersionUID = 1L;
    private final String[]         enabledCipherSuites;
    private final String[]         enabledProtocols;
    private final SSLSocketFactory socketFactory;

    public ConfigurableSslRmiClientSocketFactory(SSLContext sslContext) {
        this(sslContext.getSocketFactory(), null, null);
    }

    public ConfigurableSslRmiClientSocketFactory(SSLContext sslContext,
                                                 String[] enabledCipherSuites,
                                                 String[] enabledProtocols) {
        this(sslContext.getSocketFactory(), enabledCipherSuites,
             enabledProtocols);
    }

    public ConfigurableSslRmiClientSocketFactory(SSLSocketFactory socketFactory,
                                                 String[] enabledCipherSuites,
                                                 String[] enabledProtocols) {
        this.enabledCipherSuites = enabledCipherSuites;
        this.enabledProtocols = enabledProtocols;
        this.socketFactory = socketFactory;
    }

    /* (non-Javadoc)
     * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String, int)
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {

        SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
        if (enabledCipherSuites != null) {
            socket.setEnabledCipherSuites(enabledCipherSuites);
        }
        if (enabledProtocols != null) {
            socket.setEnabledProtocols(enabledProtocols);
        }
        return socket;
    }

}
