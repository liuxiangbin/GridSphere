/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer.impl;

import org.gridlab.gridsphere.layout.PortletLayoutEngine;
import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
import org.gridlab.gridsphere.portletcontainer.PortletWebApplication;
import org.gridlab.gridsphere.portletcontainer.impl.descriptor.SportletDefinition;
import org.gridlab.gridsphere.portletcontainer.impl.descriptor.PortletDeploymentDescriptor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.net.URL;

/**
 * The <code>PortletWebApplicationImpl</code> ia an implementation of a <code>PortletWebApplication</code> that
 * represents a collection of portlets contained in a packaged WAR file. Currently
 * under development is the notion of dynamically managing portlet web applications.
 */
public class PortletWebApplicationImpl implements PortletWebApplication {

    private String webApplicationName = null;
    private Map appPortlets = new Hashtable();
    private RequestDispatcher rd = null;
    private String webAppDescription;

    // PortletLayout engine handles layout.xml
    private PortletLayoutEngine layoutEngine = PortletLayoutEngine.getInstance();

    /**
     * Default instantiation is disallowed
     */
    private PortletWebApplicationImpl() {}

    /**
     * Constructs an instance of a PortletWebApplicationImpl from a supplied web application name and corresponding
     * <code>ServletContext</code>
     *
     * @param webApplicationName the teh web application name
     * @param context the <code>ServletContext</code>
     */
    public PortletWebApplicationImpl(String webApplicationName, ServletContext context) {
        this.webApplicationName = webApplicationName;
        // get the servlet context for the coreportlets webapp
        String contextURIPath = "/" + webApplicationName;
        if (webApplicationName.startsWith("/")) {
            contextURIPath = webApplicationName;
        } else {
            contextURIPath = "/" + webApplicationName;
        }

        ServletContext ctx = context.getContext(contextURIPath);
        //System.err.println("contextURIPath: " + contextURIPath);
        //System.err.println("contextName: " + ctx.getServletContextName());
        //System.err.println("context path: " + ctx.getRealPath(""));


        //System.err.println("testing example portlets");
        //ServletContext testsc = context.getContext("/exampleportlets");
        //System.err.println("description: " + testsc.getServletContextName());
        //System.err.println("context path: " + testsc.getRealPath(""));
        //System.err.println("testing core portlets");
        //testsc = context.getContext("/coreportlets");
        //System.err.println("description: " + testsc.getServletContextName());
        //System.err.println("context path: " + testsc.getRealPath(""));


        this.webAppDescription = ctx.getServletContextName();

        if (ctx == null) System.err.println("Unable to get ServletContext for: " + contextURIPath);
        rd = ctx.getNamedDispatcher(webApplicationName);
        // load portlet.xml
        loadPortlets(ctx);
        // load layout.xml
        loadLayout(ctx);
    }

    /**
     * Loads collection of portlets from portlet descriptor file using the associated <code>ServletContext</code>
     *
     * @param ctx the <code>ServletContext</code>
     */
    protected void loadPortlets(ServletContext ctx) {
        // load in the portlet.xml file
        String portletXMLfile = ctx.getRealPath("") + "/WEB-INF/portlet.xml";
        String portletMappingFile = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_PORTLET_MAPPING);

        PortletDeploymentDescriptor pdd = null;
        try {
            pdd = new PortletDeploymentDescriptor(portletXMLfile, portletMappingFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Every SportletDefinition has a PortletApplication and possibly multiple ConcretePortletConfig's
        Iterator portletDefs = pdd.getPortletDefinitionList().iterator();

        // Iterate thru portlet definitions for portlet applications
        while (portletDefs.hasNext()) {
            SportletDefinition portletDef = (SportletDefinition) portletDefs.next();
            ApplicationPortlet portletApp = new ApplicationPortletImpl(pdd, portletDef, webApplicationName, ctx);
            String portletAppID = portletApp.getApplicationPortletID();
            appPortlets.put(portletAppID, portletApp);
        }
    }

    /**
     * Loads in a layout descriptor file from the associated servlet context
     *
     * @param ctx the <code>ServletContext</code>
     */
    protected void loadLayout(ServletContext ctx) {
        // load in the portlet.xml file
        String layoutXMLfile = ctx.getRealPath("") + "/WEB-INF/layout.xml";
        File f = new File(layoutXMLfile);
        if (f.exists()) {
            String layoutMappingFile = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_LAYOUT_MAPPING);
            layoutMappingFile = this.getClass().getResource(layoutMappingFile).getFile();
            layoutEngine.addApplicationTab(webApplicationName, layoutXMLfile);
        }
    }

    public void init() {

    }

    /**
     * Under development. A portlet web application can unregister itself from the application server
     */
    public void destroy() {
        layoutEngine.removeApplicationTab(webApplicationName);
    }

    /**
     * Returns the portlet web application name
     *
     * @return the web application name
     */
    public String getWebApplicationName() {
        return webApplicationName;
    }

    /**
     * Returns the portlet web application description
     *
     * @return the portlet web application description
     */
    public String getWebApplicationDescription() {
        return webAppDescription;
    }

    /**
     * Returns an application portlet contained by the portlet web application with the supplied id
     *
     * @param applicationPortletID an application portlet id
     * @return an application portlet
     */
    public ApplicationPortlet getApplicationPortlet(String portletApplicationID) {
        return (ApplicationPortlet) appPortlets.get(portletApplicationID);
    }

    /**
     * Returns the collection of application portlets contained by this portlet web application
     *
     * @return the collection of application portlets
     */
    public Collection getAllApplicationPortlets() {
        return appPortlets.values();
    }

}
