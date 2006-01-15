/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlet.service.spi.impl;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.service.PortletService;
import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceAuthorizer;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceCollection;
import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceDefinition;
import org.gridlab.gridsphere.portlet.service.spi.impl.descriptor.SportletServiceDescriptor;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
import org.gridlab.gridsphere.portletcontainer.PortletSessionManager;
import org.gridlab.gridsphere.services.core.user.UserSessionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.lang.reflect.Constructor;
import java.util.*;
import java.net.URL;
import java.io.File;

/**
 * The <code>SportletServiceFactory</code> provides a factory for the creation
 * of portlet services. The <code>SportletServiceFactory</code> is also
 * responsible for portlet service lifecycle management including
 * initialization and shutdown.
 */
public class SportletServiceFactory implements PortletServiceFactory, PortletSessionListener {

    private static PortletLog log = SportletLog.getInstance(SportletServiceFactory.class);
    private static SportletServiceFactory instance = null;
    private static PortletSessionManager portletSessionManager = PortletSessionManager.getInstance();
    private static UserSessionManager userSessionManager = UserSessionManager.getInstance();


    // Maintain a single copy of each service instantiated
    // as a classname and PortletServiceProvider pair
    private static Hashtable initServices = new Hashtable();

    // Hash of all services key = service interface name, value = SportletServiceDefinition
    private static Hashtable allServices = new Hashtable();

    // Hash of all user services
    private static Hashtable userServices = new Hashtable();

    // Hash of all user services
    private static Hashtable serviceContexts = new Hashtable();


    // Hash of all user services
    private static Hashtable classLoaders = new Hashtable();

    private static Hashtable webappServices = new Hashtable();

    //public static String servicesMappingPath = null;

    public static URL servicesMappingStream = null;

    public static Hashtable springBeans = new Hashtable();

    /**
     * Private constructor. Use getDefault() instead.
     */
    private SportletServiceFactory() {
        servicesMappingStream = this.getClass().getResource("/gridsphere/portlet-services-mapping.xml");
    }

    public static synchronized SportletServiceFactory getInstance() {
        if (instance == null) {
            instance = new SportletServiceFactory();
        }
        return instance;
    }

    public void init() throws PortletServiceException {
        // Reads in the service definitions from the xml file and stores them in allServices
        // organized according to service interface keys and service definition values
        String servicesPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/GridSphereServices.xml");
        //servicesMappingPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/mapping/portlet-services-mapping.xml");
        addServices(GridSphereConfig.getServletContext(), servicesPath);
        // playing with Spring
        //addSpringServices(GridSphereConfig.getServletContext());
    }

    public void login(PortletRequest req) throws PortletException {

    }

    public void logout(PortletSession session) throws PortletException {
        log.debug("in logout of SportletServiceFactory");
        String userid = userSessionManager.getUserIdFromSession(session);
        if ((userid != null) && (userServices.containsKey(userid))) {
            log.debug("Removing services for userid: " + userid);
            Map servs = (Map) userServices.get(userid);
            Collection c = servs.values();
            Iterator it = c.iterator();
            while (it.hasNext()) {
                PortletServiceProvider psp = (PortletServiceProvider) it.next();
                psp.destroy();
                psp = null;
            }
            userServices.remove(userid);
        }
    }

    /**
     * Adds spring beans defined in a portlet application's aaplicationContext.xml to the springbeans HashMap
     * to be access using the createSpringService method
     *
     * @param ctx the Servlet Context
     */
    public synchronized void addSpringServices(ServletContext ctx) {
        try {
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(ctx);
        String[] beanDefs = context.getBeanDefinitionNames();
        for (int i = 0; i < beanDefs.length; i++) {
            springBeans.put(beanDefs[i], context);
        }
        } catch (IllegalStateException e) {
            log.error("No applicationContext.xml found for: " + ctx.getServletContextName());
        }
    }

    /**
     * Umarshalls services from the descriptor file found in servicesPath
     * using the mapping file specified
     *
     * @param ctx the servlet context
     * @param servicesPath the path to the portlet services descriptor file
     */
    public synchronized void addServices(ServletContext ctx, String servicesPath) throws PortletServiceException {
        // check if services path represents a single file or a directory
        File f = new File(servicesPath);
        String[] servicePaths = null;
        if (f.isDirectory()) {
            servicePaths = f.list();
            for (int i = 0; i < servicePaths.length; i++) {
                servicePaths[i] = servicesPath + File.separator + servicePaths[i];
            }
        } else {
            servicePaths = new String[1];
            servicePaths[0] = servicesPath;
        }
        for (int i = 0; i < servicePaths.length; i++) {
            SportletServiceDescriptor descriptor = null;
            try {
                System.err.println("loading from: " + servicePaths[i]);
                descriptor = new SportletServiceDescriptor(servicePaths[i], servicesMappingStream);
            } catch (Exception e) {
                //log.error("error unmarshalling " + servicesPath + " using " + servicesMappingPath + " : " + e.getMessage());
                throw new PortletServiceException("error unmarshalling " + servicesPath, e);
            }
            SportletServiceCollection serviceCollection = descriptor.getServiceCollection();
            List services = serviceCollection.getPortletServicesList();
            Iterator it = services.iterator();
            while (it.hasNext()) {
                SportletServiceDefinition serviceDef = (SportletServiceDefinition) it.next();
                serviceDef.setServiceDescriptor(descriptor);
                allServices.put(serviceDef.getServiceInterface(), serviceDef);
                log.debug("adding service: " + serviceDef.getServiceInterface() + " service def: " + serviceDef.toString());
                serviceContexts.put(serviceDef.getServiceInterface(), ctx);
            }
            it = services.iterator();
            while (it.hasNext()) {
                SportletServiceDefinition serviceDef = (SportletServiceDefinition) it.next();
                if (serviceDef.isLoadOnStartup()) {
                    log.debug("loading service : " + serviceDef.getServiceInterface());
                    try {
                        createPortletService(Class.forName(serviceDef.getServiceInterface()), ctx, true);
                    } catch (ClassNotFoundException e) {
                        log.error("Unable to find class : " + serviceDef.getServiceImplementation());
                    }
                }
            }
        }
    }

    /**
     * Umarshalls services from the descriptor file found in servicesPath
     * using the mapping file specified
     *
     * @param webappName the name of the web application
     * @param ctx the servlet context
     * @param servicesPath the path to the portlet services descriptor file
     * @param loader the class loader
     */
    public synchronized void addServices(String webappName, ServletContext ctx, String servicesPath, ClassLoader loader) throws PortletServiceException {
        SportletServiceDescriptor descriptor = null;
        File f = new File(servicesPath);
        String[] servicePaths = null;
        if (f.isDirectory()) {
            servicePaths = f.list();
            for (int i = 0; i < servicePaths.length; i++) {
                servicePaths[i] = servicesPath + File.separator + servicePaths[i];
            }

        } else {
            servicePaths = new String[1];
            servicePaths[0] = servicesPath;
        }
        for (int i = 0; i < servicePaths.length; i++) {
            try {
                System.err.println("loading from: " + servicePaths[i]);
                descriptor = new SportletServiceDescriptor(servicePaths[i], servicesMappingStream);
            } catch (Exception e) {
                log.error("Error unmarshalling " + servicesPath, e);
                throw new PortletServiceException("Error unmarshalling " + servicesPath, e);
            }
            SportletServiceCollection serviceCollection = descriptor.getServiceCollection();
            List services = serviceCollection.getPortletServicesList();
            List webapplist = new ArrayList();
            Iterator it = services.iterator();
            while (it.hasNext()) {
                SportletServiceDefinition serviceDef = (SportletServiceDefinition) it.next();
                serviceDef.setServiceDescriptor(descriptor);
                allServices.put(serviceDef.getServiceInterface(), serviceDef);
                log.debug("adding service: " + serviceDef.getServiceInterface() + " service def: " + serviceDef.toString());
                serviceContexts.put(serviceDef.getServiceInterface(), ctx);
                classLoaders.put(serviceDef.getServiceInterface(), loader);
                webapplist.add(serviceDef.getServiceInterface());
            }
            webappServices.put(webappName, webapplist);
            it = services.iterator();
            while (it.hasNext()) {
                SportletServiceDefinition serviceDef = (SportletServiceDefinition) it.next();
                if (serviceDef.isLoadOnStartup()) {
                    log.debug("loading service : " + serviceDef.getServiceInterface());
                    try {
                        createPortletService(Class.forName(serviceDef.getServiceInterface(), true, loader), ctx, true);
                    } catch (ClassNotFoundException e) {
                        log.error("Unable to find class : " + serviceDef.getServiceImplementation());
                    }
                }
            }
        }
    }

    /**
     * Returns a Spring (www.springframework.org) service defined in applicationContext.xml by its
     * bean name
     *
     * @param beanName the bean name identifying the spring service
     * @return the Spring service defined in applicationContext.xml or null if none exists
     */
    public Object createSpringService(String beanName) {
        WebApplicationContext ctx = (WebApplicationContext)springBeans.get(beanName);
        return (ctx != null) ? ctx.getBean(beanName) : null;
    }

    /**
     * createPortletServiceFactory instantiates the given class and initializes it. The portlet service properties
     * file must be specified in the ServletContext as an InitParameter with the "PortletServices.properties" key.
     * If no properties file is found or any error occurs an exception is thrown.
     *
     * @param service        the class of the service
     * @param servletContext the servlet configuration
     * @return the instantiated portlet service
     * @throws PortletServiceUnavailableException
     *          if the portlet service is unavailable
     * @throws PortletServiceNotFoundException
     *          if the PortletService is not found
     */
    public PortletService createPortletService(Class service,
                                               ServletContext servletContext,
                                               boolean useCachedService)
            throws PortletServiceUnavailableException, PortletServiceNotFoundException {



        PortletServiceProvider psp = null;
        /*
        try {
        psp = (PortletServiceProvider)springContext.getBean(service.getName());
        if (psp != null) {
            System.err.println("Actually found " + service.getName() + " from spring!");
            return psp;
        }
        } catch (BeansException e) {
            log.info("Unable to find bean :" + service.getName());
        }
        */

        // see if we already have an instance of this service
        if (service == null) {
            throw new PortletServiceUnavailableException("Received null service class");
        }

        String serviceName = service.getName();
        // if init'ed service exists then use it
        if (useCachedService) {
            psp = (PortletServiceProvider) initServices.get(serviceName);
            if (psp != null) return psp;
        }

        SportletServiceDefinition def = (SportletServiceDefinition) allServices.get(serviceName);
        if (def == null) {
            log.error("Unable to find portlet service interface: " + serviceName +
                    " . Please check PortletServices.xml file for proper service entry");
            throw new PortletServiceNotFoundException("Unable to find portlet service: " + serviceName);
        }

        // if user is required then pass in Guest user privileges
        if (def.getUserRequired()) {
            return createUserPortletService(service, null, servletContext, useCachedService);
        }

        /* Create the service implementation */
        String serviceImpl = def.getServiceImplementation();
        if (serviceImpl == null) {
            log.error("Unable to find implementing portlet service: " + serviceName +
                    " . Please check PortletServices.xml file for proper service entry");
            throw new PortletServiceNotFoundException("Unable to find implementing portlet service for interface: " + serviceName);
        }

        ServletContext ctx = (ServletContext) serviceContexts.get(serviceName);
        PortletServiceConfig portletServiceConfig = new SportletServiceConfig(def, ctx);

        try {
            ClassLoader loader = (ClassLoader) classLoaders.get(serviceName);
            if (loader != null) {
                psp = (PortletServiceProvider) Class.forName(serviceImpl, true, loader).newInstance();
            } else {
                psp = (PortletServiceProvider) Class.forName(serviceImpl).newInstance();
            }
        } catch (InstantiationException e) {
            // InstantiationException - if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason. 
            log.error("Unable to create portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl + " Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason.", e);
        } catch (IllegalAccessException e) {
            // IllegalAccessException - if the class or its nullary constructor is not accessible. 
            log.error("Unable to create portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl + " class or its nullary constructor is not accessible.", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to create portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl + " Class not found.", e);
        } catch (ExceptionInInitializerError e) {
            // the initialization provoked by this method fails.
            log.error("Unable to create portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl + " the initialization provoked by this method fails.", e);
        } catch (Exception e) {
            log.error("Unable to create portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceImpl, e);
        }

        try {
            psp.init(portletServiceConfig);
        } catch (PortletServiceUnavailableException e) {
            log.error("Unable to initialize portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to initialize portlet service: " + serviceImpl, e);
        }

        initServices.put(serviceName, psp);
        return psp;
    }

    /**
     * Creates a user specific portlet service. If no instance exists, the service
     * will be initialized before it is returned to the client.
     *
     * @param service          the class of the service
     * @param user             the User
     * @param servletContext   the servlet configuration
     * @param useCachedService reuse a previous initialized service if <code>true</code>,
     *                         otherwise create a new service instance if <code>false</code>
     * @return the instantiated portlet service
     * @throws PortletServiceUnavailableException
     *          if the portlet service is unavailable
     * @throws PortletServiceNotFoundException
     *          if the PortletService is not found
     */
    public PortletService createUserPortletService(Class service, User user,
                                                   ServletContext servletContext,
                                                   boolean useCachedService)
            throws PortletServiceUnavailableException, PortletServiceNotFoundException {

        //PortletServiceProvider psp = null;

        String serviceName = service.getName();

        SportletServiceDefinition def = (SportletServiceDefinition) allServices.get(serviceName);
        if (def == null) {
            log.error("Unable to find portlet service interface: " + serviceName +
                    " . Please check PortletServices.xml file for proper service entry");
            throw new PortletServiceNotFoundException("Unable to find portlet service: " + serviceName);
        }
        if (!def.getUserRequired()) {
            return createPortletService(service, servletContext, useCachedService);
        } else if (user == null) {
            throw new PortletServiceNotFoundException("Unable to create service: " + serviceName + " user is null");
        }

        if ((user == null) && (initServices.containsKey(serviceName))) {
            return (PortletService) initServices.get(serviceName);
        }

        if (useCachedService) {
            Map userServiceMap = (Map) userServices.get(user.getID());
            if (userServiceMap != null) {
                if (userServiceMap.containsKey(serviceName)) {
                    return (PortletService) userServiceMap.get(serviceName);
                }
            }
        }

        /* Create the service implementation */
        String serviceImpl = def.getServiceImplementation();
        if (serviceImpl == null) {
            log.error("Unable to find implementing portlet service: " + serviceName +
                    " . Please check PortletServices.xml file for proper service entry");
            throw new PortletServiceNotFoundException("Unable to find implementing portlet service for interface: " + serviceName);
        }

        ServletContext ctx = (ServletContext) serviceContexts.get(serviceName);
        PortletServiceConfig portletServiceConfig =
                new SportletServiceConfig(def, ctx);

        // Create an authroizer for the secure service
        //PortletServiceAuthorizer auth = new SportletServiceAuthorizer(user, aclManager);

        // instantiate wrapper with user and impl
        PortletServiceProvider psp = null;
        //PortletServiceProvider psp = (PortletServiceProvider)initServices.get(service);
        //if (psp == null) {
        try {
            ClassLoader loader = (ClassLoader) classLoaders.get(serviceName);
            Class c = null;
            if (loader != null) {
                c = Class.forName(serviceImpl, true, loader);
            } else {
                c = Class.forName(serviceImpl);
            }
            Class[] parameterTypes = new Class[]{PortletServiceAuthorizer.class};
            Object[] obj = new Object[]{};
            Constructor con = c.getConstructor(parameterTypes);
            psp = (PortletServiceProvider) con.newInstance(obj);
        } catch (Exception e) {
            log.error("Unable to create portlet service wrapper: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("Unable to create portlet service: " + serviceName, e);
        }

        try {
            psp.init(portletServiceConfig);
        } catch (PortletServiceUnavailableException e) {
            log.error("Unable to initialize portlet service: " + serviceImpl, e);
            throw new PortletServiceNotFoundException("The SportletServiceFactory was unable to initialize the portlet service: " + serviceImpl, e);
        }

        if (user == null) {
            initServices.put(serviceName, psp);
            return psp;
        }

        Map userServiceMap = (Map) userServices.get(user.getID());
        if (userServiceMap == null) userServiceMap = new HashMap();

        userServiceMap.put(serviceName, psp);
        log.debug("Creating a user service for user: " + user.getID() + " " + serviceName);
        userServices.put(user.getID(), userServiceMap);

        List sessions = userSessionManager.getSessions(user);
        if (sessions != null) {
            Iterator it = sessions.iterator();
            while (it.hasNext()) {
                PortletSession session = (PortletSession) it.next();
                log.debug("Adding a session listener for session: " + session.getId() + " to portlet session manager");
                if ((session != null) && (session.getId() != null)) portletSessionManager.addSessionListener(session.getId(), this);
            }
        }
        return psp;
    }

    /**
     * Returns an enumaration of the active services (services that have been
     * initialized)
     *
     * @return an enumaration of the active services
     */
    public Enumeration getActiveServices() {
        return initServices.keys();
    }

    /**
     * Destroys a portlet service identified by its class
     *
     * @param service the service class to shutdown
     */
    public void shutdownService(Class service) {
        if (initServices.containsKey(service.getName())) {
            log.info("Shutting down service: " + service.getName());
            PortletServiceProvider psp = (PortletServiceProvider) initServices.get(service);
            psp.destroy();
        }
    }

    /**
     * Shuts down all portlet services managed by this factory
     */
    public void shutdownServices() {
        // Calls destroy() on all services we know about
        log.info("Shutting down all portlet services:");
        Enumeration keys = initServices.keys();
        while (keys.hasMoreElements()) {
            String serviceName = (String) keys.nextElement();
            PortletServiceProvider psp = (PortletServiceProvider) initServices.get(serviceName);
            log.info("Shutting down service: " + serviceName + " impl: " + psp.getClass().getName());
            psp.destroy();
        }
        keys = userServices.keys();
        while (keys.hasMoreElements()) {
            String userID = (String) keys.nextElement();
            Map userServiceMap = (Map) userServices.get(userID);
            Collection userServColl = userServiceMap.values();
            Iterator i = userServColl.iterator();
            while (i.hasNext()) {
                PortletServiceProvider psp = (PortletServiceProvider) i.next();
                psp.destroy();
                i.remove();
            }
        }
    }

    /**
     * Shuts down all portlet services managed by this factory
     */
    public void shutdownServices(String webappName) {
        // Calls destroy() on all services we know about
        List services = (List) webappServices.get(webappName);
        if (services == null) return;
        log.info("Shutting down  portlet services for webapp: " + webappName);
        List remServices = new ArrayList();
        Iterator it = services.iterator();
        while (it.hasNext()) {
            String iface = (String) it.next();
            // Check standard portlet services that belong to this webapp
            Enumeration keys = initServices.keys();
            while (keys.hasMoreElements()) {
                String serviceName = (String) keys.nextElement();
                if (serviceName.equals(iface)) {
                    PortletServiceProvider psp = (PortletServiceProvider) initServices.get(serviceName);
                    if (psp != null) {
                        log.info("Shutting down service: " + serviceName + " impl: " + psp.getClass().getName());
                        psp.destroy();
                        remServices.add(serviceName);
                    }
                }
            }
            // Check user services that contain service name
            Enumeration ukeys = userServices.keys();
            while (ukeys.hasMoreElements()) {
                String userID = (String) ukeys.nextElement();
                Map userServiceMap = (Map) userServices.get(userID);
                Set s = userServiceMap.keySet();
                Iterator i = s.iterator();
                while (i.hasNext()) {
                    String sname = (String) i.next();
                    if (sname.equals(iface)) {
                        PortletServiceProvider psp = (PortletServiceProvider) userServiceMap.get(sname);
                        psp.destroy();
                        i.remove();
                    }
                }
            }
        }

        it = remServices.iterator();
        while (it.hasNext()) {
            String serviceName = (String) it.next();
            initServices.remove(serviceName);
            allServices.remove(serviceName);
            classLoaders.remove(serviceName);
            serviceContexts.remove(serviceName);
        }
        webappServices.remove(webappName);
    }

    public void logStatistics() {
        Enumeration e = null;
        if (initServices != null) {
            log.debug("printing inited services");
            e = initServices.keys();
            String ser = "services:\n";
            while (e.hasMoreElements()) {
                String s = (String) e.nextElement();
                ser += s + "\n";
            }
            log.debug(ser);
        }
        if (userServices != null) {
            Set s = userServices.keySet();
            Iterator users = s.iterator();
            log.debug("printing user services ");
            while (users.hasNext()) {
                String uid = (String) users.next();
                log.debug("user: " + uid);
                e = userServices.keys();
                while (e.hasMoreElements()) {
                    String u = (String) e.nextElement();
                    Map l = (Map) userServices.get(u);
                    Iterator it = l.keySet().iterator();
                    String ser = "services:\n";
                    while (it.hasNext()) {
                        String j = (String) it.next();
                        ser += j + "\n";
                    }
                    log.debug(ser);
                }
            }
        }
    }
}
