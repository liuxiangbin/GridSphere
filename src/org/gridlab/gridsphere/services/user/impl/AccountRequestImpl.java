/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.services.user.impl;

import org.gridlab.gridsphere.services.user.AccountRequest;
import org.gridlab.gridsphere.services.security.acl.impl2.UserACL;
import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.impl.*;
import org.gridlab.gridsphere.core.persistence.BaseObject;
import org.gridlab.gridsphere.core.persistence.ConfigurationException;
import org.gridlab.gridsphere.core.persistence.CreateException;
import org.gridlab.gridsphere.core.persistence.PersistenceException;
import org.gridlab.gridsphere.core.persistence.castor.StringVector;
import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerRdbms;

import java.util.List;
import java.util.Vector;

/**
 * @table arimpl
 *
 */
public class AccountRequestImpl extends BaseObject implements AccountRequest {

    protected transient static PortletLog log = SportletLog.getInstance(AccountRequestImpl.class);

    /**
     * @sq-size 32
     * @sql-name userid
     */
    private String UserID = "";
    /**
     * @sql-size 30
     * @sql-name givenname
     */
    private String GivenName = "";
    /**
     * @sql-size 50
     * @sql-name familyname
     */
    private String FamilyName = "";
    /**
     * @sql-size 256
     * @sql-name fullname
     */
    private String FullName = "";
    /**
     * @sql-size 128
     * @sql-name emailaddress
     */
    private String EmailAddress = "";
    /**
     * @sql-size 256
     * @sql-name organization
     */
    private String Organization = "";

    private transient List Userdns = new Vector();
    private transient List MyproxyUserNames = new Vector();

    /**
     * @field-type org.gridlab.gridsphere.services.user.impl.AccountRequestImplUserdns
     * @sql-name userdnssv
     * @many-key reference
     */
    private transient Vector UserdnsSV = new Vector();            // ready
    /**
     * @field-type org.gridlab.gridsphere.services.user.impl.AccountRequestImplMyproxyUserNames
     * @sql-name myproxyusernamessv
     * @many-key reference
     */
    private transient Vector MyproxyUserNamesSV = new Vector();   // ready


    public AccountRequestImpl() {
        super();
    }

    public AccountRequestImpl(User user) {
        setOid(user.getID());
        setEmailAddress(user.getEmailAddress());
        setFamilyName(user.getFamilyName());
        setFullName(user.getFullName());
        setGivenName(user.getGivenName());
        setOrganization(user.getOrganization());
        setUserID(user.getUserID());
    }

    /**
     * Returns the internal unique user id.
     *
     * @return the internal unique id
     */
    public String getID() {
        return getOid();
    }

    /**
     * Sets the internal unique user id.
     *
     * @param id the internal unique id
     */
    public void setID(String id) {
        setOid(id);
    }

    /**
     * Returns the user id of the user, or null if the user id is not available.
     *
     * @return the user id
     */
    public String getUserID() {
        return UserID;
    }

    /**
     * Sets the user id of the user, or null if the user id is not available.
     *
     * @param userID the user id
     */
    public void setUserID(String userID) {
        this.UserID = userID;
    }


    /**
     * Returns the full name of the user, or null if the full name is not available.
     * The full name contains given names, family names and possibly a title or suffix.
     * Therefore, the full name may be different from the concatenation of given and family name.
     *
     * @return the full name
     */
    public String getFullName() {
        return FullName;
    }

    /**
     * Sets the full name of the user, or null if the full name is not available.
     * The full name contains given names, family names and possibly a title or suffix.
     * Therefore, the full name may be different from the concatenation of given and family name.
     *
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.FullName = fullName;
    }

    /**
     * Sets the family (aka last) name of the user.
     *
     * @param familyName the family name
     */
    public void setFamilyName(String familyName) {
        this.FamilyName = familyName;
    }

    /**
     * Returns the family (aka first) name of the user, or null if the family name is not available.
     *
     * @return the family name
     */
    public String getFamilyName() {
        return FamilyName;
    }

    /**
     * Returns the given (aka first) name of the user, or  if the given name is not available.
     *
     * @return the given name
     */
    public String getGivenName() {
        return GivenName;
    }

    /**
     * Sets the given (aka first) name of the user, or  if the given name is not available.
     *
     * @param givenName the given name
     */
    public void setGivenName(String givenName) {
        this.GivenName = givenName;
    }

    /**
     * Returns the given e-mail of the user or null if none is available.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        return EmailAddress;
    }

    /**
     * Sets the given e-mail of the user.
     *
     * @param emailAddress the email address
     */
    public void setEmailAddress(String emailAddress) {
        this.EmailAddress = emailAddress;
    }

    /**
     * Sets the users organizational affiliation
     *
     * @param organization the users organizational affiliation
     */
    public void setOrganization(String organization) {
        this.Organization = organization;
    }

    /**
     * Returns the users organizational affiliation
     *
     * @return the users organizational affiliation
     */
    public String getOrganization() {
        return Organization;
    }

    // -----

    /**
     * Sets the list of myproxy user names that can be used for this user
     *
     * @param myproxyUserNames userdns the array of strings containing user DN information
     */
    public void setMyproxyUserNames(List myproxyUserNames) {
        MyproxyUserNames = myproxyUserNames;
        MyproxyUserNamesSV = this.convertToStringVector(this, MyproxyUserNames, AccountRequestImplMyproxyUserNames.class);

    }

    /**
     * Returns the list of myproxy user names that can be used for this user
     *
     * @return userdns the array of strings containing user DN information
     */
    public List getMyproxyUserNames() {
        return MyproxyUserNames;
    }

    public List getMyproxyUserNamesSV() {
        return MyproxyUserNamesSV;
    }

    public void setMyproxyUserNamesSV(Vector myproxyUserNamesSV) {
        MyproxyUserNamesSV = myproxyUserNamesSV;
        MyproxyUserNames = this.convertToVector(MyproxyUserNamesSV);
    }


    /**
     * Sets the list of myproxy user names that can be used for this user
     *
     * @param userdns the array of strings containing user DN information
     */
    public void setMyproxyUserDN(List userdns) {
        Userdns = userdns;
        UserdnsSV = this.convertToStringVector(this, Userdns, AccountRequestImplUserdns.class);
    }

    /**
     * Returns the list of myproxy user names that can be used for this user
     */
    public List getMyProxyUserDN() {
        return Userdns;
    }

    public List getUserdnsSV() {
        return UserdnsSV;
    }

    public void setUserdnsSV(Vector userdnsSV) {
        UserdnsSV  =  userdnsSV;
        Userdns =  this.convertToVector(UserdnsSV);
    }

    //@todo should be done using the aclmanager service!
    /**
     * Adds a user with status 'candidate' to a group
     *
     * @param group
     */
    public void addToGroup(PortletGroup group, PortletRole role){
        UserACL acl;
        acl = new UserACL(this.getID(),role.getRole() ,group.getID());
        PersistenceManagerRdbms pm = new PersistenceManagerRdbms();
        try {
            pm.create(acl);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the constents of the account request to a String
     *
     * @return the account request information
     */
    public String toString() {
        int i;
        StringBuffer sb = new StringBuffer();
        sb.append("Given Name: " + GivenName);
        sb.append("Family Name: " + FamilyName);
        sb.append("Full Name: " + FullName);
        sb.append("Email Address: " + EmailAddress);
        sb.append("Organization: " + Organization);
        sb.append("Requested Groups: ");
        sb.append("Role DNs: ");
        for (i = 0; i < Userdns.size(); i++) {
            sb.append(Userdns.get(i));
        }
        sb.append("Myproxy Role Names: ");
        for (i = 0; i < MyproxyUserNames.size(); i++) {
            sb.append(MyproxyUserNames.get(i));
        }
        return sb.toString();
    }
}
