/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer;

import java.util.Collection;

/**
 * A <code>PortletWebApplication</code> represents a collection of portlets contained in a packaged WAR file. Currently
 * under development is the notion of dynamically managing portlet web applications.
 */
public interface PortletWebApplication {

    /**
     * Under development. A portlet web application can unregister itself from the application server
     */
    public void destroy();

    /**
     * Returns the portlet web application name
     *
     * @return the web application name
     */
    public String getWebApplicationName();

    /**
     * Returns the group owner name of this portlet web application
     *
     * @return the group owner name of this portlet web application
     */
    public String getGroupOwnerName();

    /**
     * Returns the portlet web application description
     *
     * @return the portlet web application description
     */
    public String getWebApplicationDescription();

    /**
     * Returns an application portlet contained by the portlet web application with the supplied id
     *
     * @param applicationPortletID an application portlet id
     * @return an application portlet
     */
    public ApplicationPortlet getApplicationPortlet(String applicationPortletID);

    /**
     * Returns the collection of application portlets contained by this portlet web application
     *
     * @return the collection of application portlets
     */
    public Collection getAllApplicationPortlets();
}
