/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portlet;

import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Comparator;

/**
 * A portlet is a small Java program that runs within a portlet container.
 * Portlets receive and respond to requests from the portlet container.
 * There is ever only one portlet object instance per portlet configuration in the web deployment descriptor.
 * There may be many PortletSettings objects parameterisng the same portlet object according to the
 * Flyweight pattern, provided on a per-request basis. A concrete parameterization of a portlet object
 * is referred to as a concrete portlet. The settings of concrete portlets may change at any time caused
 * by administrators modifying portlet settings, e.g. using the config mode of a portlet.
 * <p>
 * Additionally, user can have personal views of concrete portlets. Therefore, the transient portlet session
 * and persistent concrete portlet data carries vital information for the portlet to create a personalized
 * user experience. A concrete portlet in conjunction with portlet data creates a concrete portlet instance.
 * This is similar to why a servlet may not store things depending on requests or sessions in instance variables.
 * As a consequence, the portlet should not attempt to store any data that depends on portlet settings,
 * portlet data or the portlet session or any other user-related information as instance or class variables.
 * The general programming rules for servlets also apply to portlets - instance variables should only used
 * when the intent is to share them between all the parallel threads that concurrently execute a portlet, and
 * portlet code should avoid synchronization unless absolutely required.
 * <p>
 * As part of running within the portlet container each portlet has a life-cycle.
 * The corresponding methods are called in the following sequence:
 * <p>
 * <ol>
 * <li>The portlet is constructed, then initialized with the init() method.</li>
 * <li>A concrete portlet is initialized with the {@link #initConcrete} method for each PortletSettings.</li>
 * <li>Any calls from the portlet container to the service() method are handled.</li>
 * <li>The concrete portlet is taken out of service with the destroyConcrete() method.</li>
 * <li>The portlet is taken out of service, then destroyed with the destroy() method,
 * then garbage collected and finalized.</li>
 * </ol>
 * <p>
 * The <it>concrete portlet instance</it> is created and destroyed with the login() and logout() methods, respectively.
 * If a portlet provides personalized views these methods should be implemented.
 * <p>
 * The portlet container loads and instantiates the portlet class.
 * This can happen during startup of the portal server or later,
 * but no later then when the first request to the portlet has to be serviced.
 * Also, if a portlet is taken out of service temporarily, for example while administrating it,
 * the portlet container may finish the life-cycle before taking the portlet out of service.
 * When the administration is done, the portlet will be newly initialized.
 */
public abstract class Portlet extends HttpServlet
        implements PortletSessionListener, Servlet, ServletConfig, Serializable {

    protected PortletConfig portletConfig = null;
    protected PortletSettings portletSettings = null;

    protected transient static PortletLog log = SportletLog.getInstance(Portlet.class);

    /**
     * A <code>Mode</code> is an immutable representation of the portlet mode.
     * Possible mode values are <code>View</code>, <code>EDIT</code>,
     * <code>HELP</code> and <code>CONFIGURE</code>
     */
    public static class Mode implements Comparator, Serializable {

        protected static final int VIEW_MODE = 1;
        protected static final int CONFIGURE_MODE = 2;
        protected static final int EDIT_MODE = 3;
        protected static final int HELP_MODE = 4;

        public static final Mode EDIT = new Mode(EDIT_MODE);
        public static final Mode VIEW = new Mode(VIEW_MODE);
        public static final Mode HELP = new Mode(HELP_MODE);
        public static final Mode CONFIGURE = new Mode(CONFIGURE_MODE);

        private int mode = VIEW_MODE;

        /**
         * Private constructor creates pre-defined Mode objects
         *
         * @param mode the portlet mode to create
         */
        private Mode(int mode) {
            this.mode = mode;
        }

        /**
         * Return a portlet mode from parsing a <code>String</code> representation
         * of a portlet mode.
         *
         * @throws IllegalArgumentException if the supplied <code>String</code>
         * does not match a predefined portlet mode.
         */
        public static Portlet.Mode toMode(String mode) throws IllegalArgumentException {
            if (mode == null) return null;
            if (mode.equalsIgnoreCase(EDIT.toString())) {
                return EDIT;
            } else if (mode.equalsIgnoreCase(VIEW.toString())) {
                return VIEW;
            } else if (mode.equalsIgnoreCase(HELP.toString())) {
                return HELP;
            } else if (mode.equalsIgnoreCase(CONFIGURE.toString())) {
                return CONFIGURE;
            }
            throw new IllegalArgumentException("Unable to parse supplied mode: " + mode);
        }

        /**
         * Returns the portlet mode as an integer
         *
         * @return id the portlet mode
         */
        public int getMode() {
            return mode;
        }

        /**
         * Returns the portlet mode as a <code>String</code>
         *
         * @return the portlet mode as a <code>String</code>
         */
        public String toString() {
            String tagstring = "Unknowm Portlet Mode!";
            if (mode == EDIT_MODE) {
                tagstring = "EDIT";
            } else if (mode == HELP_MODE) {
                tagstring = "HELP";
            } else if (mode == CONFIGURE_MODE) {
                tagstring = "CONFIGURE";
            } else if (mode == VIEW_MODE) {
                tagstring = "VIEW";
            }
            return tagstring;
        }

        public int compare(Object left, Object right) {
            int leftID  =  ((Portlet.Mode)left).getMode();
            int rightID  = ((Portlet.Mode)right).getMode();
            int result;
            if ( leftID < rightID ) { result = -1; }
            else if ( leftID > rightID ) { result = 1; }
            else { result = 0; }
            return result;
        }

        public boolean equals(Object o) {
            if ((o != null)  && (o instanceof Portlet.Mode)) {
                return (this.mode == ((Portlet.Mode)o).getMode() ? true : false);
            }
            return false;
        }

        public int hashCode() {
            return mode;
        }
    }

    /**
     * Called by the portlet container to indicate to this portlet that it is put into service.
     *
     * The portlet container calls the init() method for the whole life-cycle of the portlet.
     * The init() method must complete successfully before concrete portlets are created through
     * the initConcrete() method.
     *
     * The portlet container cannot place the portlet into service if the init() method
     *
     * 1. throws UnavailableException
     * 2. does not return within a time period defined by the portlet container.
     *
     * @param config the portlet configuration
     * @throws UnavailableException if an exception has occurrred that interferes with the portlet's
     * normal initialization
     */
    public abstract void init(PortletConfig config) throws UnavailableException;

    /**
     * Called by the portlet container to indicate to this portlet that it is taken out of service.
     * This method is only called once all threads within the portlet's service() method have exited
     * or after a timeout period has passed. After the portlet container calls this method,
     * it will not call the service() method again on this portlet.
     *
     * This method gives the portlet an opportunity to clean up any resources that are
     * being held (for example, memory, file handles, threads).
     *
     * @param config the portlet configuration
     */
    public abstract void destroy(PortletConfig config);

    /**
     * Called by the portlet container to indicate that the concrete portlet is put into service.
     * The portlet container calls the initConcrete() method for the whole life-cycle of the portlet.
     * The initConcrete() method must complete successfully before concrete portlet instances can be
     * created through the login() method.
     *
     * The portlet container cannot place the portlet into service if the initConcrete() method
     *
     * 1. throws UnavailableException
     * 2. does not return within a time period defined by the portlet container.
     *
     * @param settings the portlet settings
     */
    public abstract void initConcrete(PortletSettings settings) throws UnavailableException;

    /**
     * Called by the portlet container to indicate that the concrete portlet is taken out of service.
     * This method is only called once all threads within the portlet's service() method have exited
     * or after a timeout period has passed. After the portlet container calls this method,
     * it will not call the service() method again on this portlet.
     *
     * This method gives the portlet an opportunity to clean up any resources that are being
     * held (for example, memory, file handles, threads).
     *
     * @param settings the portlet settings
     */
    public abstract void destroyConcrete(PortletSettings settings);

    /**
     * Called by the portlet container to ask this portlet to generate its markup using the given
     * request/response pair. Depending on the mode of the portlet and the requesting client device,
     * the markup will be different. Also, the portlet can take language preferences and/or
     * personalized settings into account.
     *
     * @param request the portlet request
     * @param response the portlet response
     *
     * @throws PortletException if the portlet has trouble fulfilling the rendering request
     * @throws IOException if the streaming causes an I/O problem
     */
    public abstract void service(PortletRequest request, PortletResponse response)
            throws PortletException, IOException;

    /**
     * Description copied from interface: PortletSessionListener
     * Called by the portlet container to ask the portlet to initialize a personalized user experience.
     * In addition to initializing the session this method allows the portlet to initialize the
     * concrete portlet instance, for example, to store attributes in the session.
     *
     * @param request the portlet request
     */
    public abstract void login(PortletRequest request);

    /**
     * Description copied from interface: PortletSessionListener
     * Called by the portlet container to indicate that a concrete portlet instance is being removed.
     * This method gives the concrete portlet instance an opportunity to clean up any resources
     * (for example, memory, file handles, threads), before it is removed.
     * This happens if the user logs out, or decides to remove this portlet from a page.
     *
     * @param session the portlet session
     */
    public abstract void logout(PortletSession session);

    /**
     * Returns the time the response of the PortletInfo  object was last modified, in milliseconds since midnight
     * January 1, 1970 GMT. If the time is unknown, this method returns a negative number (the default).
     *
     * Portlets that can quickly determine their last modification time should override this method.
     * This makes browser and proxy caches work more effectively, reducing the load on server and network resources.
     *
     * @param request the portlet request
     * @return long a long integer specifying the time the response of the PortletInfo
     * object was last modified, in milliseconds since midnight, January 1, 1970 GMT, or -1 if the time is not known
     */
    public abstract long getLastModified(PortletRequest request);

    /**
     * Returns the PortletConfig object of the portlet
     *
     * @return the PortletConfig object
     */
    protected abstract PortletConfig getPortletConfig();

    /**
     * Returns the PortletSettings object of the concrete portlet.
     *
     * @return the PortletSettings object, or NULL if no PortletSettings object is available.
     */
    protected PortletSettings getPortletSettings() {
        return this.portletSettings;
    }

    /**
     * Initializes the PortletConfig using the web.xml file entry for this portlet
     */
    public final void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public final void init() throws ServletException {
        super.init();
    }

    public final ServletConfig getServletConfig() {
        return super.getServletConfig();
    }

    public final String getInitParameter(String name) {
        return super.getInitParameter(name);
    }

    public final Enumeration getInitParameterNames() {
        return super.getInitParameterNames();
    }

    public final ServletContext getServletContext() {
        return super.getServletContext();
    }

    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    public String getServletInfo() {
        return super.getServletInfo();
    }

    public final void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        super.service(request, response);
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // create portlet request and response objects
        PortletRequest portletRequest = new SportletRequestImpl(request);
        PortletResponse portletResponse = new SportletResponse(response, portletRequest);
        String method = (String) request.getAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
        if (method != null) {

            if (method.equals(SportletProperties.INIT)) {
                ApplicationPortletConfig app = (ApplicationPortletConfig) request.getAttribute(SportletProperties.PORTLET_APPLICATION);
                if (app != null) {
                this.portletConfig = new SportletConfig(getServletConfig(), app);
                init(this.portletConfig);
                } else {
                    log.error("Unable to perform init(): Received NULL PortletApplication in request");
                }
            } else if (method.equals(SportletProperties.SERVICE)) {
                service(portletRequest, portletResponse);
            } else if (method.equals(SportletProperties.DESTROY)) {
                destroy(this.portletConfig);
            } else if (method.equals(SportletProperties.INIT_CONCRETE)) {
                PortletSettings settings = (PortletSettings) request.getAttribute(SportletProperties.PORTLET_SETTINGS);
                if (settings != null) {
                    initConcrete(settings);
                } else {
                    log.error("Unable to perform initConcrete(): Received NULL PortletSettings in request");
                }
            } else if (method.equals(SportletProperties.DESTROY_CONCRETE)) {
                PortletSettings settings = (PortletSettings) request.getAttribute(SportletProperties.PORTLET_SETTINGS);
                if (settings != null) {
                    destroyConcrete(settings);
                } else {
                    log.error("Unable to perform destroyConcrete(): Received NULL PortletSettings in request");
                }
            } else if (method.equals(SportletProperties.LOGIN)) {
                login(portletRequest);
            } else if (method.equals(SportletProperties.LOGOUT)) {
                PortletSession portletSession = portletRequest.getPortletSession();
                logout(portletSession);
            }

        }
        request.removeAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doGet(req, res);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doPut(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doPost(req, res);
    }

    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doTrace(req, res);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.doDelete(req, res);
    }

    public final void destroy() {
        super.destroy();
    }

}










































