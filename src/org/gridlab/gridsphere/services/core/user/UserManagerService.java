/*
 * @version: $Id$
 */
package org.gridlab.gridsphere.services.core.user;

import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.impl.SportletUser;

import java.util.List;

public interface UserManagerService  extends LoginUserModule {

    /**
     * Creates a new user
     *
     * @return a blank user
     */
    public SportletUser createUser();

    /**
     * Edits an exising user
     *
     * @return existing user
     */
    public SportletUser editUser(User user);

    /**
     * Adds a user
     *
     * @param user a supplied User object
     */
    public void saveUser(User user);

    /**
      * Delete a user
      *
      * @param user the user
      */
    public void deleteUser(User user);

    /**
      * Administrators can retrieve all pending account request
      *
      * @return a list of pending account requests
      */
    public List getUsers();

    /**
      * Retrieves a user object with the given id from this service.
      *
      * @param id the user name or login id of the user in question
      */
    public User getUser(String id);

    /**
      * Retrieves a user object with the given username from this service.
      *
      * @param loginName the user name or login id of the user in question
      */
    public User getUserByUserName(String loginName);

    /**
      * Checks to see if account exists for a user
      *
      * @param loginName the user login ID
      * @return true if the user exists, false otherwise
      */
    public boolean existsUserName(String loginName);

}
