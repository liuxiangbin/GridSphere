/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer;

import org.gridlab.gridsphere.event.WindowEvent;
import org.gridlab.gridsphere.event.MessageEvent;
import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.impl.SportletLog;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;


public class PortletInvoker {

    private static PortletLog log = SportletLog.getInstance(PortletInvoker.class);
    private static PortletRegistry registry = PortletRegistry.getInstance();

    public static final void init(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in init " + concretePortletID);
        String appID = getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher portletWrapper = appPortlet.getPortletWrapper();
            ConcretePortlet concPortlet = appPortlet.getConcretePortlet(concretePortletID);
            PortletSettings settings = concPortlet.getPortletSettings();
            // init the application portlet
            portletWrapper.init(req, res);
        } else {
            log.info("in init: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void initConcrete(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in initConcrete " + concretePortletID);
        String appID = getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher portletWrapper = appPortlet.getPortletWrapper();
            ConcretePortlet concPortlet = appPortlet.getConcretePortlet(concretePortletID);
            PortletSettings settings = concPortlet.getPortletSettings();
            // init the concrete portlet
            portletWrapper.initConcrete(settings, req, res);
        } else {
            log.info("in initConcrete: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void destroy(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in destroy " + concretePortletID);
        String appID = getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            // destroy the application portlet
            dispatcher.destroy(req, res);
        } else {
            log.info("in destroy: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void destroyConcrete(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in destroyConcrete " + concretePortletID);
        String appID = getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            ConcretePortlet concPortlet = appPortlet.getConcretePortlet(concretePortletID);
            PortletSettings settings = concPortlet.getPortletSettings();
            // destroy the concrete portlet
            dispatcher.destroyConcrete(settings, req, res);
        } else {
            log.info("in destroyConcrete: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void login(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in login " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.login(req, res);
        } else {
            log.info("in login: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void logout(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in logout " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.logout(req, res);
        } else {
            log.info("in logout: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void service(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in service " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.service(req, res);
        } else {
            log.info("in service: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void actionPerformed(String concretePortletID, DefaultPortletAction action, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in actionPerformed " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.actionPerformed(action, req, res);
        } else {
            log.info("in actionPerformed: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void doTitle(String concretePortletID, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in doTitle " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.doTitle(req, res);
        } else {
            log.info("in doTitle: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void windowEvent(String concretePortletID, WindowEvent winEvent, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in windowEvent " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.windowEvent(winEvent, req, res);
        } else {
            log.info("in windowEvent: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void messageEvent(String concretePortletID, DefaultPortletMessage msgEvent, PortletRequest req, PortletResponse res) throws PortletException {
        log.info("in messageEvent " + concretePortletID);
        String appID = registry.getApplicationPortletID(concretePortletID);
        ApplicationPortlet appPortlet = registry.getApplicationPortlet(appID);
        if (appPortlet != null) {
            PortletDispatcher dispatcher = appPortlet.getPortletWrapper();
            dispatcher.messageEvent(msgEvent, req, res);
        } else {
            log.info("in messageEvent: Unable to find portlet in registry: " + concretePortletID);
        }
    }

    public static final void initAllPortlets(PortletRequest req, PortletResponse res) throws PortletException {
        // Initialize all concrete portlets for each application portlet
        Collection appPortlets = registry.getAllApplicationPortlets();
        PortletDispatcher portletWrapper = null;
        Iterator it = appPortlets.iterator();
        while (it.hasNext()) {
            ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
            portletWrapper = appPortlet.getPortletWrapper();
            List concPortlets = appPortlet.getConcretePortlets();
            Iterator concIt = concPortlets.iterator();
            PortletSettings settings = null;
            // initialize the application portlet
            log.info("initializing application portlet " + appPortlet.getPortletAppID());
            portletWrapper.init(req, res);
            while (concIt.hasNext()) {
                ConcretePortlet concPortlet = (ConcretePortlet) concIt.next();
                settings = concPortlet.getPortletSettings();
                // initialize the concrete portlet
                log.info("initializing concrete portlet " + concPortlet.getConcretePortletAppID());
                portletWrapper.initConcrete(settings, req, res);
            }
        }
    }

    public static final void initPortletWebApp(String webApplicationName, PortletRequest req, PortletResponse res) throws PortletException {
        // Initialize all concrete portlets for each application portlet
        Collection appPortlets = registry.getApplicationPortlets(webApplicationName);
        PortletDispatcher portletWrapper = null;
        Iterator it = appPortlets.iterator();
        while (it.hasNext()) {
            ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
            portletWrapper = appPortlet.getPortletWrapper();
            List concPortlets = appPortlet.getConcretePortlets();
            Iterator concIt = concPortlets.iterator();
            PortletSettings settings = null;
            // initialize the application portlet
            log.info("initializing application portlet " + appPortlet.getPortletAppID());
            portletWrapper.init(req, res);
            while (concIt.hasNext()) {
                ConcretePortlet concPortlet = (ConcretePortlet) concIt.next();
                settings = concPortlet.getPortletSettings();
                // initialize the concrete portlet
                log.info("initializing concrete portlet " + concPortlet.getConcretePortletAppID());
                portletWrapper.initConcrete(settings, req, res);
            }
        }
    }

    public static final void destroyAllPortlets(PortletRequest req, PortletResponse res) throws PortletException {
        // First destroy all concrete portlets for each application portlet
        Collection appPortlets = registry.getAllApplicationPortlets();
        PortletDispatcher portletWrapper = null;
        Iterator it = appPortlets.iterator();
        while (it.hasNext()) {
            ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
            portletWrapper = appPortlet.getPortletWrapper();
            List concPortlets = appPortlet.getConcretePortlets();
            Iterator concIt = concPortlets.iterator();
            PortletSettings settings = null;
            log.info("destroying application portlet " + appPortlet.getPortletAppID());
            while (concIt.hasNext()) {
                ConcretePortlet concPortlet = (ConcretePortlet) concIt.next();
                settings = concPortlet.getPortletSettings();
                // destroy the concrete portlet
                log.info("destroying concrete portlet " + concPortlet.getConcretePortletAppID());
                portletWrapper.destroyConcrete(settings, req, res);
            }
        }
        // destroy the application portlet
        portletWrapper.destroy(req, res);
    }

    public static final void destroyPortletWebApp(String webApplicationName, PortletRequest req, PortletResponse res) throws PortletException {
        // First destroy all concrete portlets for each application portlet
        Collection appPortlets = registry.getApplicationPortlets(webApplicationName);
        PortletDispatcher portletWrapper = null;
        Iterator it = appPortlets.iterator();
        while (it.hasNext()) {
            ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
            portletWrapper = appPortlet.getPortletWrapper();
            List concPortlets = appPortlet.getConcretePortlets();
            Iterator concIt = concPortlets.iterator();
            PortletSettings settings = null;
            log.info("destroying application portlet " + appPortlet.getPortletAppID());
            while (concIt.hasNext()) {
                ConcretePortlet concPortlet = (ConcretePortlet) concIt.next();
                settings = concPortlet.getPortletSettings();
                // destroy the concrete portlet
                log.info("destroying concrete portlet " + concPortlet.getConcretePortletAppID());
                portletWrapper.destroyConcrete(settings, req, res);
            }
            // destroy the application portlet
            portletWrapper.destroy(req, res);
        }

    }

    protected static String getApplicationPortletID(String concretePortletID) {
        int i = concretePortletID.lastIndexOf(".");
        if (i < 0) return "";
        return concretePortletID.substring(0, i);
    }

}
