<%@ page import="org.gridlab.gridsphere.portlet.User,
                 org.gridlab.gridsphere.portlet.PortletGroup,
                 org.gridlab.gridsphere.portlets.core.beans.AccessControllerBean,
                 java.util.List,
                 org.gridlab.gridsphere.portlet.PortletRole,
                 org.gridlab.gridsphere.portlet.PortletGroup,
                 java.util.Iterator,
                 org.gridlab.gridsphere.services.security.acl.GroupEntry"%>
<%@ taglib uri="/portletWidgets" prefix="gs" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>
<portletAPI:init/>
<jsp:useBean id="aclManagerBean"
             class="org.gridlab.gridsphere.portlets.core.beans.AccessControllerBean"
             scope="request"/>
<form name="AccessControllerPortlet" method="POST"
      action="<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_VIEW)%>">
  <input type="hidden" name="groupID" value="<%=aclManagerBean.getGroupID()%>"/>
  <script language="JAVASCRIPT">

    function AccessControllerPortlet_listGroup_onClick() {
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_LIST)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_newGroup_onClick() {
      document.AccessControllerPortlet.groupID.value="";
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_EDIT)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_editGroup_onClick() {
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_EDIT)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_deleteGroup_onClick() {
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_DELETE)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_viewGroupEntry_onClick(groupEntryID) {
      document.AccessControllerPortlet.groupEntryID.value=groupEntryID;
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_VIEW)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_newGroupEntry_onClick() {
      document.AccessControllerPortlet.groupEntryID.value="";
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_ENTRY_EDIT)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_editGroupEntry_onClick() {
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_ENTRY_EDIT)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_addGroupEntry_onClick() {
      // If displaying group entry list
      if (document.AccessControllerPortlet.groupEntryID) {
        // Clear all selections
        GridSphere_CheckBoxList_clear(document.AccessControllerPortlet.groupEntryID);
      }
      // Then submit action
      var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_ENTRY_ADD)%>";
      document.AccessControllerPortlet.action=action;
      document.AccessControllerPortlet.submit();
    }

    function AccessControllerPortlet_removeGroupEntry_onClick() {
      var isValid = GridSphere_CheckBoxList_validateCheckOneOrMore(document.AccessControllerPortlet.groupEntryID);
      // Validate remove action
      if (isValid == false) {
        alert("Please select the users you would like to remove from this group.");
      } else {
        var action = "<%=aclManagerBean.getPortletActionURI(AccessControllerBean.ACTION_GROUP_ENTRY_REMOVE)%>";
        document.AccessControllerPortlet.action=action;
        document.AccessControllerPortlet.submit();
      }
    }

  </script>
<table class="portlet-pane" cellspacing="1">
  <tr>
    <td>
      <table class="portlet-frame" cellspacing="1" width="100%">
        <tr>
          <td class="portlet-frame-title">
              View Group [<%=aclManagerBean.getGroupName()%>]
          </td>
        </tr>
        <tr>
          <td class="portlet-frame-actions">
            <input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_LIST%>"
                   value="List Groups"
                   onClick="javascript:AccessControllerPortlet_listGroup_onClick()"/>
            &nbsp;&nbsp;<input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_EDIT%>"
                   value="New Group"
                   onClick="javascript:AccessControllerPortlet_newGroup_onClick()"/>
            &nbsp;&nbsp;<input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_EDIT%>"
                   value="Edit Group"
                   onClick="javascript:AccessControllerPortlet_editGroup_onClick()"/>
            &nbsp;&nbsp;<input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_DELETE%>"
                   value="Delete Group"
                   onClick="javascript:AccessControllerPortlet_deleteGroup_onClick()"/>
            &nbsp;&nbsp;<input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_ENTRY_ADD%>"
                   value="Add Users"
                   onClick="javascript:AccessControllerPortlet_addGroupEntry_onClick()"/>
            &nbsp;&nbsp;<input type="submit"
                   name="<%=AccessControllerBean.ACTION_GROUP_ENTRY_REMOVE%>"
                   value="Remove Users"
                   onClick="javascript:AccessControllerPortlet_removeGroupEntry_onClick()"/>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table class="portlet-frame" cellspacing="1" width="100%">
        <tr>
          <td class="portlet-frame-label" width="200">
              Group Name:&nbsp;
          </td>
          <td class="portlet-frame-text">
             <%=aclManagerBean.getGroupName()%>
          </td>
        </tr>
        <tr>
          <td class="portlet-frame-label">
              Group Label:&nbsp;
          </td>
          <td class="portlet-frame-text">
             <%=aclManagerBean.getGroupName()%>
          </td>
        </tr>
        <tr>
          <td class="portlet-frame-label">
              Group Description:&nbsp;
          </td>
          <td class="portlet-frame-text">
             <%=aclManagerBean.getGroupName()%>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table class="portlet-frame" cellspacing="1" width="100%">
<% Iterator groupEntries = aclManagerBean.getGroupEntryList().iterator();
   if (groupEntries.hasNext()) { %>
        <tr>
          <td class="portlet-frame-header-checkbox">
              <input type="checkbox"
               name="groupEntryID"
               value=""
               onClick="javascript:GridSphere_CheckBoxList_checkAll(document.AccessControllerPortlet.groupEntryID)"/>
          </td>
          <td class="portlet-frame-header" width="100">
              User
          </td>
          <td class="portlet-frame-header" width="150">
              Full Name
          </td>
          <td class="portlet-frame-header" width="100">
              Role
          </td>
        </tr>
<%     while (groupEntries.hasNext()) {
           GroupEntry groupEntry = (GroupEntry)groupEntries.next();
           User groupEntryUser = groupEntry.getUser();
           PortletRole groupEntryRole = groupEntry.getRole(); %>
        <tr>
          <td class="portlet-frame-entry-checkbox">
            <font size="-1">
              <input type="checkbox"
               name="groupEntryID"
               value="<%=groupEntry.getID()%>"
               onClick="javascript:GridSphere_CheckBoxList_onClick(document.AccessControllerPortlet.groupEntryID,
                                                                   this)"/>
            </font>
          </td>
          <td class="portlet-frame-text">
            <%=groupEntryUser.getUserID()%>
          </td>
          <td class="portlet-frame-text">
            <%=groupEntryUser.getFullName()%>
          </td>
          <td class="portlet-frame-text">
            <%=groupEntryRole%>
          </td>
        </tr>
<%     }
   } else { %>
        <tr>
          <td class="portlet-frame-message-alert">
            There are no users in this group.
          </td>
        </tr>
  </tr>
<%  } %>
      </table>
    </td>
  </tr>
</table>
</form>
<script type="text/javascript">

  /**************************************************************************
   * GridSphere Form Action Functions
   **************************************************************************/

  function GridSphere_Form_submitAction(form, action) {
    form.action=action;
    form.submit();
  }

  /**************************************************************************
   * GridSpehre Check Box List Functions
   **************************************************************************/

  function GridSphere_CheckBoxList_checkAll(list) {

    if (list[0].checked == true) {

      // alert("GridSphere CheckBoxList Check All True");

      for (i = 1; i < list.length; i++) {

        list[i].checked = true;
      }

      // Select first list value if none selected yet
      if (list[0].value == "") {

        if (list.length > 1) {

          list[0].value = list[1].value;
        }
      }

    } else {

      // alert("GridSphere CheckBoxList Check All False");

      GridSphere_CheckBoxList_clear(list);
    }
  }

  function GridSphere_CheckBoxList_clear(list) {

    // alert("GridSphere CheckBoxList Clear");

    for (i = 0; i < list.length; i++) {

      list[i].checked = false;
    }

    // Clear selected value
    list[0].value = "";
  }

  function GridSphere_CheckBoxList_checkOne(list)
  {
    // alert("GridSphere CheckBoxList Check One");

    // Uncheck "all" option
    list[0].checked = false;

    // Uncheck those that don't match selection
    for (i = 1; i < list.length; i++) {

      if (list[i].value != list[0].value) {

        list[i].checked = false;
      }
    }
  }

  function GridSphere_CheckBoxList_onClick(list, newSelection)
  {
    // alert("GridSphere CheckBoxList On Click");

    // alert("GridSphere CheckBoxList current selection: " + list[0].value);

    if (newSelection.checked == true) {

      // Save selection only if none made yet
      if (list[0].value == "") {

        list[0].value = newSelection.value;
      }

    } else {

      // If saved selection was this one
      if (list[0].value == newSelection.value) {

        var found = false;

        // Set selection to first checked item other than this
        for (i = 1; i < list.length && !found; i++) {

          if (list[i].checked == true) {

            if (list[i].value != item.value) {

              list[0].value = list[i].value;

              found = true;
            }
          }
        }

        // If we didn't find a checked value
        if (!found) {

          // Set selection to none
          list[0].value = "";
        }
      }
    }

    // alert("GridSphere CheckBoxList new selection: " + selection.value);
  }

  function GridSphere_CheckBoxList_validateCheckOneOrMore(list)
  {
    // alert("GridSphere CheckBoxList Validate Check One Or More");

    return (list[0].value != "");
  }

</script>
