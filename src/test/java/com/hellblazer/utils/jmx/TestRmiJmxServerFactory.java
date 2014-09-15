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

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;

import com.hellblazer.utils.Utils;

/**
 * @author hhildebrand
 *
 */
public class TestRmiJmxServerFactory {

    @Test
    public void testClientSideAuth() throws Exception {
        SSLContext sslContext = getSslContext();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer server = RmiJmxServerFactory.construct(new InetSocketAddress(
                                                                                        Utils.allocatePort()),
                                                                  sslContext,
                                                                  false, mbs);
        server.start();
        JMXServiceURL url = server.getAddress();
        Map<String, Object> env = new HashMap<>();
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                new ConfigurableSslRmiClientSocketFactory(sslContext));
        JMXConnector connector = JMXConnectorFactory.connect(url, env);
        connector.connect();
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        assertNotNull(mbsc);
    }

    protected SSLContext getSslContext() throws KeyStoreException, IOException,
                                        NoSuchAlgorithmException,
                                        CertificateException,
                                        FileNotFoundException,
                                        UnrecoverableKeyException,
                                        KeyManagementException {
        KeyStore keystore = KeyStore.getInstance("jks");
        try (InputStream is = new FileInputStream("target/test.ks")) {
            keystore.load(is, "storepass".toCharArray());
        }
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(getKeyManagers(keystore),
                        getTrustAll() /*getTrustManagers(keystore)*/,
                        new SecureRandom());
        return sslContext;
    }

    protected KeyManager[] getKeyManagers(KeyStore keystore)
                                                            throws NoSuchAlgorithmException,
                                                            KeyStoreException,
                                                            UnrecoverableKeyException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, "keypass".toCharArray());
        KeyManager[] keystoreManagers = kmf.getKeyManagers();
        if (keystoreManagers.length == 0) {
            throw new NoSuchAlgorithmException("no key manager found");
        }
        return keystoreManagers;
    }

    protected TrustManager[] getTrustManagers(KeyStore keystore)
                                                                throws NoSuchAlgorithmException,
                                                                KeyStoreException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        return trustmanagers;
    }

    protected TrustManager[] getTrustAll() {
        return new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }

        } };
    }
}
