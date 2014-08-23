package com.hellblazer.utils.jmx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

/**
 * 
 * @author hal.hildebrand
 *
 */
public abstract class MBeanServerConnectionWrapper implements MBeanServer {

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        try {
            connection().addNotificationListener(name, listener, filter,
                                                 handback);
        } catch (IOException x) {
            throw handleIOException(x, "addNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        try {
            connection().addNotificationListener(name, listener, filter,
                                                 handback);
        } catch (IOException x) {
            throw handleIOException(x, "addNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public ObjectInstance createMBean(String className, ObjectName name)
                                                                        throws ReflectionException,
                                                                        InstanceAlreadyExistsException,
                                                                        MBeanRegistrationException,
                                                                        MBeanException,
                                                                        NotCompliantMBeanException {
        try {
            return connection().createMBean(className, name);
        } catch (IOException x) {
            throw handleIOException(x, "createMBean");
        }
    }

    // --------------------------------------------
    // --------------------------------------------
    //
    // Implementation of the MBeanServer interface
    //
    // --------------------------------------------
    // --------------------------------------------

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public ObjectInstance createMBean(String className, ObjectName name,
                                      Object params[], String signature[])
                                                                          throws ReflectionException,
                                                                          InstanceAlreadyExistsException,
                                                                          MBeanRegistrationException,
                                                                          MBeanException,
                                                                          NotCompliantMBeanException {
        try {
            return connection().createMBean(className, name, params, signature);
        } catch (IOException x) {
            throw handleIOException(x, "createMBean");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public ObjectInstance createMBean(String className, ObjectName name,
                                      ObjectName loaderName)
                                                            throws ReflectionException,
                                                            InstanceAlreadyExistsException,
                                                            MBeanRegistrationException,
                                                            MBeanException,
                                                            NotCompliantMBeanException,
                                                            InstanceNotFoundException {
        try {
            return connection().createMBean(className, name, loaderName);
        } catch (IOException x) {
            throw handleIOException(x, "createMBean");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public ObjectInstance createMBean(String className, ObjectName name,
                                      ObjectName loaderName, Object params[],
                                      String signature[])
                                                         throws ReflectionException,
                                                         InstanceAlreadyExistsException,
                                                         MBeanRegistrationException,
                                                         MBeanException,
                                                         NotCompliantMBeanException,
                                                         InstanceNotFoundException {
        try {
            return connection().createMBean(className, name, loaderName,
                                            params, signature);
        } catch (IOException x) {
            throw handleIOException(x, "createMBean");
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data)
                                                                      throws InstanceNotFoundException,
                                                                      OperationsException {
        throw new UnsupportedOperationException("deserialize");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ObjectInputStream deserialize(String className, byte[] data)
                                                                       throws OperationsException,
                                                                       ReflectionException {
        throw new UnsupportedOperationException("deserialize");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName, byte[] data)
                                                                            throws InstanceNotFoundException,
                                                                            OperationsException,
                                                                            ReflectionException {
        throw new UnsupportedOperationException("deserialize");
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public Object getAttribute(ObjectName name, String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException {
        try {
            return connection().getAttribute(name, attribute);
        } catch (IOException x) {
            throw handleIOException(x, "getAttribute");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException {
        try {
            return connection().getAttributes(name, attributes);
        } catch (IOException x) {
            throw handleIOException(x, "getAttributes");
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ClassLoader getClassLoader(ObjectName loaderName)
                                                            throws InstanceNotFoundException {
        throw new UnsupportedOperationException("getClassLoader");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
                                                              throws InstanceNotFoundException {
        throw new UnsupportedOperationException("getClassLoaderFor");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ClassLoaderRepository getClassLoaderRepository() {
        throw new UnsupportedOperationException("getClassLoaderRepository");
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public String getDefaultDomain() {
        try {
            return connection().getDefaultDomain();
        } catch (IOException x) {
            throw handleIOException(x, "getDefaultDomain");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public String[] getDomains() {
        try {
            return connection().getDomains();
        } catch (IOException x) {
            throw handleIOException(x, "getDomains");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public Integer getMBeanCount() {
        try {
            return connection().getMBeanCount();
        } catch (IOException x) {
            throw handleIOException(x, "getMBeanCount");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public MBeanInfo getMBeanInfo(ObjectName name)
                                                  throws InstanceNotFoundException,
                                                  IntrospectionException,
                                                  ReflectionException {
        try {
            return connection().getMBeanInfo(name);
        } catch (IOException x) {
            throw handleIOException(x, "getMBeanInfo");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public ObjectInstance getObjectInstance(ObjectName name)
                                                            throws InstanceNotFoundException {
        try {
            return connection().getObjectInstance(name);
        } catch (IOException x) {
            throw handleIOException(x, "getObjectInstance");
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public Object instantiate(String className) throws ReflectionException,
                                               MBeanException {
        throw new UnsupportedOperationException("instantiate");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public Object instantiate(String className, Object params[],
                              String signature[]) throws ReflectionException,
                                                 MBeanException {
        throw new UnsupportedOperationException("instantiate");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public Object instantiate(String className, ObjectName loaderName)
                                                                      throws ReflectionException,
                                                                      MBeanException,
                                                                      InstanceNotFoundException {
        throw new UnsupportedOperationException("instantiate");
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public Object instantiate(String className, ObjectName loaderName,
                              Object params[], String signature[])
                                                                  throws ReflectionException,
                                                                  MBeanException,
                                                                  InstanceNotFoundException {
        throw new UnsupportedOperationException("instantiate");
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public Object invoke(ObjectName name, String operationName,
                         Object params[], String signature[])
                                                             throws InstanceNotFoundException,
                                                             MBeanException,
                                                             ReflectionException {
        try {
            return connection().invoke(name, operationName, params, signature);
        } catch (IOException x) {
            throw handleIOException(x, "invoke");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException {
        try {
            return connection().isInstanceOf(name, className);
        } catch (IOException x) {
            throw handleIOException(x, "isInstanceOf");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public boolean isRegistered(ObjectName name) {
        try {
            return connection().isRegistered(name);
        } catch (IOException x) {
            throw handleIOException(x, "isRegistered");
        }
    }

    /**
     * Forward this method to the wrapped object. If an IOException is raised,
     * returns an empty Set.
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        try {
            return connection().queryMBeans(name, query);
        } catch (IOException x) {
            handleIOException(x, "queryMBeans");
            return new HashSet<ObjectInstance>();
        }
    }

    /**
     * Forward this method to the wrapped object. If an IOException is raised,
     * returns an empty Set.
     */
    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        try {
            return connection().queryNames(name, query);
        } catch (IOException x) {
            handleIOException(x, "queryNames");
            return new HashSet<ObjectName>();
        }
    }

    /**
     * Throws an {@link UnsupportedOperationException}. This behavior can be
     * changed by subclasses.
     */
    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name)
                                                                       throws InstanceAlreadyExistsException,
                                                                       MBeanRegistrationException,
                                                                       NotCompliantMBeanException {
        throw new UnsupportedOperationException("registerMBean");
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException {
        try {
            connection().removeNotificationListener(name, listener);
        } catch (IOException x) {
            throw handleIOException(x, "removeNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException {
        try {
            connection().removeNotificationListener(name, listener, filter,
                                                    handback);
        } catch (IOException x) {
            throw handleIOException(x, "removeNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener)
                                                                                throws InstanceNotFoundException,
                                                                                ListenerNotFoundException {
        try {
            connection().removeNotificationListener(name, listener);
        } catch (IOException x) {
            throw handleIOException(x, "removeNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException {
        try {
            connection().removeNotificationListener(name, listener, filter,
                                                    handback);
        } catch (IOException x) {
            throw handleIOException(x, "removeNotificationListener");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void setAttribute(ObjectName name, Attribute attribute)
                                                                  throws InstanceNotFoundException,
                                                                  AttributeNotFoundException,
                                                                  InvalidAttributeValueException,
                                                                  MBeanException,
                                                                  ReflectionException {
        try {
            connection().setAttribute(name, attribute);
        } catch (IOException x) {
            throw handleIOException(x, "setAttribute");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException {
        try {
            return connection().setAttributes(name, attributes);
        } catch (IOException x) {
            throw handleIOException(x, "setAttributes");
        }
    }

    /**
     * Forward this method to the wrapped object.
     */
    @Override
    public void unregisterMBean(ObjectName name)
                                                throws InstanceNotFoundException,
                                                MBeanRegistrationException {
        try {
            connection().unregisterMBean(name);
        } catch (IOException x) {
            throw handleIOException(x, "unregisterMBean");
        }
    }

    // Take care of getMBeanServerConnection returning null.
    //
    private synchronized MBeanServerConnection connection() throws IOException {
        final MBeanServerConnection c = getMBeanServerConnection();
        if (c == null) {
            throw new IOException("MBeanServerConnection unavailable");
        }
        return c;
    }

    /**
     * Returns an MBeanServerConnection. This method is called each time an
     * operation must be invoked on the underlying MBeanServerConnection.
     **/
    protected abstract MBeanServerConnection getMBeanServerConnection()
                                                                       throws IOException;

    /**
     * This method is called each time an IOException is raised when trying to
     * forward an operation to the underlying MBeanServerConnection, as a result
     * of calling {@link #getMBeanServerConnection()} or as a result of invoking
     * the operation on the returned connection. Subclasses may redefine this
     * method if they need to perform any specific handling of IOException
     * (logging etc...).
     * 
     * @param x
     *            The raised IOException.
     * @param method
     *            The name of the method in which the exception was raised. This
     *            is one of the methods of the MBeanServer interface.
     * @return A RuntimeException that should be thrown by the caller. In this
     *         default implementation, this is an
     *         {@link UndeclaredThrowableException} wrapping <var>x</var>.
     **/
    protected RuntimeException handleIOException(IOException x, String method) {
        final RuntimeException r = new UndeclaredThrowableException(x);
        return r;
    }

    // ----------------
    // PRIVATE METHODS
    // ----------------

}