/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.tags;

import org.gridlab.gridsphere.provider.portletui.beans.ActionParamBean;
import org.gridlab.gridsphere.provider.portletui.beans.ActionSubmitBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * An <code>ActionSubmitTag</code> provides a button element that includes a <code>DefaultPortletAction</code> and may
 * also include nested <code>ActionParamTag</code>s
 */
public class ActionSubmitTag extends ActionTag {

    protected String key = "";

    protected ActionSubmitBean actionSubmitBean = null;

    /**
     * Returns the action link key used to locate localized text
     *
     * @return the action link key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the action link key used to locate localized text
     *
     * @param key the action link key
     */
    public void setKey(String key) {
        this.key = key;
    }

    public int doStartTag() throws JspException {
        actionSubmitBean = new ActionSubmitBean();
        if (!beanId.equals("")) {
            actionSubmitBean = (ActionSubmitBean)pageContext.getAttribute(getBeanKey());
        }
        if (actionSubmitBean == null) actionSubmitBean = new ActionSubmitBean();
        paramBeans = new ArrayList();

        actionSubmitBean.setName(createActionURI());

        if (!key.equals("")) {
            actionSubmitBean.setKey(key);
            Locale locale = pageContext.getRequest().getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle("Portlet", locale);
            String localizedText = bundle.getString(actionSubmitBean.getKey());
            if (localizedText != null) {
                value = localizedText;
            }
        }

        if (!beanId.equals("")) {
            this.updateBaseComponentBean(actionSubmitBean);
        } else {
            this.setBaseComponentBean(actionSubmitBean);
        }

        actionSubmitBean.setAction(action);

        Object parentTag = getParent();
        if (parentTag instanceof ContainerTag) {
            ContainerTag containerTag = (ContainerTag)parentTag;
            containerTag.addTagBean(actionSubmitBean);
        } else {
            try {
                JspWriter out = pageContext.getOut();
                out.print(actionSubmitBean.toStartString());
            } catch (Exception e) {
                throw new JspException(e.getMessage());
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        Iterator it = paramBeans.iterator();
        while (it.hasNext()) {
            ActionParamBean pbean = (ActionParamBean)it.next();
            portletAction.addParameter(pbean.getName(), pbean.getValue());
        }

        String actionURI = createActionURI();

        actionSubmitBean.setName(actionURI);

        if (portletAction != null) actionSubmitBean.setAction(portletAction.toString());

        if ((bodyContent != null) && (value == null)) {
            actionSubmitBean.setValue(bodyContent.getString());
        }

        try {
            JspWriter out = pageContext.getOut();
            out.print(actionSubmitBean.toEndString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return EVAL_PAGE;
    }

}
