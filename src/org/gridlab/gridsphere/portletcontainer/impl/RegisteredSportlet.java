/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.portletcontainer.impl;

import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.portletcontainer.descriptor.PortletApplication;
import org.gridlab.gridsphere.portletcontainer.descriptor.PortletDeploymentDescriptor;
import org.gridlab.gridsphere.portletcontainer.descriptor.ConcretePortletApplication;
import org.gridlab.gridsphere.portletcontainer.descriptor.Owner;
import org.gridlab.gridsphere.portletcontainer.RegisteredPortlet;
import org.gridlab.gridsphere.portletcontainer.RegisteredPortletException;
import org.gridlab.gridsphere.services.security.acl.AccessControlService;

import javax.servlet.ServletConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * A RegisteredSportlet provides the portlet container with information used to create and manage the
 * portlet's lifecycle. A RegisteredPortlet is created given a PortletApplication and a corresponding
 * ConcretePortletApplication provided by the PortletDeploymentDescriptor. The RegisteredPortlet parses
 * the information provided and provides PortletSettings. The RegisteredPortlet also maintains an instantiated
 * portlet that is managed by the portlet container.
 */
public class RegisteredSportlet implements RegisteredPortlet {

    private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(RegisteredSportlet.class);

    private AbstractPortlet abstractPortlet = null;
    private SportletSettings sportletSettings = null;
    private SportletConfig sportletConfig = null;
    private PortletData portletData = null;
    private String portletName = "Undefined PortletInfo";
    private String portletClass = "Unknown Portlet Class";
    private String appID = null;
    private String concreteID = null;
    private AccessControlService aclService = null;
    private Owner owner;
    private List roleList = new Vector();
    private List groupList = new Vector();
    private PortletGroup ownerGroup = SportletGroup.getBaseGroup();
    private PortletRole ownerRole = SportletRole.getGuestRole();

    public RegisteredSportlet(PortletDeploymentDescriptor pdd,
                              PortletApplication portletApp,
                              ConcretePortletApplication concreteApp,
                              AccessControlService aclService) throws RegisteredPortletException {

        log.info("in RegisteredSportlet construcor");

        this.aclService = aclService;

        int index;
        String appname, cappname;

        // Get PortletApplication UID  e.g. classname.number
        appID = portletApp.getUID();
        index = appID.lastIndexOf(".");
        appname = appID.substring(0, index);
        String appNo = appID.substring(index+1);

        // Get ConcretePortletApplication UID e.g. classname.number.number
        concreteID = concreteApp.getUID();
        index = concreteID.lastIndexOf(".");
        String concreteNo = concreteID.substring(index+1);
        String cappNo = concreteID.substring(0, index);
        index = cappNo.lastIndexOf(".");
        cappNo = cappNo.substring(index+1);
        cappname = concreteID.substring(0, index);

        // Check that cappID = appID and cappname = appname
        if ((!appNo.equals(cappNo)) || (!appname.equals(cappname))) {
            log.error("<portlet-app uid=" + appname + appNo + " does not match <concrete-portlet-app uid=" + cappname + cappNo);
            throw new RegisteredPortletException("<portlet-app uid=" + appname + appNo + " does not match <concrete-portlet-app uid=" + cappname + cappNo);
        }

        portletClass = cappname;
        portletName = concreteApp.getName();

        try {
            abstractPortlet = (AbstractPortlet) Class.forName(portletClass).newInstance();
        } catch (Exception e) {
            log.error("Unable to create AbstractPortlet: " + portletClass, e);
            throw new RegisteredPortletException("Unable to create instance of portlet: " + portletClass);
        }

         // SINCE ACL SERVICE DOESN"T WORK YET
        Vector knownGroups = new Vector();
        Vector knownRoles = new Vector();
        //knownGroups = aclService.getAllGroups();
        //knownRoles = aclService.getAllRoles();

        // Get groups list
        List groups = concreteApp.getConcretePortletInfo().getGroupList();

        // Make sure groups exist
        while (knownGroups.iterator().hasNext()) {
            PortletGroup pg = (PortletGroup)knownGroups.iterator().next();
            while (groups.iterator().hasNext()) {
                if (pg.getName().equalsIgnoreCase((String)groups.iterator().next())) {
                    groupList.add(pg);
                    break;
                }
            }
        }

        // groupList should at least contain BASE group if empty
        if (groupList.isEmpty()) {
            groupList.add(SportletGroup.getBaseGroup());
        }

        // Get roles list
        List roles = concreteApp.getConcretePortletInfo().getRoleList();
        // Make sure roles exist
        while (knownRoles.iterator().hasNext()) {
            PortletRole pr = (PortletRole)knownRoles.iterator().next();
            while (roles.iterator().hasNext()) {
                if (pr.getRoleName().equalsIgnoreCase((String)roles.iterator().next())) {
                    roleList.add(pr);
                    break;
                }
            }
        }

        // roleList should at least contain GUEST role if empty
        if (roleList.isEmpty()) {
            roleList.add(SportletRole.getGuestRole());
        }

        owner = concreteApp.getConcretePortletInfo().getOwner();
        String groupName = owner.getGroupName();
        Iterator it = knownGroups.iterator();

        while (it.hasNext()) {
            PortletGroup group = (PortletGroup)it.next();
            if (group.getName().equalsIgnoreCase(groupName)) {
                ownerGroup = group;
                break;
            }
        }

        String roleName = owner.getGroupName();
        it = knownRoles.iterator();

        while (it.hasNext()) {
            PortletRole role = (PortletRole)it.next();
            if (role.getRoleName().equalsIgnoreCase(groupName)) {
                ownerRole = role;
                break;
            }
        }

        owner.setGroup(ownerGroup);
        owner.setRole(ownerRole);

        sportletSettings = new SportletSettings(pdd, concreteApp, knownGroups, knownRoles);
    }

    public String getPortletAppID() {
        return appID;
    }

    public String getConcretePortletAppID() {
        return concreteID;
    }

    /**
     * Returns the portlet config for this portlet
     *
     * @return the portlet config
     */
    public PortletConfig getPortletConfig() {
        return abstractPortlet.getConfig();
    }

    /**
     * Returns the portlet settings for this portlet
     *
     * @param the user to retrieve settings for; if the user is an admin of this portlet
     *  they can modify the portlet settings, otherwise they cannot.
     * @return the portlet settings
     */
    public PortletSettings getPortletSettings(boolean enableConfig) {
        sportletSettings.enableConfigurePermission(enableConfig);
        return (PortletSettings)sportletSettings;
    }

    /**
     * Return the name of this portlet
     *
     * @return the portlet name
     */
    public String getPortletName() {
        return portletName;
    }

    /**
     * Return the instantiated abstract portlet instance
     *
     * @return the instantiated abstract portlet instance
     */
    public AbstractPortlet getActivePortlet() {
        return abstractPortlet;
    }

    public String getPortletClass() {
        return portletClass;
    }


    /**
     * Return the Owner of the concrete portlet that can reconfigure the settings
     *
     * @return the concrete portlet owner
     */
    public Owner getPortletOwner() {
        return owner;
    }

       /**
     * Returns the list of supported groups
     * NOTE: THIS IS NOT PART OF THE WPS PORTLET API 4.1
     *
     * @return the list of supported PortletGroup objects
     */
    public List getSupportedGroups() {
        return groupList;
    }

    /**
     * Returns the list of supported roles
     * NOTE: THIS IS NOT PART OF THE WPS PORTLET API 4.1
     *
     * @return the list of supported PortletRole objects
     */
    public List getSupportedRoles() {
        return roleList;
    }

}
