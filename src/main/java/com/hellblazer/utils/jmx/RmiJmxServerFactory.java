/** (C) Copyright 2014 Hal Hildebrand, All Rights Reserved
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import sun.management.ConnectorAddressLink;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

import com.hellblazer.utils.Utils;
import com.sun.jmx.remote.internal.RMIExporter;

/**
 * A simple factory for constructing rmi connector servers.
 *
 * @author hhildebrand
 *
 */
@SuppressWarnings("restriction")
public class RmiJmxServerFactory {
    private static class Exporter implements RMIExporter {
        /**
         * <p>
         * Prevents our RMI server objects from keeping the JVM alive.
         * </p>
         *
         * <p>
         * We use a private interface in Sun's JMX Remote API implementation
         * that allows us to specify how to export RMI objects. We do so using
         * UnicastServerRef, a class in Sun's RMI implementation. This is all
         * non-portable, of course, so this is only valid because we are inside
         * Sun's JRE.
         * </p>
         *
         * <p>
         * Objects are exported using
         * {@link UnicastServerRef#exportObject(Remote, Object, boolean)}. The
         * boolean parameter is called <code>permanent</code> and means both
         * that the object is not eligible for Distributed Garbage Collection,
         * and that its continued existence will not prevent the JVM from
         * exiting. It is the latter semantics we want (we already have the
         * former because of the way the JMX Remote API works). Hence the
         * somewhat misleading name of this class.
         * </p>
         */

        @Override
        public Remote exportObject(Remote obj, int port,
                                   RMIClientSocketFactory csf,
                                   RMIServerSocketFactory ssf)
                                                              throws RemoteException {
            final UnicastServerRef ref;
            if (csf == null && ssf == null) {
                ref = new UnicastServerRef(port);
            } else {
                ref = new UnicastServerRef2(port, csf, ssf);
            }
            return ref.exportObject(obj, null, true);
        }

        // Nothing special to be done for this case
        @Override
        public boolean unexportObject(Remote obj, boolean force)
                                                                throws NoSuchObjectException {
            return UnicastRemoteObject.unexportObject(obj, force);
        }
    }

    public static JMXConnectorServer construct(InetSocketAddress jmxEndpoint,
                                               MBeanServer mbs,
                                               Map<String, Object> env)
                                                                       throws MalformedURLException,
                                                                       IOException {
        JMXServiceURL url = new JMXServiceURL("rmi", jmxEndpoint.getHostName(),
                                              jmxEndpoint.getPort());
        return JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
    }

    public static JMXConnectorServer construct(InetSocketAddress endpoint,
                                               SSLContext sslContext,
                                               boolean needClientAuth,
                                               MBeanServer mbs)
                                                               throws IOException {
        return construct(endpoint, sslContext, null, null, needClientAuth, mbs);

    }

    public static JMXConnectorServer construct(InetSocketAddress endpoint,
                                               SSLContext sslContext,
                                               String[] enabledCipherSuites,
                                               String[] enabledProtocols,
                                               boolean needClientAuth,
                                               MBeanServer mbs)
                                                               throws IOException {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        //
        System.setProperty("java.rmi.server.randomIDs", "true");

        HashMap<String, Object> env = new HashMap<String, Object>();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, new Exporter());
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                new SslRMIClientSocketFactory());
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                new SslRMIServerSocketFactory(sslContext, enabledCipherSuites,
                                              enabledProtocols, needClientAuth));
        return construct(endpoint, mbs, env);
    }

    public static JMXConnectorServer construct(InetSocketAddress endpoint,
                                               String protocol,
                                               SecureRandom random,
                                               String provider,
                                               TrustManager[] trustManagers,
                                               KeyManager[] keyManagers,
                                               String[] enabledCipherSuites,
                                               String[] enabledProtocols,
                                               boolean needClientAuth,
                                               MBeanServer mbs)
                                                               throws IOException,
                                                               NoSuchAlgorithmException,
                                                               NoSuchProviderException,
                                                               KeyManagementException {
        SSLContext context = SSLContext.getInstance(protocol, provider);
        context.init(keyManagers, trustManagers, random);
        return construct(endpoint, context, enabledCipherSuites,
                         enabledProtocols, needClientAuth, mbs);
    }

    public static JMXConnectorServer contruct(InetSocketAddress jmxEndpoint,
                                              MBeanServer mbs)
                                                              throws IOException {

        // Ensure cryptographically strong random number generater used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");

        // This RMI server should not keep the VM alive
        Map<String, Object> env = new HashMap<String, Object>();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, new Exporter());
        return construct(jmxEndpoint, mbs, env);
    }

    /**
     * Answer a server that can be contacted by knowing this process' pid. The
     * server has already been started
     *
     * @param mbs
     * @return the started JMXConnectorServer
     * @throws IOException
     */
    public static JMXConnectorServer startLocalJmxServer(MBeanServer mbs)
                                                                         throws IOException {
        JMXConnectorServer server;
        // Ensure cryptographically strong random number generater used
        // to choose the object number - see java.rmi.server.ObjID
        System.setProperty("java.rmi.server.randomIDs", "true");
        // Ensure that the rmi server socket binds to the localhost, rather than the translated IP address
        System.setProperty("java.rmi.server.hostname", NO_PLACE_LIKE_HOME);

        // This RMI server should not keep the VM alive
        Map<String, RMIExporter> env = new HashMap<String, RMIExporter>();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, new Exporter());
        int port = Utils.allocatePort(InetAddress.getByName(NO_PLACE_LIKE_HOME));
        JMXServiceURL url = new JMXServiceURL("rmi", NO_PLACE_LIKE_HOME, port);
        server = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        server.start();
        ConnectorAddressLink.export(server.getAddress().toString());
        return server;
    }

    private static final String NO_PLACE_LIKE_HOME = "127.0.0.1";

    private RmiJmxServerFactory() {
    }
}
