/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.tags.portletui;

import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.BaseComponentBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Vector;
import java.util.Iterator;

public class ListBoxTag extends BaseComponentTag {

    protected ListBoxBean listbox = null;

    protected int size = 1;

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

    public int doStartTag() throws JspException {

        if (!beanId.equals("")) {
            listbox = (ListBoxBean)pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (listbox == null) {
                listbox = new ListBoxBean(beanId);
                listbox.setSize(size);
                this.setBaseComponentBean(listbox);
            } else {
                this.updateBaseComponentBean(listbox);
            }
        } else {
            listbox = new ListBoxBean();
            listbox.setSize(size);
            this.setBaseComponentBean(listbox);
        }
        try {
            JspWriter out = pageContext.getOut();
            out.print(listbox.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        try {
            JspWriter out = pageContext.getOut();
            out.print(listbox.toEndString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return EVAL_PAGE;
    }

}
