/*
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.tags.portletui;

import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.PasswordBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Locale;
import java.util.ResourceBundle;

public class PasswordTag extends BaseComponentTag {

    protected PasswordBean passwordBean = null;
    public static final String PASSWORD_STYLE = "portlet-frame-text";

    protected int size = 10;
    protected int maxlength = 15;

    /**
     * Returns the (html) size of the field.
     * @return size of the field
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the (html) size of the field
     * @param size size of the field
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns the (html) maxlength of the field
     * @return maxlength of the field
     */
    public int getMaxlength() {
        return maxlength;
    }

    /**
     * Sets the (html) maxlnegth of the field
     * @param maxlength maxlength of the field
     */
    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public int doStartTag() throws JspException {

        if (!beanId.equals("")) {
            passwordBean = (PasswordBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (passwordBean == null) {
                passwordBean = new PasswordBean(beanId);
                this.setBaseComponentBean(passwordBean);
            } else {
                this.updateBaseComponentBean(passwordBean);
            }
        } else {
            passwordBean = new PasswordBean();
            passwordBean.setCssStyle(PASSWORD_STYLE);
            passwordBean.setMaxLength(maxlength);
            passwordBean.setSize(size);
            this.setBaseComponentBean(passwordBean);
        }

        try {
            JspWriter out = pageContext.getOut();
            out.print(passwordBean.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }
        return SKIP_BODY;
    }

}
