/**
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.tags.web.element;

public interface LabelBean extends ElementBean {

    /**
     * Gets the label of the bean.
     * @return label of the bean
     */
    public String getLabel();

    /**
     * Sets the label of the bean
     * @param label the label to be set
     */
    public void setLabel(String label);


}
